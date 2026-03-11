package com.gratitude.app.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息 VO
 */
@Data
@ApiModel(description = "用户信息响应")
public class UserInfoVO {

    @ApiModelProperty("用户ID")
    private Long id;

    @ApiModelProperty("手机号(脱敏: 138****8888)")
    private String phone;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("性别 0未知 1男 2女")
    private Integer gender;

    @ApiModelProperty("是否VIP")
    private Boolean isVip;

    @ApiModelProperty("VIP到期时间(毫秒时间戳)")
    private Long vipExpireTimestamp;

    @ApiModelProperty("是否开启密码锁")
    private Boolean hasPasswordLock;

    @ApiModelProperty("是否开启写日记提醒")
    private Boolean reminderEnabled;

    @ApiModelProperty("提醒时间 HH:mm")
    private String reminderTime;

    @ApiModelProperty("累计获得VIP总天数")
    private Integer totalEarnedVipDays;

    @ApiModelProperty("连续签到天数")
    private Integer continuousDays;
}
