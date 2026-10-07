package com.fruitmall.order.state;

import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.order.domain.FmOrderStatusLog;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrderStatusLogMapper;
import com.fruitmall.order.mapper.FmOrdersMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机：订单状态的唯一入口。
 * 任何地方都不允许直接 update 订单状态，必须经过本类；
 * 每次迁移都会校验"当前状态是否允许该动作"，并写入 fm_order_status_log 留痕。
 * 允许的迁移规则见 docs/05-数据库设计.md 第 3.7 节。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStateMachine {

    /** 允许的迁移表：key 为当前状态，value 为可迁移到的目标状态集合 */
    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatusEnum.PENDING_PAY.getCode(),
            Set.of(OrderStatusEnum.PAID.getCode(), OrderStatusEnum.CANCELLED.getCode()),
            OrderStatusEnum.PAID.getCode(),
            Set.of(OrderStatusEnum.PICKING.getCode(), OrderStatusEnum.REFUNDING.getCode()),
            OrderStatusEnum.PICKING.getCode(),
            Set.of(OrderStatusEnum.DELIVERING.getCode()),
            OrderStatusEnum.DELIVERING.getCode(),
            Set.of(OrderStatusEnum.COMPLETED.getCode()),
            OrderStatusEnum.COMPLETED.getCode(),
            Set.of(OrderStatusEnum.REFUNDING.getCode()),
            OrderStatusEnum.REFUNDING.getCode(),
            Set.of(OrderStatusEnum.REFUNDED.getCode()),
            OrderStatusEnum.CANCELLED.getCode(),
            Set.of(),
            OrderStatusEnum.REFUNDED.getCode(),
            Set.of()
    );

    private final FmOrdersMapper ordersMapper;
    private final FmOrderStatusLogMapper statusLogMapper;

    /**
     * 执行状态迁移。
     *
     * @param update       待更新实体，必须带 id；调用方可在其中带上本次要一并写入的字段
     *                     （如 payTime、cancelTime、cancelReason），未设置的字段不会被更新
     * @param target       目标状态
     * @param action       触发动作
     * @param operatorType 操作者类型
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param remark       备注
     * @return 迁移后的订单（状态已更新）
     */
    @Transactional(rollbackFor = Exception.class)
    public FmOrders transition(FmOrders update, OrderStatusEnum target, OrderActionEnum action,
                               OperatorTypeEnum operatorType, Long operatorId,
                               String operatorName, String remark) {
        if (update == null || update.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "订单ID不能为空");
        }
        FmOrders current = ordersMapper.selectById(update.getId());
        if (current == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单不存在");
        }

        Integer fromStatus = current.getStatus();
        assertTransitionAllowed(fromStatus, target);

        update.setStatus(target.getCode());
        ordersMapper.updateById(update);

        FmOrderStatusLog statusLog = new FmOrderStatusLog();
        statusLog.setOrderId(current.getId());
        statusLog.setOrderNo(current.getOrderNo());
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(target.getCode());
        statusLog.setAction(action.getCode());
        statusLog.setOperatorType(operatorType.getCode());
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorName(operatorName);
        statusLog.setRemark(remark);
        statusLog.setCreateTime(LocalDateTime.now());
        statusLogMapper.insert(statusLog);

        log.info("订单状态流转：orderNo={}, {} -> {}, action={}",
                current.getOrderNo(), fromStatus, target.getCode(), action.getCode());
        current.setStatus(target.getCode());
        return current;
    }

    /**
     * 校验状态迁移是否合法，非法迁移抛 409。
     */
    public void assertTransitionAllowed(Integer fromStatus, OrderStatusEnum target) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(target.getCode())) {
            OrderStatusEnum from = OrderStatusEnum.of(fromStatus);
            throw new BizException(ResultCode.CONFLICT,
                    "非法状态流转：" + (from == null ? fromStatus : from.getDesc()) + " → " + target.getDesc());
        }
    }
}
