package com.fruitmall.stat.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 经营分析查询条件。 */
@Data
@Schema(description = "经营分析查询条件")
public class StatQuery {

    @Schema(description = "统计天数，用于销售趋势", example = "30")
    @Min(value = 1, message = "统计天数至少为 1")
    @Max(value = 365, message = "统计天数不能超过 365")
    private Integer days = 30;

    @Schema(description = "TOP 条数，用于商品销量排行", example = "10")
    @Min(value = 1, message = "条数至少为 1")
    @Max(value = 50, message = "条数不能超过 50")
    private Integer limit = 10;
}
