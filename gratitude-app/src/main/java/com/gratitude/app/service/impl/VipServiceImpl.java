package com.gratitude.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gratitude.app.entity.User;
import com.gratitude.app.entity.VipPackage;
import com.gratitude.app.entity.VipRecord;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.mapper.VipPackageMapper;
import com.gratitude.app.mapper.VipRecordMapper;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * VIP服务实现
 * 核心防护: 使用Redisson分布式锁防止并发下VIP天数计算错误
 */
@Slf4j
@Service
public class VipServiceImpl implements VipService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VipPackageMapper vipPackageMapper;

    @Autowired
    private VipRecordMapper vipRecordMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public List<VipPackage> listPackages() {
        return vipPackageMapper.selectList(
                new LambdaQueryWrapper<VipPackage>()
                        .eq(VipPackage::getStatus, 1)
                        .orderByDesc(VipPackage::getSort));
    }

    @Override
    public boolean isVip(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        return user.getVipExpireTime() != null
                && user.getVipExpireTime().isAfter(LocalDateTime.now());
    }

    /**
     * 给用户增加VIP天数 (并发安全版本)
     * 设计思路:
     * 1. 用用户级别的分布式锁保证同一个用户不会并发修改VIP时间
     * 2. 从当前VIP到期时间 or 当前时间(两者取较大值) 上累加天数
     * 3. 同步更新user表和写一条vip_record流水
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalDateTime addVipDays(Long userId, int days, int source, String sourceDesc, Long orderId) {
        if (days <= 0) {
            throw new BusinessException("VIP天数必须大于0");
        }

        String lockKey = "lock:vip:user:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException("系统繁忙，请重试");
            }

            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            // 从当前时间 或 现有VIP到期时间 (取较大值) 开始累加
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime baseTime = (user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(now))
                    ? user.getVipExpireTime()
                    : now;
            LocalDateTime newExpireTime = baseTime.plusDays(days);

            // 更新user表VIP到期时间和累计天数
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, userId)
                    .set(User::getVipExpireTime, newExpireTime)
                    .set(User::getTotalEarnedVipDays, user.getTotalEarnedVipDays() + days));

            // 写流水
            VipRecord record = new VipRecord();
            record.setUserId(userId);
            record.setDays(days);
            record.setSource(source);
            record.setSourceDesc(sourceDesc);
            record.setOrderId(orderId);
            record.setVipExpireAfter(newExpireTime);
            vipRecordMapper.insert(record);

            log.info("用户[{}] 获得VIP {}天, 来源={}, 新到期时间={}", userId, days, sourceDesc, newExpireTime);
            return newExpireTime;

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("系统繁忙，请重试");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public List<?> getMyVipRecords(Long userId) {
        return vipRecordMapper.selectList(
                new LambdaQueryWrapper<VipRecord>()
                        .eq(VipRecord::getUserId, userId)
                        .orderByDesc(VipRecord::getCreateTime));
    }
}
