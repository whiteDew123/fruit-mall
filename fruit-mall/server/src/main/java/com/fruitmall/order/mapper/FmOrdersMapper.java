package com.fruitmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.order.domain.FmOrders;
import org.apache.ibatis.annotations.Mapper;

/** 订单 Mapper。 */
@Mapper
public interface FmOrdersMapper extends BaseMapper<FmOrders> {
}
