package com.fruitmall.common.enums;

import lombok.Getter;

/**
 * 登录用户类型，取值与 sys_login_log.user_type 一致。
 */
@Getter
public enum UserTypeEnum {

    /** 系统用户：后台管理员、运营、履约人员 */
    ADMIN(10, "系统用户"),
    /** 会员：消费者端用户 */
    MEMBER(20, "会员");

    private final Integer code;
    private final String desc;

    UserTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
