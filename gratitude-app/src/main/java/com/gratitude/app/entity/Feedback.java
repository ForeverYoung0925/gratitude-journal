package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 意见反馈表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_feedback")
@ApiModel(description = "意见反馈实体")
public class Feedback extends BaseEntity {

    @ApiModelProperty("反馈用户ID")
    private Long userId;

    @ApiModelProperty("反馈内容")
    private String content;

    @ApiModelProperty("附件图片(JSON数组)")
    private String images;

    @ApiModelProperty("联系方式")
    private String contact;

    @ApiModelProperty("状态 0待处理 1处理中 2已处理")
    private Integer status;

    @ApiModelProperty("后台处理回复备注")
    private String replyNote;
}
