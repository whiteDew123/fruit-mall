package com.fruitmall.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 修改购物车数量入参。 */
@Data
@Schema(description = "修改购物车数量入参")
public class CartQuantityDTO {

    @Schema(description = "数量", example = "3")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;
}
