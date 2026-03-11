package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.dto.diary.DiaryPublishReq;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.service.DiaryService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 日记控制器
 */
@RestController
@RequestMapping("/diary")
@Api(tags = "日记模块")
@SaCheckLogin
public class DiaryController {

    @Autowired
    private DiaryService diaryService;

    @PostMapping("/publish")
    @ApiOperation("发布日记")
    public R<Long> publish(@RequestBody @Validated DiaryPublishReq req) {
        return R.ok(diaryService.publishDiary(req));
    }

    @PostMapping("/draft")
    @ApiOperation("暂存草稿")
    public R<Long> saveDraft(@RequestBody @Validated DiaryPublishReq req) {
        return R.ok(diaryService.saveDraft(req));
    }

    @GetMapping("/my")
    @ApiOperation("我的日记列表")
    public R<?> getMyDiaries(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("状态筛选(可选)") @RequestParam(required = false) Integer status) {
        return R.ok(diaryService.getMyDiaries(new Page<>(pageNum, pageSize), status));
    }

    @GetMapping("/square")
    @ApiOperation("日记广场(最新/最热)")
    public R<?> getSquare(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("排序 1最新 2最多点赞 3最热评论") @RequestParam(defaultValue = "1") Integer sortType,
            @ApiParam("关键词搜索") @RequestParam(required = false) String keyword) {
        return R.ok(diaryService.getSquare(new Page<>(pageNum, pageSize), sortType, keyword));
    }

    @GetMapping("/{diaryId}")
    @ApiOperation("日记详情")
    public R<Diary> detail(@PathVariable Long diaryId) {
        return R.ok(diaryService.getDiaryDetail(diaryId));
    }

    @PostMapping("/{diaryId}/like")
    @ApiOperation("点赞/取消点赞")
    public R<Boolean> like(@PathVariable Long diaryId) {
        return R.ok(diaryService.toggleLike(diaryId));
    }

    @PostMapping("/{diaryId}/collect")
    @ApiOperation("收藏/取消收藏")
    public R<Boolean> collect(@PathVariable Long diaryId) {
        return R.ok(diaryService.toggleCollect(diaryId));
    }

    @PostMapping("/{diaryId}/bin")
    @ApiOperation("移入垃圾桶")
    public R<Void> moveToBin(@PathVariable Long diaryId) {
        diaryService.moveToBin(diaryId);
        return R.ok();
    }

    @PostMapping("/{diaryId}/restore")
    @ApiOperation("从垃圾桶恢复")
    public R<Void> restore(@PathVariable Long diaryId) {
        diaryService.restoreFromBin(diaryId);
        return R.ok();
    }

    @DeleteMapping("/{diaryId}")
    @ApiOperation("彻底删除日记")
    public R<Void> deleteForever(@PathVariable Long diaryId) {
        diaryService.deleteForever(diaryId);
        return R.ok();
    }

    @PostMapping("/{diaryId}/report")
    @ApiOperation("举报日记")
    public R<String> report(@PathVariable Long diaryId, @RequestParam String reason) {
        diaryService.report(diaryId, reason);
        return R.ok("举报已提交，感谢您的反馈");
    }

    @PostMapping("/wx-audit-callback")
    @ApiOperation(value = "微信内容安全异步审核回调", hidden = true)
    public R<Void> wxAuditCallback(
            @RequestParam String traceId,
            @RequestParam Integer result,
            @RequestParam(required = false) String label) {
        diaryService.handleWxAuditCallback(traceId, result, label);
        return R.ok();
    }

    @PutMapping("/{diaryId}")
    @ApiOperation("编辑日记")
    public R<Long> editDiary(@PathVariable Long diaryId, @RequestBody @Validated DiaryPublishReq req) {
        return R.ok(diaryService.editDiary(diaryId, req));
    }

    @PostMapping("/{diaryId}/top")
    @ApiOperation("置顶/取消置顶日记")
    public R<Boolean> toggleTop(@PathVariable Long diaryId) {
        return R.ok(diaryService.toggleTop(diaryId));
    }

    @PostMapping("/{diaryId}/star")
    @ApiOperation("星标/取消星标日记")
    public R<Boolean> toggleStar(@PathVariable Long diaryId) {
        return R.ok(diaryService.toggleStar(diaryId));
    }

    @PostMapping("/{diaryId}/share")
    @ApiOperation("分享日记(前端分享微信好友后调用)")
    public R<Void> shareDiary(@PathVariable Long diaryId) {
        diaryService.shareDiary(diaryId);
        return R.ok();
    }

    @GetMapping("/export")
    @ApiOperation(value = "导出我的日记(文本形式)", produces = "text/plain")
    public R<String> exportMyDiaries() {
        return R.ok("导出成功", diaryService.exportMyDiariesToText());
    }
}
