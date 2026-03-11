package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.User;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/user")
@Api(tags = "管理后台-用户管理")
@SaCheckLogin(type = "admin")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VipService vipService;

    @GetMapping("/page")
    @ApiOperation("用户分页查询")
    public R<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        return R.ok(userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新用户状态(封禁/解封)")
    public R<Void> updateStatus(
            @PathVariable Long id,
            @ApiParam("0正常 1封禁") @RequestParam Integer status) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getStatus, status));
        return R.ok();
    }

    @PostMapping("/{id}/vip")
    @ApiOperation("人工发放VIP天数(特批补贴)")
    public R<String> addVipDays(
            @PathVariable Long id,
            @ApiParam("增加VIP天数") @RequestParam Integer days,
            @ApiParam("操作备注") @RequestParam(defaultValue = "管理员后台手发特权") String remark) {
        vipService.addVipDays(id, days, AppConstants.VIP_SOURCE_ADMIN, remark, null);
        return R.ok("发放成功");
    }
}
