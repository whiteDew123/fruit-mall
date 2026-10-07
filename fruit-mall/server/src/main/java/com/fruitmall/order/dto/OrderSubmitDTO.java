package com.fruitmall.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 提交订单入参。商品取自购物车中已勾选的项，不接受前端传入价格与数量。 */
@Data
@Schema(description = "提交订单入参")
public class OrderSubmitDTO {

    @Schema(description = "收货地址ID", example = "1")
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    @Schema(description = "会员备注", example = "工作日送，放门口")
    @Size(max = 255, message = "备注长度不能超过 255")
    private String memberRemark;
}
