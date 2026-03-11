package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * VIP奖励流水表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_vip_record")
@ApiModel(description = "VIP流水实体")
public class VipRecord extends BaseEntity {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("变动天数(正数为增加,负数为扣除)")
    private Integer days;

    @ApiModelProperty("来源 1购买 2注册赠送 3任务奖励 4管理员发放")
    private Integer source;

    @ApiModelProperty("来源描述(如: 发布日记奖励)")
    private String sourceDesc;

    @ApiModelProperty("关联订单ID")
    private Long orderId;

    @ApiModelProperty("VIP变动后的到期时间")
    private LocalDateTime vipExpireAfter;
}
