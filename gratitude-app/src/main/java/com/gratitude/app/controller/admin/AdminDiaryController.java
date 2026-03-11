package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.mapper.DiaryMapper;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/diary")
@Api(tags = "管理后台-日记内容安全管理")
@SaCheckLogin(type = "admin")
public class AdminDiaryController {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/page")
    @ApiOperation("日记大盘分页监控(查询每日提交状况)")
    public R<Page<Diary>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        if (userId != null)
            wrapper.eq(Diary::getUserId, userId);
        if (status != null)
            wrapper.eq(Diary::getStatus, status);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Diary::getTitle, keyword).or().like(Diary::getContent, keyword));
        }
        wrapper.orderByDesc(Diary::getCreateTime);

        return R.ok(diaryMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PutMapping("/{id}/status")
    @ApiOperation("违规日记强制下架强制拒绝")
    public R<String> updateStatus(
            @PathVariable Long id,
            @ApiParam("日记最终状态，传3代表强制拒绝并隐藏") @RequestParam Integer status,
            @ApiParam("拒绝理由给用户展示") @RequestParam(required = false) String rejectReason) {
        diaryMapper.update(null, new LambdaUpdateWrapper<Diary>()
                .eq(Diary::getId, id)
                .set(Diary::getStatus, status)
                .set(StrUtil.isNotBlank(rejectReason), Diary::getRejectReason, rejectReason));
        return R.ok("处理成功");
    }
}
