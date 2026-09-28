package com.fruitmall.common.util;

/**
 * 敏感信息脱敏工具。
 * 手机号、收货地址在列表页脱敏展示，只有下单与履约等必要场景才展示完整信息。
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /**
     * 手机号脱敏：保留前 3 位与后 4 位，如 138****8888
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
