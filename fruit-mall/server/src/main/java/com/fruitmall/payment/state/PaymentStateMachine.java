package com.fruitmall.payment.state;

import com.fruitmall.common.enums.PayStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.payment.domain.FmPaymentRecord;
import com.fruitmall.payment.mapper.FmPaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * 支付状态机：支付单状态的唯一入口。
 * 允许的迁移：
 * 已创建 10 → 支付中 20 → 支付成功 30 / 支付失败 40；
 * 已创建 10 或支付中 20 → 已关闭 50（订单取消、超时关单）；
 * 支付成功 30 → 退款中 60 → 已退款 70。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStateMachine {

    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            PayStatusEnum.INIT.getCode(),
            Set.of(PayStatusEnum.PAYING.getCode(), PayStatusEnum.SUCCESS.getCode(),
                    PayStatusEnum.FAILED.getCode(), PayStatusEnum.CLOSED.getCode()),
            PayStatusEnum.PAYING.getCode(),
            Set.of(PayStatusEnum.SUCCESS.getCode(), PayStatusEnum.FAILED.getCode(),
                    PayStatusEnum.CLOSED.getCode()),
            PayStatusEnum.SUCCESS.getCode(),
            Set.of(PayStatusEnum.REFUNDING.getCode()),
            PayStatusEnum.FAILED.getCode(),
            Set.of(PayStatusEnum.PAYING.getCode(), PayStatusEnum.SUCCESS.getCode(),
                    PayStatusEnum.CLOSED.getCode()),
            PayStatusEnum.REFUNDING.getCode(),
            Set.of(PayStatusEnum.REFUNDED.getCode()),
            PayStatusEnum.CLOSED.getCode(),
            Set.of(),
            PayStatusEnum.REFUNDED.getCode(),
            Set.of()
    );

    private final FmPaymentRecordMapper paymentRecordMapper;

    /**
     * 执行支付单状态迁移。
     *
     * @param update 待更新实体，必须带 id；可带上本次要一并写入的字段（payTime、callbackTime、failReason 等）
     * @param target 目标状态
     * @return 迁移后的支付单
     */
    @Transactional(rollbackFor = Exception.class)
    public FmPaymentRecord transition(FmPaymentRecord update, PayStatusEnum target) {
        if (update == null || update.getId() == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "支付记录ID不能为空");
        }
        FmPaymentRecord current = paymentRecordMapper.selectById(update.getId());
        if (current == null) {
            throw new BizException(ResultCode.NOT_FOUND, "支付记录不存在");
        }
        assertTransitionAllowed(current.getStatus(), target);

        update.setStatus(target.getCode());
        paymentRecordMapper.updateById(update);

        log.info("支付状态流转：payNo={}, {} -> {}",
                current.getPayNo(), current.getStatus(), target.getCode());
        current.setStatus(target.getCode());
        return current;
    }

    /** 校验支付状态迁移是否合法，非法迁移抛 409 */
    public void assertTransitionAllowed(Integer fromStatus, PayStatusEnum target) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowed == null || !allowed.contains(target.getCode())) {
            PayStatusEnum from = null;
            for (PayStatusEnum status : PayStatusEnum.values()) {
                if (status.getCode().equals(fromStatus)) {
                    from = status;
                }
            }
            throw new BizException(ResultCode.CONFLICT,
                    "非法支付状态流转：" + (from == null ? fromStatus : from.getDesc()) + " → " + target.getDesc());
        }
    }
}
