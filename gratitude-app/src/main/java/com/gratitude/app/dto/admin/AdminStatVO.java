package com.gratitude.app.dto.admin;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AdminStatVO {
    @ApiModelProperty("今日新增用户数")
    private Long newUsersToday;

    @ApiModelProperty("今日新增日记数")
    private Long newDiariesToday;

    @ApiModelProperty("今日新增VIP数")
    private Long newVipsToday;

    @ApiModelProperty("今日充值流水汇总(分)")
    private Long rechargeAmountToday;
}
