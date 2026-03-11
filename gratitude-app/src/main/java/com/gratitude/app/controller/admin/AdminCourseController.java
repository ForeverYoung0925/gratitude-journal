package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Course;
import com.gratitude.app.mapper.CourseMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/course")
@Api(tags = "管理后台-课程与视频管理")
@SaCheckLogin(type = "admin")
public class AdminCourseController {

    @Autowired
    private CourseMapper courseMapper;

    @GetMapping("/page")
    @ApiOperation("查询录播视频列表")
    public R<Page<Course>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(title))
            wrapper.like(Course::getTitle, title);
        if (status != null)
            wrapper.eq(Course::getStatus, status);

        wrapper.orderByDesc(Course::getCreateTime);

        return R.ok(courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PostMapping
    @ApiOperation("发布课程")
    public R<String> save(@RequestBody Course course) {
        // 根据业务需求自动填充默认值，如新发布默认上架等
        if (course.getStatus() == null)
            course.setStatus(1);
        courseMapper.insert(course);
        return R.ok("课程发布成功");
    }

    @PutMapping
    @ApiOperation("编辑课程/调整定价")
    public R<String> update(@RequestBody Course course) {
        if (course.getId() == null) {
            return R.fail("课程ID不能为空");
        }
        courseMapper.updateById(course);
        return R.ok("修改成功");
    }

    @PutMapping("/{id}/status")
    @ApiOperation("课程上架 / 下架")
    public R<String> updateStatus(
            @PathVariable Long id,
            @ApiParam("1上架 0下架") @RequestParam Integer status) {
        courseMapper.update(null, new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, id)
                .set(Course::getStatus, status));
        return R.ok("状态已更新");
    }
}
