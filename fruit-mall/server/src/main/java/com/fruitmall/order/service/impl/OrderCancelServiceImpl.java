package com.fruitmall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.enums.PayStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.inventory.service.IInventoryTransactionService;
import com.fruitmall.order.domain.FmOrderItem;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrderItemMapper;
import com.fruitmall.order.service.IOrderCancelService;
import com.fruitmall.order.state.OrderStateMachine;
import com.fruitmall.payment.domain.FmPaymentRecord;
import com.fruitmall.payment.mapper.FmPaymentRecordMapper;
import com.fruitmall.payment.state.PaymentStateMachine;
import com.fruitmall.product.domain.FmProductSku;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单关单服务实现。
 *
 * 说明：关闭未完成的支付单时直接使用了支付模块的 Mapper 与状态机，而不是支付服务。
 * 原因是支付服务在"模拟支付超时"分支也需要调用本类的关单逻辑，
 * 若本类再反向依赖支付服务就会形成循环依赖；Mapper 属于叶子依赖，不会成环。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelServiceImpl implements IOrderCancelService {

    private final FmOrderItemMapper orderItemMapper;
    private final FmProductSkuMapper skuMapper;
    private final IInventoryTransactionService inventoryTransactionService;
    private final OrderStateMachine orderStateMachine;
    private final FmPaymentRecordMapper paymentRecordMapper;
    private final PaymentStateMachine paymentStateMachine;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void close(FmOrders order, OrderActionEnum action, OperatorTypeEnum operatorType,
                      Long operatorId, String operatorName, String reason) {
        if (order == null || order.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "订单ID不能为空");
        }
        if (!OrderStatusEnum.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "只有待支付订单可以关闭，已支付订单请走售后流程");
        }

        releaseStock(order, operatorId, operatorName);

        FmOrders update = new FmOrders();
        update.setId(order.getId());
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason(StringUtils.hasText(reason) ? reason : "订单已关闭");
        orderStateMachine.transition(update, OrderStatusEnum.CANCELLED, action,
                operatorType, operatorId, operatorName, reason);

        closeUnfinishedPayments(order);
    }

    /**
     * 释放订单占用的预占库存并写流水。
     * 释放失败说明数据已经不一致，这里记错误日志后继续，避免订单永久卡在待支付；
     * 不一致由一致性校验脚本兜底发现。
     */
    private void releaseStock(FmOrders order, Long operatorId, String operatorName) {
        for (FmOrderItem item : orderItemMapper.selectList(Wrappers.<FmOrderItem>lambdaQuery()
                .eq(FmOrderItem::getOrderId, order.getId()))) {
            FmProductSku sku = skuMapper.selectById(item.getSkuId());
            int beforeStock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
            int affected = skuMapper.releaseStock(item.getSkuId(), item.getQuantity());
            if (affected == 0) {
                log.error("释放预占库存失败，可能存在数据不一致：orderNo={}, skuId={}, quantity={}",
                        order.getOrderNo(), item.getSkuId(), item.getQuantity());
                continue;
            }
            inventoryTransactionService.record(item.getSkuId(), InventoryTxnTypeEnum.RELEASE,
                    item.getQuantity(), beforeStock, beforeStock + item.getQuantity(),
                    InventoryBizTypeEnum.ORDER, order.getOrderNo(), operatorId, operatorName, "订单关闭释放库存");
        }
    }

    /** 关单时把未完成的支付单一起关闭，避免留下"订单已取消但支付单还在支付中"的脏数据 */
    private void closeUnfinishedPayments(FmOrders order) {
        List<FmPaymentRecord> records = paymentRecordMapper.selectList(
                Wrappers.<FmPaymentRecord>lambdaQuery().eq(FmPaymentRecord::getOrderId, order.getId()));
        for (FmPaymentRecord record : records) {
            if (PayStatusEnum.CLOSED.getCode().equals(record.getStatus())
                    || PayStatusEnum.REFUNDED.getCode().equals(record.getStatus())) {
                continue;
            }
            FmPaymentRecord update = new FmPaymentRecord();
            update.setId(record.getId());
            update.setCloseTime(LocalDateTime.now());
            paymentStateMachine.transition(update, PayStatusEnum.CLOSED);
        }
    }
}
