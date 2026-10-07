package com.fruitmall.inventory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.inventory.domain.FmInventoryTransaction;
import com.fruitmall.inventory.mapper.FmInventoryTransactionMapper;
import com.fruitmall.inventory.service.IInventoryTransactionService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 库存流水服务实现。 */
@Service
public class InventoryTransactionServiceImpl
        extends ServiceImpl<FmInventoryTransactionMapper, FmInventoryTransaction>
        implements IInventoryTransactionService {

    @Override
    public void record(Long skuId, InventoryTxnTypeEnum typeEnum, Integer quantity,
                       Integer beforeStock, Integer afterStock,
                       InventoryBizTypeEnum bizTypeEnum, String bizNo,
                       Long operatorId, String operatorName, String remark) {
        FmInventoryTransaction txn = new FmInventoryTransaction();
        txn.setSkuId(skuId);
        txn.setType(typeEnum.getCode());
        txn.setQuantity(quantity);
        txn.setBeforeQuantity(beforeStock);
        txn.setAfterQuantity(afterStock);
        txn.setBizType(bizTypeEnum.getCode());
        txn.setBizNo(bizNo);
        txn.setOperatorId(operatorId);
        txn.setOperatorName(operatorName);
        txn.setRemark(remark);
        txn.setCreateTime(LocalDateTime.now());
        this.save(txn);
    }
}
