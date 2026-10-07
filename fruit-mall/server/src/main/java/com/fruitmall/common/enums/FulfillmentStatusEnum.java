package com.fruitmall.common.enums;

import lombok.Getter;

/** 履约状态，取值与 fm_fulfillment_order.status 一致。迁移规则见 docs/05-数据库设计.md 第 3.7 节。 */
@Getter
public enum FulfillmentStatusEnum {

    /** 待分拣 */
    PENDING_PICK(10, "待分拣"),
    /** 分拣完成 */
    PICKED(20, "分拣完成"),
    /** 待配送 */
    PENDING_DELIVERY(30, "待配送"),
    /** 配送中 */
    DELIVERING(40, "配送中"),
    /** 已送达 */
    ARRIVED(50, "已送达"),
    /** 已签收 */
    SIGNED(60, "已签收"),
    /** 异常：破损/缺货/拒收/改期 */
    EXCEPTION(90, "异常");

    private final Integer code;
    private final String desc;

    FulfillmentStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按编码匹配，未匹配返回 null */
    public static FulfillmentStatusEnum of(Integer code) {
        for (FulfillmentStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
