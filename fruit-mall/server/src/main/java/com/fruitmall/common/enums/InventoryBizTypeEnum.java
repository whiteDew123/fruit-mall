package com.fruitmall.common.enums;

import lombok.Getter;

/** 库存流水的业务来源，取值与 fm_inventory_transaction.biz_type 一致。 */
@Getter
public enum InventoryBizTypeEnum {

    /** 订单 */
    ORDER(10, "订单"),
    /** 售后 */
    AFTER_SALE(20, "售后"),
    /** 手工调整 */
    MANUAL(30, "手工调整");

    private final Integer code;
    private final String desc;

    InventoryBizTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
