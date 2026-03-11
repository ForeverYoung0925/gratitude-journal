package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.entity.VipPackage;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.VipService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VIP控制器
 */
@RestController
@RequestMapping("/vip")
@Api(tags = "VIP模块")
@SaCheckLogin
public class VipController {

    @Autowired
    private VipService vipService;

    @Autowired
    private AuthService authService;

    @GetMapping("/packages")
    @ApiOperation("获取VIP套餐列表")
    public R<List<VipPackage>> packages() {
        return R.ok(vipService.listPackages());
    }

    @GetMapping("/records")
    @ApiOperation("我的VIP流水记录")
    public R<?> myRecords() {
        Long userId = authService.getCurrentUserId();
        return R.ok(vipService.getMyVipRecords(userId));
    }
}
