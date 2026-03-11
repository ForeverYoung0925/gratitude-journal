package com.gratitude.app.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 修改用户信息请求
 */
@Data
@ApiModel(description = "修改用户信息请求")
public class UpdateUserReq {

    @Size(max = 20, message = "昵称不能超过20个字符")
    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL(直传OSS后回传)")
    private String avatar;

    @ApiModelProperty("生日")
    private LocalDate birthday;

    @ApiModelProperty("性别 0未知 1男 2女")
    private Integer gender;
}
