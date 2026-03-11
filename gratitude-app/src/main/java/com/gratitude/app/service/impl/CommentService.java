package com.gratitude.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.dto.comment.CommentReq;
import com.gratitude.app.dto.comment.CommentVO;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.entity.DiaryComment;
import com.gratitude.app.entity.User;
import com.gratitude.app.mapper.DiaryCommentMapper;
import com.gratitude.app.mapper.DiaryMapper;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.constants.CacheConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import com.gratitude.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论服务
 *
 * 设计思路:
 * 1. 两层结构: 顶级评论 + 子评论(回复), 前端展示顶级评论列表，每条下最多展示3条子评论
 * 2. 发表评论前调用微信msgSecCheck进行文本安全检测，同步拦截违规内容
 * 3. 评论计数同步更新到t_diary的comment_count冗余字段
 */
@Slf4j
@Service
public class CommentService {

    @Autowired
    private DiaryCommentMapper commentMapper;

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedisUtil redisUtil;

    // 前端展示最多子评论数
    private static final int MAX_CHILDREN_PREVIEW = 3;

    // ========== 发表评论 ==========

    @Transactional(rollbackFor = Exception.class)
    public Long addComment(CommentReq req) {
        Long userId = authService.getCurrentUserId();

        // 1. 校验日记是否存在且已发布
        Diary diary = diaryMapper.selectById(req.getDiaryId());
        if (diary == null || !AppConstants.DIARY_STATUS_PUBLISHED.equals(diary.getStatus())) {
            throw new BusinessException(ResultCode.DIARY_NOT_EXIST);
        }

        // 2. 如果是回复，检查父评论是否存在
        if (req.getParentId() != null && req.getParentId() > 0) {
            DiaryComment parent = commentMapper.selectById(req.getParentId());
            if (parent == null || !parent.getDiaryId().equals(req.getDiaryId())) {
                throw new BusinessException("被回复的评论不存在");
            }
        }

        // 3. 微信文本安全检测(同步, 拦截违规内容)
        // TODO: 调用 wx.msgSecCheck 接口, 违规则抛出 BusinessException
        // wxSecurityService.checkText(req.getContent(), userId);

        // 4. 写入评论
        DiaryComment comment = new DiaryComment();
        comment.setDiaryId(req.getDiaryId());
        comment.setUserId(userId);
        comment.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
        comment.setReplyCommentId(req.getReplyCommentId());
        comment.setReplyUserId(req.getReplyUserId());
        comment.setContent(req.getContent());
        comment.setLikeCount(0);
        comment.setStatus(0);
        commentMapper.insert(comment);

        // 5. 更新日记评论计数
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, req.getDiaryId())
                .setSql("comment_count = comment_count + 1"));

        return comment.getId();
    }

    // ========== 获取日记评论列表(两层树) ==========

    public List<CommentVO> listComments(Long diaryId, Integer pageNum, Integer pageSize) {
        Long currentUserId = authService.getCurrentUserId();

        // 1. 分页查询顶级评论
        List<DiaryComment> topComments = commentMapper.selectList(
                new LambdaQueryWrapper<DiaryComment>()
                        .eq(DiaryComment::getDiaryId, diaryId)
                        .eq(DiaryComment::getParentId, 0L)
                        .eq(DiaryComment::getStatus, 0)
                        .orderByDesc(DiaryComment::getLikeCount)
                        .orderByAsc(DiaryComment::getCreateTime)
                        .last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize));

        if (topComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 批量查询子评论
        List<Long> topIds = topComments.stream().map(DiaryComment::getId).collect(Collectors.toList());
        List<DiaryComment> childComments = commentMapper.selectList(
                new LambdaQueryWrapper<DiaryComment>()
                        .in(DiaryComment::getParentId, topIds)
                        .eq(DiaryComment::getStatus, 0)
                        .orderByAsc(DiaryComment::getCreateTime));

        // 3. 收集所有需要的用户ID -> 批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        topComments.forEach(c -> userIds.add(c.getUserId()));
        childComments.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null)
                userIds.add(c.getReplyUserId());
        });

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        // 4. 子评论按parentId分组
        Map<Long, List<DiaryComment>> childrenMap = childComments.stream()
                .collect(Collectors.groupingBy(DiaryComment::getParentId));

        // 5. 组装VO
        return topComments.stream()
                .map(top -> buildCommentVO(top, childrenMap, userMap, currentUserId))
                .collect(Collectors.toList());
    }

    // ========== 获取子评论列表(查看更多回复) ==========

    public List<CommentVO> listChildComments(Long parentId, Integer pageNum, Integer pageSize) {
        Long currentUserId = authService.getCurrentUserId();

        List<DiaryComment> children = commentMapper.selectList(
                new LambdaQueryWrapper<DiaryComment>()
                        .eq(DiaryComment::getParentId, parentId)
                        .eq(DiaryComment::getStatus, 0)
                        .orderByAsc(DiaryComment::getCreateTime)
                        .last("LIMIT " + (pageNum - 1) * pageSize + "," + pageSize));

        Set<Long> userIds = new HashSet<>();
        children.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null)
                userIds.add(c.getReplyUserId());
        });

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        return children.stream()
                .map(c -> buildCommentVO(c, Collections.emptyMap(), userMap, currentUserId))
                .collect(Collectors.toList());
    }

    // ========== 点赞/取消点赞评论 ==========

    public Boolean toggleCommentLike(Long commentId) {
        Long userId = authService.getCurrentUserId();
        String likeKey = "comment:like:" + commentId + ":" + userId;

        Boolean liked = redisUtil.get(likeKey, Boolean.class);
        if (Boolean.TRUE.equals(liked)) {
            redisUtil.delete(likeKey);
            commentMapper.update(null, new LambdaUpdateWrapper<DiaryComment>()
                    .eq(DiaryComment::getId, commentId)
                    .setSql("like_count = GREATEST(0, like_count - 1)"));
            return false;
        } else {
            redisUtil.set(likeKey, true, 30, TimeUnit.DAYS);
            commentMapper.update(null, new LambdaUpdateWrapper<DiaryComment>()
                    .eq(DiaryComment::getId, commentId)
                    .setSql("like_count = like_count + 1"));
            return true;
        }
    }

    // ========== 删除自己的评论 ==========

    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Long userId = authService.getCurrentUserId();
        DiaryComment comment = commentMapper.selectById(commentId);
        if (comment == null)
            return;
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人评论");
        }
        commentMapper.deleteById(commentId);
        // 更新日记评论计数
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, comment.getDiaryId())
                .setSql("comment_count = GREATEST(0, comment_count - 1)"));
    }

    // ========== 私有辅助: 构建CommentVO ==========

    private CommentVO buildCommentVO(DiaryComment comment,
            Map<Long, List<DiaryComment>> childrenMap,
            Map<Long, User> userMap,
            Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setParentId(comment.getParentId());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreateTime(comment.getCreateTime());

        // 评论者信息
        User author = userMap.get(comment.getUserId());
        if (author != null) {
            vo.setNickname(author.getNickname());
            vo.setAvatar(author.getAvatar());
        }

        // 被回复用户昵称
        if (comment.getReplyUserId() != null) {
            User replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyNickname(replyUser.getNickname());
            }
        }

        // 当前用户是否点赞此评论
        String likeKey = "comment:like:" + comment.getId() + ":" + currentUserId;
        vo.setLiked(Boolean.TRUE.equals(redisUtil.get(likeKey, Boolean.class)));

        // 子评论(前N条预览)
        List<DiaryComment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<CommentVO> childVOs = children.stream()
                    .limit(MAX_CHILDREN_PREVIEW)
                    .map(c -> buildCommentVO(c, Collections.emptyMap(), userMap, currentUserId))
                    .collect(Collectors.toList());
            vo.setChildren(childVOs);
        }

        return vo;
    }
}
