package com.gratitude.app.dto.comment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论 VO (树状结构)
 */
@Data
@ApiModel(description = "评论响应VO")
public class CommentVO {

    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("评论者用户ID")
    private Long userId;

    @ApiModelProperty("评论者昵称")
    private String nickname;

    @ApiModelProperty("评论者头像")
    private String avatar;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("父评论ID")
    private Long parentId;

    @ApiModelProperty("被回复用户昵称")
    private String replyNickname;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("是否已点赞(当前用户)")
    private Boolean liked;

    @ApiModelProperty("发表时间")
    private LocalDateTime createTime;

    @ApiModelProperty("子评论列表(最多展示3条，多了用「查看更多回复」)")
    private List<CommentVO> children;
}
