package com.fruitmall.common.enums;

import lombok.Getter;

/**
 * 操作日志结果，取值与 sys_oper_log.result_status 一致。
 */
@Getter
public enum OperLogResultEnum {

    /** 成功 */
    SUCCESS(10, "成功"),
    /** 失败 */
    FAIL(20, "失败");

    private final Integer code;
    private final String desc;

    OperLogResultEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
