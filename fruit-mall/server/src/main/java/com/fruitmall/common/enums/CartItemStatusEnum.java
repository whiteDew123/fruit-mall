package com.fruitmall.common.enums;

import lombok.Getter;

/** 购物车项状态，取值与 fm_cart_item.status 一致。 */
@Getter
public enum CartItemStatusEnum {

    /** 有效 */
    NORMAL(10, "有效"),
    /** 失效：商品已下架或规格已停用 */
    INVALID(20, "失效");

    private final Integer code;
    private final String desc;

    CartItemStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
