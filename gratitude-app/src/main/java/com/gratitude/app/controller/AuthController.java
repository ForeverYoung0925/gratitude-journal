package com.gratitude.app.controller;

import com.gratitude.app.dto.auth.LoginVO;
import com.gratitude.app.dto.auth.PhoneLoginReq;
import com.gratitude.app.dto.auth.WxLoginReq;
import com.gratitude.app.service.AuthService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证模块")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/wx-login")
    @ApiOperation("微信授权码登录")
    public R<LoginVO> wxLogin(@RequestBody @Validated WxLoginReq req) {
        return R.ok(authService.wxLogin(req));
    }

    @PostMapping("/phone-login")
    @ApiOperation("手机号+验证码登录")
    public R<LoginVO> phoneLogin(@RequestBody @Validated PhoneLoginReq req) {
        return R.ok(authService.phoneLogin(req));
    }

    @PostMapping("/sms/send")
    @ApiOperation("发送短信验证码")
    public R<String> sendSmsCode(
            @RequestParam @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号") String phone) {
        authService.sendSmsCode(phone);
        return R.ok("验证码已发送");
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
