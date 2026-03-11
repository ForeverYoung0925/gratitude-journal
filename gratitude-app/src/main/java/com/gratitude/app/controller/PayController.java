package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.dto.pay.CreateOrderReq;
import com.gratitude.app.dto.pay.WxPayParamsVO;
import com.gratitude.app.service.impl.PayService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/pay")
@Api(tags = "支付模块")
public class PayController {

    @Autowired
    private PayService payService;

    @PostMapping("/order/create")
    @ApiOperation(value = "创建支付订单", notes = "成功返回微信支付参数，前端拿到后调用 wx.requestPayment() 拉起支付")
    @SaCheckLogin
    public R<WxPayParamsVO> createOrder(@RequestBody @Validated CreateOrderReq req) {
        return R.ok(payService.createOrder(req));
    }

    @PostMapping("/order/{orderNo}/cancel")
    @ApiOperation("取消未支付订单")
    @SaCheckLogin
    public R<Void> cancelOrder(@PathVariable String orderNo) {
        payService.cancelOrder(orderNo);
        return R.ok();
    }

    /**
     * 微信支付异步回调
     * 注意: 此接口不需要登录校验，由微信服务器主动调用
     * 生产环境: 必须验证请求来源IP + 微信V3签名
     */
    @PostMapping("/notify")
    @ApiOperation(value = "微信支付异步回调", hidden = true, notes = "微信服务器回调接口，不对外暴露。必须返回HTTP 200 + 成功标识，否则微信会重复通知")
    public String payNotify(@RequestBody String rawBody,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {
        try {
            payService.handlePayNotify(rawBody, signature);
            // 微信要求成功时返回固定JSON
            return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return "{\"code\":\"FAIL\",\"message\":\"处理失败\"}";
        }
    }
}
