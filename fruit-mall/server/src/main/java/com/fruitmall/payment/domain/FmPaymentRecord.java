package com.fruitmall.payment.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录。一个订单只有一条支付记录（uk_order_id），
 * 支付失败后复用同一条记录重新发起，因此 pay_no 唯一索引天然保证回调幂等。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_payment_record")
public class FmPaymentRecord extends BaseEntity {

    /** 支付流水号（幂等键） */
    private String payNo;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 支付渠道，本课题固定 MOCK */
    private String channel;

    /** 支付金额 */
    private BigDecimal amount;

    /** 状态，见 PayStatusEnum */
    private Integer status;

    /** 发起支付时间 */
    private LocalDateTime payTime;

    /** 回调时间 */
    private LocalDateTime callbackTime;

    /** 关闭时间 */
    private LocalDateTime closeTime;

    /** 失败原因 */
    private String failReason;

    /** 乐观锁版本 */
    private Integer version;
}
