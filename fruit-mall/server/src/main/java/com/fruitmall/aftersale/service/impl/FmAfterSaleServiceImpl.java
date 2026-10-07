package com.fruitmall.aftersale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.aftersale.domain.FmAfterSale;
import com.fruitmall.aftersale.domain.FmAfterSaleItem;
import com.fruitmall.aftersale.domain.FmRefundRecord;
import com.fruitmall.aftersale.dto.AfterSaleApplyDTO;
import com.fruitmall.aftersale.mapper.FmAfterSaleItemMapper;
import com.fruitmall.aftersale.mapper.FmAfterSaleMapper;
import com.fruitmall.aftersale.mapper.FmRefundRecordMapper;
import com.fruitmall.aftersale.query.AfterSaleQuery;
import com.fruitmall.aftersale.service.IFmAfterSaleService;
import com.fruitmall.aftersale.state.AfterSaleStateMachine;
import com.fruitmall.aftersale.vo.AfterSaleItemVO;
import com.fruitmall.aftersale.vo.AfterSaleListVO;
import com.fruitmall.aftersale.vo.AfterSaleVO;
import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.enums.AfterSaleActionEnum;
import com.fruitmall.common.enums.AfterSaleStatusEnum;
import com.fruitmall.common.enums.AfterSaleTypeEnum;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.enums.PayStatusEnum;
import com.fruitmall.common.enums.RefundStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.BizNoUtil;
import com.fruitmall.inventory.service.IInventoryTransactionService;
import com.fruitmall.order.domain.FmOrderItem;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrderItemMapper;
import com.fruitmall.order.mapper.FmOrdersMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 售后服务实现。
 * R-08 可售后数量 ≤ 订单项数量 − 已售后数量：申请时校验并占用，驳回或撤销时归还。
 * R-09 退货回补进入退货暂存、不进入可售库存：确认收到退货时只写类型 50 的流水，不动 SKU 可售库存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmAfterSaleServiceImpl extends ServiceImpl<FmAfterSaleMapper, FmAfterSale>
        implements IFmAfterSaleService {

    /** 本课题不接真实退款渠道 */
    private static final String CHANNEL_MOCK = "MOCK";

    private final FmAfterSaleItemMapper afterSaleItemMapper;
    private final FmRefundRecordMapper refundRecordMapper;
    private final FmOrderItemMapper orderItemMapper;
    private final FmOrdersMapper ordersMapper;
    private final FmPaymentRecordMapper paymentRecordMapper;
    private final FmProductSkuMapper skuMapper;
    private final IInventoryTransactionService inventoryTransactionService;
    private final AfterSaleStateMachine afterSaleStateMachine;
    private final OrderStateMachine orderStateMachine;
    private final PaymentStateMachine paymentStateMachine;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long apply(AfterSaleApplyDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();
        FmOrderItem orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单项不存在");
        }
        FmOrders order = ordersMapper.selectById(orderItem.getOrderId());
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单项不存在");
        }
        if (!OrderStatusEnum.PAID.getCode().equals(order.getStatus())
                && !OrderStatusEnum.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.CONFLICT,
                    "当前订单状态不支持申请售后：备货中与配送中的订单请先完成收货，或联系客服处理");
        }

        // R-08：可售后数量校验
        int soldQuantity = orderItem.getQuantity() == null ? 0 : orderItem.getQuantity();
        int afterSaleQuantity = orderItem.getAfterSaleQuantity() == null ? 0 : orderItem.getAfterSaleQuantity();
        int available = soldQuantity - afterSaleQuantity;
        if (dto.getQuantity() > available) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "超出可售后数量，本商品最多还能申请 " + available + " 件");
        }

        BigDecimal refundAmount = orderItem.getPrice() == null
                ? BigDecimal.ZERO
                : orderItem.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        FmAfterSale afterSale = new FmAfterSale();
        afterSale.setAfterSaleNo(BizNoUtil.generate(BizNoUtil.AFTER_SALE_PREFIX));
        afterSale.setOrderId(order.getId());
        afterSale.setOrderNo(order.getOrderNo());
        afterSale.setMemberId(memberId);
        afterSale.setType(dto.getType());
        afterSale.setStatus(AfterSaleStatusEnum.PENDING_AUDIT.getCode());
        afterSale.setQuantity(dto.getQuantity());
        afterSale.setRefundAmount(refundAmount);
        afterSale.setReason(dto.getReason());
        afterSale.setEvidenceImages(dto.getEvidenceImages());
        afterSale.setApplyTime(LocalDateTime.now());
        this.save(afterSale);

        FmAfterSaleItem afterSaleItem = new FmAfterSaleItem();
        afterSaleItem.setAfterSaleId(afterSale.getId());
        afterSaleItem.setOrderItemId(orderItem.getId());
        afterSaleItem.setSkuId(orderItem.getSkuId());
        afterSaleItem.setQuantity(dto.getQuantity());
        afterSaleItem.setRefundAmount(refundAmount);
        afterSaleItemMapper.insert(afterSaleItem);

        // 占用可售后数量，避免同一订单项被重复申请超出总量
        FmOrderItem itemUpdate = new FmOrderItem();
        itemUpdate.setId(orderItem.getId());
        itemUpdate.setAfterSaleQuantity(afterSaleQuantity + dto.getQuantity());
        orderItemMapper.updateById(itemUpdate);

        log.info("售后申请提交：afterSaleNo={}, orderNo={}, quantity={}",
                afterSale.getAfterSaleNo(), order.getOrderNo(), dto.getQuantity());
        return afterSale.getId();
    }

    @Override
    public PageResult<AfterSaleListVO> pageMine(AfterSaleQuery query) {
        return page(query, UserContext.getRequiredMemberId());
    }

    @Override
    public PageResult<AfterSaleListVO> pageAll(AfterSaleQuery query) {
        return page(query, null);
    }

    @Override
    public AfterSaleVO detailMine(Long id) {
        return buildVO(requireMine(id));
    }

    @Override
    public AfterSaleVO detailForAdmin(Long id) {
        return buildVO(requireAfterSale(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelByMember(Long id, String reason) {
        FmAfterSale afterSale = requireMine(id);
        if (!AfterSaleStatusEnum.PENDING_AUDIT.getCode().equals(afterSale.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "只有待审核的售后单可以撤销");
        }
        LoginUser operator = UserContext.getRequired();
        FmAfterSale update = new FmAfterSale();
        update.setId(id);
        update.setCancelReason(StringUtils.hasText(reason) ? reason : "会员撤销申请");
        afterSaleStateMachine.transition(update, AfterSaleStatusEnum.CANCELLED,
                AfterSaleActionEnum.CANCEL, OperatorTypeEnum.MEMBER.getCode(),
                operator.getUserId(), operator.getUsername());
        releaseAfterSaleQuantity(afterSale);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void audit(Long id, boolean pass, String remark) {
        FmAfterSale afterSale = requireAfterSale(id);
        if (!AfterSaleStatusEnum.PENDING_AUDIT.getCode().equals(afterSale.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "该售后单已审核，不能重复审核");
        }
        LoginUser operator = UserContext.getRequired();
        FmAfterSale update = new FmAfterSale();
        update.setId(id);
        update.setAuditTime(LocalDateTime.now());
        update.setAuditorId(operator.getUserId());
        update.setAuditRemark(remark);

        if (!pass) {
            afterSaleStateMachine.transition(update, AfterSaleStatusEnum.REJECTED,
                    AfterSaleActionEnum.AUDIT_REJECT, OperatorTypeEnum.MERCHANT.getCode(),
                    operator.getUserId(), operator.getUsername());
            releaseAfterSaleQuantity(afterSale);
            return;
        }

        afterSaleStateMachine.transition(update, AfterSaleStatusEnum.APPROVED,
                AfterSaleActionEnum.AUDIT_PASS, OperatorTypeEnum.MERCHANT.getCode(),
                operator.getUserId(), operator.getUsername());
        syncOrderStatus(afterSale.getOrderId(), OrderStatusEnum.REFUNDING, OrderActionEnum.REFUND,
                "售后审核通过，订单进入退款流程", operator);
        // 进入退款流程时支付单同步转退款中；
        // 两条售后路径都必须在这里转，否则"退货退款"路径的支付单会一直停在支付成功，
        // 后续直接跳"已退款"会被支付状态机拦下（曾因此返回 409）。
        ensurePaymentRefunding(afterSale.getOrderId());

        if (AfterSaleTypeEnum.RETURN_REFUND.getCode().equals(afterSale.getType())) {
            FmAfterSale returnUpdate = new FmAfterSale();
            returnUpdate.setId(id);
            afterSaleStateMachine.transition(returnUpdate, AfterSaleStatusEnum.RETURNING,
                    AfterSaleActionEnum.AUDIT_PASS, OperatorTypeEnum.MERCHANT.getCode(),
                    operator.getUserId(), operator.getUsername());
        } else {
            startRefund(afterSale, operator);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receive(Long id, String remark) {
        FmAfterSale afterSale = requireAfterSale(id);
        if (!AfterSaleStatusEnum.RETURNING.getCode().equals(afterSale.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "只有退货中的售后单可以确认收货");
        }
        LoginUser operator = UserContext.getRequired();
        FmAfterSale update = new FmAfterSale();
        update.setId(id);
        update.setReceiveTime(LocalDateTime.now());
        afterSaleStateMachine.transition(update, AfterSaleStatusEnum.REFUNDING,
                AfterSaleActionEnum.RECEIVE, OperatorTypeEnum.MERCHANT.getCode(),
                operator.getUserId(), operator.getUsername());

        // R-09：生鲜退货不可二次销售，只记录退货回补流水（类型 50），可售库存保持不变
        for (FmAfterSaleItem item : listItems(id)) {
            FmProductSku sku = skuMapper.selectById(item.getSkuId());
            int stock = sku == null || sku.getStock() == null ? 0 : sku.getStock();
            inventoryTransactionService.record(item.getSkuId(), InventoryTxnTypeEnum.RETURN_IN,
                    item.getQuantity(), stock, stock, InventoryBizTypeEnum.AFTER_SALE,
                    afterSale.getAfterSaleNo(), operator.getUserId(), operator.getUsername(),
                    "退货回补至退货暂存，不进入可售库存");
        }
        startRefundRecord(afterSale, operator);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refund(Long id, String remark) {
        FmAfterSale afterSale = requireAfterSale(id);
        if (!AfterSaleStatusEnum.REFUNDING.getCode().equals(afterSale.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "只有退款中的售后单可以确认退款");
        }
        LoginUser operator = UserContext.getRequired();
        LocalDateTime now = LocalDateTime.now();
        FmAfterSale update = new FmAfterSale();
        update.setId(id);
        update.setRefundTime(now);
        afterSaleStateMachine.transition(update, AfterSaleStatusEnum.COMPLETED,
                AfterSaleActionEnum.REFUND_DONE, OperatorTypeEnum.MERCHANT.getCode(),
                operator.getUserId(), operator.getUsername());

        // 兜底：支付单若仍停留在支付成功（历史数据或异常中断），先补一次退款中流转
        ensurePaymentRefunding(afterSale.getOrderId());
        FmRefundRecord refundRecord = getRefundRecord(id);
        if (refundRecord != null) {
            FmRefundRecord refundUpdate = new FmRefundRecord();
            refundUpdate.setId(refundRecord.getId());
            refundUpdate.setStatus(RefundStatusEnum.SUCCESS.getCode());
            refundUpdate.setRefundTime(now);
            refundUpdate.setOperatorId(operator.getUserId());
            refundRecordMapper.updateById(refundUpdate);
            updatePaymentStatus(afterSale.getOrderId(), PayStatusEnum.REFUNDED);
        }

        syncOrderStatus(afterSale.getOrderId(), OrderStatusEnum.REFUNDED, OrderActionEnum.REFUND_DONE,
                StringUtils.hasText(remark) ? remark : "退款完成", operator);
        log.info("退款完成：afterSaleNo={}, amount={}", afterSale.getAfterSaleNo(), afterSale.getRefundAmount());
    }

    /** 仅退款场景：售后单转退款中并创建退款单 */
    private void startRefund(FmAfterSale afterSale, LoginUser operator) {
        FmAfterSale update = new FmAfterSale();
        update.setId(afterSale.getId());
        afterSaleStateMachine.transition(update, AfterSaleStatusEnum.REFUNDING,
                AfterSaleActionEnum.REFUND, OperatorTypeEnum.MERCHANT.getCode(),
                operator.getUserId(), operator.getUsername());
        startRefundRecord(afterSale, operator);
    }

    /**
     * 确保支付单已进入退款中。
     * 支付状态机允许 支付成功 → 退款中 → 已退款，
     * 因此进入退款流程时必须先转"退款中"，不能直接从"支付成功"跳到"已退款"。
     */
    private void ensurePaymentRefunding(Long orderId) {
        FmPaymentRecord record = paymentRecordMapper.selectOne(
                Wrappers.<FmPaymentRecord>lambdaQuery().eq(FmPaymentRecord::getOrderId, orderId));
        if (record == null || !PayStatusEnum.SUCCESS.getCode().equals(record.getStatus())) {
            return;
        }
        FmPaymentRecord update = new FmPaymentRecord();
        update.setId(record.getId());
        paymentStateMachine.transition(update, PayStatusEnum.REFUNDING);
    }

    /** 创建退款记录（状态为退款中） */
    private void startRefundRecord(FmAfterSale afterSale, LoginUser operator) {
        if (getRefundRecord(afterSale.getId()) != null) {
            return;
        }
        FmRefundRecord refundRecord = new FmRefundRecord();
        refundRecord.setRefundNo(BizNoUtil.generate(BizNoUtil.REFUND_PREFIX));
        refundRecord.setAfterSaleId(afterSale.getId());
        refundRecord.setOrderId(afterSale.getOrderId());
        refundRecord.setOrderNo(afterSale.getOrderNo());
        refundRecord.setMemberId(afterSale.getMemberId());
        refundRecord.setRefundAmount(afterSale.getRefundAmount());
        refundRecord.setStatus(RefundStatusEnum.REFUNDING.getCode());
        refundRecord.setChannel(CHANNEL_MOCK);
        refundRecord.setOperatorId(operator.getUserId());
        refundRecordMapper.insert(refundRecord);
    }

    /** 联动订单状态 */
    private void syncOrderStatus(Long orderId, OrderStatusEnum target, OrderActionEnum action,
                                 String remark, LoginUser operator) {
        FmOrders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        FmOrders update = new FmOrders();
        update.setId(orderId);
        orderStateMachine.transition(update, target, action, OperatorTypeEnum.MERCHANT,
                operator.getUserId(), operator.getUsername(), remark);
    }

    /** 同步支付单状态，退款流程中支付单也应有对应的状态变化 */
    private void updatePaymentStatus(Long orderId, PayStatusEnum target) {
        FmPaymentRecord record = paymentRecordMapper.selectOne(
                Wrappers.<FmPaymentRecord>lambdaQuery().eq(FmPaymentRecord::getOrderId, orderId));
        if (record == null) {
            return;
        }
        FmPaymentRecord update = new FmPaymentRecord();
        update.setId(record.getId());
        paymentStateMachine.transition(update, target);
    }

    /** 归还已占用的可售后数量（驳回或撤销时） */
    private void releaseAfterSaleQuantity(FmAfterSale afterSale) {
        for (FmAfterSaleItem item : listItems(afterSale.getId())) {
            FmOrderItem orderItem = orderItemMapper.selectById(item.getOrderItemId());
            if (orderItem == null) {
                continue;
            }
            int current = orderItem.getAfterSaleQuantity() == null ? 0 : orderItem.getAfterSaleQuantity();
            FmOrderItem update = new FmOrderItem();
            update.setId(orderItem.getId());
            update.setAfterSaleQuantity(Math.max(current - item.getQuantity(), 0));
            orderItemMapper.updateById(update);
        }
    }

    private PageResult<AfterSaleListVO> page(AfterSaleQuery query, Long memberId) {
        LambdaQueryWrapper<FmAfterSale> wrapper = Wrappers.<FmAfterSale>lambdaQuery()
                .eq(memberId != null, FmAfterSale::getMemberId, memberId)
                .eq(query.getStatus() != null, FmAfterSale::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getOrderNo()), FmAfterSale::getOrderNo, query.getOrderNo())
                .orderByDesc(FmAfterSale::getId);
        Page<FmAfterSale> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<AfterSaleListVO> list = new ArrayList<>();
        for (FmAfterSale afterSale : page.getRecords()) {
            AfterSaleListVO vo = new AfterSaleListVO();
            vo.setId(afterSale.getId());
            vo.setAfterSaleNo(afterSale.getAfterSaleNo());
            vo.setOrderNo(afterSale.getOrderNo());
            vo.setType(afterSale.getType());
            vo.setTypeDesc(typeDesc(afterSale.getType()));
            vo.setStatus(afterSale.getStatus());
            vo.setStatusDesc(statusDesc(afterSale.getStatus()));
            vo.setQuantity(afterSale.getQuantity());
            vo.setRefundAmount(afterSale.getRefundAmount());
            vo.setReason(afterSale.getReason());
            vo.setCreateTime(afterSale.getCreateTime());
            list.add(vo);
        }
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }

    private AfterSaleVO buildVO(FmAfterSale afterSale) {
        AfterSaleVO vo = new AfterSaleVO();
        vo.setId(afterSale.getId());
        vo.setAfterSaleNo(afterSale.getAfterSaleNo());
        vo.setOrderId(afterSale.getOrderId());
        vo.setOrderNo(afterSale.getOrderNo());
        vo.setType(afterSale.getType());
        vo.setTypeDesc(typeDesc(afterSale.getType()));
        vo.setStatus(afterSale.getStatus());
        vo.setStatusDesc(statusDesc(afterSale.getStatus()));
        vo.setQuantity(afterSale.getQuantity());
        vo.setRefundAmount(afterSale.getRefundAmount());
        vo.setReason(afterSale.getReason());
        vo.setEvidenceImages(afterSale.getEvidenceImages());
        vo.setApplyTime(afterSale.getApplyTime());
        vo.setAuditTime(afterSale.getAuditTime());
        vo.setAuditRemark(afterSale.getAuditRemark());
        vo.setReturnTime(afterSale.getReturnTime());
        vo.setReceiveTime(afterSale.getReceiveTime());
        vo.setRefundTime(afterSale.getRefundTime());
        vo.setCancelReason(afterSale.getCancelReason());

        for (FmAfterSaleItem item : listItems(afterSale.getId())) {
            AfterSaleItemVO itemVO = new AfterSaleItemVO();
            itemVO.setOrderItemId(item.getOrderItemId());
            itemVO.setSkuId(item.getSkuId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setRefundAmount(item.getRefundAmount());
            itemVO.setReturnBatchId(item.getReturnBatchId());
            FmOrderItem orderItem = orderItemMapper.selectById(item.getOrderItemId());
            if (orderItem != null) {
                itemVO.setSpuName(orderItem.getSpuName());
                itemVO.setSkuName(orderItem.getSkuName());
                itemVO.setImage(orderItem.getImage());
            }
            vo.getItems().add(itemVO);
        }

        FmRefundRecord refundRecord = getRefundRecord(afterSale.getId());
        if (refundRecord != null) {
            vo.setRefundNo(refundRecord.getRefundNo());
            vo.setRefundStatusDesc(refundStatusDesc(refundRecord.getStatus()));
        }
        return vo;
    }

    private List<FmAfterSaleItem> listItems(Long afterSaleId) {
        return afterSaleItemMapper.selectList(Wrappers.<FmAfterSaleItem>lambdaQuery()
                .eq(FmAfterSaleItem::getAfterSaleId, afterSaleId)
                .orderByAsc(FmAfterSaleItem::getId));
    }

    private FmRefundRecord getRefundRecord(Long afterSaleId) {
        return refundRecordMapper.selectOne(Wrappers.<FmRefundRecord>lambdaQuery()
                .eq(FmRefundRecord::getAfterSaleId, afterSaleId));
    }

    private FmAfterSale requireMine(Long id) {
        Long memberId = UserContext.getRequiredMemberId();
        FmAfterSale afterSale = this.getById(id);
        if (afterSale == null || !memberId.equals(afterSale.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "售后单不存在");
        }
        return afterSale;
    }

    private FmAfterSale requireAfterSale(Long id) {
        FmAfterSale afterSale = this.getById(id);
        if (afterSale == null) {
            throw new BizException(ResultCode.NOT_FOUND, "售后单不存在");
        }
        return afterSale;
    }

    private String typeDesc(Integer type) {
        for (AfterSaleTypeEnum typeEnum : AfterSaleTypeEnum.values()) {
            if (typeEnum.getCode().equals(type)) {
                return typeEnum.getDesc();
            }
        }
        return type == null ? null : String.valueOf(type);
    }

    private String statusDesc(Integer status) {
        AfterSaleStatusEnum statusEnum = AfterSaleStatusEnum.of(status);
        return statusEnum == null ? String.valueOf(status) : statusEnum.getDesc();
    }

    private String refundStatusDesc(Integer status) {
        for (RefundStatusEnum refundStatus : RefundStatusEnum.values()) {
            if (refundStatus.getCode().equals(status)) {
                return refundStatus.getDesc();
            }
        }
        return status == null ? null : String.valueOf(status);
    }
}
