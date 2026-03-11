package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gratitude.app.entity.Faq;
import com.gratitude.app.mapper.FaqMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/faq")
@Api(tags = "管理后台-常见问题管理")
@SaCheckLogin(type = "admin")
public class AdminFaqController {

    @Autowired
    private FaqMapper faqMapper;

    @GetMapping("/page")
    @ApiOperation("FAQ分页查询")
    public R<Page<Faq>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Faq> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Faq::getQuestion, keyword);
        }
        if (status != null) {
            wrapper.eq(Faq::getStatus, status);
        }
        wrapper.orderByDesc(Faq::getSort).orderByDesc(Faq::getCreateTime);

        return R.ok(faqMapper.selectPage(new Page<>(pageNum, pageSize), wrapper));
    }

    @PostMapping
    @ApiOperation("新增FAQ")
    public R<String> save(@RequestBody Faq faq) {
        if (faq.getStatus() == null)
            faq.setStatus(1);
        faqMapper.insert(faq);
        return R.ok("新增成功");
    }

    @PutMapping
    @ApiOperation("修改FAQ")
    public R<String> update(@RequestBody Faq faq) {
        faqMapper.updateById(faq);
        return R.ok("修改成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除FAQ")
    public R<String> delete(@PathVariable Long id) {
        faqMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新FAQ状态(0隐藏 1显示)")
    public R<Void> updateStatus(
            @PathVariable Long id,
            @ApiParam("0隐藏 1显示") @RequestParam Integer status) {
        faqMapper.update(null, new LambdaUpdateWrapper<Faq>()
                .eq(Faq::getId, id)
                .set(Faq::getStatus, status));
        return R.ok();
    }
}
