package com.fruitmall.common.enums;

import lombok.Getter;

/** 售后类型，取值与 fm_after_sale.type 一致。 */
@Getter
public enum AfterSaleTypeEnum {

    /** 仅退款：不涉及退货，审核通过后直接退款 */
    REFUND_ONLY(10, "仅退款"),
    /** 退货退款：需要会员寄回商品，商家收货后退款 */
    RETURN_REFUND(20, "退货退款");

    private final Integer code;
    private final String desc;

    AfterSaleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
