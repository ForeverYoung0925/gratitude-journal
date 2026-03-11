package com.gratitude.app.dto.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发表评论请求
 */
@Data
@ApiModel(description = "发表评论请求")
public class CommentReq {

    @NotNull(message = "日记ID不能为空")
    @ApiModelProperty(value = "日记ID", required = true)
    private Long diaryId;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 300, message = "评论内容不能超过300个字")
    @ApiModelProperty(value = "评论内容", required = true)
    private String content;

    @ApiModelProperty("父评论ID(回复评论时传入，不传则为顶级评论)")
    private Long parentId;

    @ApiModelProperty("被回复的评论ID")
    private Long replyCommentId;

    @ApiModelProperty("被回复的用户ID")
    private Long replyUserId;
}
