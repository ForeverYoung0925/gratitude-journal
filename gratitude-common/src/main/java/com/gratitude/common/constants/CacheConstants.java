package com.gratitude.common.constants;

/**
 * Redis Key 常量
 * 规范: 模块:业务:标识
 */
public interface CacheConstants {

    // ---- 微信鉴权 ----
    /** 微信授权码->openid 缓存 (5分钟) */
    String WX_CODE_PREFIX = "wx:code:";
    /** 手机号验证码 (5分钟) */
    String SMS_CODE_PREFIX = "sms:code:";
    /** 验证码发送计数 (24小时, 每日上限) */
    String SMS_SEND_COUNT_PREFIX = "sms:count:";

    // ---- 用户 ----
    /** 用户信息缓存 (30分钟) */
    String USER_INFO_PREFIX = "user:info:";
    /** 用户VIP状态缓存 (10分钟) */
    String USER_VIP_STATUS_PREFIX = "user:vip:";

    // ---- 日记 ----
    /** 日记点赞集合 */
    String DIARY_LIKE_PREFIX = "diary:like:";
    /** 日记评论计数 */
    String DIARY_COMMENT_COUNT_PREFIX = "diary:comment:count:";

    // ---- 任务/签到 ----
    /** 用户今日签到 key */
    String USER_CHECKIN_PREFIX = "task:checkin:";
    /** 用户任务完成状态 */
    String USER_TASK_STATE_PREFIX = "task:state:";

    // ---- 限流 ----
    /** 接口限流前缀 */
    String RATE_LIMIT_PREFIX = "limit:";

    // ---- 过期时间(秒) ----
    long EXPIRE_5_MIN = 5 * 60L;
    long EXPIRE_10_MIN = 10 * 60L;
    long EXPIRE_30_MIN = 30 * 60L;
    long EXPIRE_1_HOUR = 60 * 60L;
    long EXPIRE_1_DAY = 24 * 60 * 60L;
}
