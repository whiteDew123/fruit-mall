package com.fruitmall.common.enums;

import lombok.Getter;

/** 商品特色属性的数据类型，取值与 fm_product_attr_def.data_type 一致。 */
@Getter
public enum AttrDataTypeEnum {

    /** 数值型，如 甜度、糖度 */
    NUMBER(10, "数值"),
    /** 枚举型，如 品级、认证 */
    ENUM(20, "枚举"),
    /** 文本型，如 产地描述 */
    TEXT(30, "文本"),
    /** 布尔型，如 是否礼盒装 */
    BOOLEAN(40, "布尔");

    private final Integer code;
    private final String desc;

    AttrDataTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
