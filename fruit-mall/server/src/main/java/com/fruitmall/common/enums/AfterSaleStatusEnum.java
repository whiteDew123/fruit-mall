package com.fruitmall.common.enums;

import lombok.Getter;

/** 售后状态，取值与 fm_after_sale.status 一致。迁移规则见 docs/05-数据库设计.md 第 3.7 节。 */
@Getter
public enum AfterSaleStatusEnum {

    /** 待审核 */
    PENDING_AUDIT(10, "待审核"),
    /** 已同意 */
    APPROVED(20, "已同意"),
    /** 已驳回 */
    REJECTED(30, "已驳回"),
    /** 退货中：等待会员寄回 */
    RETURNING(40, "退货中"),
    /** 退款中 */
    REFUNDING(50, "退款中"),
    /** 已完成 */
    COMPLETED(60, "已完成"),
    /** 已取消 */
    CANCELLED(70, "已取消");

    private final Integer code;
    private final String desc;

    AfterSaleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按编码匹配，未匹配返回 null */
    public static AfterSaleStatusEnum of(Integer code) {
        for (AfterSaleStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
