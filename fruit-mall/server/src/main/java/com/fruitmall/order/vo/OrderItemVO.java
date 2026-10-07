package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 订单项出参（下单时的快照）。 */
@Data
@Schema(description = "订单项")
public class OrderItemVO {

    @Schema(description = "订单项ID")
    private Long id;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "商品名称（快照）")
    private String spuName;

    @Schema(description = "规格名称（快照）")
    private String skuName;

    @Schema(description = "规格快照 JSON")
    private String skuSnapshot;

    @Schema(description = "商品图片（快照）")
    private String image;

    @Schema(description = "成交单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计金额")
    private BigDecimal amount;

    @Schema(description = "累计已售后数量")
    private Integer afterSaleQuantity;

    @Schema(description = "是否已评价：0 否 / 1 是")
    private Integer reviewed;
}
