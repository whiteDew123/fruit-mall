package com.fruitmall.order.task;

import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.service.IFmOrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 待支付订单超时关单定时任务。
 * 每 ${fruit-mall.order.timeout-scan-interval:60000} 毫秒扫描一次，
 * 把超过 expire_time 仍未支付的订单关闭并释放预占库存。
 * 单笔失败只记日志，不影响同批其他订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    /** 单批最多处理的订单数，避免一次扫描过多拖慢数据库 */
    private static final int BATCH_SIZE = 100;

    private final IFmOrdersService fmOrdersService;

    @Scheduled(fixedDelayString = "${fruit-mall.order.timeout-scan-interval:60000}",
            initialDelayString = "${fruit-mall.order.timeout-scan-initial-delay:30000}")
    public void closeExpiredOrders() {
        List<FmOrders> expiredOrders = fmOrdersService.listExpiredOrders(BATCH_SIZE);
        if (expiredOrders.isEmpty()) {
            return;
        }
        log.info("超时关单任务：发现 {} 笔待支付超时订单", expiredOrders.size());
        int closed = 0;
        for (FmOrders order : expiredOrders) {
            try {
                fmOrdersService.closeExpiredOrder(order.getId());
                closed++;
            } catch (Exception e) {
                log.error("超时关单失败：orderNo={}", order.getOrderNo(), e);
            }
        }
        log.info("超时关单任务：本次关闭 {} 笔", closed);
    }
}
