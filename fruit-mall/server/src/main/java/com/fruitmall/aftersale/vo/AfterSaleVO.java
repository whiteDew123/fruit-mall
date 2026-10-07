package com.fruitmall.aftersale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 售后单详情。 */
@Data
@Schema(description = "售后单详情")
public class AfterSaleVO {

    @Schema(description = "售后单ID")
    private Long id;

    @Schema(description = "售后单号")
    private String afterSaleNo;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "类型编码：10 仅退款 / 20 退货退款")
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

    @Schema(description = "凭证图片 JSON 数组")
    private String evidenceImages;

    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "会员寄回时间")
    private LocalDateTime returnTime;

    @Schema(description = "商家收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "退款完成时间")
    private LocalDateTime refundTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "售后明细")
    private List<AfterSaleItemVO> items = new ArrayList<>();

    @Schema(description = "退款流水号，尚未发起退款时为空")
    private String refundNo;

    @Schema(description = "退款状态说明")
    private String refundStatusDesc;
}
