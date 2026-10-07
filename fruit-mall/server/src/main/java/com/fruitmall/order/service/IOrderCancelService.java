package com.fruitmall.order.service;

import com.fruitmall.common.enums.OperatorTypeEnum;
import com.fruitmall.common.enums.OrderActionEnum;
import com.fruitmall.order.domain.FmOrders;

/**
 * 订单关单服务：会员取消、后台取消、支付超时、定时关单四条路径共用同一套逻辑。
 * 抽成独立服务是为了让"释放预占库存 + 状态流转 + 关闭未完成支付单"只有一处实现。
 */
public interface IOrderCancelService {

    /**
     * 关闭待支付订单
     *
     * @param order        订单（状态必须为待支付）
     * @param action       触发动作：CANCEL 用户取消 / TIMEOUT 超时关单
     * @param operatorType 操作者类型
     * @param operatorId   操作人ID，系统触发时为 null
     * @param operatorName 操作人名称
     * @param reason       关单原因
     */
    void close(FmOrders order, OrderActionEnum action, OperatorTypeEnum operatorType,
               Long operatorId, String operatorName, String reason);
}
