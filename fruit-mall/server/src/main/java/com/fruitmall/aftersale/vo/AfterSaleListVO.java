package com.fruitmall.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 售后单列表项。 */
@Data
@Schema(description = "售后单列表项")
public class AfterSaleListVO {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "类型编码")
    private Integer type;

    @Schema(description = "类型说明")
    private String typeDesc;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "状态说明")
    private String statusDesc;

    @Schema(description = "售后数量")
    private Integer quantity;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "申请原因")
    private String reason;

    @Schema(description = "申请时间")
    private LocalDateTime createTime;
}
