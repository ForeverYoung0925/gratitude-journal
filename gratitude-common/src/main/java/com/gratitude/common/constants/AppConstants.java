package com.gratitude.common.constants;

/**
 * 业务常量
 */
public interface AppConstants {

    // ---- 用户状态 ----
    Integer USER_STATUS_NORMAL = 0; // 正常
    Integer USER_STATUS_LOCKED = 1; // 封禁

    // ---- 性别 ----
    Integer GENDER_UNKNOWN = 0;
    Integer GENDER_MALE = 1;
    Integer GENDER_FEMALE = 2;

    // ---- VIP类型 ----
    Integer VIP_TYPE_MONTH = 1; // 月度VIP
    Integer VIP_TYPE_YEAR = 2; // 年度VIP

    // ---- VIP来源 ----
    Integer VIP_SOURCE_PAY = 1; // 购买
    Integer VIP_SOURCE_REGISTER = 2; // 注册赠送
    Integer VIP_SOURCE_TASK = 3; // 任务奖励
    Integer VIP_SOURCE_ADMIN = 4; // 管理员手动发放

    // ---- 日记状态 ----
    Integer DIARY_STATUS_DRAFT = 0; // 草稿
    Integer DIARY_STATUS_AUDITING = 1; // 审核中
    Integer DIARY_STATUS_PUBLISHED = 2; // 已发布
    Integer DIARY_STATUS_REJECTED = 3; // 审核拒绝
    Integer DIARY_STATUS_DELETED = 4; // 垃圾桶

    // ---- 日记可见性 ----
    Integer DIARY_VISIBLE_PRIVATE = 0; // 仅自己可见
    Integer DIARY_VISIBLE_PUBLIC = 1; // 公开广场

    // ---- 课程 ----
    Integer COURSE_TYPE_FREE = 0; // 免费
    Integer COURSE_TYPE_VIP = 1; // VIP免费
    Integer COURSE_TYPE_PAID = 2; // 单独付费

    // ---- 任务类型 ----
    Integer TASK_TYPE_DAILY_CHECKIN = 1; // 每日签到
    Integer TASK_TYPE_PUBLISH_DIARY = 2; // 发布日记
    Integer TASK_TYPE_RECEIVE_LIKE = 3; // 被他人点赞
    Integer TASK_TYPE_COMMENT = 4; // 评论他人日记
    Integer TASK_TYPE_HOT_DIARY = 5; // 日记入选热榜

    // ---- 奖励类型 ----
    Integer REWARD_TYPE_VIP_DAYS = 1; // 赠送VIP天数
    Integer REWARD_TYPE_COURSE_UNLOCK = 2; // 解锁课程

    // ---- 支付 ----
    Integer ORDER_STATUS_UNPAID = 0; // 待支付
    Integer ORDER_STATUS_PAID = 1; // 已支付
    // 商品类型
    Integer ORDER_TYPE_VIP = 1; // VIP套餐
    Integer ORDER_TYPE_COURSE = 2; // 单课购买
    Integer ORDER_STATUS_CANCELLED = 2; // 已取消
    Integer ORDER_STATUS_REFUNDED = 3; // 已退款

    // ---- 举报状态 ----
    Integer REPORT_STATUS_PENDING = 0; // 待处理
    Integer REPORT_STATUS_CONFIRMED = 1; // 已确认
    Integer REPORT_STATUS_IGNORED = 2; // 忽略

    // ---- 验证码每日发送上限 ----
    int SMS_DAILY_LIMIT = 5;

    // ---- 新用户VIP赠送天数 ----
    int NEW_USER_VIP_DAYS = 7;

    // ---- 日记图片上限 ----
    int DIARY_IMAGE_MAX_COUNT = 9;
}
