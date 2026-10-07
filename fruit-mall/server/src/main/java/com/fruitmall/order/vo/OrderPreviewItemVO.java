package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 确认订单页的商品行。 */
@Data
@Schema(description = "确认订单商品行")
public class OrderPreviewItemVO {

    @Schema(description = "购物车项ID，提交订单时用于清理购物车")
    private Long cartItemId;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "规格名称")
    private String specName;

    @Schema(description = "规格键值 JSON")
    private String specJson;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "当前售价（实时价，不是购物车缓存价）")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计金额")
    private BigDecimal amount;

    @Schema(description = "可售数量")
    private Integer availableStock;
}
