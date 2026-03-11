package com.gratitude.app.dto.auth;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
@ApiModel(description = "登录响应")
public class LoginVO {

    @ApiModelProperty("Sa-Token 令牌")
    private String token;

    @ApiModelProperty("是否新用户")
    private Boolean isNewUser;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("昵称")
    private String nickname;

    @ApiModelProperty("头像URL")
    private String avatar;

    @ApiModelProperty("是否VIP")
    private Boolean isVip;

    @ApiModelProperty("VIP到期时间(毫秒时间戳，非VIP则为null)")
    private Long vipExpireTimestamp;
}
