package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Course;
import com.gratitude.app.mapper.CourseMapper;
import com.gratitude.app.service.AuthService;
import com.gratitude.app.service.VipService;
import com.gratitude.app.service.impl.OssService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.R;
import com.gratitude.common.result.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 录播课程控制器
 */
@RestController
@RequestMapping("/course")
@Api(tags = "课程模块")
@SaCheckLogin
public class CourseController {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private VipService vipService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OssService ossService;

    @GetMapping("/list")
    @ApiOperation("课程列表分页查询")
    public R<?> list(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("分类ID(可选)") @RequestParam(required = false) Long categoryId) {

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByDesc(Course::getSort);

        if (categoryId != null) {
            wrapper.eq(Course::getCategoryId, categoryId);
        }

        return R.ok(courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/free")
    @ApiOperation("免费课程/试看专区")
    public R<?> freeList(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(courseMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getType, AppConstants.COURSE_TYPE_FREE)
                        .eq(Course::getStatus, 1)
                        .orderByDesc(Course::getSort)));
    }

    @GetMapping("/{courseId}/play")
    @ApiOperation(value = "获取课程播放URL", notes = "免费课直接返回URL；VIP课检查VIP状态；付费课检查购买记录。非权限用户返回试看URL")
    public R<?> play(@PathVariable Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() == 0) {
            throw new BusinessException(ResultCode.COURSE_NOT_EXIST);
        }

        Long userId = authService.getCurrentUserId();

        // 免费课直接返回
        if (AppConstants.COURSE_TYPE_FREE.equals(course.getType())) {
            String signedUrl = ossService.getSignedVideoUrl(
                    extractOssKey(course.getVideoUrl()), 7200L);
            return R.ok(java.util.Collections.singletonMap("playUrl", signedUrl));
        }

        // VIP课: 检查会员状态
        if (AppConstants.COURSE_TYPE_VIP.equals(course.getType())) {
            if (!vipService.isVip(userId)) {
                // 非VIP: 返回试看URL + 提示
                return R.fail(ResultCode.COURSE_NEED_VIP.getCode(), ResultCode.COURSE_NEED_VIP.getMsg());
            }
            String signedUrl = ossService.getSignedVideoUrl(
                    extractOssKey(course.getVideoUrl()), 7200L);
            return R.ok(java.util.Collections.singletonMap("playUrl", signedUrl));
        }

        // 单独付费课: 检查购买记录 TODO: 查询t_order是否存在该用户该课程的已支付订单
        return R.fail(ResultCode.COURSE_NEED_BUY);
    }

    /**
     * 从完整OSS URL中提取objectKey
     */
    private String extractOssKey(String videoUrl) {
        // 例: https://xxx.oss-cn-hangzhou.aliyuncs.com/course/abc.mp4 -> course/abc.mp4
        int idx = videoUrl.indexOf(".com/");
        return idx >= 0 ? videoUrl.substring(idx + 5) : videoUrl;
    }
}
