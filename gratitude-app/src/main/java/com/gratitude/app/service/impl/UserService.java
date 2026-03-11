package com.gratitude.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gratitude.app.dto.user.PasswordLockReq;
import com.gratitude.app.dto.user.UpdateUserReq;
import com.gratitude.app.dto.user.UserInfoVO;
import com.gratitude.app.entity.User;
import com.gratitude.app.entity.UserCheckin;
import com.gratitude.app.mapper.UserCheckinMapper;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 用户信息服务
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ========== 获取用户信息 ==========

    public UserInfoVO getMyInfo() {
        User user = authService.getCurrentUser();
        return convertToVO(user);
    }

    // ========== 修改用户信息 ==========

    @Transactional(rollbackFor = Exception.class)
    public void updateMyInfo(UpdateUserReq req) {
        Long userId = authService.getCurrentUserId();

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId);

        if (StrUtil.isNotBlank(req.getNickname())) {
            wrapper.set(User::getNickname, req.getNickname());
        }
        if (StrUtil.isNotBlank(req.getAvatar())) {
            wrapper.set(User::getAvatar, req.getAvatar());
        }
        if (req.getBirthday() != null) {
            wrapper.set(User::getBirthday, req.getBirthday());
        }
        if (req.getGender() != null) {
            wrapper.set(User::getGender, req.getGender());
        }

        userMapper.update(null, wrapper);
    }

    // ========== 密码锁: 设置 ==========

    @Transactional(rollbackFor = Exception.class)
    public void setPasswordLock(PasswordLockReq req) {
        validatePasswordFormat(req.getPassword());
        Long userId = authService.getCurrentUserId();

        User user = authService.getCurrentUser();
        // 如果已有密码锁，需要验证旧密码
        if (user.getHasPasswordLock() != null && user.getHasPasswordLock() == 1) {
            if (StrUtil.isBlank(req.getOldPassword())) {
                throw new BusinessException("请输入旧密码");
            }
            if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordLock())) {
                throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
            }
        }

        String encodedPwd = passwordEncoder.encode(req.getPassword());
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getHasPasswordLock, 1)
                .set(User::getPasswordLock, encodedPwd));
    }

    // ========== 密码锁: 关闭 ==========

    @Transactional(rollbackFor = Exception.class)
    public void disablePasswordLock(String password) {
        User user = authService.getCurrentUser();
        if (user.getHasPasswordLock() == null || user.getHasPasswordLock() == 0) {
            throw new BusinessException("未设置密码锁");
        }
        if (!passwordEncoder.matches(password, user.getPasswordLock())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, user.getId())
                .set(User::getHasPasswordLock, 0)
                .set(User::getPasswordLock, null));
    }

    // ========== 密码锁: 校验(进入日记前验证) ==========

    public boolean verifyPasswordLock(String password) {
        User user = authService.getCurrentUser();
        if (user.getHasPasswordLock() == null || user.getHasPasswordLock() == 0) {
            return true; // 未设置密码锁，直接通过
        }
        if (!passwordEncoder.matches(password, user.getPasswordLock())) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        return true;
    }

    // ========== 写日记提醒设置 ==========

    @Transactional(rollbackFor = Exception.class)
    public void updateReminder(Boolean enabled, String reminderTime) {
        Long userId = authService.getCurrentUserId();
        if (Boolean.TRUE.equals(enabled) && StrUtil.isBlank(reminderTime)) {
            throw new BusinessException("开启提醒时请设置提醒时间");
        }
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getReminderEnabled, enabled ? 1 : 0)
                .set(User::getReminderTime, reminderTime));
    }

    // ========== 注销账号(逻辑删除) ==========

    @Transactional(rollbackFor = Exception.class)
    public void cancelAccount(String password) {
        User user = authService.getCurrentUser();
        // 有密码锁的账号必须先验证
        if (user.getHasPasswordLock() != null && user.getHasPasswordLock() == 1) {
            verifyPasswordLock(password);
        }
        userMapper.deleteById(user.getId());
        log.info("用户[{}]已注销账号", user.getId());
    }

    // ========== 私有辅助 ==========

    private UserInfoVO convertToVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        // 手机号脱敏
        vo.setPhone(desensitizePhone(user.getPhone()));
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBirthday(user.getBirthday());
        vo.setGender(user.getGender());

        boolean isVip = user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
        vo.setIsVip(isVip);
        if (isVip) {
            vo.setVipExpireTimestamp(user.getVipExpireTime()
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }

        vo.setHasPasswordLock(user.getHasPasswordLock() != null && user.getHasPasswordLock() == 1);
        vo.setReminderEnabled(user.getReminderEnabled() != null && user.getReminderEnabled() == 1);
        vo.setReminderTime(user.getReminderTime());
        vo.setTotalEarnedVipDays(user.getTotalEarnedVipDays());

        // 连续签到天数
        try {
            UserCheckin latestCheckin = userCheckinMapper.selectOne(
                    new LambdaQueryWrapper<UserCheckin>()
                            .eq(UserCheckin::getUserId, user.getId())
                            .orderByDesc(UserCheckin::getCheckDate)
                            .last("LIMIT 1"));
            vo.setContinuousDays(latestCheckin != null ? latestCheckin.getContinuousDays() : 0);
        } catch (Exception e) {
            vo.setContinuousDays(0);
        }

        return vo;
    }

    private String desensitizePhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private void validatePasswordFormat(String password) {
        if (StrUtil.isBlank(password) || !password.matches("\\d{4,6}")) {
            throw new BusinessException("密码锁必须为4~6位数字");
        }
    }
}
