package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Feedback;
import com.gratitude.app.mapper.FeedbackMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/feedback")
@Api(tags = "管理后台-意见反馈追踪")
@SaCheckLogin(type = "admin")
public class AdminFeedbackController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @GetMapping("/page")
    @ApiOperation("分页获取用户意见反馈列表")
    public R<Page<Feedback>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Feedback::getStatus, status);
        }
        // 按创建时间降序
        wrapper.orderByDesc(Feedback::getCreateTime);

        return R.ok(feedbackMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PutMapping("/{id}/reply")
    @ApiOperation("处理/回复意见反馈")
    public R<String> reply(
            @PathVariable Long id,
            @ApiParam("0待处理 1处理中 2已处理") @RequestParam Integer status,
            @ApiParam("内部处理备注") @RequestParam(required = false) String replyNote) {
        feedbackMapper.update(null, new LambdaUpdateWrapper<Feedback>()
                .eq(Feedback::getId, id)
                .set(Feedback::getStatus, status)
                .set(StrUtil.isNotBlank(replyNote), Feedback::getReplyNote, replyNote));
        return R.ok("处理状态已更新");
    }
}
