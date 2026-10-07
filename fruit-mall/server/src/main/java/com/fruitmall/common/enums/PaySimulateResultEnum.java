package com.fruitmall.common.enums;

import lombok.Getter;

/**
 * 模拟支付的结果选项。
 * 本课题不接真实支付渠道，由前端在支付页选择"支付成功/支付失败/超时"，
 * 后端据此走不同的状态分支，用于演示支付闭环与异常分支。
 */
@Getter
public enum PaySimulateResultEnum {

    /** 支付成功：订单转为已支付，预占库存转实扣 */
    SUCCESS("SUCCESS", "支付成功"),
    /** 支付失败：订单保持待支付，可重新发起支付 */
    FAIL("FAIL", "支付失败"),
    /** 支付超时：关闭支付单并关单释放库存 */
    TIMEOUT("TIMEOUT", "支付超时");

    private final String code;
    private final String desc;

    PaySimulateResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按编码匹配，未匹配返回 null */
    public static PaySimulateResultEnum of(String code) {
        for (PaySimulateResultEnum result : values()) {
            if (result.code.equals(code)) {
                return result;
            }
        }
        return null;
    }
}
