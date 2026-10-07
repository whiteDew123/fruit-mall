package com.fruitmall.aftersale.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 申请售后入参。 */
@Data
@Schema(description = "申请售后入参")
public class AfterSaleApplyDTO {

    @Schema(description = "订单项ID", example = "1")
    @NotNull(message = "订单项ID不能为空")
    private Long orderItemId;

    @Schema(description = "售后类型：10 仅退款 / 20 退货退款", example = "10")
    @NotNull(message = "请选择售后类型")
    private Integer type;

    @Schema(description = "售后数量", example = "1")
    @NotNull(message = "售后数量不能为空")
    @Min(value = 1, message = "售后数量至少为 1")
    private Integer quantity;

    @Schema(description = "申请原因", example = "收到时有两颗压伤")
    @NotBlank(message = "请填写申请原因")
    @Size(max = 255, message = "申请原因长度不能超过 255")
    private String reason;

    @Schema(description = "凭证图片地址 JSON 数组")
    @Size(max = 1000, message = "凭证图片地址长度不能超过 1000")
    private String evidenceImages;
}
