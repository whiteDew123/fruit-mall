package com.fruitmall.aftersale.state;

import com.fruitmall.aftersale.domain.FmAfterSale;
import com.fruitmall.aftersale.mapper.FmAfterSaleMapper;
import com.fruitmall.common.enums.AfterSaleActionEnum;
import com.fruitmall.common.enums.AfterSaleStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * 售后状态机：售后状态的唯一入口。
 * 允许的迁移：
 * 待审核 10 → 已同意 20 / 已驳回 30 / 已取消 70；
 * 已同意 20 → 退货中 40（退货退款）或 退款中 50（仅退款）；
 * 退货中 40 → 退款中 50（商家收到退货）；
 * 退款中 50 → 已完成 60；
 * 已驳回 30、已完成 60、已取消 70 为终态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AfterSaleStateMachine {

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            AfterSaleStatusEnum.PENDING_AUDIT.getCode(),
            Set.of(AfterSaleStatusEnum.APPROVED.getCode(),
                    AfterSaleStatusEnum.REJECTED.getCode(),
                    AfterSaleStatusEnum.CANCELLED.getCode()),
            AfterSaleStatusEnum.APPROVED.getCode(),
            Set.of(AfterSaleStatusEnum.RETURNING.getCode(), AfterSaleStatusEnum.REFUNDING.getCode()),
            AfterSaleStatusEnum.RETURNING.getCode(),
            Set.of(AfterSaleStatusEnum.REFUNDING.getCode()),
            AfterSaleStatusEnum.REFUNDING.getCode(),
            Set.of(AfterSaleStatusEnum.COMPLETED.getCode()),
            AfterSaleStatusEnum.REJECTED.getCode(),
            Set.of(),
            AfterSaleStatusEnum.COMPLETED.getCode(),
            Set.of(),
            AfterSaleStatusEnum.CANCELLED.getCode(),
            Set.of()
    );

    private final FmAfterSaleMapper afterSaleMapper;

    /**
     * 执行售后状态迁移。
     *
     * @param update       待更新实体，必须带 id；可带上本次要一并写入的字段（auditTime、auditRemark 等）
     * @param target       目标状态
     * @param action       触发动作
     * @param operatorType 操作者类型
     * @param operatorId   操作人ID
     * @param operatorName 操作人名称
     */
    @Transactional(rollbackFor = Exception.class)
    public FmAfterSale transition(FmAfterSale update, AfterSaleStatusEnum target, AfterSaleActionEnum action,
                                  Integer operatorType, Long operatorId, String operatorName) {
        if (update == null || update.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "售后单ID不能为空");
        }
        FmAfterSale current = afterSaleMapper.selectById(update.getId());
        if (current == null) {
            throw new BizException(ResultCode.NOT_FOUND, "售后单不存在");
        }
        assertTransitionAllowed(current.getStatus(), target);

        update.setStatus(target.getCode());
        afterSaleMapper.updateById(update);

        log.info("售后状态流转：afterSaleNo={}, {} -> {}, action={}, operator={}",
                current.getAfterSaleNo(), current.getStatus(), target.getCode(),
                action.getCode(), operatorName);
        current.setStatus(target.getCode());
        return current;
    }

    /** 校验售后状态迁移是否合法，非法迁移抛 409 */
    public void assertTransitionAllowed(Integer fromStatus, AfterSaleStatusEnum target) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(target.getCode())) {
            AfterSaleStatusEnum from = AfterSaleStatusEnum.of(fromStatus);
            throw new BizException(ResultCode.CONFLICT,
                    "非法售后状态流转：" + (from == null ? fromStatus : from.getDesc())
                            + " → " + target.getDesc());
        }
    }
}
