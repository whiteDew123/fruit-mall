package com.fruitmall.order.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 订单列表查询条件。 */
@Data
@Schema(description = "订单查询条件")
public class OrderQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，上限 100", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    @Schema(description = "订单状态，见 OrderStatusEnum")
    private Integer status;

    @Schema(description = "订单号，精确匹配")
    private String orderNo;

    @Schema(description = "会员ID，仅后台使用")
    private Long memberId;
}
