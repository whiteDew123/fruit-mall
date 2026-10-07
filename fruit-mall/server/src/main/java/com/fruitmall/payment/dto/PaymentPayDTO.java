package com.fruitmall.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 模拟支付入参。 */
@Data
@Schema(description = "模拟支付入参")
public class PaymentPayDTO {

    @Schema(description = "订单ID", example = "1")
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "模拟结果：SUCCESS 支付成功 / FAIL 支付失败 / TIMEOUT 支付超时",
            example = "SUCCESS")
    @NotBlank(message = "请选择模拟支付结果")
    private String result;
}
