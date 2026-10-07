package com.fruitmall.fulfillment.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 履约单。一个订单一张履约单（uk_order_id）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_fulfillment_order")
public class FmFulfillmentOrder extends BaseEntity {

    /** 履约单号 */
    private String fulfillmentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 状态，见 FulfillmentStatusEnum */
    private Integer status;

    /** 收货人（快照） */
    private String receiverName;

    /** 收货电话（快照） */
    private String receiverPhone;

    /** 收货地址（快照） */
    private String receiverAddress;

    /** 备注 */
    private String remark;

    /** 分拣完成时间 */
    private LocalDateTime pickTime;

    /** 发货时间 */
    private LocalDateTime deliveryTime;

    /** 送达时间 */
    private LocalDateTime arriveTime;

    /** 签收时间 */
    private LocalDateTime signTime;

    /** 乐观锁版本 */
    private Integer version;
}
