package com.fruitmall.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 购物车项出参。 */
@Data
@Schema(description = "购物车项")
public class CartItemVO {

    @Schema(description = "购物车项ID")
    private Long id;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "是否选中：0 否 / 1 是")
    private Integer selected;

    @Schema(description = "状态：10 有效 / 20 失效")
    private Integer status;

    @Schema(description = "失效原因，正常时为空")
    private String invalidReason;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "商品主图")
    private String mainImage;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "商品状态：10 草稿 / 20 上架 / 30 下架")
    private Integer spuStatus;

    @Schema(description = "规格编码")
    private String skuCode;

    @Schema(description = "规格名")
    private String specName;

    @Schema(description = "规格键值 JSON")
    private String specJson;

    @Schema(description = "规格图片")
    private String skuImage;

    @Schema(description = "当前售价")
    private BigDecimal price;

    @Schema(description = "可售数量")
    private Integer availableStock;

    @Schema(description = "规格状态：10 启用 / 20 停用")
    private Integer skuStatus;

    @Schema(description = "小计金额 = 单价 × 数量")
    private BigDecimal subtotal;
}
