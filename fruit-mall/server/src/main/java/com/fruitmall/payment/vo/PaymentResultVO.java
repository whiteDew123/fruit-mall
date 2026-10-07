package com.fruitmall.payment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 模拟支付结果，供支付结果页展示。 */
@Data
@Builder
@Schema(description = "模拟支付结果")
public class PaymentResultVO {

    @Schema(description = "本次支付是否成功")
    private Boolean success;

    @Schema(description = "结果说明")
    private String message;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付单号")
    private String payNo;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "订单状态编码")
    private Integer orderStatus;

    @Schema(description = "订单状态说明")
    private String orderStatusDesc;

    @Schema(description = "支付单状态说明")
    private String payStatusDesc;
}
