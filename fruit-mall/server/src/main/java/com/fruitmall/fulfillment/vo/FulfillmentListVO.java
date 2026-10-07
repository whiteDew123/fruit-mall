package com.fruitmall.fulfillment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 履约单列表项。 */
@Data
@Schema(description = "履约单列表项")
public class FulfillmentListVO {

    @Schema(description = "履约单ID")
    private Long id;

    @Schema(description = "履约单号")
    private String fulfillmentNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "履约状态编码")
    private Integer status;

    @Schema(description = "履约状态说明")
    private String statusDesc;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话（已脱敏）")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
