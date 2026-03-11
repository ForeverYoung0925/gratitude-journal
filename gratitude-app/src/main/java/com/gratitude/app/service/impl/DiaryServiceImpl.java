package com.gratitude.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.dto.diary.DiaryPublishReq;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.entity.User;
import com.gratitude.app.mapper.DiaryMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.DiaryService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.constants.CacheConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import com.gratitude.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 日记服务实现
 * 核心设计:
 * - 点赞数先写Redis, 定期异步同步到DB, 避免频繁更新导致行锁竞争
 * - 发布日记触发微信异步内容安全审核, 不阻塞用户发布操作
 */
@Slf4j
@Service
public class DiaryServiceImpl implements DiaryService {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisUtil redisUtil;

    // ========== 发布日记 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishDiary(DiaryPublishReq req) {
        User currentUser = authService.getCurrentUser();

        Diary diary = buildDiaryFromReq(req, currentUser.getId());

        // 如果从草稿发布, 先删草稿
        if (req.getDraftId() != null) {
            diaryMapper.deleteById(req.getDraftId());
        }

        // 先保存, 状态为"审核中"
        diary.setStatus(AppConstants.DIARY_STATUS_AUDITING);
        diaryMapper.insert(diary);

        // 异步发起微信内容安全审核
        asyncTriggerWxSecurityCheck(diary, currentUser.getOpenid());

