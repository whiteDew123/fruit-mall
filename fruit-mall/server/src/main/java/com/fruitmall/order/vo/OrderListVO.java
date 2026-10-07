package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单列表项。 */
@Data
@Schema(description = "订单列表项")
public class OrderListVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "状态编码")
    private Integer status;

    @Schema(description = "状态说明")
    private String statusDesc;

    @Schema(description = "应付金额")
    private BigDecimal payAmount;

    @Schema(description = "商品件数合计")
    private Integer totalQuantity;

    @Schema(description = "商品种类数")
    private Integer itemCount;

    @Schema(description = "首个商品名称，用于列表摘要")
    private String firstItemName;

    @Schema(description = "首个商品图片")
    private String firstItemImage;

    @Schema(description = "收货人（快照）")
    private String receiverName;

    @Schema(description = "收货电话（已脱敏）")
    private String receiverPhone;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "支付截止时间")
    private LocalDateTime expireTime;
}
