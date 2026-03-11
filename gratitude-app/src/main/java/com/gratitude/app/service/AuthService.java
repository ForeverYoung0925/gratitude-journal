package com.gratitude.app.service;

import com.gratitude.app.dto.auth.LoginVO;
import com.gratitude.app.dto.auth.PhoneLoginReq;
import com.gratitude.app.dto.auth.WxLoginReq;
import com.gratitude.app.entity.User;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 微信授权码登录
     */
    LoginVO wxLogin(WxLoginReq req);

    /**
     * 手机号+验证码登录
     */
    LoginVO phoneLogin(PhoneLoginReq req);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 获取当前登录用户(不存在则抛出异常)
     */
    User getCurrentUser();

    /**
     * 获取当前登录用户ID
     */
    Long getCurrentUserId();

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String phone);
}
