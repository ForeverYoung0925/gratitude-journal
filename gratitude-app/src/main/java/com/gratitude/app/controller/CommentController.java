package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.dto.comment.CommentReq;
import com.gratitude.app.dto.comment.CommentVO;
import com.gratitude.app.service.impl.CommentService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/comment")
@Api(tags = "评论模块")
@SaCheckLogin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    @ApiOperation(value = "发表评论", notes = "parentId=null 时为顶级评论；传 parentId 表示回复某条评论")
    public R<Long> addComment(@RequestBody @Validated CommentReq req) {
        return R.ok(commentService.addComment(req));
    }

    @GetMapping("/diary/{diaryId}")
    @ApiOperation(value = "获取日记评论列表", notes = "返回顶级评论，每条附最多3条子评论预览，超出子评论通过「查看更多回复」接口加载")
    public R<List<CommentVO>> listComments(
            @PathVariable Long diaryId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return R.ok(commentService.listComments(diaryId, pageNum, pageSize));
    }

    @GetMapping("/{parentId}/children")
    @ApiOperation("查看更多子评论(加载所有回复)")
    public R<List<CommentVO>> listChildComments(
            @PathVariable Long parentId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return R.ok(commentService.listChildComments(parentId, pageNum, pageSize));
    }

    @PostMapping("/{commentId}/like")
    @ApiOperation("点赞/取消点赞评论")
    public R<Boolean> likeComment(@PathVariable Long commentId) {
        return R.ok(commentService.toggleCommentLike(commentId));
    }

    @DeleteMapping("/{commentId}")
    @ApiOperation("删除自己的评论")
    public R<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return R.ok();
    }
}
