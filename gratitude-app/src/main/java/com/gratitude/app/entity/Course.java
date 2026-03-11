package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_course")
@ApiModel(description = "录播课程实体")
public class Course extends BaseEntity {

    @ApiModelProperty("课程标题")
    private String title;

    @ApiModelProperty("课程简介")
    private String description;

    @ApiModelProperty("封面图URL")
    private String coverImage;

    @ApiModelProperty("讲师名称")
    private String teacherName;

    @ApiModelProperty("讲师头像URL")
    private String teacherAvatar;

    @ApiModelProperty("类型 0免费 1VIP免费 2单独付费")
    private Integer type;

    @ApiModelProperty("单独购买价格(分)")
    private Integer price;

    @ApiModelProperty("试看视频URL(前3分钟)")
    private String previewUrl;

    @ApiModelProperty("正式播放URL(OSS防盗链基础URL，需要服务端签名)")
    private String videoUrl;

    @ApiModelProperty("视频时长(秒)")
    private Integer duration;

    @ApiModelProperty("视频大小(字节)")
    private Long videoSize;

    @ApiModelProperty("观看次数")
    private Integer viewCount;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("排序权重(越大越靠前)")
    private Integer sort;

    @ApiModelProperty("状态 0下架 1上架")
    private Integer status;
}
