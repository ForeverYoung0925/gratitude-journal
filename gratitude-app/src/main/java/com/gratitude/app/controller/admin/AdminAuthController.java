package com.gratitude.app.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.dto.admin.AdminLoginReq;
import com.gratitude.app.entity.SysUser;
import com.gratitude.app.mapper.SysUserMapper;
import com.gratitude.app.util.StpAdminUtil;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@Api(tags = "管理后台-认证模块")
public class AdminAuthController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public R<Map<String, Object>> login(@RequestBody @Validated AdminLoginReq req) {
        SysUser admin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, req.getUsername()));

        if (admin == null || !passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() == 1) {
            throw new BusinessException("账号已被禁用");
        }

        StpAdminUtil.login(admin.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("token", StpAdminUtil.getTokenValue());
        map.put("username", admin.getUsername());
        map.put("realName", admin.getRealName());

        return R.ok("登录成功", map);
    }

    @PostMapping("/logout")
    @ApiOperation("登出")
    public R<String> logout() {
        StpAdminUtil.logout();
        return R.ok("已退出登录");
    }
}
