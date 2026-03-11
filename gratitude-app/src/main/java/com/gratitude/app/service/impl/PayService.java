package com.gratitude.app.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gratitude.app.config.WxPayConfig;
import com.gratitude.app.dto.pay.CreateOrderReq;
import com.gratitude.app.dto.pay.WxPayParamsVO;
import com.gratitude.app.entity.*;
import com.gratitude.app.mapper.*;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

/**
 * 微信支付服务
 *
 * 流程:
 * 1. 客户端调用 createOrder -> 后端创建本地订单 -> 调用微信统一下单API -> 返回支付签名参数给前端
 * 2. 前端调用 wx.requestPayment() 拉起支付
 * 3. 微信服务器在用户支付成功后, 异步回调 payNotify 接口
 * 4. 后端核验签名 -> 更新订单状态 -> 异步发货(给VIP或解锁课程)
 *
 * 注意: 此服务依赖 WxPayConfig 配置类(mchId/mchKey/notifyUrl)
 * 微信支付V3 API的签名算法使用 RSA-SHA256, 此处为简化演示用HMAC-SHA256替代
 * 生产环境务必按微信官方文档实现V3签名并使用微信支付SDK
 */
@Slf4j
@Service
public class PayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private VipPackageMapper vipPackageMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private VipService vipService;

    @Autowired
    private WxPayConfig wxPayConfig;

    // ========== 创建订单并发起微信支付 ==========

    @Transactional(rollbackFor = Exception.class)
    public WxPayParamsVO createOrder(CreateOrderReq req) {
        Long userId = authService.getCurrentUserId();

        // 1. 解析商品信息
        String goodsName;
        int payAmount;
        int vipDays = 0;

        if (AppConstants.ORDER_TYPE_VIP.equals(req.getGoodsType())) {
            VipPackage pkg = vipPackageMapper.selectById(req.getGoodsId());
            if (pkg == null || pkg.getStatus() != 1) {
                throw new BusinessException("VIP套餐不存在或已下架");
            }
            goodsName = pkg.getName();
            payAmount = pkg.getSalePrice();
            vipDays = pkg.getVipDays();
        } else if (AppConstants.ORDER_TYPE_COURSE.equals(req.getGoodsType())) {
            Course course = courseMapper.selectById(req.getGoodsId());
            if (course == null || course.getStatus() != 1) {
                throw new BusinessException(ResultCode.COURSE_NOT_EXIST);
            }
            if (!AppConstants.COURSE_TYPE_PAID.equals(course.getType())) {
                throw new BusinessException("该课程无需单独购买");
            }
            // 检查是否已购买
            long bought = userCourseMapper.selectCount(
                    new LambdaQueryWrapper<UserCourse>()
                            .eq(UserCourse::getUserId, userId)
                            .eq(UserCourse::getCourseId, req.getGoodsId()));
            if (bought > 0) {
                throw new BusinessException("您已购买该课程");
            }
            goodsName = course.getTitle();
            payAmount = course.getPrice();
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        // 2. 生成业务订单号
        String orderNo = generateOrderNo();

        // 3. 写本地订单(待支付状态)
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setGoodsType(req.getGoodsType());
        order.setGoodsId(req.getGoodsId());
        order.setGoodsName(goodsName);
        order.setPayAmount(payAmount);
        order.setStatus(AppConstants.ORDER_STATUS_UNPAID);
        order.setExpireTime(LocalDateTime.now().plusMinutes(15)); // 15分钟内完成支付
        orderMapper.insert(order);

        log.info("创建订单: orderNo={}, userId={}, goods={}, amount={}分",
                orderNo, userId, goodsName, payAmount);

        // 4. 调用微信统一下单(JSAPI模式)，获取prepay_id
        // TODO: 此处为模拟, 生产环境替换为真实微信V3 API调用
        String prepayId = callWxUnifiedOrder(orderNo, goodsName, payAmount, userId);

        // 5. 更新订单存储prepay_id
        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .set(Order::getWxPrepayId, prepayId));

        // 6. 构造前端支付参数并签名
        return buildPayParams(orderNo, prepayId, payAmount, goodsName);
    }

    // ========== 微信支付异步回调 ==========

    @Transactional(rollbackFor = Exception.class)
    public void handlePayNotify(String rawBody, String wechatSignature) {
        // 1. 核验微信签名(防止伪造回调)
        // TODO: 生产环境必须实现微信V3回调签名验证
        // wxPaySignatureVerifier.verify(rawBody, wechatSignature);

        // 2. 解析通知内容(微信V3密文解密)
        // 此处为简化, 生产需要对resource.ciphertext进行AES-256-GCM解密
        String transactionId = parseTransactionId(rawBody);
        String orderNo = parseOrderNo(rawBody);
        String tradeState = parseTradeState(rawBody);

        log.info("微信支付回调 orderNo={}, transactionId={}, state={}", orderNo, transactionId, tradeState);

        if (!"SUCCESS".equals(tradeState)) {
            log.warn("支付未成功, state={}", tradeState);
            return;
        }

        // 3. 幂等处理: 查询本地订单
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            log.error("回调订单不存在: orderNo={}", orderNo);
            return;
        }
        if (AppConstants.ORDER_STATUS_PAID.equals(order.getStatus())) {
            log.warn("订单已处理,忽略重复回调: orderNo={}", orderNo);
            return; // 幂等: 已处理过直接返回
        }

        // 4. 更新订单状态为已支付
        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getStatus, AppConstants.ORDER_STATUS_UNPAID) // 乐观锁
                .set(Order::getStatus, AppConstants.ORDER_STATUS_PAID)
                .set(Order::getWxTransactionId, transactionId)
                .set(Order::getPaidTime, LocalDateTime.now()));

        // 5. 发货
        deliverGoods(order);
    }

    // ========== 取消未支付订单 ==========

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        Long userId = authService.getCurrentUserId();
        int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId)
                .eq(Order::getStatus, AppConstants.ORDER_STATUS_UNPAID)
                .set(Order::getStatus, AppConstants.ORDER_STATUS_CANCELLED));
        if (rows == 0) {
            throw new BusinessException("订单不存在或无法取消");
        }
    }

    // ========== 私有: 发货 ==========

    private void deliverGoods(Order order) {
        if (AppConstants.ORDER_TYPE_VIP.equals(order.getGoodsType())) {
            // 查询套餐天数
            VipPackage pkg = vipPackageMapper.selectById(order.getGoodsId());
            if (pkg != null) {
                vipService.addVipDays(order.getUserId(), pkg.getVipDays(),
                        AppConstants.VIP_SOURCE_PAY, "购买" + pkg.getName(), order.getId());
                log.info("VIP发货完成: userId={}, 天数={}", order.getUserId(), pkg.getVipDays());
            }
        } else if (AppConstants.ORDER_TYPE_COURSE.equals(order.getGoodsType())) {
            // 解锁课程
            UserCourse uc = new UserCourse();
            uc.setUserId(order.getUserId());
            uc.setCourseId(order.getGoodsId());
            uc.setOrderId(order.getId());
            userCourseMapper.insert(uc);
            log.info("课程发货完成: userId={}, courseId={}", order.getUserId(), order.getGoodsId());
        }
    }

    // ========== 私有: 调用微信统一下单(模拟) ==========

    private String callWxUnifiedOrder(String orderNo, String goodsName, int amount, Long userId) {
        // TODO: 替换为真实微信支付V3 JSAPI统一下单
        // POST https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi
        // 请求体包含: appid, mchid, description, out_trade_no, notify_url, amount,
        // payer.openid
        log.info("[模拟]调用微信统一下单: orderNo={}, amount={}分", orderNo, amount);
        return "wx" + System.currentTimeMillis(); // 模拟prepay_id
    }

    // ========== 私有: 构造前端支付签名参数 ==========

    private WxPayParamsVO buildPayParams(String orderNo, String prepayId, int amount, String goodsName) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = IdUtil.fastSimpleUUID();
        String packageStr = "prepay_id=" + prepayId;

        // 签名原串: appId\n时间戳\n随机字符串\npackage\n
        String signStr = wxPayConfig.getAppId() + "\n"
                + timeStamp + "\n"
                + nonceStr + "\n"
                + packageStr + "\n";

        String paySign = hmacSha256(signStr, wxPayConfig.getMchKey());

        WxPayParamsVO vo = new WxPayParamsVO();
        vo.setOrderNo(orderNo);
        vo.setAppId(wxPayConfig.getAppId());
        vo.setTimeStamp(timeStamp);
        vo.setNonceStr(nonceStr);
        vo.setPackageStr(packageStr);
        vo.setSignType("HMAC-SHA256");
        vo.setPaySign(paySign);
        vo.setPayAmount(amount);
        vo.setGoodsName(goodsName);
        return vo;
    }

    private String generateOrderNo() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmss") + IdUtil.fastSimpleUUID().substring(0, 10).toUpperCase();
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException("签名失败");
        }
    }

    // ========== 以下为微信回调XML/JSON解析(模拟) ==========

    private String parseTransactionId(String body) {
        // TODO: 实际解析微信V3回调JSON -> 解密resource.ciphertext
        return "wx_transaction_mock";
    }

    private String parseOrderNo(String body) {
        // TODO: 解析 out_trade_no
        return "";
    }

    private String parseTradeState(String body) {
        // TODO: 解析 trade_state
        return "SUCCESS";
    }
}
