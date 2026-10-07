package com.fruitmall.aftersale.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 退款记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_refund_record")
public class FmRefundRecord extends BaseEntity {

    /** 退款流水号 */
    private String refundNo;

    /** 售后单ID */
    private Long afterSaleId;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 状态，见 RefundStatusEnum */
    private Integer status;

    /** 退款渠道，本课题固定 MOCK */
    private String channel;

    /** 退款完成时间 */
    private LocalDateTime refundTime;

    /** 失败原因 */
    private String failReason;

    /** 操作人ID */
    private Long operatorId;
}
