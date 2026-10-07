package com.fruitmall.common.enums;

import lombok.Getter;

/** 订单状态，取值与 fm_orders.status 一致。迁移规则见 docs/05-数据库设计.md 第 3.7 节。 */
@Getter
public enum OrderStatusEnum {

    /** 待支付：下单成功，库存已预占 */
    PENDING_PAY(10, "待支付"),
    /** 已支付：待商家备货 */
    PAID(20, "已支付"),
    /** 备货中：分拣进行中，库存已实扣 */
    PICKING(30, "备货中"),
    /** 配送中 */
    DELIVERING(40, "配送中"),
    /** 已完成：已签收，可评价、可发起售后 */
    COMPLETED(50, "已完成"),
    /** 已取消：用户取消或超时关单，库存已释放 */
    CANCELLED(60, "已取消"),
    /** 退款中：售后审核通过，退款尚未完成 */
    REFUNDING(70, "退款中"),
    /** 已退款：退款完成的终态 */
    REFUNDED(80, "已退款");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按编码匹配，未匹配返回 null */
    public static OrderStatusEnum of(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
