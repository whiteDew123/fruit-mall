package com.fruitmall.stat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 销售趋势的单日数据。 */
@Data
@Schema(description = "销售趋势数据点")
public class SalesTrendVO {

    @Schema(description = "日期", example = "2026-10-07")
    private String statDate;

    @Schema(description = "订单数")
    private Integer orderCount;

    @Schema(description = "销售额")
    private BigDecimal amount;
}
