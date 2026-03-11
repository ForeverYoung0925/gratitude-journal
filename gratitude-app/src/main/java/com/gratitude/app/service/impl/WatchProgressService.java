package com.gratitude.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gratitude.app.entity.WatchProgress;
import com.gratitude.app.mapper.CourseMapper;
import com.gratitude.app.mapper.WatchProgressMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 视频观看进度服务 (支持断点续播)
 */
@Slf4j
@Service
public class WatchProgressService {

    @Autowired
    private WatchProgressMapper watchProgressMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private VipService vipService;

    /**
     * 获取播放进度(进入课程页时调用)
     */
    public WatchProgress getProgress(Long courseId) {
        Long userId = authService.getCurrentUserId();
        return watchProgressMapper.selectOne(
                new LambdaQueryWrapper<WatchProgress>()
                        .eq(WatchProgress::getUserId, userId)
                        .eq(WatchProgress::getCourseId, courseId));
    }

    /**
     * 上报播放进度(每15秒由前端上报一次)
     * 异步处理，不阻塞播放
     */
    @Async
    public void reportProgress(Long courseId, Integer progressSeconds, Boolean finished) {
        Long userId = authService.getCurrentUserId();
        boolean isFinished = Boolean.TRUE.equals(finished);

        WatchProgress existing = watchProgressMapper.selectOne(
                new LambdaQueryWrapper<WatchProgress>()
                        .eq(WatchProgress::getUserId, userId)
                        .eq(WatchProgress::getCourseId, courseId));

        if (existing == null) {
            WatchProgress progress = new WatchProgress();
            progress.setUserId(userId);
            progress.setCourseId(courseId);
            progress.setProgressSeconds(progressSeconds);
            progress.setIsFinished(isFinished ? 1 : 0);
            progress.setLastWatchTime(LocalDateTime.now());
            watchProgressMapper.insert(progress);
        } else {
            watchProgressMapper.update(null, new LambdaUpdateWrapper<WatchProgress>()
                    .eq(WatchProgress::getUserId, userId)
                    .eq(WatchProgress::getCourseId, courseId)
                    .set(WatchProgress::getProgressSeconds, progressSeconds)
                    .set(WatchProgress::getIsFinished, isFinished ? 1 : 0)
                    .set(WatchProgress::getLastWatchTime, LocalDateTime.now()));
        }

        // 更新课程观看量(仅第一次观看时+1)
        if (existing == null) {
            courseMapper.update(null, new LambdaUpdateWrapper<>()
                    .eq(com.gratitude.app.entity.Course::getId, courseId)
                    .setSql("view_count = view_count + 1"));
        }
    }
}
