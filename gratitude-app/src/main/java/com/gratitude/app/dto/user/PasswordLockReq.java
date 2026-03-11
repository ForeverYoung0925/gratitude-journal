package com.gratitude.app.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 密码锁操作请求
 */
@Data
@ApiModel(description = "密码锁请求")
public class PasswordLockReq {

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码(4~6位数字)", required = true)
    private String password;

    @ApiModelProperty("旧密码(修改密码时需要)")
    private String oldPassword;
}
