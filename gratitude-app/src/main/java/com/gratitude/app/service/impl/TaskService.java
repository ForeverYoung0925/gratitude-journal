package com.gratitude.app.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.entity.UserCheckin;
import com.gratitude.app.mapper.UserCheckinMapper;
import com.gratitude.app.service.VipService;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.constants.CacheConstants;
import com.gratitude.common.exception.BusinessException;
import com.gratitude.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 签到 & 任务 服务
 *
 * 当前VIP激励规则(后续可配置化到数据库):
 * - 每日签到: +1天 VIP
 * - 连续签到7天: 额外 +3天 VIP
 * - 发布1篇日记: +1天 VIP (每日上限1次)
 * - 当日日记被点赞≥5: +1天 VIP (每日上限1次)
 */
@Slf4j
@Service
public class TaskService {

    @Autowired
    private UserCheckinMapper userCheckinMapper;

    @Autowired
    private VipService vipService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 每日签到
     * 
     * @return 返回本次签到连续天数
     */
    @Transactional(rollbackFor = Exception.class)
    public int checkIn(Long userId) {
        int today = Integer.parseInt(DateUtil.format(new Date(), "yyyyMMdd"));

        // 防重签: 检查今日是否已签到
        UserCheckin todayRecord = userCheckinMapper.selectOne(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .eq(UserCheckin::getCheckDate, today));
        if (todayRecord != null) {
            throw new BusinessException("今日已签到，明天再来哦~");
        }

        // 查昨天签到记录，计算连续天数
        int yesterday = Integer.parseInt(DateUtil.format(
                DateUtil.offsetDay(new Date(), -1), "yyyyMMdd"));
        UserCheckin yesterdayRecord = userCheckinMapper.selectOne(
                new LambdaQueryWrapper<UserCheckin>()
                        .eq(UserCheckin::getUserId, userId)
                        .eq(UserCheckin::getCheckDate, yesterday));

        int continuousDays = (yesterdayRecord != null) ? yesterdayRecord.getContinuousDays() + 1 : 1;

        // 计算奖励
        int rewardDays = 1; // 基础奖励1天
        String rewardDesc = "每日签到奖励";

        if (continuousDays % 7 == 0) {
            rewardDays += 3; // 连续7天额外奖3天
            rewardDesc = "连续签到" + continuousDays + "天特别奖励";
        }

        // 写签到记录
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckDate(today);
        checkin.setContinuousDays(continuousDays);
        checkin.setRewardVipDays(rewardDays);
        userCheckinMapper.insert(checkin);

        // 发放VIP
        vipService.addVipDays(userId, rewardDays, AppConstants.VIP_SOURCE_TASK, rewardDesc, null);

        log.info("用户[{}]签到成功, 连续{}天, 获得{}天VIP", userId, continuousDays, rewardDays);
        return continuousDays;
    }

    /**
     * 发布日记奖励 (由DiaryService发布成功后调用)
     * 每日限制一次
     */
    @Transactional(rollbackFor = Exception.class)
    public void rewardForPublishDiary(Long userId) {
        String key = CacheConstants.USER_TASK_STATE_PREFIX + userId + ":publish:" + todayStr();
        if (Boolean.TRUE.equals(redisUtil.get(key, Boolean.class))) {
            return; // 今日已领取过
        }
        redisUtil.set(key, true, CacheConstants.EXPIRE_1_DAY, TimeUnit.SECONDS);
        vipService.addVipDays(userId, 1, AppConstants.VIP_SOURCE_TASK, "发布日记奖励", null);
        log.info("用户[{}]发布日记获得1天VIP奖励", userId);
    }

    /**
     * 被点赞奖励 (由点赞service触发)
     * 条件: 当日被点赞数达到5次，每日限制奖励一次
     */
    @Transactional(rollbackFor = Exception.class)
    public void rewardForReceiveLike(Long diaryAuthorId, Long diaryId) {
        // 检查今日是否已经触发过该奖励
        String rewardedKey = CacheConstants.USER_TASK_STATE_PREFIX + diaryAuthorId + ":like-reward:" + todayStr();
        if (Boolean.TRUE.equals(redisUtil.get(rewardedKey, Boolean.class))) {
            return;
        }

        // 统计当日被点赞次数
        String likeCountKey = CacheConstants.USER_TASK_STATE_PREFIX + diaryAuthorId + ":like-count:" + todayStr();
        Long count = redisUtil.increment(likeCountKey, 1L);
        if (count == 1) {
            redisUtil.expire(likeCountKey, CacheConstants.EXPIRE_1_DAY, TimeUnit.SECONDS);
        }

        if (count != null && count >= 5) {
            // 标记已发放
            redisUtil.set(rewardedKey, true, CacheConstants.EXPIRE_1_DAY, TimeUnit.SECONDS);
            vipService.addVipDays(diaryAuthorId, 1, AppConstants.VIP_SOURCE_TASK,
                    "日记被点赞达到5次奖励", null);
            log.info("用户[{}]被点赞达5次，获得1天VIP奖励", diaryAuthorId);
        }
    }

    private String todayStr() {
        return DateUtil.format(new Date(), "yyyyMMdd");
    }
}
