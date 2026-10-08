package com.fruitmall.recommend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 推荐商品出参：商品信息 + 推荐理由 + 打分明细。 */
@Data
@Schema(description = "推荐商品")
public class RecommendItemVO {

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "副标题/卖点")
    private String subtitle;

    @Schema(description = "主图")
    private String mainImage;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "应季月份")
    private String seasonMonths;

    @Schema(description = "最低售价")
    private BigDecimal minPrice;

    @Schema(description = "可售库存")
    private Integer availableStock;

    @Schema(description = "综合得分")
    private BigDecimal score;

    @Schema(description = "推荐理由，前台直接展示")
    private String reasonText;

    @Schema(description = "命中的召回通道，用于说明推荐来源")
    private List<String> recallChannels = new ArrayList<>();

    @Schema(description = "打分明细")
    private List<RecommendFactorVO> factors = new ArrayList<>();
}
