package com.fruitmall.order.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表。
 * 收货信息为下单时的快照，会员之后修改地址不影响历史订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_orders")
public class FmOrders extends BaseEntity {

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 状态，见 OrderStatusEnum */
    private Integer status;

    /** 商品金额合计 */
    private BigDecimal totalAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 应付金额 */
    private BigDecimal payAmount;

    /** 收货人（快照） */
    private String receiverName;

    /** 收货电话（快照） */
    private String receiverPhone;

    /** 省（快照） */
    private String receiverProvince;

    /** 市（快照） */
    private String receiverCity;

    /** 区县（快照） */
    private String receiverDistrict;

    /** 详细地址（快照） */
    private String receiverAddress;

    /** 会员备注 */
    private String memberRemark;

    /** 商家备注 */
    private String adminRemark;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 取消原因 */
    private String cancelReason;

    /** 完成时间 */
    private LocalDateTime finishTime;

    /** 乐观锁版本 */
    private Integer version;
}
