package com.fruitmall.common.enums;

import lombok.Getter;

/** 评价状态，取值与 fm_review.status 一致。 */
@Getter
public enum ReviewStatusEnum {

    /** 显示 */
    VISIBLE(10, "显示"),
    /** 隐藏：内容违规或商家下架展示 */
    HIDDEN(20, "隐藏");

    private final Integer code;
    private final String desc;

    ReviewStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
