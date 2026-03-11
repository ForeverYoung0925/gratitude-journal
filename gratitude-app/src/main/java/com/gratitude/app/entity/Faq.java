package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 常见问题表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_faq")
@ApiModel(description = "常见问题")
public class Faq extends BaseEntity {

    @ApiModelProperty("问题标题")
    private String question;

    @ApiModelProperty("问题解答")
    private String answer;

    @ApiModelProperty("排序权重(降序)")
    private Integer sort;

    @ApiModelProperty("状态 0隐藏 1显示")
    private Integer status;
}
