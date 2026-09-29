package com.fruitmall.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 商品规格入参。 */
@Data
@Schema(description = "商品规格入参")
public class SkuSaveDTO {

    @Schema(description = "规格ID，编辑时传入；不传表示新增")
    private Long id;

    @Schema(description = "规格编码", example = "MANGO-5J")
    @NotBlank(message = "规格编码不能为空")
    @Size(max = 50, message = "规格编码长度不能超过 50")
    private String skuCode;

    @Schema(description = "规格名", example = "5斤装")
    @NotBlank(message = "规格名不能为空")
    @Size(max = 100, message = "规格名长度不能超过 100")
    private String specName;

    @Schema(description = "规格键值 JSON", example = "{\"净重\":\"5斤\",\"果径\":\"80mm\"}")
    private String specJson;

    @Schema(description = "规格图片")
    @Size(max = 255, message = "规格图片地址长度不能超过 255")
    private String image;

    @Schema(description = "划线价", example = "79.00")
    @DecimalMin(value = "0.00", message = "划线价不能为负")
    private BigDecimal originalPrice;

    @Schema(description = "售价", example = "59.90")
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @Schema(description = "可售库存", example = "100")
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负")
    private Integer stock;

    @Schema(description = "低库存预警阈值", example = "10")
    @Min(value = 0, message = "预警阈值不能为负")
    private Integer warnStock = 0;

    @Schema(description = "状态：10 启用 / 20 停用", example = "10")
    private Integer status = 10;
}
