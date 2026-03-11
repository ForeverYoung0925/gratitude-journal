package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 观看进度表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_watch_progress")
@ApiModel(description = "观看进度实体")
public class WatchProgress extends BaseEntity {

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("播放进度(秒)")
    private Integer progressSeconds;

    @ApiModelProperty("是否看完 0否 1是")
    private Integer isFinished;

    @ApiModelProperty("最近观看时间")
    private LocalDateTime lastWatchTime;
}
