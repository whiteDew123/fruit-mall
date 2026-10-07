package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 提交订单结果。 */
@Data
@Builder
@Schema(description = "提交订单结果")
public class OrderSubmitVO {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "应付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;
}
