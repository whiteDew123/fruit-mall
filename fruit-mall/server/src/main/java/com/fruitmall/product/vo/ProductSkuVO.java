package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 商品规格出参。 */
@Data
@Schema(description = "商品规格")
public class ProductSkuVO {

    @Schema(description = "规格ID")
    private Long id;

    @Schema(description = "规格编码")
    private String skuCode;

    @Schema(description = "规格名")
    private String specName;

    @Schema(description = "规格键值 JSON")
    private String specJson;

    @Schema(description = "规格图片")
    private String image;

    @Schema(description = "划线价")
    private BigDecimal originalPrice;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "可售库存总量")
    private Integer stock;

    @Schema(description = "预占库存")
    private Integer lockedStock;

    @Schema(description = "可售数量 = 库存 - 预占")
    private Integer availableStock;

    @Schema(description = "状态：10 启用 / 20 停用")
    private Integer status;
}
