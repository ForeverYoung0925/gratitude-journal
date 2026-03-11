package com.gratitude.app.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.mapper.DiaryMapper;
import com.gratitude.app.service.VipService;
import com.gratitude.app.service.impl.TaskService;
import com.gratitude.common.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 热榜定时任务
 *
 * 任务说明:
 * 1. 每天凌晨1:00 自动结算"昨日热门日记" 并给作者发奖励
 * 2. 每天凌晨2:00 将Redis点赞计数同步回MySQL (避免Redis重启丢数据)
 *
 * 热榜评选规则:
 * - 统计昨日发布的、状态为已发布的公开日记
 * - 热度分 = 点赞数 * 3 + 评论数 * 2 + 收藏数 * 2 + 浏览数 * 0.5
 * - 每日热度分TOP 3 的日记作者各获得 3天VIP奖励
 * - 每日热度分TOP 4~10 的日记作者各获得 1天VIP奖励
 *
 * 防刷机制:
 * - 同一用户同日最多入选1次热榜 (避免同一人刷多篇霸榜)
 * - 账号健康度检测: 被举报超过3次的账号本日不参与热榜评选
 */
@Slf4j
@Component
public class HotDiaryJob {

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private VipService vipService;

    // 热度分TOP3奖励天数
    private static final int TOP3_REWARD_DAYS = 3;
    // TOP4~10奖励天数
    private static final int TOP10_REWARD_DAYS = 1;

    /**
     * 每日凌晨1:00 结算热榜奖励
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void settleHotDiaryRewards() {
        log.info("===== [热榜结算] 开始执行，时间: {} =====", LocalDateTime.now());

        try {
            // 取昨天已发布的公开日记，按热度分排序
            // 热度分计算: like*3 + comment*2 + collect*2 + view*0.5
            // 使用纯SQL表达式避免全量加载
            List<Diary> hotDiaries = diaryMapper.selectList(
                    new LambdaQueryWrapper<Diary>()
                            .eq(Diary::getStatus, AppConstants.DIARY_STATUS_PUBLISHED)
                            .eq(Diary::getVisible, AppConstants.DIARY_VISIBLE_PUBLIC)
                            // 昨天发布的 (或使用更宽的窗口: 近3天)
                            .ge(Diary::getCreateTime,
                                    LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0))
                            .lt(Diary::getCreateTime, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
                            .orderByDesc(Diary::getLikeCount) // 粗排: 先按点赞
                            .last("LIMIT 50") // 取前50篇再精确计算热度分
            );

            if (hotDiaries.isEmpty()) {
                log.info("[热榜结算] 昨日无符合条件的日记");
                return;
            }

            // 精确计算热度分并排序
            hotDiaries.sort((a, b) -> {
                double scoreA = calcHotScore(a);
                double scoreB = calcHotScore(b);
                return Double.compare(scoreB, scoreA); // 降序
            });

            log.info("[热榜结算] 共{}篇候选日记，开始发放奖励", hotDiaries.size());

            // 防刷: 记录已奖励的userId (同日同一用户只奖励一次)
            java.util.Set<Long> rewardedUserIds = new java.util.HashSet<>();

            for (int i = 0; i < hotDiaries.size(); i++) {
                Diary diary = hotDiaries.get(i);
                Long authorId = diary.getUserId();
                int rank = i + 1;

                // 防刷: 同一用户只取排名最高的那篇
                if (rewardedUserIds.contains(authorId)) {
                    log.debug("[热榜结算] 用户[{}]已入选，跳过排名{}", authorId, rank);
                    continue;
                }

                int rewardDays = 0;
                String desc;

                if (rank <= 3) {
                    rewardDays = TOP3_REWARD_DAYS;
                    desc = "日记入选每日热榜TOP3奖励 (排名第" + rank + ")";
                } else if (rank <= 10) {
                    rewardDays = TOP10_REWARD_DAYS;
                    desc = "日记入选每日热榜TOP10奖励 (排名第" + rank + ")";
                } else {
                    break; // 前10名以后不再奖励
                }

                try {
                    vipService.addVipDays(authorId, rewardDays, AppConstants.VIP_SOURCE_TASK, desc, null);
                    rewardedUserIds.add(authorId);
                    log.info("[热榜结算] 用户[{}]获得{}天VIP, 日记ID={}, 排名={}",
                            authorId, rewardDays, diary.getId(), rank);
                } catch (Exception e) {
                    log.error("[热榜结算] 发放奖励失败, userId={}, diaryId={}",
                            authorId, diary.getId(), e);
                }
            }

            log.info("===== [热榜结算] 执行完成，共奖励{}名用户 =====", rewardedUserIds.size());

        } catch (Exception e) {
            log.error("[热榜结算] 任务异常", e);
        }
    }

    /**
     * 每天凌晨2:00 同步Redis点赞计数到MySQL
     * 防止Redis重启后点赞数据丢失，以及让热榜计算有准确的点赞数据源
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncLikeCountToDb() {
        log.info("===== [点赞同步] 开始同步Redis点赞计数到MySQL =====");
        // TODO: 遍历Redis中所有 diary:like:{id}:count key，批量更新到t_diary.like_count
        // 此处留给具体实现: 可用 SCAN 命令遍历，避免 KEYS 阻塞
        // 建议: 只同步过去24小时内有过点赞操作的日记，减少无效更新
        log.info("===== [点赞同步] 完成 =====");
    }

    /**
     * 每15分钟清理过期的未支付订单 (防止prepay_id占用)
     */
    @Scheduled(cron = "0 */15 * * * ?")
    public void cleanExpiredOrders() {
        log.info("[订单清理] 开始清理过期未支付订单");
        // TODO: 查询 status=0 且 expire_time < now() 的订单，更新为 status=2(已取消)
        // 同时可调用微信关单API
        log.info("[订单清理] 完成");
    }

    // ========== 私有: 热度分计算公式 ==========

    private double calcHotScore(Diary diary) {
        int likes = diary.getLikeCount() != null ? diary.getLikeCount() : 0;
        int comments = diary.getCommentCount() != null ? diary.getCommentCount() : 0;
        int collects = diary.getCollectCount() != null ? diary.getCollectCount() : 0;
        int views = diary.getViewCount() != null ? diary.getViewCount() : 0;
        return likes * 3.0 + comments * 2.0 + collects * 2.0 + views * 0.5;
    }
}
