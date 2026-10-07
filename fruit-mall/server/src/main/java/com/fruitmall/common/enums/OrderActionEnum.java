package com.fruitmall.common.enums;

import lombok.Getter;

/** 订单状态流转动作，写入 fm_order_status_log.action。 */
@Getter
public enum OrderActionEnum {

    /** 提交订单：订单创建时的初始节点 */
    CREATE("CREATE", "提交订单"),
    /** 支付成功 */
    PAY("PAY", "支付成功"),
    /** 用户主动取消 */
    CANCEL("CANCEL", "取消订单"),
    /** 系统超时关单 */
    TIMEOUT("TIMEOUT", "超时关单"),
    /** 开始备货 */
    PICK("PICK", "开始备货"),
    /** 发货 */
    DELIVER("DELIVER", "发货配送"),
    /** 签收 */
    SIGN("SIGN", "确认签收"),
    /** 进入退款流程 */
    REFUND("REFUND", "进入退款"),
    /** 退款完成 */
    REFUND_DONE("REFUND_DONE", "退款完成");

    private final String code;
    private final String desc;

    OrderActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 按编码匹配，未匹配返回 null */
    public static OrderActionEnum of(String code) {
        for (OrderActionEnum action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        return null;
    }
}
