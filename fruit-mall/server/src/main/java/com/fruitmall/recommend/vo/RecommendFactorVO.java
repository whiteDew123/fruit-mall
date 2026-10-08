package com.fruitmall.recommend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 单个打分因素的明细，用于解释"为什么推荐这个商品"。 */
@Data
@Schema(description = "推荐打分因素明细")
public class RecommendFactorVO {

    @Schema(description = "因素编码：CONTENT / SEASON / HOT / PRICE / QUALITY")
    private String code;

    @Schema(description = "因素名称")
    private String name;

    @Schema(description = "原始分（0-1）")
    private BigDecimal value;

    @Schema(description = "该因素权重")
    private BigDecimal weight;

    @Schema(description = "对最终得分的贡献值")
    private BigDecimal contribution;

    @Schema(description = "该因素命中的说明文案")
    private String text;
}
