package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 感恩日记表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_diary")
@ApiModel(description = "感恩日记实体")
public class Diary extends BaseEntity {

    @ApiModelProperty("作者用户ID")
    private Long userId;

    @ApiModelProperty("日记标题")
    private String title;

    @ApiModelProperty("日记正文")
    private String content;

    @ApiModelProperty("图片URL列表, JSON数组格式")
    private String images;

    @ApiModelProperty("情绪标签 1开心 2感动 3平静 4难过")
    private Integer mood;

    @ApiModelProperty("可见性 0私密 1公开")
    private Integer visible;

    @ApiModelProperty("状态 0草稿 1审核中 2已发布 3已拒绝 4垃圾桶")
    private Integer status;

    @ApiModelProperty("拒绝原因(审核拒绝时填充)")
    private String rejectReason;

    @ApiModelProperty("点赞数(冗余字段,定期从Redis同步)")
    private Integer likeCount;

    @ApiModelProperty("评论数(冗余字段)")
    private Integer commentCount;

    @ApiModelProperty("收藏数(冗余字段)")
    private Integer collectCount;

    @ApiModelProperty("查看次数")
    private Integer viewCount;

    @ApiModelProperty("分享次数")
    private Integer shareCount;

    @ApiModelProperty("是否置顶 0否 1是")
    private Integer isTop;

    @ApiModelProperty("是否星标 0否 1是")
    private Integer isStar;

    @ApiModelProperty("微信内容检测任务ID(异步审核追踪)")
    private String wxMediaJobId;
}
