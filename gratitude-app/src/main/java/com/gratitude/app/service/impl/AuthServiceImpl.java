package com.gratitude.app.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.config.OssConfig;
import com.gratitude.app.dto.auth.LoginVO;
import com.gratitude.app.dto.auth.PhoneLoginReq;
import com.gratitude.app.dto.auth.WxLoginReq;
import com.gratitude.app.entity.User;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.constants.CacheConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import com.gratitude.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Value("${wx.miniapp.app-id}")
    private String appId;

    @Value("${wx.miniapp.app-secret}")
    private String appSecret;

    private static final String WX_CODE2SESSION_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appId}&secret={secret}&js_code={code}&grant_type=authorization_code";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private VipService vipService;

    @Autowired
    private RestTemplate restTemplate;

    // ========== 微信授权登录 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO wxLogin(WxLoginReq req) {
        // 1. 用code换取openid
        String openid = getOpenidByCode(req.getCode());

        // 2. 查询或创建用户
        boolean isNewUser = false;
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid)
        );

        if (user == null) {
            user = createUserByWx(openid, req.getNickname(), req.getAvatar());
            isNewUser = true;
            // 新用户赠送7天VIP
            vipService.addVipDays(user.getId(), AppConstants.NEW_USER_VIP_DAYS,
                    AppConstants.VIP_SOURCE_REGISTER, "新用户注册免费体验", null);
        }

        // 3. 检查封禁状态
        checkUserStatus(user);

        // 4. 颁发Sa-Token令牌
        StpUtil.login(user.getId());

        return buildLoginVO(user, StpUtil.getTokenValue(), isNewUser);
    }

    // ========== 手机号登录 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO phoneLogin(PhoneLoginReq req) {
        // 1. 验证短信验证码
        String codeKey = CacheConstants.SMS_CODE_PREFIX + req.getPhone();
        String cachedCode = redisUtil.getString(codeKey);
        if (StrUtil.isBlank(cachedCode) || !cachedCode.equals(req.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        // 验证通过立即删除
        redisUtil.delete(codeKey);

        // 2. 查询或创建用户
        boolean isNewUser = false;
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );

        if (user == null) {
            user = createUserByPhone(req.getPhone());
            isNewUser = true;
            vipService.addVipDays(user.getId(), AppConstants.NEW_USER_VIP_DAYS,
                    AppConstants.VIP_SOURCE_REGISTER, "新用户注册免费体验", null);
        }

        // 3. 绑定微信(如果同时传了wxCode)
        if (StrUtil.isNotBlank(req.getWxCode()) && StrUtil.isBlank(user.getOpenid())) {
            try {
                String openid = getOpenidByCode(req.getWxCode());
                user.setOpenid(openid);
                userMapper.updateById(user);
            } catch (Exception e) {
                log.warn("绑定微信失败, phone={}", req.getPhone());
            }
        }

        checkUserStatus(user);
        StpUtil.login(user.getId());
        return buildLoginVO(user, StpUtil.getTokenValue(), isNewUser);
    }

    // ========== 发送短信验证码 ==========

    @Override
    public void sendSmsCode(String phone) {
        // 1. 检查每日发送次数上限
        String countKey = CacheConstants.SMS_SEND_COUNT_PREFIX + phone;
        Long count = redisUtil.increment(countKey, 1L);
        if (count == 1) {
            // 首次发送，设置24小时过期
            redisUtil.expire(countKey, CacheConstants.EXPIRE_1_DAY, TimeUnit.SECONDS);
        }
        if (count > AppConstants.SMS_DAILY_LIMIT) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUEST.getCode(),
                    "今日验证码发送次数已达上限(" + AppConstants.SMS_DAILY_LIMIT + "次)");
        }

        // 2. 生成6位验证码
        String code = RandomUtil.randomNumbers(6);
        log.info("【短信验证码】手机号: {}, 验证码: {}", phone, code);

        // 3. 缓存验证码(5分钟有效)
        redisUtil.set(CacheConstants.SMS_CODE_PREFIX + phone, code,
                CacheConstants.EXPIRE_5_MIN, TimeUnit.SECONDS);

        // 4. TODO: 调用短信服务商API(阿里云短信、腾讯短信等)
        // smsService.send(phone, code);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return user;
    }

    @Override
    public Long getCurrentUserId() {
        StpUtil.checkLogin();
        return Long.parseLong(StpUtil.getLoginId().toString());
    }

    // ========== 私有辅助方法 ==========

    private String getOpenidByCode(String code) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    WX_CODE2SESSION_URL, String.class, appId, appSecret, code
            );
            JSONObject json = JSON.parseObject(response.getBody());
            String openid = json.getString("openid");
            if (StrUtil.isBlank(openid)) {
                log.error("微信code换取openid失败: {}", response.getBody());
                throw new BusinessException(ResultCode.WX_AUTH_FAIL);
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException(ResultCode.WX_AUTH_FAIL);
        }
    }

    private User createUserByWx(String openid, String nickname, String avatar) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname(StrUtil.isBlank(nickname) ? "感恩用户" + RandomUtil.randomNumbers(6) : nickname);
        user.setAvatar(StrUtil.isBlank(avatar) ? "" : avatar);
        user.setStatus(AppConstants.USER_STATUS_NORMAL);
        user.setSource(1);
        user.setHasPasswordLock(0);
        user.setReminderEnabled(0);
        user.setTotalEarnedVipDays(0);
        userMapper.insert(user);
        return user;
    }

    private User createUserByPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname("感恩用户" + RandomUtil.randomNumbers(6));
        user.setAvatar("");
        user.setStatus(AppConstants.USER_STATUS_NORMAL);
        user.setSource(2);
        user.setHasPasswordLock(0);
        user.setReminderEnabled(0);
        user.setTotalEarnedVipDays(0);
        userMapper.insert(user);
        return user;
    }

    private void checkUserStatus(User user) {
        if (AppConstants.USER_STATUS_LOCKED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USER_LOCKED);
        }
    }

    private LoginVO buildLoginVO(User user, String token, boolean isNewUser) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setIsNewUser(isNewUser);
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        boolean isVip = user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
        vo.setIsVip(isVip);
        if (isVip) {
            vo.setVipExpireTimestamp(
                    user.getVipExpireTime().atZone(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli()
            );
        }
        return vo;
    }
}
