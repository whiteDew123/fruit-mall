package com.fruitmall.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 商品特色属性取值入参。 */
@Data
@Schema(description = "特色属性取值入参")
public class AttrValueSaveDTO {

    @Schema(description = "属性定义ID", example = "1")
    @NotNull(message = "属性定义ID不能为空")
    private Long attrDefId;

    @Schema(description = "属性原始值", example = "4")
    @Size(max = 500, message = "属性值长度不能超过 500")
    private String attrValue;

    @Schema(description = "数值化取值，数值型属性填此列", example = "4.0")
    private BigDecimal numValue;
}
