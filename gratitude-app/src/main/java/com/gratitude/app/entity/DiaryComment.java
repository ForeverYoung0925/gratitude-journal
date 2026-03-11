package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 日记评论表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_diary_comment")
@ApiModel(description = "日记评论实体")
public class DiaryComment extends BaseEntity {

    @ApiModelProperty("评论所属日记ID")
    private Long diaryId;

    @ApiModelProperty("评论者用户ID")
    private Long userId;

    @ApiModelProperty("父评论ID(0表示顶级评论)")
    private Long parentId;

    @ApiModelProperty("被回复的评论ID")
    private Long replyCommentId;

    @ApiModelProperty("被回复的用户ID")
    private Long replyUserId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论点赞数")
    private Integer likeCount;

    @ApiModelProperty("状态 0正常 1已审核删除")
    private Integer status;
}
