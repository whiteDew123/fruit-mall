package com.fruitmall.common.enums;

import lombok.Getter;

/** 操作者类型，用于订单状态流水、履约轨迹等留痕字段。 */
@Getter
public enum OperatorTypeEnum {

    /** 会员 */
    MEMBER(10, "会员"),
    /** 商家（后台用户） */
    MERCHANT(20, "商家"),
    /** 系统（定时任务、自动流程） */
    SYSTEM(30, "系统");

    private final Integer code;
    private final String desc;

    OperatorTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
