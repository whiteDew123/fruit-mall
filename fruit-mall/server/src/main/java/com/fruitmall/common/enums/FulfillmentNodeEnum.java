package com.fruitmall.common.enums;

import lombok.Getter;

/** 履约轨迹节点，取值与 fm_fulfillment_trace.node 一致。 */
@Getter
public enum FulfillmentNodeEnum {

    /** 分拣 */
    PICK(10, "分拣"),
    /** 打包 */
    PACK(20, "打包"),
    /** 出库配送 */
    DELIVER(30, "出库配送"),
    /** 送达 */
    ARRIVED(40, "送达"),
    /** 签收 */
    SIGN(50, "签收"),
    /** 异常登记 */
    EXCEPTION(90, "异常");

    private final Integer code;
    private final String desc;

    FulfillmentNodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
