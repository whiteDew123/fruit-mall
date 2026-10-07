package com.fruitmall.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 支付单出参。 */
@Data
@Schema(description = "支付单")
public class PaymentVO {

    @Schema(description = "支付流水号（幂等键）")
    private String payNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付渠道，本课题固定 MOCK")
    private String channel;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "状态说明")
    private String statusDesc;

    @Schema(description = "发起支付时间")
    private LocalDateTime payTime;

    @Schema(description = "回调时间")
    private LocalDateTime callbackTime;

    @Schema(description = "关闭时间")
    private LocalDateTime closeTime;

    @Schema(description = "失败原因")
    private String failReason;
}
