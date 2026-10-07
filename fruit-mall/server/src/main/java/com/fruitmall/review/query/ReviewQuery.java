package com.fruitmall.review.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 评价查询条件。 */
@Data
@Schema(description = "评价查询条件")
public class ReviewQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，上限 100", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "星级 1-5")
    private Integer star;

    @Schema(description = "状态：10 显示 / 20 隐藏，仅后台使用")
    private Integer status;

    /** 消费者端固定只查显示中的评价，不接受前端传参 */
    @Schema(hidden = true)
    private boolean onlyVisible = false;
}
