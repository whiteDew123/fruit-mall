package com.fruitmall.fulfillment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 履约轨迹节点出参。 */
@Data
@Schema(description = "履约轨迹节点")
public class FulfillmentTraceVO {

    @Schema(description = "节点编码")
    private Integer node;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "操作者类型：10 会员 / 20 商家 / 30 系统")
    private Integer operatorType;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "备注或异常原因")
    private String remark;

    @Schema(description = "图片地址（JSON 数组字符串）")
    private String images;

    @Schema(description = "节点时间")
    private LocalDateTime createTime;
}
