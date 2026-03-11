package com.gratitude.app.dto.pay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 微信支付拉起参数 VO
 * 前端收到后调用 wx.requestPayment() 拉起支付
 */
@Data
@ApiModel(description = "微信支付参数VO")
public class WxPayParamsVO {

    @ApiModelProperty("业务订单号")
    private String orderNo;

    @ApiModelProperty("appId")
    private String appId;

    @ApiModelProperty("时间戳")
    private String timeStamp;

    @ApiModelProperty("随机字符串")
    private String nonceStr;

    @ApiModelProperty("订单详情扩展字符串 prepay_id=...")
    private String packageStr;

    @ApiModelProperty("签名方式 RSA")
    private String signType;

    @ApiModelProperty("签名")
    private String paySign;

    @ApiModelProperty("支付金额(分)")
    private Integer payAmount;

    @ApiModelProperty("商品名称")
    private String goodsName;
}
