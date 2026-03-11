package com.gratitude.app.dto.diary;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 发布日记请求
 */
@Data
@ApiModel(description = "发布日记请求")
public class DiaryPublishReq {

    @ApiModelProperty("日记标题(可为空)")
    @Size(max = 50, message = "标题不能超过50个字")
    private String title;

    @NotBlank(message = "日记内容不能为空")
    @Size(min = 10, max = 5000, message = "日记内容需在10~5000字之间")
    @ApiModelProperty(value = "日记正文", required = true)
    private String content;

    @ApiModelProperty("图片URL列表(最多9张,直传OSS后传URL)")
    @Size(max = 9, message = "图片最多上传9张")
    private List<String> images;

    @ApiModelProperty("情绪标签 1开心 2感动 3平静 4难过")
    private Integer mood;

    @ApiModelProperty("可见性 0私密 1公开广场")
    private Integer visible;

    @ApiModelProperty("草稿ID(从草稿发布时传入)")
    private Long draftId;
}
