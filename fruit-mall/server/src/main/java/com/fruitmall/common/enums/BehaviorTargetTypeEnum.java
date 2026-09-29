package com.fruitmall.common.enums;

import lombok.Getter;

/** 行为埋点的目标类型，取值与 fm_user_behavior.target_type 一致。 */
@Getter
public enum BehaviorTargetTypeEnum {

    /** 商品SPU */
    SPU("SPU", "商品"),
    /** 商品SKU */
    SKU("SKU", "规格"),
    /** 分类 */
    CATEGORY("CATEGORY", "分类"),
    /** 搜索关键词 */
    KEYWORD("KEYWORD", "关键词");

    private final String code;
    private final String desc;

    BehaviorTargetTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
