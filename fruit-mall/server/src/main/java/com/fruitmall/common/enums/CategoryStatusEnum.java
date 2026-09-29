package com.fruitmall.common.enums;

import lombok.Getter;

/** 分类状态，取值与 fm_category.status 一致。 */
@Getter
public enum CategoryStatusEnum {

    /** 启用 */
    ENABLED(10, "启用"),
    /** 停用 */
    DISABLED(20, "停用");

    private final Integer code;
    private final String desc;

    CategoryStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
