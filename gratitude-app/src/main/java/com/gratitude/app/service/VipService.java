package com.gratitude.app.service;

import com.gratitude.app.entity.VipPackage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP 服务接口
 */
public interface VipService {

    /**
     * 获取VIP套餐列表
     */
    List<VipPackage> listPackages();

    /**
     * 判断用户是否为有效VIP
     */
    boolean isVip(Long userId);

    /**
     * 给用户增加VIP天数
     * @param userId     用户ID
     * @param days       增加天数
     * @param source     来源(参考AppConstants.VIP_SOURCE_*)
     * @param sourceDesc 来源描述
     * @param orderId    关联订单ID(无则传null)
     */
    LocalDateTime addVipDays(Long userId, int days, int source, String sourceDesc, Long orderId);

    /**
     * 获取我的VIP流水记录(前端: 赚VIP记录)
     */
    List<?> getMyVipRecords(Long userId);
}
