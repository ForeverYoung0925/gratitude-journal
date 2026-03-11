package com.gratitude.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gratitude.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VIP套餐配置表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_vip_package")
@ApiModel(description = "VIP套餐实体")
public class VipPackage extends BaseEntity {

    @ApiModelProperty("套餐名称")
    private String name;

    @ApiModelProperty("套餐描述")
    private String description;

    @ApiModelProperty("原价(分)")
    private Integer originalPrice;

    @ApiModelProperty("售卖价格(分)")
    private Integer salePrice;

    @ApiModelProperty("赠送VIP天数")
    private Integer vipDays;

    @ApiModelProperty("VIP类型 1月度 2年度")
    private Integer vipType;

    @ApiModelProperty("是否推荐 0否 1是")
    private Integer recommended;

    @ApiModelProperty("状态 0下架 1上架")
    private Integer status;

    @ApiModelProperty("排序权重")
    private Integer sort;

    @ApiModelProperty("展示标签(如:最划算)")
    private String tag;
}
