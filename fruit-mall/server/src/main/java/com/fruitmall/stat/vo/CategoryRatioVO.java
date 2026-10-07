package com.fruitmall.stat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 品类销售占比。 */
@Data
@Schema(description = "品类销售占比")
public class CategoryRatioVO {

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "销售件数")
    private Integer quantity;

    @Schema(description = "销售金额")
    private BigDecimal amount;

    @Schema(description = "金额占比（百分比，保留两位小数）")
    private BigDecimal ratio;
}
