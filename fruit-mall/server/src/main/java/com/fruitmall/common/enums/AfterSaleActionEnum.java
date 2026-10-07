package com.fruitmall.common.enums;

import lombok.Getter;

/** 售后流转动作，用于状态机内部识别触发来源。 */
@Getter
public enum AfterSaleActionEnum {

    /** 会员申请 */
    APPLY("APPLY", "申请售后"),
    /** 审核通过 */
    AUDIT_PASS("AUDIT_PASS", "审核通过"),
    /** 审核驳回 */
    AUDIT_REJECT("AUDIT_REJECT", "审核驳回"),
    /** 商家收到退货 */
    RECEIVE("RECEIVE", "收到退货"),
    /** 发起退款 */
    REFUND("REFUND", "发起退款"),
    /** 退款完成 */
    REFUND_DONE("REFUND_DONE", "退款完成"),
    /** 会员撤销 */
    CANCEL("CANCEL", "撤销申请");

    private final String code;
    private final String desc;

    AfterSaleActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
