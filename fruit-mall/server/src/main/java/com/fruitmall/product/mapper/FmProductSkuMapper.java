package com.fruitmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.product.domain.FmProductSku;
import org.apache.ibatis.annotations.Mapper;

/** 商品 SKU Mapper。 */
@Mapper
public interface FmProductSkuMapper extends BaseMapper<FmProductSku> {
}