        return diary.getId();
    }

    // ========== 暂存草稿 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(DiaryPublishReq req) {
        Long userId = authService.getCurrentUserId();

        if (req.getDraftId() != null) {
            // 更新已有草稿
            Diary existing = diaryMapper.selectById(req.getDraftId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
            }
            existing.setTitle(req.getTitle());
            existing.setContent(req.getContent());
            existing.setImages(req.getImages() != null ? JSON.toJSONString(req.getImages()) : null);
            existing.setMood(req.getMood());
            existing.setVisible(req.getVisible());
            diaryMapper.updateById(existing);
            return existing.getId();
        }

        Diary diary = buildDiaryFromReq(req, userId);
        diary.setStatus(AppConstants.DIARY_STATUS_DRAFT);
        diaryMapper.insert(diary);
        return diary.getId();
    }

    // ========== 我的日记列表 ==========

    @Override
    public IPage<Diary> getMyDiaries(Page<?> page, Integer status) {
        Long userId = authService.getCurrentUserId();
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .orderByDesc(Diary::getCreateTime);

        if (status != null) {
            wrapper.eq(Diary::getStatus, status);
        } else {
            // 默认排除垃圾桶
            wrapper.ne(Diary::getStatus, AppConstants.DIARY_STATUS_DELETED);
        }
        return diaryMapper.selectPage(page, wrapper);
    }

    // ========== 广场 ==========

    @Override
    public IPage<Diary> getSquare(Page<?> page, Integer sortType, String keyword) {
        return diaryMapper.selectSquarePage(page, sortType, keyword);
    }

    // ========== 日记详情 ==========

    @Override
    public Diary getDiaryDetail(Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException(ResultCode.DIARY_NOT_EXIST);
        }
        // 权限: 私密日记只有本人可见
        if (AppConstants.DIARY_VISIBLE_PRIVATE.equals(diary.getVisible())) {
            Long currentUserId = authService.getCurrentUserId();
            if (!diary.getUserId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
            }
        }
        // 浏览量+1 (异步, 不影响主流程)
        asyncIncrViewCount(diaryId);
        return diary;
    }

    // ========== 点赞/取消点赞 ==========

    @Override
    public Boolean toggleLike(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        String likeKey = CacheConstants.DIARY_LIKE_PREFIX + diaryId;

        Boolean isMember = redisUtil.get(likeKey + ":" + userId, Boolean.class);
        if (Boolean.TRUE.equals(isMember)) {
            // 已点赞 -> 取消
            redisUtil.delete(likeKey + ":" + userId);
            redisUtil.increment(likeKey + ":count", -1L);
            return false;
        } else {
            // 未点赞 -> 点赞
            redisUtil.set(likeKey + ":" + userId, true, 30, TimeUnit.DAYS);
            redisUtil.increment(likeKey + ":count", 1L);
            return true;
        }
    }

    // ========== 收藏/取消收藏 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleCollect(Long diaryId) {
        // TODO: 需要t_diary_collect表配合，此处为示意
        return true;
    }

    // ========== 移入垃圾桶 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveToBin(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        int rows = diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, diaryId)
                .eq(Diary::getUserId, userId)
                .set(Diary::getStatus, AppConstants.DIARY_STATUS_DELETED));
        if (rows == 0) {
            throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
        }
    }

    // ========== 从垃圾桶恢复 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreFromBin(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        Diary diary = diaryMapper.selectOne(
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getId, diaryId)
                        .eq(Diary::getUserId, userId)
                        .eq(Diary::getStatus, AppConstants.DIARY_STATUS_DELETED));
        if (diary == null) {
            throw new BusinessException(ResultCode.DIARY_NOT_EXIST);
        }
        diary.setStatus(AppConstants.DIARY_STATUS_DRAFT);
        diaryMapper.updateById(diary);
    }

    // ========== 彻底删除 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteForever(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        int rows = diaryMapper.delete(
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getId, diaryId)
                        .eq(Diary::getUserId, userId));
        if (rows == 0) {
            throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
        }
    }

    // ========== 举报 ==========

    @Override
    public void report(Long diaryId, String reason) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new BusinessException(ResultCode.DIARY_NOT_EXIST);
        }
        // TODO: 写入举报记录表t_report
        log.info("日记[{}]被举报, 原因: {}", diaryId, reason);
    }

    // ========== 微信审核回调 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWxAuditCallback(String traceId, Integer result, String label) {
        // result: 0-审核通过 1-违规
        Diary diary = diaryMapper.selectOne(
                new LambdaQueryWrapper<Diary>().eq(Diary::getWxMediaJobId, traceId));
        if (diary == null) {
            log.warn("微信审核回调未找到对应日记, traceId={}", traceId);
            return;
        }

        if (result == 0) {
            diary.setStatus(AppConstants.DIARY_STATUS_PUBLISHED);
        } else {
            diary.setStatus(AppConstants.DIARY_STATUS_REJECTED);
            diary.setRejectReason("内容包含: " + label);
        }
        diaryMapper.updateById(diary);
        log.info("微信审核结果处理完成, diaryId={}, result={}", diary.getId(), result);
    }

    // ========== 编辑日记 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long editDiary(Long diaryId, DiaryPublishReq req) {
        Long userId = authService.getCurrentUserId();
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
        }

        diary.setTitle(req.getTitle());
        diary.setContent(req.getContent());
        diary.setImages(req.getImages() != null ? JSON.toJSONString(req.getImages()) : null);
        diary.setMood(req.getMood() != null ? req.getMood() : 1);
        diary.setVisible(req.getVisible() != null ? req.getVisible() : AppConstants.DIARY_VISIBLE_PUBLIC);

        // 编辑后重新进入审核状态
        diary.setStatus(AppConstants.DIARY_STATUS_AUDITING);
        diaryMapper.updateById(diary);

        // 发起异步内容安全审核
        asyncTriggerWxSecurityCheck(diary, authService.getCurrentUser().getOpenid());

        return diary.getId();
    }

    // ========== 置顶/取消置顶 ==========

    @Override
    public Boolean toggleTop(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
        }
        boolean isTopNow = (diary.getIsTop() != null && diary.getIsTop() == 1);
        diary.setIsTop(isTopNow ? 0 : 1);
        diaryMapper.updateById(diary);
        return !isTopNow;
    }

    // ========== 星标/取消星标 ==========

    @Override
    public Boolean toggleStar(Long diaryId) {
        Long userId = authService.getCurrentUserId();
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.DIARY_NO_PERMISSION);
        }
        boolean isStarNow = (diary.getIsStar() != null && diary.getIsStar() == 1);
        diary.setIsStar(isStarNow ? 0 : 1);
        diaryMapper.updateById(diary);
        return !isStarNow;
    }

    // ========== 记录分享次数 ==========

    @Override
    public void shareDiary(Long diaryId) {
        // 同步增加分享次数
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, diaryId)
                .setSql("share_count = share_count + 1"));
    }

    // ========== 导出所有日记 ==========

    @Override
    public String exportMyDiariesToText() {
        Long userId = authService.getCurrentUserId();
        // 取出该用户所有非删除状态的日记
        java.util.List<Diary> list = diaryMapper.selectList(
                new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getUserId, userId)
                        .ne(Diary::getStatus, AppConstants.DIARY_STATUS_DELETED)
                        .orderByDesc(Diary::getCreateTime));

        if (cn.hutool.core.collection.CollUtil.isEmpty(list)) {
            return "您还没有写过任何日记哦~";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========== 我的感恩日记 ==========\n\n");
        for (Diary d : list) {
            String dateStr = cn.hutool.core.date.DateUtil.formatDateTime(d.getCreateTime());
            sb.append("时间: ").append(dateStr).append("\n");
            if (cn.hutool.core.util.StrUtil.isNotBlank(d.getTitle())) {
                sb.append("标题: ").append(d.getTitle()).append("\n");
            }
            sb.append("情绪: ").append(parseMood(d.getMood())).append("\n");
            sb.append("正文: \n").append(d.getContent()).append("\n");
            sb.append("--------------------------------------\n\n");
        }
        return sb.toString();
    }

    private String parseMood(Integer mood) {
        if (mood == null)
            return "未知";
        switch (mood) {
            case 1:
                return "开心";
            case 2:
                return "感动";
            case 3:
                return "平静";
            case 4:
                return "难过";
            default:
                return "未知";
        }
    }

    // ========== 私有辅助 ==========

    private Diary buildDiaryFromReq(DiaryPublishReq req, Long userId) {
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(req.getTitle());
        diary.setContent(req.getContent());
        diary.setImages(req.getImages() != null ? JSON.toJSONString(req.getImages()) : null);
        diary.setMood(req.getMood() != null ? req.getMood() : 1);
        diary.setVisible(req.getVisible() != null ? req.getVisible() : AppConstants.DIARY_VISIBLE_PUBLIC);
        diary.setLikeCount(0);
        diary.setCommentCount(0);
        diary.setCollectCount(0);
        diary.setViewCount(0);
        diary.setShareCount(0);
        diary.setIsTop(0);
        diary.setIsStar(0);
        return diary;
    }

    @Async
    protected void asyncTriggerWxSecurityCheck(Diary diary, String openid) {
        // TODO: 调用微信msgSecCheck接口做文本审核
        // 如果文本审核通过, 直接设为已发布(提升用户体验)
        // 如果文本有问题, 转人工或直接拒绝
        log.info("异步触发微信内容安全检测, diaryId={}", diary.getId());
        // 模拟文本安全检测通过后直接发布
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, diary.getId())
                .eq(Diary::getStatus, AppConstants.DIARY_STATUS_AUDITING)
                .set(Diary::getStatus, AppConstants.DIARY_STATUS_PUBLISHED));
    }

    @Async
    protected void asyncIncrViewCount(Long diaryId) {
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, diaryId)
                .setSql("view_count = view_count + 1"));
    }
}
