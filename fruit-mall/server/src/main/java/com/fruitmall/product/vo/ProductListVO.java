package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品列表项，价格为该商品所有启用规格的最低价与最高价。 */
@Data
@Schema(description = "商品列表项")
public class ProductListVO {

    @Schema(description = "商品SPU ID")
    private Long id;

    @Schema(description = "商品编码")
    private String spuCode;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "副标题/卖点")
    private String subtitle;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "应季月份")
    private String seasonMonths;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "主图地址")
    private String mainImage;

    @Schema(description = "最低售价")
    private BigDecimal minPrice;

    @Schema(description = "最高售价")
    private BigDecimal maxPrice;

    @Schema(description = "可售库存合计")
    private Integer availableStock;

    @Schema(description = "累计销量")
    private Integer salesCount;

    @Schema(description = "状态：10 草稿 / 20 上架 / 30 下架")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
