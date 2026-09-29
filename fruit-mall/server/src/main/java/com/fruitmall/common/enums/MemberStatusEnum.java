package com.fruitmall.common.enums;

import lombok.Getter;

/** 会员状态，取值与 fm_member.status 一致。 */
@Getter
public enum MemberStatusEnum {

    /** 正常 */
    NORMAL(10, "正常"),
    /** 禁用 */
    DISABLED(20, "禁用");

    private final Integer code;
    private final String desc;

    MemberStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
