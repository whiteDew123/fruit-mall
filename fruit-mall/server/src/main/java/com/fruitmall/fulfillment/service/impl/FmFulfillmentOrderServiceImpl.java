package com.fruitmall.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.enums.FulfillmentNodeEnum;
import com.fruitmall.common.enums.FulfillmentStatusEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.BizNoUtil;
import com.fruitmall.common.util.MaskUtil;
import com.fruitmall.fulfillment.domain.FmFulfillmentOrder;
import com.fruitmall.fulfillment.domain.FmFulfillmentTrace;
import com.fruitmall.fulfillment.mapper.FmFulfillmentOrderMapper;
import com.fruitmall.fulfillment.mapper.FmFulfillmentTraceMapper;
import com.fruitmall.fulfillment.query.FulfillmentQuery;
import com.fruitmall.fulfillment.service.IFmFulfillmentOrderService;
import com.fruitmall.fulfillment.state.FulfillmentStateMachine;
import com.fruitmall.fulfillment.vo.FulfillmentListVO;
import com.fruitmall.fulfillment.vo.FulfillmentTraceVO;
import com.fruitmall.fulfillment.vo.FulfillmentVO;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrdersMapper;
import com.fruitmall.order.state.OrderStateMachine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 履约服务实现。
 * 履约推进与订单状态联动：每次推进都会同步调用 OrderStateMachine，
 * 两个状态机在同一个事务内，任一校验失败则整体回滚，不会出现"履约走了、订单没动"的中间状态。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmFulfillmentOrderServiceImpl extends ServiceImpl<FmFulfillmentOrderMapper, FmFulfillmentOrder>
        implements IFmFulfillmentOrderService {

    private final FmFulfillmentTraceMapper traceMapper;
    private final FmOrdersMapper ordersMapper;
    private final FulfillmentStateMachine fulfillmentStateMachine;
    private final OrderStateMachine orderStateMachine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createForOrder(FmOrders order) {
        if (getByOrderId(order.getId()) != null) {
            // 幂等：重复回调不会产生第二张履约单
            return;
        }
        FmFulfillmentOrder fulfillment = new FmFulfillmentOrder();
        fulfillment.setFulfillmentNo(BizNoUtil.generate(BizNoUtil.FULFILLMENT_PREFIX));
        fulfillment.setOrderId(order.getId());
        fulfillment.setOrderNo(order.getOrderNo());
        fulfillment.setMemberId(order.getMemberId());
        fulfillment.setStatus(FulfillmentStatusEnum.PENDING_PICK.getCode());
        fulfillment.setReceiverName(order.getReceiverName());
        fulfillment.setReceiverPhone(order.getReceiverPhone());
        fulfillment.setReceiverAddress(buildAddress(order));
        fulfillment.setVersion(0);
        this.save(fulfillment);

        // 时间轴的第一个节点
        FmFulfillmentTrace trace = new FmFulfillmentTrace();
        trace.setFulfillmentId(fulfillment.getId());
        trace.setFulfillmentNo(fulfillment.getFulfillmentNo());
        trace.setNode(FulfillmentNodeEnum.PICK.getCode());
        trace.setNodeName("待分拣");
        trace.setOperatorType(OperatorTypeEnum.SYSTEM.getCode());
        trace.setOperatorName("系统");
        trace.setRemark("订单已支付，生成履约单，等待分拣");
        trace.setCreateTime(LocalDateTime.now());
        traceMapper.insert(trace);

        log.info("生成履约单：orderNo={}, fulfillmentNo={}",
                order.getOrderNo(), fulfillment.getFulfillmentNo());
    }

    @Override
    public FulfillmentVO getByOrderIdForMember(Long orderId) {
        Long memberId = UserContext.getRequiredMemberId();
        FmOrders order = ordersMapper.selectById(orderId);
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        FmFulfillmentOrder fulfillment = getByOrderId(orderId);
        if (fulfillment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "该订单还没有履约信息");
        }
        return buildVO(fulfillment);
    }

    @Override
    public PageResult<FulfillmentListVO> pageAll(FulfillmentQuery query) {
        LambdaQueryWrapper<FmFulfillmentOrder> wrapper = Wrappers.<FmFulfillmentOrder>lambdaQuery()
                .eq(query.getStatus() != null, FmFulfillmentOrder::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getOrderNo()), FmFulfillmentOrder::getOrderNo, query.getOrderNo())
                .orderByAsc(FmFulfillmentOrder::getStatus)
                .orderByDesc(FmFulfillmentOrder::getId);
        Page<FmFulfillmentOrder> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<FulfillmentListVO> list = new ArrayList<>();
        for (FmFulfillmentOrder fulfillment : page.getRecords()) {
            FulfillmentListVO vo = new FulfillmentListVO();
            vo.setId(fulfillment.getId());
            vo.setFulfillmentNo(fulfillment.getFulfillmentNo());
            vo.setOrderNo(fulfillment.getOrderNo());
            vo.setStatus(fulfillment.getStatus());
            vo.setStatusDesc(statusDesc(fulfillment.getStatus()));
            vo.setReceiverName(fulfillment.getReceiverName());
            // 列表页手机号脱敏
            vo.setReceiverPhone(MaskUtil.maskPhone(fulfillment.getReceiverPhone()));
            vo.setReceiverAddress(fulfillment.getReceiverAddress());
            vo.setCreateTime(fulfillment.getCreateTime());
            list.add(vo);
        }
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }

    @Override
    public FulfillmentVO detailForAdmin(Long id) {
        return buildVO(requireFulfillment(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pick(Long id, String remark, String images) {
        FmFulfillmentOrder fulfillment = requireFulfillment(id);
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        update.setPickTime(LocalDateTime.now());
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.PICKED,
                FulfillmentNodeEnum.PICK, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                StringUtils.hasText(remark) ? remark : "分拣完成", images);
        syncOrderStatus(fulfillment.getOrderId(), OrderStatusEnum.PICKING, OrderActionEnum.PICK, "开始备货");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void ready(Long id, String remark, String images) {
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.PENDING_DELIVERY,
                FulfillmentNodeEnum.PACK, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                StringUtils.hasText(remark) ? remark : "打包完成，等待配送", images);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deliver(Long id, String remark, String images) {
        FmFulfillmentOrder fulfillment = requireFulfillment(id);
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        update.setDeliveryTime(LocalDateTime.now());
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.DELIVERING,
                FulfillmentNodeEnum.DELIVER, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                StringUtils.hasText(remark) ? remark : "已出库配送", images);
        syncOrderStatus(fulfillment.getOrderId(), OrderStatusEnum.DELIVERING, OrderActionEnum.DELIVER, "商品已发货");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void arrive(Long id, String remark, String images) {
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        update.setArriveTime(LocalDateTime.now());
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.ARRIVED,
                FulfillmentNodeEnum.ARRIVED, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                StringUtils.hasText(remark) ? remark : "已送达", images);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void sign(Long id, String remark, String images) {
        FmFulfillmentOrder fulfillment = requireFulfillment(id);
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        update.setSignTime(LocalDateTime.now());
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.SIGNED,
                FulfillmentNodeEnum.SIGN, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                StringUtils.hasText(remark) ? remark : "已签收", images);
        syncOrderStatus(fulfillment.getOrderId(), OrderStatusEnum.COMPLETED, OrderActionEnum.SIGN, "订单已完成");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void exception(Long id, String remark, String images) {
        if (!StringUtils.hasText(remark)) {
            throw new BizException(ResultCode.BAD_REQUEST, "异常登记必须填写原因");
        }
        FmFulfillmentOrder update = new FmFulfillmentOrder();
        update.setId(id);
        fulfillmentStateMachine.transition(update, FulfillmentStatusEnum.EXCEPTION,
                FulfillmentNodeEnum.EXCEPTION, OperatorTypeEnum.MERCHANT, operatorId(), operatorName(),
                remark, images);
    }

    /** 联动订单状态，与履约状态在同一事务内 */
    private void syncOrderStatus(Long orderId, OrderStatusEnum target, OrderActionEnum action, String remark) {
        FmOrders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }
        FmOrders update = new FmOrders();
        update.setId(orderId);
        if (OrderStatusEnum.COMPLETED == target) {
            update.setFinishTime(LocalDateTime.now());
        }
        orderStateMachine.transition(update, target, action, OperatorTypeEnum.MERCHANT,
                operatorId(), operatorName(), remark);
    }

    private FmFulfillmentOrder getByOrderId(Long orderId) {
        return this.getOne(Wrappers.<FmFulfillmentOrder>lambdaQuery()
                .eq(FmFulfillmentOrder::getOrderId, orderId));
    }

    private FmFulfillmentOrder requireFulfillment(Long id) {
        FmFulfillmentOrder fulfillment = this.getById(id);
        if (fulfillment == null) {
            throw new BizException(ResultCode.NOT_FOUND, "履约单不存在");
        }
        return fulfillment;
    }

    private Long operatorId() {
        return UserContext.getRequired().getUserId();
    }

    private String operatorName() {
        return UserContext.getRequired().getUsername();
    }

    private FulfillmentVO buildVO(FmFulfillmentOrder fulfillment) {
        FulfillmentVO vo = new FulfillmentVO();
        vo.setId(fulfillment.getId());
        vo.setFulfillmentNo(fulfillment.getFulfillmentNo());
        vo.setOrderId(fulfillment.getOrderId());
        vo.setOrderNo(fulfillment.getOrderNo());
        vo.setStatus(fulfillment.getStatus());
        vo.setStatusDesc(statusDesc(fulfillment.getStatus()));
        vo.setReceiverName(fulfillment.getReceiverName());
        vo.setReceiverPhone(fulfillment.getReceiverPhone());
        vo.setReceiverAddress(fulfillment.getReceiverAddress());
        vo.setRemark(fulfillment.getRemark());
        vo.setPickTime(fulfillment.getPickTime());
        vo.setDeliveryTime(fulfillment.getDeliveryTime());
        vo.setArriveTime(fulfillment.getArriveTime());
        vo.setSignTime(fulfillment.getSignTime());
        vo.setCreateTime(fulfillment.getCreateTime());

        for (FmFulfillmentTrace trace : traceMapper.selectList(Wrappers.<FmFulfillmentTrace>lambdaQuery()
                .eq(FmFulfillmentTrace::getFulfillmentId, fulfillment.getId())
                .orderByAsc(FmFulfillmentTrace::getId))) {
            FulfillmentTraceVO traceVO = new FulfillmentTraceVO();
            traceVO.setNode(trace.getNode());
            traceVO.setNodeName(trace.getNodeName());
            traceVO.setOperatorType(trace.getOperatorType());
            traceVO.setOperatorName(trace.getOperatorName());
            traceVO.setRemark(trace.getRemark());
            traceVO.setImages(trace.getImages());
            traceVO.setCreateTime(trace.getCreateTime());
            vo.getTraces().add(traceVO);
        }
        return vo;
    }

    private String statusDesc(Integer status) {
        FulfillmentStatusEnum statusEnum = FulfillmentStatusEnum.of(status);
        return statusEnum == null ? String.valueOf(status) : statusEnum.getDesc();
    }

    private String buildAddress(FmOrders order) {
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
}
