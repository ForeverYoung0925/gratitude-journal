package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户课程购买记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_course")
@ApiModel(description = "用户课程购买记录")
public class UserCourse extends BaseEntity {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("关联订单ID")
    private Long orderId;
}
