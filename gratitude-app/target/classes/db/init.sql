-- =============================================
-- 感恩日记小程序 数据库初始化脚本
-- 数据库: gratitude_journal
-- 字符集: utf8mb4 (支持emoji)
-- =============================================

CREATE DATABASE IF NOT EXISTS `gratitude_journal`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `gratitude_journal`;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_user` (
    `id`                BIGINT(20)   NOT NULL COMMENT '主键ID(雪花算法)',
    `openid`            VARCHAR(64)  DEFAULT NULL COMMENT '微信openid',
    `unionid`           VARCHAR(64)  DEFAULT NULL COMMENT '微信unionid',
    `phone`             VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `nickname`          VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`            VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `birthday`          DATE         DEFAULT NULL COMMENT '生日',
    `gender`            TINYINT(1)   DEFAULT 0   COMMENT '性别 0未知 1男 2女',
    `status`            TINYINT(1)   DEFAULT 0   COMMENT '状态 0正常 1封禁',
    `vip_expire_time`   DATETIME     DEFAULT NULL COMMENT 'VIP到期时间',
    `has_password_lock` TINYINT(1)   DEFAULT 0   COMMENT '是否设置密码锁 0否 1是',
    `password_lock`     VARCHAR(100) DEFAULT NULL COMMENT '密码锁(加密存储)',
    `reminder_enabled`  TINYINT(1)   DEFAULT 0   COMMENT '写日记提醒开关',
    `reminder_time`     VARCHAR(10)  DEFAULT NULL COMMENT '提醒时间 HH:mm',
    `source`            TINYINT(1)   DEFAULT 1   COMMENT '注册来源 1微信 2手机号',
    `total_earned_vip_days` INT      DEFAULT 0   COMMENT '累计获得VIP总天数',
    `create_time`       DATETIME     NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT(1)   NOT NULL    DEFAULT 0 COMMENT '逻辑删除 0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_vip_expire` (`vip_expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 感恩日记表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_diary` (
    `id`             BIGINT(20)    NOT NULL COMMENT '主键ID',
    `user_id`        BIGINT(20)    NOT NULL COMMENT '作者用户ID',
    `title`          VARCHAR(100)  DEFAULT NULL COMMENT '日记标题',
    `content`        TEXT          NOT NULL COMMENT '日记正文',
    `images`         JSON          DEFAULT NULL COMMENT '图片URL列表(JSON数组)',
    `mood`           TINYINT(1)    DEFAULT 1 COMMENT '情绪 1开心 2感动 3平静 4难过',
    `visible`        TINYINT(1)    DEFAULT 1 COMMENT '可见性 0私密 1公开广场',
    `status`         TINYINT(1)    DEFAULT 0 COMMENT '状态 0草稿 1审核中 2已发布 3已拒绝 4垃圾桶',
    `reject_reason`  VARCHAR(200)  DEFAULT NULL COMMENT '拒绝原因',
    `like_count`     INT           DEFAULT 0 COMMENT '点赞数',
    `comment_count`  INT           DEFAULT 0 COMMENT '评论数',
    `collect_count`  INT           DEFAULT 0 COMMENT '收藏数',
    `view_count`     INT           DEFAULT 0 COMMENT '查看次数',
    `share_count`    INT           DEFAULT 0 COMMENT '分享次数',
    `is_top`         TINYINT(1)    DEFAULT 0 COMMENT '是否置顶 0否 1是',
    `is_star`        TINYINT(1)    DEFAULT 0 COMMENT '是否星标 0否 1是',
    `wx_media_job_id` VARCHAR(100) DEFAULT NULL COMMENT '微信异步审核任务ID',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_visible` (`status`, `visible`),
    KEY `idx_like_count` (`like_count`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='感恩日记表';

-- ----------------------------
-- 日记评论表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_diary_comment` (
    `id`               BIGINT(20)   NOT NULL COMMENT '主键ID',
    `diary_id`         BIGINT(20)   NOT NULL COMMENT '所属日记ID',
    `user_id`          BIGINT(20)   NOT NULL COMMENT '评论者用户ID',
    `parent_id`        BIGINT(20)   DEFAULT 0 COMMENT '父评论ID(0=顶级)',
    `reply_comment_id` BIGINT(20)   DEFAULT NULL COMMENT '被回复评论ID',
    `reply_user_id`    BIGINT(20)   DEFAULT NULL COMMENT '被回复用户ID',
    `content`          VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count`       INT          DEFAULT 0 COMMENT '评论点赞数',
    `status`           TINYINT(1)   DEFAULT 0 COMMENT '状态 0正常 1违规删除',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_diary_id` (`diary_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记评论表';

-- ----------------------------
-- 日记收藏表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_diary_collect` (
    `id`          BIGINT(20) NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT(20) NOT NULL COMMENT '用户ID',
    `diary_id`    BIGINT(20) NOT NULL COMMENT '日记ID',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_diary` (`user_id`, `diary_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日记收藏表';

-- ----------------------------
-- 课程分类表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_course_category` (
    `id`          BIGINT(20)   NOT NULL COMMENT '主键ID',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `icon`        VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    `sort`        INT          DEFAULT 0 COMMENT '排序权重',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态 0下架 1上架',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';

-- ----------------------------
-- 录播课程表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_course` (
    `id`             BIGINT(20)   NOT NULL COMMENT '主键ID',
    `title`          VARCHAR(100) NOT NULL COMMENT '课程标题',
    `description`    TEXT         DEFAULT NULL COMMENT '课程简介',
    `cover_image`    VARCHAR(500) NOT NULL COMMENT '封面图URL',
    `teacher_name`   VARCHAR(50)  DEFAULT NULL COMMENT '讲师名称',
    `teacher_avatar` VARCHAR(500) DEFAULT NULL COMMENT '讲师头像URL',
    `type`           TINYINT(1)   DEFAULT 0 COMMENT '类型 0免费 1VIP免费 2单独付费',
    `price`          INT          DEFAULT 0 COMMENT '单买价格(分)',
    `preview_url`    VARCHAR(500) DEFAULT NULL COMMENT '试看URL',
    `video_url`      VARCHAR(500) NOT NULL COMMENT '正式视频URL(OSS Key)',
    `duration`       INT          DEFAULT 0 COMMENT '视频时长(秒)',
    `video_size`     BIGINT       DEFAULT 0 COMMENT '文件大小(字节)',
    `view_count`     INT          DEFAULT 0 COMMENT '观看次数',
    `category_id`    BIGINT(20)   DEFAULT NULL COMMENT '分类ID',
    `sort`           INT          DEFAULT 0 COMMENT '排序权重',
    `status`         TINYINT(1)   DEFAULT 1 COMMENT '状态 0下架 1上架',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录播课程表';

-- ----------------------------
-- 用户课程购买记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_user_course` (
    `id`          BIGINT(20) NOT NULL COMMENT '主键ID',
    `user_id`     BIGINT(20) NOT NULL COMMENT '用户ID',
    `course_id`   BIGINT(20) NOT NULL COMMENT '课程ID',
    `order_id`    BIGINT(20) DEFAULT NULL COMMENT '关联订单ID',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户课程购买记录表';

-- ----------------------------
-- 用户观看进度表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_watch_progress` (
    `id`               BIGINT(20) NOT NULL COMMENT '主键ID',
    `user_id`          BIGINT(20) NOT NULL COMMENT '用户ID',
    `course_id`        BIGINT(20) NOT NULL COMMENT '课程ID',
    `progress_seconds` INT        DEFAULT 0 COMMENT '播放进度(秒)',
    `is_finished`      TINYINT(1) DEFAULT 0 COMMENT '是否看完',
    `last_watch_time`  DATETIME   DEFAULT NULL COMMENT '最近观看时间',
    `create_time`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户课程观看进度表';

-- ----------------------------
-- VIP套餐表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_vip_package` (
    `id`             BIGINT(20)   NOT NULL COMMENT '主键ID',
    `name`           VARCHAR(50)  NOT NULL COMMENT '套餐名称',
    `description`    VARCHAR(200) DEFAULT NULL COMMENT '套餐描述',
    `original_price` INT          NOT NULL COMMENT '原价(分)',
    `sale_price`     INT          NOT NULL COMMENT '售价(分)',
    `vip_days`       INT          NOT NULL COMMENT '赠送VIP天数',
    `vip_type`       TINYINT(1)   DEFAULT 1 COMMENT 'VIP类型 1月度 2年度',
    `recommended`    TINYINT(1)   DEFAULT 0 COMMENT '是否推荐 0否 1是',
    `status`         TINYINT(1)   DEFAULT 1 COMMENT '状态 0下架 1上架',
    `sort`           INT          DEFAULT 0 COMMENT '排序',
    `tag`            VARCHAR(20)  DEFAULT NULL COMMENT '展示标签(如:最划算)',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP套餐表';

-- ----------------------------
-- VIP流水表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_vip_record` (
    `id`                 BIGINT(20)   NOT NULL COMMENT '主键ID',
    `user_id`            BIGINT(20)   NOT NULL COMMENT '用户ID',
    `days`               INT          NOT NULL COMMENT '变动天数(正=增加)',
    `source`             TINYINT(1)   NOT NULL COMMENT '来源 1购买 2注册 3任务 4管理员',
    `source_desc`        VARCHAR(100) DEFAULT NULL COMMENT '来源描述',
    `order_id`           BIGINT(20)   DEFAULT NULL COMMENT '关联订单ID',
    `vip_expire_after`   DATETIME     NOT NULL COMMENT 'VIP变动后的到期时间',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`            TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='VIP流水表';

-- ----------------------------
-- 支付订单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_order` (
    `id`               BIGINT(20)   NOT NULL COMMENT '主键ID',
    `order_no`         VARCHAR(32)  NOT NULL COMMENT '业务订单号',
    `user_id`          BIGINT(20)   NOT NULL COMMENT '用户ID',
    `goods_type`       TINYINT(1)   NOT NULL COMMENT '商品类型 1VIP套餐 2单课',
    `goods_id`         BIGINT(20)   NOT NULL COMMENT '商品ID',
    `goods_name`       VARCHAR(100) NOT NULL COMMENT '商品快照名称',
    `pay_amount`       INT          NOT NULL COMMENT '支付金额(分)',
    `status`           TINYINT(1)   DEFAULT 0 COMMENT '状态 0待支付 1已支付 2已取消 3已退款',
    `wx_prepay_id`     VARCHAR(64)  DEFAULT NULL COMMENT '微信prepay_id',
    `wx_transaction_id` VARCHAR(64) DEFAULT NULL COMMENT '微信支付交易号',
    `paid_time`        DATETIME     DEFAULT NULL COMMENT '支付完成时间',
    `expire_time`      DATETIME     DEFAULT NULL COMMENT '订单过期时间',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`          TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_wx_transaction_id` (`wx_transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- ----------------------------
-- 用户签到表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_user_checkin` (
    `id`              BIGINT(20) NOT NULL COMMENT '主键ID',
    `user_id`         BIGINT(20) NOT NULL COMMENT '用户ID',
    `check_date`      INT        NOT NULL COMMENT '签到日期 yyyyMMdd',
    `continuous_days` INT        DEFAULT 1 COMMENT '连续签到天数',
    `reward_vip_days` INT        DEFAULT 0 COMMENT '当日奖励VIP天数',
    `create_time`     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `check_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户签到表';

-- ----------------------------
-- 举报记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_report` (
    `id`            BIGINT(20)   NOT NULL COMMENT '主键ID',
    `reporter_id`   BIGINT(20)   NOT NULL COMMENT '举报者用户ID',
    `target_type`   TINYINT(1)   NOT NULL COMMENT '举报目标类型 1日记 2评论',
    `target_id`     BIGINT(20)   NOT NULL COMMENT '举报目标ID',
    `reason`        VARCHAR(200) NOT NULL COMMENT '举报原因',
    `status`        TINYINT(1)   DEFAULT 0 COMMENT '处理状态 0待处理 1已确认 2已忽略',
    `handle_note`   VARCHAR(200) DEFAULT NULL COMMENT '处理备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报记录表';

-- ----------------------------
-- 初始化VIP套餐数据
-- ----------------------------
INSERT INTO `t_vip_package` (`id`, `name`, `description`, `original_price`, `sale_price`, `vip_days`, `vip_type`, `recommended`, `status`, `sort`, `tag`)
VALUES
    (1000001, '月度会员', '享受全部VIP课程', 1999, 999, 30, 1, 0, 1, 1, NULL),
    (1000002, '季度会员', '享受全部VIP课程', 4999, 2499, 90, 1, 0, 1, 2, '限时优惠'),
    (1000003, '年度会员', '享受全部VIP课程', 14999, 9999, 365, 2, 1, 1, 3, '最划算');

-- ----------------------------
-- 管理后台系统账户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_sys_user` (
    `id`          BIGINT(20)   NOT NULL COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    `status`      TINYINT(1)   DEFAULT 0 COMMENT '状态 0正常 1禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

-- 初始化超级管理员 (密码为 admin123)
-- BCrypt 密文对应明文 'admin123'
INSERT IGNORE INTO `t_sys_user` (`id`, `username`, `password`, `real_name`, `status`)
VALUES
    (1, 'admin', '$2a$10$wTf3rW3hZk./8gP6sKqLx.21.J3M7jT8mEqe5Ew0WwB4Hl9HhGv3S', '超级管理员', 0);

-- ----------------------------
-- 常见问题表 (FAQ)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_faq` (
    `id`             BIGINT(20)   NOT NULL COMMENT '主键ID',
    `question`       VARCHAR(255) NOT NULL COMMENT '问题标题',
    `answer`         TEXT         NOT NULL COMMENT '问题解答',
    `sort`           INT          DEFAULT 0 COMMENT '排序权重(降序)',
    `status`         TINYINT(1)   DEFAULT 1 COMMENT '状态 0隐藏 1显示',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='常见问题表';

-- ----------------------------
-- 意见反馈表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `t_feedback` (
    `id`             BIGINT(20)   NOT NULL COMMENT '主键ID',
    `user_id`        BIGINT(20)   NOT NULL COMMENT '反馈用户ID',
    `content`        VARCHAR(1000) NOT NULL COMMENT '反馈内容',
    `images`         JSON         DEFAULT NULL COMMENT '附件图片',
    `contact`        VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    `status`         TINYINT(1)   DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已处理',
    `reply_note`     VARCHAR(500) DEFAULT NULL COMMENT '回复备注',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见反馈表';


