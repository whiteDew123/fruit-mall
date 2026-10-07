package com.fruitmall.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务单号生成工具。
 * 格式：前缀 + yyyyMMddHHmmss + 6 位随机数字，如 FM20261007203015123456。
 * 单号只是业务标识，真正保证唯一性的是数据库唯一索引，重复时插入会失败并回滚。
 */
public final class BizNoUtil {

    /** 订单号前缀 */
    public static final String ORDER_PREFIX = "FM";

    /** 支付单号前缀 */
    public static final String PAYMENT_PREFIX = "PY";

    /** 售后单号前缀 */
    public static final String AFTER_SALE_PREFIX = "AS";

    /** 退款单号前缀 */
    public static final String REFUND_PREFIX = "RF";

    /** 履约单号前缀 */
    public static final String FULFILLMENT_PREFIX = "FF";

    /** 批次号前缀 */
    public static final String BATCH_PREFIX = "BT";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private BizNoUtil() {
    }

    /**
     * 生成业务单号
     *
     * @param prefix 业务前缀，如 FM
     */
    public static String generate(String prefix) {
        String time = LocalDateTime.now().format(TIME_FORMATTER);
        String random = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        return prefix + time + random;
    }
}
