package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
@ApiModel(description = "用户实体")
public class User extends BaseEntity {

    @ApiModelProperty("微信openid")
    private String openid;

    @ApiModelProperty("微信unionid")
    private String unionid;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("性别 0未知 1男 2女")
    private Integer gender;

    @ApiModelProperty("账号状态 0正常 1封禁")
    private Integer status;

    @ApiModelProperty("VIP到期时间")
    private LocalDateTime vipExpireTime;

    @ApiModelProperty("是否设置了密码锁 0否 1是")
    private Integer hasPasswordLock;

    @ApiModelProperty("密码锁(加密存储)")
    private String passwordLock;

    @ApiModelProperty("是否开启写日记提醒 0否 1是")
    private Integer reminderEnabled;

    @ApiModelProperty("提醒时间 格式HH:mm")
    private String reminderTime;

    @ApiModelProperty("注册来源 1微信授权 2手机号")
    private Integer source;

    @ApiModelProperty("累计获得VIP总天数(用于激励展示)")
    private Integer totalEarnedVipDays;
}
