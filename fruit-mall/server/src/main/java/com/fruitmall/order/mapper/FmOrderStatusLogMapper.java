package com.fruitmall.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.order.domain.FmOrderStatusLog;
import org.apache.ibatis.annotations.Mapper;

/** 订单状态流水 Mapper。 */
@Mapper
public interface FmOrderStatusLogMapper extends BaseMapper<FmOrderStatusLog> {
}
