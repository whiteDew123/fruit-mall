package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 订单详情。 */
@Data
@Schema(description = "订单详情")
public class OrderDetailVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "状态说明")
    private String statusDesc;

    @Schema(description = "商品金额合计")
    private BigDecimal totalAmount;

    @Schema(description = "运费")
    private BigDecimal freightAmount;

    @Schema(description = "应付金额")
    private BigDecimal payAmount;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话")
    private String receiverPhone;

    @Schema(description = "完整收货地址")
    private String fullAddress;

    @Schema(description = "会员备注")
    private String memberRemark;

    @Schema(description = "商家备注")
    private String adminRemark;

    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "商品明细")
    private List<OrderItemVO> items = new ArrayList<>();

    @Schema(description = "状态时间轴")
    private List<OrderStatusLogVO> statusLogs = new ArrayList<>();
}
