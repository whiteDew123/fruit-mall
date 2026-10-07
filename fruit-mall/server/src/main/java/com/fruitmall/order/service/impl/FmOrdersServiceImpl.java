package com.fruitmall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.behavior.dto.BehaviorReportDTO;
import com.fruitmall.behavior.service.IBehaviorService;
import com.fruitmall.cart.service.IFmCartItemService;
import com.fruitmall.cart.vo.CartItemVO;
import com.fruitmall.common.enums.BehaviorTargetTypeEnum;
import com.fruitmall.common.enums.BehaviorTypeEnum;
import com.fruitmall.common.enums.InventoryBizTypeEnum;
import com.fruitmall.common.enums.InventoryTxnTypeEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.BizNoUtil;
import com.fruitmall.common.util.MaskUtil;
import com.fruitmall.inventory.service.IInventoryTransactionService;
import com.fruitmall.member.service.IFmMemberAddressService;
import com.fruitmall.member.vo.MemberAddressVO;
import com.fruitmall.order.domain.FmOrderItem;
import com.fruitmall.order.domain.FmOrderStatusLog;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.dto.OrderSubmitDTO;
import com.fruitmall.order.mapper.FmOrderItemMapper;
import com.fruitmall.order.mapper.FmOrderStatusLogMapper;
import com.fruitmall.order.mapper.FmOrdersMapper;
import com.fruitmall.order.query.OrderQuery;
import com.fruitmall.order.service.IFmOrdersService;
import com.fruitmall.order.state.OrderStateMachine;
import com.fruitmall.order.vo.OrderDetailVO;
import com.fruitmall.order.vo.OrderItemVO;
import com.fruitmall.order.vo.OrderListVO;
import com.fruitmall.order.vo.OrderPreviewItemVO;
import com.fruitmall.order.vo.OrderPreviewVO;
import com.fruitmall.order.vo.OrderStatusLogVO;
import com.fruitmall.order.vo.OrderSubmitVO;
import com.fruitmall.product.domain.FmProductSku;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 * 下单是整套系统一致性要求最高的操作：
 * 库存预占用条件更新（WHERE 带库存判断）+ 影响行数判断，与订单、订单项、库存流水的写入同处一个事务，
 * 任一环节失败整单回滚，从而保证不超卖、不出现负库存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmOrdersServiceImpl extends ServiceImpl<FmOrdersMapper, FmOrders> implements IFmOrdersService {

    /** 本版不计算运费，固定为 0 */
    private static final BigDecimal FREIGHT_AMOUNT = BigDecimal.ZERO;

    private final FmOrderItemMapper orderItemMapper;
    private final FmOrderStatusLogMapper orderStatusLogMapper;
    private final FmProductSkuMapper skuMapper;
    private final IFmCartItemService fmCartItemService;
    private final IFmMemberAddressService fmMemberAddressService;
    private final IInventoryTransactionService inventoryTransactionService;
    private final OrderStateMachine orderStateMachine;
    private final IBehaviorService behaviorService;

    /** 支付截止时长（分钟），超时后由定时任务关单 */
    @Value("${fruit-mall.order.expire-minutes:30}")
    private int expireMinutes;

    @Override
    public OrderPreviewVO preview() {
        Long memberId = UserContext.getRequiredMemberId();
        OrderPreviewVO vo = new OrderPreviewVO();
        vo.setAddress(fmMemberAddressService.getDefaultAddress(memberId));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO cartItem : fmCartItemService.listSelectedForOrder(memberId)) {
            OrderPreviewItemVO line = new OrderPreviewItemVO();
            line.setCartItemId(cartItem.getId());
            line.setSpuId(cartItem.getSpuId());
            line.setSkuId(cartItem.getSkuId());
            line.setSpuName(cartItem.getSpuName());
            line.setSpecName(cartItem.getSpecName());
            line.setSpecJson(cartItem.getSpecJson());
            line.setImage(StringUtils.hasText(cartItem.getSkuImage())
                    ? cartItem.getSkuImage() : cartItem.getMainImage());
            line.setPrice(cartItem.getPrice());
            line.setQuantity(cartItem.getQuantity());
            line.setAvailableStock(cartItem.getAvailableStock());
            BigDecimal amount = multiply(cartItem.getPrice(), cartItem.getQuantity());
            line.setAmount(amount);
            vo.getItems().add(line);
            totalAmount = totalAmount.add(amount);
        }
        vo.setTotalAmount(totalAmount);
        vo.setFreightAmount(FREIGHT_AMOUNT);
        vo.setPayAmount(totalAmount.add(FREIGHT_AMOUNT));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submit(OrderSubmitDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();
        LoginUser loginUser = UserContext.getRequired();
        // 地址必须属于当前会员
        MemberAddressVO address = fmMemberAddressService.getMine(dto.getAddressId());
        List<CartItemVO> cartItems = fmCartItemService.listSelectedForOrder(memberId);
        if (cartItems.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请先在购物车中选择要购买的商品");
        }

        // 金额按 SKU 实时价重算，不信任前端传入的价格
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO cartItem : cartItems) {
            if (cartItem.getPrice() == null) {
                throw new BizException(ResultCode.CONFLICT, "商品「" + cartItem.getSpuName() + "」价格异常");
            }
            totalAmount = totalAmount.add(multiply(cartItem.getPrice(), cartItem.getQuantity()));
        }
        BigDecimal payAmount = totalAmount.add(FREIGHT_AMOUNT);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(expireMinutes);
        String orderNo = BizNoUtil.generate(BizNoUtil.ORDER_PREFIX);

        FmOrders order = new FmOrders();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setStatus(OrderStatusEnum.PENDING_PAY.getCode());
        order.setTotalAmount(totalAmount);
        order.setFreightAmount(FREIGHT_AMOUNT);
        order.setPayAmount(payAmount);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverDistrict(address.getDistrict());
        order.setReceiverAddress(address.getDetailAddress());
        order.setMemberRemark(dto.getMemberRemark());
        order.setExpireTime(expireTime);
        order.setVersion(0);
        this.save(order);

        // 订单时间轴的第一个节点：提交订单
        FmOrderStatusLog createLog = new FmOrderStatusLog();
        createLog.setOrderId(order.getId());
        createLog.setOrderNo(orderNo);
        createLog.setFromStatus(null);
        createLog.setToStatus(OrderStatusEnum.PENDING_PAY.getCode());
        createLog.setAction(OrderActionEnum.CREATE.getCode());
        createLog.setOperatorType(OperatorTypeEnum.MEMBER.getCode());
        createLog.setOperatorId(memberId);
        createLog.setOperatorName(loginUser.getUsername());
        createLog.setRemark("提交订单");
        createLog.setCreateTime(now);
        orderStatusLogMapper.insert(createLog);

        for (CartItemVO cartItem : cartItems) {
            lockStockAndSaveItem(order, cartItem, loginUser);
        }

        // 下单成功，把已购买的商品从购物车移除
        fmCartItemService.removeOrderedItems(
                cartItems.stream().map(CartItemVO::getId).toList(), memberId);
        // 埋点放在事务提交之后，避免长事务，也避免埋点失败影响下单
        reportOrderBehaviorAfterCommit(cartItems);

        log.info("下单成功：orderNo={}, memberId={}, payAmount={}", orderNo, memberId, payAmount);
        return OrderSubmitVO.builder()
                .orderId(order.getId())
                .orderNo(orderNo)
                .payAmount(payAmount)
                .expireTime(expireTime)
                .build();
    }

    /**
     * 预占单个 SKU 的库存并写订单项与库存流水。
     * 条件更新返回 0 说明可售库存不足，直接抛 409 让整单回滚。
     */
    private void lockStockAndSaveItem(FmOrders order, CartItemVO cartItem, LoginUser loginUser) {
        FmProductSku sku = skuMapper.selectById(cartItem.getSkuId());
        if (sku == null) {
            throw new BizException(ResultCode.CONFLICT, "商品规格不存在：" + cartItem.getSpecName());
        }
        int beforeStock = sku.getStock() == null ? 0 : sku.getStock();
        int quantity = cartItem.getQuantity();

        int affected = skuMapper.lockStock(cartItem.getSkuId(), quantity);
        if (affected == 0) {
            throw new BizException(ResultCode.CONFLICT,
                    "「" + cartItem.getSpuName() + " " + cartItem.getSpecName() + "」库存不足，请重新选择");
        }
        inventoryTransactionService.record(cartItem.getSkuId(), InventoryTxnTypeEnum.LOCK, -quantity,
                beforeStock, beforeStock - quantity, InventoryBizTypeEnum.ORDER, order.getOrderNo(),
                loginUser.getUserId(), loginUser.getUsername(), "下单预占库存");

        FmOrderItem orderItem = new FmOrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setOrderNo(order.getOrderNo());
        orderItem.setSpuId(cartItem.getSpuId());
        orderItem.setSkuId(cartItem.getSkuId());
        orderItem.setSpuName(cartItem.getSpuName());
        orderItem.setSkuName(cartItem.getSpecName());
        orderItem.setSkuSnapshot(cartItem.getSpecJson());
        orderItem.setImage(StringUtils.hasText(cartItem.getSkuImage())
                ? cartItem.getSkuImage() : cartItem.getMainImage());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.setAmount(multiply(cartItem.getPrice(), quantity));
        orderItem.setAfterSaleQuantity(0);
        orderItem.setReviewed(0);
        orderItemMapper.insert(orderItem);
    }

    @Override
    public PageResult<OrderListVO> pageMine(OrderQuery query) {
        return pageOrders(query, UserContext.getRequiredMemberId());
    }

    @Override
    public PageResult<OrderListVO> pageAll(OrderQuery query) {
        return pageOrders(query, null);
    }

    @Override
    public OrderDetailVO detailMine(Long orderId) {
        return buildDetail(requireMine(orderId));
    }

    @Override
    public OrderDetailVO detailForAdmin(Long orderId) {
        return buildDetail(requireOrder(orderId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelByMember(Long orderId, String reason) {
        FmOrders order = requireMine(orderId);
        LoginUser loginUser = UserContext.getRequired();
        cancel(order, reason, OperatorTypeEnum.MEMBER, loginUser.getUserId(), loginUser.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelByAdmin(Long orderId, String reason) {
        FmOrders order = requireOrder(orderId);
        LoginUser loginUser = UserContext.getRequired();
        cancel(order, reason, OperatorTypeEnum.MERCHANT, loginUser.getUserId(), loginUser.getUsername());
    }

    @Override
    public void updateAdminRemark(Long orderId, String remark) {
        requireOrder(orderId);
        FmOrders update = new FmOrders();
        update.setId(orderId);
        update.setAdminRemark(remark);
        this.updateById(update);
    }

    /**
     * 取消订单：仅待支付状态可取消，先释放预占库存再走状态机。
     */
    private void cancel(FmOrders order, String reason, OperatorTypeEnum operatorType,
                        Long operatorId, String operatorName) {
        if (!OrderStatusEnum.PENDING_PAY.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "只有待支付订单可以取消，已支付订单请走售后流程");
        }
        releaseStock(order, operatorId, operatorName);

        FmOrders update = new FmOrders();
        update.setId(order.getId());
        update.setCancelTime(LocalDateTime.now());
        update.setCancelReason(StringUtils.hasText(reason) ? reason : "用户取消");
        orderStateMachine.transition(update, OrderStatusEnum.CANCELLED, OrderActionEnum.CANCEL,
                operatorType, operatorId, operatorName, reason);
    }

    /**
     * 释放订单占用的库存并写流水。
     * 释放失败（预占数量不足）说明数据已经不一致，这里记错误日志并继续，
     * 避免订单永久卡在待支付；不一致由定期的一致性校验脚本兜住。
     */
    private void releaseStock(FmOrders order, Long operatorId, String operatorName) {
        for (FmOrderItem item : listItems(order.getId())) {
            FmProductSku sku = skuMapper.selectById(item.getSkuId());
            int beforeStock = sku == null ? 0 : (sku.getStock() == null ? 0 : sku.getStock());
            int affected = skuMapper.releaseStock(item.getSkuId(), item.getQuantity());
            if (affected == 0) {
                log.error("释放预占库存失败，可能已存在数据不一致：orderNo={}, skuId={}, quantity={}",
                        order.getOrderNo(), item.getSkuId(), item.getQuantity());
                continue;
            }
            inventoryTransactionService.record(item.getSkuId(), InventoryTxnTypeEnum.RELEASE,
                    item.getQuantity(), beforeStock, beforeStock + item.getQuantity(),
                    InventoryBizTypeEnum.ORDER, order.getOrderNo(), operatorId, operatorName, "取消订单释放库存");
        }
    }

    private PageResult<OrderListVO> pageOrders(OrderQuery query, Long memberId) {
        LambdaQueryWrapper<FmOrders> wrapper = Wrappers.<FmOrders>lambdaQuery()
                .eq(memberId != null, FmOrders::getMemberId, memberId)
                .eq(memberId == null && query.getMemberId() != null, FmOrders::getMemberId, query.getMemberId())
                .eq(query.getStatus() != null, FmOrders::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getOrderNo()), FmOrders::getOrderNo, query.getOrderNo())
                .orderByDesc(FmOrders::getId);
        Page<FmOrders> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), toListVO(page.getRecords()));
    }

    /** 批量取订单项，避免列表页逐条查询 */
    private List<OrderListVO> toListVO(List<FmOrders> orders) {
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Long> orderIds = orders.stream().map(FmOrders::getId).toList();
        Map<Long, List<FmOrderItem>> itemMap = orderItemMapper.selectList(
                        Wrappers.<FmOrderItem>lambdaQuery().in(FmOrderItem::getOrderId, orderIds))
                .stream().collect(Collectors.groupingBy(FmOrderItem::getOrderId));

        List<OrderListVO> result = new ArrayList<>();
        for (FmOrders order : orders) {
            List<FmOrderItem> items = itemMap.getOrDefault(order.getId(), List.of());
            OrderListVO vo = new OrderListVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setStatus(order.getStatus());
            vo.setStatusDesc(statusDesc(order.getStatus()));
            vo.setPayAmount(order.getPayAmount());
            vo.setItemCount(items.size());
            vo.setTotalQuantity(items.stream()
                    .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum());
            if (!items.isEmpty()) {
                vo.setFirstItemName(items.get(0).getSpuName());
                vo.setFirstItemImage(items.get(0).getImage());
            }
            vo.setReceiverName(order.getReceiverName());
            // 列表页手机号脱敏，详情页才展示完整号码
            vo.setReceiverPhone(MaskUtil.maskPhone(order.getReceiverPhone()));
            vo.setCreateTime(order.getCreateTime());
            vo.setExpireTime(order.getExpireTime());
            result.add(vo);
        }
        return result;
    }

    private OrderDetailVO buildDetail(FmOrders order) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setStatusDesc(statusDesc(order.getStatus()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setFullAddress(joinAddress(order));
        vo.setMemberRemark(order.getMemberRemark());
        vo.setAdminRemark(order.getAdminRemark());
        vo.setExpireTime(order.getExpireTime());
        vo.setPayTime(order.getPayTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setFinishTime(order.getFinishTime());
        vo.setCreateTime(order.getCreateTime());

        for (FmOrderItem item : listItems(order.getId())) {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setSpuId(item.getSpuId());
            itemVO.setSkuId(item.getSkuId());
            itemVO.setSpuName(item.getSpuName());
            itemVO.setSkuName(item.getSkuName());
            itemVO.setSkuSnapshot(item.getSkuSnapshot());
            itemVO.setImage(item.getImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setAmount(item.getAmount());
            itemVO.setAfterSaleQuantity(item.getAfterSaleQuantity());
            itemVO.setReviewed(item.getReviewed());
            vo.getItems().add(itemVO);
        }
        for (FmOrderStatusLog statusLog : orderStatusLogMapper.selectList(
                Wrappers.<FmOrderStatusLog>lambdaQuery()
                        .eq(FmOrderStatusLog::getOrderId, order.getId())
                        .orderByAsc(FmOrderStatusLog::getId))) {
            OrderStatusLogVO logVO = new OrderStatusLogVO();
            logVO.setAction(statusLog.getAction());
            OrderActionEnum actionEnum = OrderActionEnum.of(statusLog.getAction());
            logVO.setActionDesc(actionEnum == null ? statusLog.getAction() : actionEnum.getDesc());
            logVO.setFromStatus(statusLog.getFromStatus());
            logVO.setToStatus(statusLog.getToStatus());
            logVO.setOperatorType(statusLog.getOperatorType());
            logVO.setOperatorName(statusLog.getOperatorName());
            logVO.setRemark(statusLog.getRemark());
            logVO.setCreateTime(statusLog.getCreateTime());
            vo.getStatusLogs().add(logVO);
        }
        return vo;
    }

    private List<FmOrderItem> listItems(Long orderId) {
        return orderItemMapper.selectList(Wrappers.<FmOrderItem>lambdaQuery()
                .eq(FmOrderItem::getOrderId, orderId)
                .orderByAsc(FmOrderItem::getId));
    }

    /** 取当前会员的订单，非本人订单抛 404 */
    private FmOrders requireMine(Long orderId) {
        Long memberId = UserContext.getRequiredMemberId();
        FmOrders order = this.getById(orderId);
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private FmOrders requireOrder(Long orderId) {
        FmOrders order = this.getById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private String statusDesc(Integer status) {
        OrderStatusEnum statusEnum = OrderStatusEnum.of(status);
        return statusEnum == null ? String.valueOf(status) : statusEnum.getDesc();
    }

    private String joinAddress(FmOrders order) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, order.getReceiverProvince());
        appendPart(builder, order.getReceiverCity());
        appendPart(builder, order.getReceiverDistrict());
        appendPart(builder, order.getReceiverAddress());
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (StringUtils.hasText(part)) {
            builder.append(part);
        }
    }

    private BigDecimal multiply(BigDecimal price, Integer quantity) {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /** 埋点注册到事务提交之后执行 */
    private void reportOrderBehaviorAfterCommit(List<CartItemVO> cartItems) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reportOrderBehavior(cartItems);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reportOrderBehavior(cartItems);
            }
        });
    }

    private void reportOrderBehavior(List<CartItemVO> cartItems) {
        for (CartItemVO cartItem : cartItems) {
            try {
                BehaviorReportDTO dto = new BehaviorReportDTO();
                dto.setBehavior(BehaviorTypeEnum.ORDER.getCode());
                dto.setTargetType(BehaviorTargetTypeEnum.SPU.getCode());
                dto.setTargetId(cartItem.getSpuId());
                behaviorService.report(dto);
            } catch (Exception e) {
                // 埋点失败不影响下单，只记日志
                log.warn("下单行为埋点失败：spuId={}", cartItem.getSpuId(), e);
            }
        }
    }
}
