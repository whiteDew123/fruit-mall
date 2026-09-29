package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 商品特色属性出参，含属性定义信息便于前端直接展示。 */
@Data
@Schema(description = "商品特色属性")
public class ProductAttrVO {

    @Schema(description = "属性定义ID")
    private Long attrDefId;

    @Schema(description = "属性编码")
    private String attrCode;

    @Schema(description = "属性名称")
    private String attrName;

    @Schema(description = "数据类型：10 数值 / 20 枚举 / 30 文本 / 40 布尔")
    private Integer dataType;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "属性值")
    private String attrValue;

    @Schema(description = "数值化取值")
    private BigDecimal numValue;
}
