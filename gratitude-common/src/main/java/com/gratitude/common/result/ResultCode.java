package com.gratitude.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 认证相关
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 参数相关
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),

    // 用户相关
    USER_NOT_EXIST(10001, "用户不存在"),
    USER_PHONE_EXIST(10002, "手机号已被注册"),
    USER_LOCKED(10003, "账号已被封禁"),
    USER_PASSWORD_ERROR(10004, "密码锁验证失败"),

    // 订单/VIP相关
    ORDER_NOT_EXIST(20001, "订单不存在"),
    ORDER_STATUS_ERROR(20002, "订单状态异常"),
    VIP_ALREADY_ACTIVE(20003, "VIP会员已激活"),

    // 日记相关
    DIARY_NOT_EXIST(30001, "日记不存在"),
    DIARY_AUDIT_REJECT(30002, "日记审核不通过，内容包含违规信息"),
    DIARY_NO_PERMISSION(30003, "无权操作该日记"),

    // 课程相关
    COURSE_NOT_EXIST(40001, "课程不存在"),
    COURSE_NEED_VIP(40002, "该内容需要VIP会员方可观看"),
    COURSE_NEED_BUY(40003, "请购买该课程后观看"),

    // 频率限制
    TOO_MANY_REQUEST(42900, "操作过于频繁，请稍后再试"),

    // 微信相关
    WX_AUTH_FAIL(50001, "微信授权失败"),
    WX_PHONE_DECRYPT_FAIL(50002, "手机号解密失败"),
    WX_PAY_FAIL(50003, "微信支付发起失败"),

    // OSS相关
    OSS_UPLOAD_FAIL(60001, "文件上传失败"),
    OSS_SIGN_FAIL(60002, "获取上传凭证失败");

    private final Integer code;
    private final String msg;
}
