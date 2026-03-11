package com.gratitude.app.util;

import cn.dev33.satoken.stp.StpLogic;

/**
 * Sa-Token 多账号隔离：管理后台的 StpLogic
 * 用于区分小程序前台用户(user)和后台管理员(admin)的登录状态
 */
public class StpAdminUtil {

    /**
     * 账号类型标识
     */
    public static final String TYPE = "admin";

    /**
     * 底层的 StpLogic 对象
     */
    public static StpLogic stpLogic = new StpLogic(TYPE);

    public static void login(Object id) {
        stpLogic.login(id);
    }

    public static void logout() {
        stpLogic.logout();
    }

    public static boolean isLogin() {
        return stpLogic.isLogin();
    }

    public static void checkLogin() {
        stpLogic.checkLogin();
    }

    public static long getLoginIdAsLong() {
        return stpLogic.getLoginIdAsLong();
    }

    public static String getTokenValue() {
        return stpLogic.getTokenValue();
    }
}
