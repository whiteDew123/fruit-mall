package com.fruitmall.payment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.enums.PaySimulateResultEnum;
import com.fruitmall.common.enums.PayStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.BizNoUtil;
import com.fruitmall.inventory.service.IInventoryTransactionService;
import com.fruitmall.order.domain.FmOrderItem;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrderItemMapper;
import com.fruitmall.order.mapper.FmOrdersMapper;
import com.fruitmall.order.service.IOrderCancelService;
import com.fruitmall.order.state.OrderStateMachine;
import com.fruitmall.payment.domain.FmPaymentRecord;
import com.fruitmall.payment.dto.PaymentPayDTO;
import com.fruitmall.payment.mapper.FmPaymentRecordMapper;
import com.fruitmall.payment.service.IFmPaymentRecordService;
import com.fruitmall.payment.state.PaymentStateMachine;
import com.fruitmall.payment.vo.PaymentResultVO;
import com.fruitmall.payment.vo.PaymentVO;
import com.fruitmall.product.domain.FmProductSku;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付服务实现（模拟支付）。
 *
 * 三条一致性保障：
 * 1）一个订单只有一条支付记录（uk_order_id），支付失败后复用同一条记录，
 *    pay_no 唯一索引从数据库层保证回调幂等；
 * 2）支付金额必须与订单应付金额一致，不一致直接阻断；
 * 3）订单状态流转必须经过 OrderStateMachine，只有待支付订单可以支付。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmPaymentRecordServiceImpl extends ServiceImpl<FmPaymentRecordMapper, FmPaymentRecord>
        implements IFmPaymentRecordService {

    /** 本课题不接真实支付渠道 */
    private static final String CHANNEL_MOCK = "MOCK";

    private final FmOrdersMapper ordersMapper;
    private final FmOrderItemMapper orderItemMapper;
    private final FmProductSkuMapper skuMapper;
    private final IInventoryTransactionService inventoryTransactionService;
    private final PaymentStateMachine paymentStateMachine;
    private final OrderStateMachine orderStateMachine;
    private final IOrderCancelService orderCancelService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PaymentResultVO pay(PaymentPayDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();
        LoginUser loginUser = UserContext.getRequired();
        PaySimulateResultEnum simulateResult = PaySimulateResultEnum.of(dto.getResult());
        if (simulateResult == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "不支持的支付结果：" + dto.getResult());
        }

        FmOrders order = ordersMapper.selectById(dto.getOrderId());
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }

        FmPaymentRecord record = getByOrderIdInternal(order.getId());

        // 幂等分支：订单已支付成功时，重复请求直接返回成功，不再核销库存、不产生新流水
        if (!OrderStatusEnum.PENDING_PAY.getCode().equals(order.getStatus())) {
            if (OrderStatusEnum.PAID.getCode().equals(order.getStatus())
                    && record != null && PayStatusEnum.SUCCESS.getCode().equals(record.getStatus())) {
                return buildResult(true, "该订单已完成支付，无需重复支付", order, order.getStatus(),
                        record.getStatus());
            }
            throw new BizException(ResultCode.CONFLICT,
                    "订单当前状态不允许支付：" + statusDesc(order.getStatus()));
        }

        // 已超时的订单：先关单再返回结果。
        // 注意这里不能"关单后抛异常"，否则事务回滚会把关单动作一起撤销。
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            orderCancelService.close(order, OrderActionEnum.TIMEOUT, OperatorTypeEnum.SYSTEM,
                    null, "系统", "支付超时，系统自动关单");
            return buildResult(false, "订单已超时，系统已自动关闭并释放库存", order,
                    OrderStatusEnum.CANCELLED.getCode(), PayStatusEnum.CLOSED.getCode());
        }

        if (record == null) {
            record = createPaymentRecord(order);
        } else if (PayStatusEnum.SUCCESS.getCode().equals(record.getStatus())) {
            return buildResult(true, "该订单已完成支付，无需重复支付", order,
                    order.getStatus(), record.getStatus());
        } else if (PayStatusEnum.CLOSED.getCode().equals(record.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "支付单已关闭，请重新下单");
        }

        // 金额一致性校验：支付金额必须与订单应付金额完全一致
        if (record.getAmount() == null || order.getPayAmount() == null
                || record.getAmount().compareTo(order.getPayAmount()) != 0) {
            log.error("支付金额与订单金额不一致：orderNo={}, 支付单={}, 订单={}",
                    order.getOrderNo(), record.getAmount(), order.getPayAmount());
            throw new BizException(ResultCode.CONFLICT, "支付金额与订单金额不一致，已阻断支付");
        }

        LocalDateTime now = LocalDateTime.now();
        return switch (simulateResult) {
            case SUCCESS -> handleSuccess(order, record, memberId, loginUser.getUsername(), now);
            case FAIL -> handleFail(order, record, now);
            case TIMEOUT -> handleTimeout(order, record);
        };
    }

    @Override
    public PaymentVO getByOrderId(Long orderId) {
        Long memberId = UserContext.getRequiredMemberId();
        FmOrders order = ordersMapper.selectById(orderId);
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return toVO(getByOrderIdInternal(orderId), orderId, order.getPayAmount());
    }

    /** 支付成功：支付单转成功 → 订单转已支付 → 预占转实扣并写流水 */
    private PaymentResultVO handleSuccess(FmOrders order, FmPaymentRecord record,
                                          Long memberId, String username, LocalDateTime now) {
        FmPaymentRecord payUpdate = new FmPaymentRecord();
        payUpdate.setId(record.getId());
        payUpdate.setPayTime(now);
        payUpdate.setCallbackTime(now);
        paymentStateMachine.transition(payUpdate, PayStatusEnum.SUCCESS);

        FmOrders orderUpdate = new FmOrders();
        orderUpdate.setId(order.getId());
        orderUpdate.setPayTime(now);
        orderStateMachine.transition(orderUpdate, OrderStatusEnum.PAID, OrderActionEnum.PAY,
                OperatorTypeEnum.MEMBER, memberId, username, "模拟支付成功");

        List<FmOrderItem> items = orderItemMapper.selectList(Wrappers.<FmOrderItem>lambdaQuery()
                .eq(FmOrderItem::getOrderId, order.getId()));
        for (FmOrderItem item : items) {
            // 核销预占：可售库存不变，只把 locked_stock 减掉，同时累加销量
            int affected = skuMapper.confirmDeduct(item.getSkuId(), item.getQuantity());
            if (affected == 0) {
                log.error("核销预占库存失败，可能存在数据不一致：orderNo={}, skuId={}, quantity={}",
                        order.getOrderNo(), item.getSkuId(), item.getQuantity());
                throw new BizException(ResultCode.CONFLICT, "库存核销失败，请联系客服");
            }
            FmProductSku sku = skuMapper.selectById(item.getSkuId());
            int stock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
            inventoryTransactionService.record(item.getSkuId(), InventoryTxnTypeEnum.DEDUCT, 0,
                    stock, stock, InventoryBizTypeEnum.ORDER, order.getOrderNo(),
                    memberId, username, "支付成功，核销预占（可售库存不变，预占库存减少）");
        }

        log.info("支付成功：orderNo={}, payNo={}, amount={}",
                order.getOrderNo(), record.getPayNo(), order.getPayAmount());
        return buildResult(true, "支付成功", order, OrderStatusEnum.PAID.getCode(),
                PayStatusEnum.SUCCESS.getCode());
    }

    /** 支付失败：订单保持待支付，可重新发起支付 */
    private PaymentResultVO handleFail(FmOrders order, FmPaymentRecord record, LocalDateTime now) {
        FmPaymentRecord payUpdate = new FmPaymentRecord();
        payUpdate.setId(record.getId());
        payUpdate.setPayTime(now);
        payUpdate.setFailReason("模拟支付失败");
        paymentStateMachine.transition(payUpdate, PayStatusEnum.FAILED);

        log.info("支付失败：orderNo={}, payNo={}", order.getOrderNo(), record.getPayNo());
        return buildResult(false, "支付失败，可重新发起支付", order,
                order.getStatus(), PayStatusEnum.FAILED.getCode());
    }

    /** 模拟超时：关闭支付单并关单释放库存 */
    private PaymentResultVO handleTimeout(FmOrders order, FmPaymentRecord record) {
        // 关单服务会顺带把未完成的支付单一起关闭
        orderCancelService.close(order, OrderActionEnum.TIMEOUT, OperatorTypeEnum.SYSTEM,
                null, "系统", "模拟支付超时，系统自动关单");
        log.info("模拟支付超时关单：orderNo={}, payNo={}", order.getOrderNo(), record.getPayNo());
        return buildResult(false, "支付超时，订单已关闭并释放库存", order,
                OrderStatusEnum.CANCELLED.getCode(), PayStatusEnum.CLOSED.getCode());
    }

    /**
     * 创建支付单。并发下若另一个请求已创建，则复用已存在的记录（uk_order_id 兜底）。
     */
    private FmPaymentRecord createPaymentRecord(FmOrders order) {
        FmPaymentRecord record = new FmPaymentRecord();
        record.setPayNo(BizNoUtil.generate(BizNoUtil.PAYMENT_PREFIX));
        record.setOrderId(order.getId());
        record.setOrderNo(order.getOrderNo());
        record.setMemberId(order.getMemberId());
        record.setChannel(CHANNEL_MOCK);
        record.setAmount(order.getPayAmount());
        record.setStatus(PayStatusEnum.INIT.getCode());
        record.setPayTime(LocalDateTime.now());
        record.setVersion(0);
        try {
            baseMapper.insert(record);
            return record;
        } catch (DuplicateKeyException e) {
            FmPaymentRecord exists = getByOrderIdInternal(order.getId());
            if (exists == null) {
                throw new BizException(ResultCode.CONFLICT, "支付单创建失败，请重试");
            }
            return exists;
        }
    }

    private FmPaymentRecord getByOrderIdInternal(Long orderId) {
        return baseMapper.selectOne(Wrappers.<FmPaymentRecord>lambdaQuery()
                .eq(FmPaymentRecord::getOrderId, orderId));
    }

    private PaymentResultVO buildResult(boolean success, String message, FmOrders order,
                                        Integer orderStatus, Integer payStatus) {
        return PaymentResultVO.builder()
                .success(success)
                .message(message)
                .orderNo(order.getOrderNo())
                .payAmount(order.getPayAmount())
                .orderStatus(orderStatus)
                .orderStatusDesc(statusDesc(orderStatus))
                .payStatusDesc(payStatusDesc(payStatus))
                .build();
    }

    private PaymentVO toVO(FmPaymentRecord record, Long orderId, java.math.BigDecimal orderPayAmount) {
        PaymentVO vo = new PaymentVO();
        if (record == null) {
            // 尚未发起过支付，返回订单应付金额方便前端展示
            vo.setOrderId(orderId);
            vo.setAmount(orderPayAmount);
            vo.setChannel(CHANNEL_MOCK);
            return vo;
        }
        vo.setPayNo(record.getPayNo());
        vo.setOrderId(record.getOrderId());
        vo.setOrderNo(record.getOrderNo());
        vo.setChannel(record.getChannel());
        vo.setAmount(record.getAmount());
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(payStatusDesc(record.getStatus()));
        vo.setPayTime(record.getPayTime());
        vo.setCallbackTime(record.getCallbackTime());
        vo.setCloseTime(record.getCloseTime());
        vo.setFailReason(record.getFailReason());
        return vo;
    }

    private String statusDesc(Integer status) {
        OrderStatusEnum statusEnum = OrderStatusEnum.of(status);
        return statusEnum == null ? String.valueOf(status) : statusEnum.getDesc();
    }

    private String payStatusDesc(Integer status) {
        for (PayStatusEnum payStatus : PayStatusEnum.values()) {
            if (payStatus.getCode().equals(status)) {
                return payStatus.getDesc();
            }
        }
        return status == null ? null : String.valueOf(status);
    }
}
