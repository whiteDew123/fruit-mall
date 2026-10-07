package com.fruitmall.aftersale.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 售后单查询条件。 */
@Data
@Schema(description = "售后单查询条件")
public class AfterSaleQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，上限 100", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    @Schema(description = "售后状态，见 AfterSaleStatusEnum")
    private Integer status;

    @Schema(description = "订单号，精确匹配")
    private String orderNo;
}
