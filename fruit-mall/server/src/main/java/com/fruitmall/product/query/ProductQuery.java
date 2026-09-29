package com.fruitmall.product.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

/** 商品列表查询条件，消费者端与商家后台共用。 */
@Data
@Schema(description = "商品查询条件")
public class ProductQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，上限 100", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    @Schema(description = "分类ID，含其一级子分类")
    private Long categoryId;

    @Schema(description = "关键词，匹配商品名、卖点与产地")
    private String keyword;

    @Schema(description = "价格下限")
    private BigDecimal minPrice;

    @Schema(description = "价格上限")
    private BigDecimal maxPrice;

    @Schema(description = "状态：10 草稿 / 20 上架 / 30 下架，消费者端固定为 20")
    private Integer status;

    @Schema(description = "排序方式：sales 销量 / priceAsc 价格升 / priceDesc 价格降 / new 最新", example = "sales")
    private String sortBy = "sales";
}
