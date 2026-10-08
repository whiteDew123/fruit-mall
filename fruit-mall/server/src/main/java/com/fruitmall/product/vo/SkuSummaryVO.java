package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 商品维度的规格汇总：最低售价与可售库存合计。 */
@Data
@Schema(description = "规格汇总")
public class SkuSummaryVO {

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "最低售价")
    private BigDecimal minPrice;

    @Schema(description = "可售库存合计")
    private Integer availableStock;
}
