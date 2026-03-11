package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台管理员表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user")
@ApiModel(description = "后台管理员")
public class SysUser extends BaseEntity {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码(BCrypt加密)")
    private String password;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("状态 0正常 1禁用")
    private Integer status;
}
