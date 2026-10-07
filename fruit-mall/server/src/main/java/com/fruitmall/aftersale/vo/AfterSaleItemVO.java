package com.fruitmall.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 售后明细出参。 */
@Data
@Schema(description = "售后明细")
public class AfterSaleItemVO {

    @Schema(description = "订单项ID")
    private Long orderItemId;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "商品名称（订单快照）")
    private String spuName;

    @Schema(description = "规格名称（订单快照）")
    private String skuName;

    @Schema(description = "商品图片（订单快照）")
    private String image;

    @Schema(description = "售后数量")
    private Integer quantity;

    @Schema(description = "该明细退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退货暂存批次ID，生鲜退货不进入可售库存")
    private Long returnBatchId;
}
