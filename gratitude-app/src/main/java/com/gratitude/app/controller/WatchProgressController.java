package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.entity.WatchProgress;
import com.gratitude.app.service.impl.WatchProgressService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程观看进度控制器
 */
@RestController
@RequestMapping("/course/progress")
@Api(tags = "观看进度模块")
@SaCheckLogin
public class WatchProgressController {

    @Autowired
    private WatchProgressService watchProgressService;

    @GetMapping("/{courseId}")
    @ApiOperation("获取课程观看进度(进入播放页时调用, 用于断点续播)")
    public R<WatchProgress> getProgress(@PathVariable Long courseId) {
        return R.ok(watchProgressService.getProgress(courseId));
    }

    @PostMapping("/{courseId}/report")
    @ApiOperation(value = "上报播放进度", notes = "前端每15秒上报一次当前播放时间(秒); 视频播放完成时额外传 finished=true")
    public R<Void> reportProgress(
            @PathVariable Long courseId,
            @ApiParam(value = "当前播放时间(秒)", required = true) @RequestParam Integer progressSeconds,
            @ApiParam("是否看完") @RequestParam(defaultValue = "false") Boolean finished) {
        watchProgressService.reportProgress(courseId, progressSeconds, finished);
        return R.ok();
    }
}
