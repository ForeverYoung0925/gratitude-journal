package com.gratitude.app.dto.pay;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建支付订单请求
 */
@Data
@ApiModel(description = "创建支付订单请求")
public class CreateOrderReq {

    @NotNull(message = "商品类型不能为空")
    @ApiModelProperty(value = "商品类型 1VIP套餐 2单课购买", required = true)
    private Integer goodsType;

    @NotNull(message = "商品ID不能为空")
    @ApiModelProperty(value = "商品ID(VIP套餐ID 或 课程ID)", required = true)
    private Long goodsId;
}
