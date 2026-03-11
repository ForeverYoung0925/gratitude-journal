package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 支付订单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
@ApiModel(description = "支付订单实体")
public class Order extends BaseEntity {

    @ApiModelProperty("订单编号(业务单号)")
    private String orderNo;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("商品类型 1VIP套餐 2单课购买")
    private Integer goodsType;

    @ApiModelProperty("商品ID(VIP套餐ID 或 课程ID)")
    private Long goodsId;

    @ApiModelProperty("商品快照名称")
    private String goodsName;

    @ApiModelProperty("支付金额(分)")
    private Integer payAmount;

    @ApiModelProperty("订单状态 0待支付 1已支付 2已取消 3已退款")
    private Integer status;

    @ApiModelProperty("微信prepay_id")
    private String wxPrepayId;

    @ApiModelProperty("微信支付交易号")
    private String wxTransactionId;

    @ApiModelProperty("支付完成时间")
    private LocalDateTime paidTime;

    @ApiModelProperty("过期时间(未支付时)")
    private LocalDateTime expireTime;
}
