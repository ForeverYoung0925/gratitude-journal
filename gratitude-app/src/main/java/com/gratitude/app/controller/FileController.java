package com.gratitude.app.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gratitude.app.service.impl.OssService;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件上传模块")
@SaCheckLogin
public class FileController {

    @Autowired
    private OssService ossService;

    @GetMapping("/presign")
    @ApiOperation(value = "获取OSS预签名上传URL (客户端直传)", notes = "前端拿到uploadUrl后使用HTTP PUT方法直传文件到OSS，完成后将accessUrl传给业务接口")
    public R<Map<String, String>> getPresignUrl(
            @ApiParam(value = "业务类型 diary/avatar/course", required = true) @RequestParam String dir,
            @ApiParam(value = "文件后缀名 如jpg/png/mp4", required = true) @RequestParam String suffix) {
        return R.ok(ossService.getPresignedUploadUrl(dir + "/", suffix));
    }
}
