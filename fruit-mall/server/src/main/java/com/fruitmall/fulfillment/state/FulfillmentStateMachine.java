package com.fruitmall.fulfillment.state;

import com.fruitmall.common.enums.FulfillmentNodeEnum;
import com.fruitmall.common.enums.FulfillmentStatusEnum;
import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.fulfillment.domain.FmFulfillmentOrder;
import com.fruitmall.fulfillment.domain.FmFulfillmentTrace;
import com.fruitmall.fulfillment.mapper.FmFulfillmentOrderMapper;
import com.fruitmall.fulfillment.mapper.FmFulfillmentTraceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 履约状态机：履约状态的唯一入口。
 * 允许的迁移：待分拣 → 分拣完成 → 待配送 → 配送中 → 已送达 → 已签收，不得跳级；
 * 任意非终态可转异常，异常修复后回到正常节点。
 * 每次迁移都会追加一条履约轨迹，形成前台可见的时间轴。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentStateMachine {

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            FulfillmentStatusEnum.PENDING_PICK.getCode(),
            Set.of(FulfillmentStatusEnum.PICKED.getCode(), FulfillmentStatusEnum.EXCEPTION.getCode()),
            FulfillmentStatusEnum.PICKED.getCode(),
            Set.of(FulfillmentStatusEnum.PENDING_DELIVERY.getCode(),
                    FulfillmentStatusEnum.EXCEPTION.getCode()),
            FulfillmentStatusEnum.PENDING_DELIVERY.getCode(),
            Set.of(FulfillmentStatusEnum.DELIVERING.getCode(), FulfillmentStatusEnum.EXCEPTION.getCode()),
            FulfillmentStatusEnum.DELIVERING.getCode(),
            Set.of(FulfillmentStatusEnum.ARRIVED.getCode(), FulfillmentStatusEnum.EXCEPTION.getCode()),
            FulfillmentStatusEnum.ARRIVED.getCode(),
            Set.of(FulfillmentStatusEnum.SIGNED.getCode(), FulfillmentStatusEnum.EXCEPTION.getCode()),
            FulfillmentStatusEnum.SIGNED.getCode(),
            Set.of(),
            FulfillmentStatusEnum.EXCEPTION.getCode(),
            Set.of(FulfillmentStatusEnum.PENDING_PICK.getCode(), FulfillmentStatusEnum.PICKED.getCode(),
                    FulfillmentStatusEnum.PENDING_DELIVERY.getCode(), FulfillmentStatusEnum.DELIVERING.getCode(),
                    FulfillmentStatusEnum.ARRIVED.getCode())
    );

    private final FmFulfillmentOrderMapper fulfillmentOrderMapper;
    private final FmFulfillmentTraceMapper fulfillmentTraceMapper;

    /**
     * 执行履约状态迁移并写入轨迹。
     *
     * @param update       待更新实体，必须带 id；可带上本次要一并写入的字段（pickTime、deliveryTime 等）
     * @param target       目标状态
     * @param node         本次对应的轨迹节点
     * @param operatorType 操作者类型
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     * @param remark       备注或异常原因
     * @param images       图片地址（JSON 数组字符串）
     */
    @Transactional(rollbackFor = Exception.class)
    public FmFulfillmentOrder transition(FmFulfillmentOrder update, FulfillmentStatusEnum target,
                                         FulfillmentNodeEnum node, OperatorTypeEnum operatorType,
                                         Long operatorId, String operatorName,
                                         String remark, String images) {
        if (update == null || update.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "履约单ID不能为空");
        }
        FmFulfillmentOrder current = fulfillmentOrderMapper.selectById(update.getId());
        if (current == null) {
            throw new BizException(ResultCode.NOT_FOUND, "履约单不存在");
        }
        assertTransitionAllowed(current.getStatus(), target);

        update.setStatus(target.getCode());
        fulfillmentOrderMapper.updateById(update);

        FmFulfillmentTrace trace = new FmFulfillmentTrace();
        trace.setFulfillmentId(current.getId());
        trace.setFulfillmentNo(current.getFulfillmentNo());
        trace.setNode(node.getCode());
        trace.setNodeName(node.getDesc());
        trace.setOperatorType(operatorType.getCode());
        trace.setOperatorId(operatorId);
        trace.setOperatorName(operatorName);
        trace.setRemark(remark);
        trace.setImages(images);
        trace.setCreateTime(LocalDateTime.now());
        fulfillmentTraceMapper.insert(trace);

        log.info("履约状态流转：fulfillmentNo={}, {} -> {}, node={}",
                current.getFulfillmentNo(), current.getStatus(), target.getCode(), node.getDesc());
        current.setStatus(target.getCode());
        return current;
    }

    /** 校验履约状态迁移是否合法，非法迁移抛 409 */
    public void assertTransitionAllowed(Integer fromStatus, FulfillmentStatusEnum target) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(target.getCode())) {
            FulfillmentStatusEnum from = FulfillmentStatusEnum.of(fromStatus);
            throw new BizException(ResultCode.CONFLICT,
                    "非法履约状态流转：" + (from == null ? fromStatus : from.getDesc())
                            + " → " + target.getDesc());
        }
    }
}
