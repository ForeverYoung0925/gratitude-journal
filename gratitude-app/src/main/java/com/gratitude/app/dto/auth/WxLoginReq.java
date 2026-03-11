package com.gratitude.app.dto.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信授权登录请求
 */
@Data
@ApiModel(description = "微信授权登录请求")
public class WxLoginReq {

    @NotBlank(message = "微信授权码不能为空")
    @ApiModelProperty(value = "微信小程序临时授权码 code", required = true)
    private String code;

    @ApiModelProperty("用户昵称(微信获取)")
    private String nickname;

    @ApiModelProperty("用户头像(微信获取)")
    private String avatar;
}
