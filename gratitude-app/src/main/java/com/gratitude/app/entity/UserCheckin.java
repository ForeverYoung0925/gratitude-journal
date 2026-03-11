package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户签到表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_checkin")
@ApiModel(description = "用户签到实体")
public class UserCheckin extends BaseEntity {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("签到日期 格式: yyyyMMdd")
    private Integer checkDate;

    @ApiModelProperty("连续签到天数")
    private Integer continuousDays;

    @ApiModelProperty("当日奖励VIP天数(0表示无奖励)")
    private Integer rewardVipDays;
}
