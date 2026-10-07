package com.fruitmall.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.inventory.domain.FmInventoryTransaction;
import org.apache.ibatis.annotations.Mapper;

/** 库存流水 Mapper。 */
@Mapper
public interface FmInventoryTransactionMapper extends BaseMapper<FmInventoryTransaction> {
}
