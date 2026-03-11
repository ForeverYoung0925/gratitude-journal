package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.entity.Feedback;
import com.gratitude.app.mapper.FeedbackMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feedback")
@Api(tags = "意见反馈模块(C端)")
@SaCheckLogin
public class FeedbackController {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Autowired
    private AuthService authService;

    @PostMapping("/submit")
    @ApiOperation("提交意见反馈")
    public R<Void> submit(@RequestBody @Validated Feedback feedback) {
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            return R.fail("反馈内容不能为空");
        }
        feedback.setUserId(authService.getCurrentUserId());
        feedback.setStatus(0);
        feedbackMapper.insert(feedback);
        return R.ok("反馈提交成功，感谢您的建议");
    }
}
