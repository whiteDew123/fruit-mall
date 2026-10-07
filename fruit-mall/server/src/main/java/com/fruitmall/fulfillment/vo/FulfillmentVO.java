package com.fruitmall.fulfillment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 履约单详情，含时间轴。 */
@Data
@Schema(description = "履约单详情")
public class FulfillmentVO {

    @Schema(description = "履约单ID")
    private Long id;

    @Schema(description = "履约单号")
    private String fulfillmentNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "履约状态编码")
    private Integer status;

    @Schema(description = "履约状态说明")
    private String statusDesc;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话")
    private String receiverPhone;

    @Schema(description = "收货地址")
    private String receiverAddress;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "分拣完成时间")
    private LocalDateTime pickTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "送达时间")
    private LocalDateTime arriveTime;

    @Schema(description = "签收时间")
    private LocalDateTime signTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "履约时间轴")
    private List<FulfillmentTraceVO> traces = new ArrayList<>();
}
