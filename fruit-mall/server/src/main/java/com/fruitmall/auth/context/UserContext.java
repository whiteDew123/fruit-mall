package com.fruitmall.auth.context;

import com.fruitmall.common.enums.UserTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;

/**
 * 当前登录用户上下文，基于 ThreadLocal。
 * 请求结束时由 AuthInterceptor 的 afterCompletion 清理，避免线程复用导致数据串号。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    /** 获取当前登录用户，未登录返回 null */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前登录用户，未登录抛 401 */
    public static LoginUser getRequired() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    /** 获取当前登录用户ID，未登录返回 null */
    public static Long getUserId() {
        LoginUser loginUser = HOLDER.get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取当前登录会员ID。
     * 未登录抛 401；后台账号调用消费者端接口抛 403，
     * 这是数据归属校验的第一道闸门，防止越权访问他人购物车与订单。
     */
    public static Long getRequiredMemberId() {
        LoginUser loginUser = getRequired();
        if (UserTypeEnum.MEMBER != loginUser.getUserType()) {
            throw new BizException(ResultCode.FORBIDDEN, "该接口仅限会员使用");
        }
        return loginUser.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
