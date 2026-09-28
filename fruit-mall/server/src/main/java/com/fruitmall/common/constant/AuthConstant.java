package com.fruitmall.common.constant;

/**
 * 认证相关常量。
 */
public final class AuthConstant {

    /** 携带令牌的请求头 */
    public static final String TOKEN_HEADER = "Authorization";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT 中的登录名 */
    public static final String CLAIM_USERNAME = "username";

    /** JWT 中的用户类型 */
    public static final String CLAIM_USER_TYPE = "userType";

    private AuthConstant() {
    }
}
