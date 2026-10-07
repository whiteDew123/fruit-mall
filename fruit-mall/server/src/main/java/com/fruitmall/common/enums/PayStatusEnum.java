package com.fruitmall.common.enums;

import lombok.Getter;

/** 支付状态，取值与 fm_payment_record.status 一致。 */
@Getter
public enum PayStatusEnum {

    /** 已创建 */
    INIT(10, "已创建"),
    /** 支付中 */
    PAYING(20, "支付中"),
    /** 支付成功 */
    SUCCESS(30, "支付成功"),
    /** 支付失败 */
    FAILED(40, "支付失败"),
    /** 已关闭：订单取消或超时关单 */
    CLOSED(50, "已关闭"),
    /** 退款中 */
    REFUNDING(60, "退款中"),
    /** 已退款 */
    REFUNDED(70, "已退款");

    private final Integer code;
    private final String desc;

    PayStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
