package com.fruitmall.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 购物车汇总。只统计有效且选中的项。 */
@Data
@Schema(description = "购物车汇总")
public class CartSummaryVO {

    @Schema(description = "购物车项总数（含失效）")
    private Integer itemCount = 0;

    @Schema(description = "选中且有效的商品件数")
    private Integer selectedQuantity = 0;

    @Schema(description = "选中商品金额合计")
    private BigDecimal selectedAmount = BigDecimal.ZERO;

    @Schema(description = "是否全选（无有效项时为 false）")
    private Boolean allSelected = false;
}
