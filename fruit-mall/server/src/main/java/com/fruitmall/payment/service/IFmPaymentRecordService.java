package com.fruitmall.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.payment.domain.FmPaymentRecord;
import com.fruitmall.payment.dto.PaymentPayDTO;
import com.fruitmall.payment.vo.PaymentResultVO;
import com.fruitmall.payment.vo.PaymentVO;

/** 支付服务。 */
public interface IFmPaymentRecordService extends IService<FmPaymentRecord> {

    /**
     * 模拟支付。
     * 幂等：同一订单重复发起且已支付成功时，直接返回成功，不产生新流水、不重复核销库存。
     *
     * @param dto 订单ID + 模拟结果
     */
    PaymentResultVO pay(PaymentPayDTO dto);

    /** 查询本人在指定订单上的支付单 */
    PaymentVO getByOrderId(Long orderId);
}
