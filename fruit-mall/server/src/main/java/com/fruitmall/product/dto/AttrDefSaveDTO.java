package com.fruitmall.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 特色属性定义入参。 */
@Data
@Schema(description = "特色属性定义入参")
public class AttrDefSaveDTO {

    @Schema(description = "属性编码", example = "SWEETNESS")
    @NotBlank(message = "属性编码不能为空")
    @Size(max = 50, message = "属性编码长度不能超过 50")
    private String attrCode;

    @Schema(description = "属性名称", example = "甜度")
    @NotBlank(message = "属性名称不能为空")
    @Size(max = 50, message = "属性名称长度不能超过 50")
    private String attrName;

    @Schema(description = "数据类型：10 数值 / 20 枚举 / 30 文本 / 40 布尔", example = "10")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @Schema(description = "单位", example = "级")
    @Size(max = 20, message = "单位长度不能超过 20")
    private String unit;

    @Schema(description = "枚举取值数组（JSON）", example = "[\"一级\",\"二级\",\"三级\"]")
    @Size(max = 500, message = "枚举取值长度不能超过 500")
    private String enumOptions;

    @Schema(description = "数值下限", example = "1")
    private BigDecimal minValue;

    @Schema(description = "数值上限", example = "5")
    private BigDecimal maxValue;

    @Schema(description = "是否上架必填：0 否 / 1 是", example = "1")
    private Integer required = 0;

    @Schema(description = "排序", example = "1")
    private Integer sort = 0;

    @Schema(description = "状态：10 正常 / 20 停用", example = "10")
    private Integer status = 10;
}
