package com.fruitmall.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 加入购物车入参。 */
@Data
@Schema(description = "加入购物车入参")
public class CartAddDTO {

    @Schema(description = "规格ID", example = "1")
    @NotNull(message = "规格ID不能为空")
    private Long skuId;

    @Schema(description = "数量", example = "2")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity = 1;
}
