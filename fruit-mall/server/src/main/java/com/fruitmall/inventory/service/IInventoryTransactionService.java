package com.fruitmall.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.inventory.domain.FmInventoryTransaction;

/** 库存流水服务。所有库存变动都必须调用本服务写一条流水。 */
public interface IInventoryTransactionService extends IService<FmInventoryTransaction> {

    /**
     * 记录一条库存流水
     *
     * @param skuId        规格ID
     * @param typeEnum     流水类型
     * @param quantity     变动数量（有符号：入库为正、出库为负）
     * @param beforeStock  变动前可售库存
     * @param afterStock   变动后可售库存
     * @param bizTypeEnum  业务类型
     * @param bizNo        业务单号（订单号/售后单号）
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param remark       备注
     */
    void record(Long skuId, InventoryTxnTypeEnum typeEnum, Integer quantity,
                Integer beforeStock, Integer afterStock,
                InventoryBizTypeEnum bizTypeEnum, String bizNo,
                Long operatorId, String operatorName, String remark);
}
