package com.gratitude.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.dto.diary.DiaryPublishReq;
import com.gratitude.app.entity.Diary;

/**
 * 日记服务接口
 */
public interface DiaryService {

    /**
     * 发布日记(含微信内容安全审核)
     */
    Long publishDiary(DiaryPublishReq req);

    /**
     * 暂存草稿
     */
    Long saveDraft(DiaryPublishReq req);

    /**
     * 获取我的日记列表
     */
    IPage<Diary> getMyDiaries(Page<?> page, Integer status);

    /**
     * 获取日记广场列表
     * 
     * @param sortType 1最新 2最多点赞 3最热评论
     */
    IPage<Diary> getSquare(Page<?> page, Integer sortType, String keyword);

    /**
     * 获取日记详情
     */
    Diary getDiaryDetail(Long diaryId);

    /**
     * 点赞/取消点赞
     */
    Boolean toggleLike(Long diaryId);

    /**
     * 收藏/取消收藏
     */
    Boolean toggleCollect(Long diaryId);

    /**
     * 移入垃圾桶
     */
    void moveToBin(Long diaryId);

    /**
     * 从垃圾桶恢复
     */
    void restoreFromBin(Long diaryId);

    /**
     * 彻底删除
     */
    void deleteForever(Long diaryId);

    /**
     * 举报日记
     */
    void report(Long diaryId, String reason);

    /**
     * 微信异步审核回调处理
     */
    void handleWxAuditCallback(String traceId, Integer result, String label);

    /**
     * 编辑日记
     */
    Long editDiary(Long diaryId, DiaryPublishReq req);

    /**
     * 置顶/取消置顶
     */
    Boolean toggleTop(Long diaryId);

    /**
     * 星标/取消星标
     */
    Boolean toggleStar(Long diaryId);

    /**
     * 记录日记分享次数
     */
    void shareDiary(Long diaryId);

    /**
     * 导出我的所有日记(文本形式)
     */
    String exportMyDiariesToText();
}
