package com.gratitude.app.dto.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 手机号登录请求
 */
@Data
@ApiModel(description = "手机号登录请求")
public class PhoneLoginReq {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @ApiModelProperty(value = "短信验证码", required = true)
    private String code;

    @ApiModelProperty("微信code(可选,用于同时绑定微信)")
    private String wxCode;
}
