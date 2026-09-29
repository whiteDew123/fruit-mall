package com.fruitmall.common.enums;

import lombok.Getter;

/** 商品状态，取值与 fm_product_spu.status 一致。 */
@Getter
public enum ProductStatusEnum {

    /** 草稿 */
    DRAFT(10, "草稿"),
    /** 上架 */
    ON_SALE(20, "上架"),
    /** 下架 */
    OFF_SALE(30, "下架");

    private final Integer code;
    private final String desc;

    ProductStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
