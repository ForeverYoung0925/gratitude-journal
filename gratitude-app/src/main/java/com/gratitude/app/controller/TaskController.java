package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.impl.TaskService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 签到 & 任务控制器
 */
@RestController
@RequestMapping("/task")
@Api(tags = "签到&任务模块")
@SaCheckLogin
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AuthService authService;

    @PostMapping("/checkin")
    @ApiOperation(value = "每日签到", notes = "连续签到规则: 每日签到+1天VIP；连续7天额外+3天")
    public R<Integer> checkIn() {
        Long userId = authService.getCurrentUserId();
        int continuousDays = taskService.checkIn(userId);
        return R.ok("签到成功，已连续签到" + continuousDays + "天", continuousDays);
    }
}
