package com.fruitmall.common.enums;

import lombok.Getter;

/** 退款状态，取值与 fm_refund_record.status 一致。 */
@Getter
public enum RefundStatusEnum {

    /** 退款中 */
    REFUNDING(10, "退款中"),
    /** 退款成功 */
    SUCCESS(20, "退款成功"),
    /** 退款失败 */
    FAILED(30, "退款失败");

    private final Integer code;
    private final String desc;

    RefundStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
