package com.fruitmall.common.enums;

import lombok.Getter;

/**
 * 系统用户状态，取值与 sys_user.status 一致。
 */
@Getter
public enum SysUserStatusEnum {

    /** 正常 */
    NORMAL(10, "正常"),
    /** 禁用 */
    DISABLED(20, "禁用");

    private final Integer code;
    private final String desc;

    SysUserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
