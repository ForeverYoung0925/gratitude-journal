package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Order;
import com.gratitude.app.mapper.OrderMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order")
@Api(tags = "管理后台-订单与充值记录")
@SaCheckLogin(type = "admin")
public class AdminOrderController {

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/page")
    @ApiOperation("订单分页与充值列表")
    public R<Page<Order>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String wechatTxId) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null)
            wrapper.eq(Order::getUserId, userId);
        if (status != null)
            wrapper.eq(Order::getStatus, status);
        if (StrUtil.isNotBlank(orderNo))
            wrapper.eq(Order::getOrderNo, orderNo);
        if (StrUtil.isNotBlank(wechatTxId))
            wrapper.eq(Order::getWxTransactionId, wechatTxId);

        // 按照支付时间或创建时间倒序排
        wrapper.orderByDesc(Order::getCreateTime);

        return R.ok(orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }
}
