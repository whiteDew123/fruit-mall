package com.fruitmall.common.enums;

import lombok.Getter;

/** 库存流水类型，取值与 fm_inventory_transaction.type 一致。 */
@Getter
public enum InventoryTxnTypeEnum {

    /** 入库 */
    INBOUND(10, "入库"),
    /** 下单预占：可售库存减少、预占库存增加 */
    LOCK(20, "预占"),
    /** 取消释放：可售库存恢复、预占库存减少 */
    RELEASE(30, "释放"),
    /** 支付实扣：预占库存核销 */
    DEDUCT(40, "实扣"),
    /** 退货回补：回到退货暂存批次 */
    RETURN_IN(50, "退货回补"),
    /** 报损 */
    LOSS(60, "报损"),
    /** 盘点调整 */
    ADJUST(70, "盘点调整");

    private final Integer code;
    private final String desc;

    InventoryTxnTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
