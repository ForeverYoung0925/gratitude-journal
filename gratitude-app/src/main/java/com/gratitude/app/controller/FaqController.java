package com.gratitude.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.entity.Faq;
import com.gratitude.app.mapper.FaqMapper;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/faq")
@Api(tags = "常见问题模块(C端)")
public class FaqController {

    @Autowired
    private FaqMapper faqMapper;

    @GetMapping("/list")
    @ApiOperation("获取展示中的常见问题列表")
    public R<List<Faq>> list() {
        // C端只查询上架的 FAQ，并按照 sort 降序、创建时间降序排列
        List<Faq> list = faqMapper.selectList(
                new LambdaQueryWrapper<Faq>()
                        .eq(Faq::getStatus, 1)
                        .orderByDesc(Faq::getSort)
                        .orderByDesc(Faq::getCreateTime));
        return R.ok(list);
    }
}
