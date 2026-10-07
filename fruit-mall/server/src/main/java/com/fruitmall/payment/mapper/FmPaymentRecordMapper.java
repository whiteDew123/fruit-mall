package com.fruitmall.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.payment.domain.FmPaymentRecord;
import org.apache.ibatis.annotations.Mapper;

/** 支付记录 Mapper。 */
@Mapper
public interface FmPaymentRecordMapper extends BaseMapper<FmPaymentRecord> {
}
