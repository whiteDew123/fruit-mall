package com.fruitmall.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 订单状态流水出参，用于订单时间轴展示。 */
@Data
@Schema(description = "订单状态流水")
public class OrderStatusLogVO {

    @Schema(description = "动作编码")
    private String action;

    @Schema(description = "动作说明")
    private String actionDesc;

    @Schema(description = "原状态")
    private Integer fromStatus;

    @Schema(description = "新状态")
    private Integer toStatus;

    @Schema(description = "操作者类型：10 会员 / 20 商家 / 30 系统")
    private Integer operatorType;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "发生时间")
    private LocalDateTime createTime;
}
