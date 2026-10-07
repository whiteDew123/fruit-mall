package com.fruitmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.order.domain.FmOrderItem;
import org.apache.ibatis.annotations.Mapper;

/** 订单项 Mapper。 */
@Mapper
public interface FmOrderItemMapper extends BaseMapper<FmOrderItem> {
}
