package com.fruitmall.inventory.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水：库存一致性的证据。
 * 每次预占、释放、实扣、入库都会写一条，可据此对账"库存流水累加 = 当前库存"。
 */
@Data
@TableName("fm_inventory_transaction")
public class FmInventoryTransaction {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID，SKU 级操作时为空 */
    private Long batchId;

    /** 规格ID */
    private Long skuId;

    /** 类型，见 InventoryTxnTypeEnum */
    private Integer type;

    /** 变动数量，入库为正、出库为负 */
    private Integer quantity;

    /** 变动前可售库存 */
    private Integer beforeQuantity;

    /** 变动后可售库存 */
    private Integer afterQuantity;

    /** 业务类型，见 InventoryBizTypeEnum */
    private Integer bizType;

    /** 业务单号：订单号/售后单号 */
    private String bizNo;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 备注 */
    private String remark;

    /** 发生时间 */
    private LocalDateTime createTime;
}
