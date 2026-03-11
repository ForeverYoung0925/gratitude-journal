package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.dto.user.PasswordLockReq;
import com.gratitude.app.dto.user.UpdateUserReq;
import com.gratitude.app.dto.user.UserInfoVO;
import com.gratitude.app.service.impl.UserService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * 用户信息控制器
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户信息模块")
@SaCheckLogin
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @ApiOperation("获取当前用户信息")
    public R<UserInfoVO> getMyInfo() {
        return R.ok(userService.getMyInfo());
    }

    @PutMapping("/me")
    @ApiOperation("修改用户信息(昵称/头像/生日/性别)")
    public R<Void> updateMyInfo(@RequestBody @Validated UpdateUserReq req) {
        userService.updateMyInfo(req);
        return R.ok();
    }

    // ========== 密码锁 ==========

    @PostMapping("/password-lock/set")
    @ApiOperation(value = "设置/修改密码锁", notes = "首次设置无需旧密码；已有密码锁时需传 oldPassword")
    public R<String> setPasswordLock(@RequestBody @Validated PasswordLockReq req) {
        userService.setPasswordLock(req);
        return R.ok("密码锁设置成功");
    }

    @PostMapping("/password-lock/disable")
    @ApiOperation("关闭密码锁")
    public R<String> disablePasswordLock(
            @ApiParam(value = "当前密码", required = true) @RequestParam @NotBlank(message = "请输入当前密码") String password) {
        userService.disablePasswordLock(password);
        return R.ok("密码锁已关闭");
    }

    @PostMapping("/password-lock/verify")
    @ApiOperation("校验密码锁(进入日记页前调用)")
    public R<Boolean> verifyPasswordLock(
            @ApiParam(value = "密码", required = true) @RequestParam @NotBlank(message = "请输入密码") String password) {
        return R.ok(userService.verifyPasswordLock(password));
    }

    // ========== 写日记提醒 ==========

    @PutMapping("/reminder")
    @ApiOperation("设置写日记提醒")
    public R<Void> updateReminder(
            @ApiParam(value = "是否开启", required = true) @RequestParam Boolean enabled,
            @ApiParam("提醒时间 HH:mm") @RequestParam(required = false) String reminderTime) {
        userService.updateReminder(enabled, reminderTime);
        return R.ok();
    }

    // ========== 注销账号 ==========

    @DeleteMapping("/me/cancel")
    @ApiOperation(value = "注销账号", notes = "开启了密码锁的用户需要先传密码验证")
    public R<String> cancelAccount(
            @ApiParam("密码锁密码(开启密码锁的用户必传)") @RequestParam(required = false) String password) {
        userService.cancelAccount(password);
        return R.ok("账号已注销");
    }
}
