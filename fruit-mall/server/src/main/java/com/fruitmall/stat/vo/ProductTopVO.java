package com.fruitmall.stat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 商品销量排行。 */
@Data
@Schema(description = "商品销量排行")
public class ProductTopVO {

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "销售件数")
    private Integer quantity;

    @Schema(description = "销售金额")
    private BigDecimal amount;
}
