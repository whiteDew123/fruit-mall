package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/** 数值型特色属性的一行原始数据，供推荐模块构建特征向量。 */
@Data
@Schema(description = "数值型属性原始行")
public class AttrValueRowVO {

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "属性编码")
    private String attrCode;

    @Schema(description = "数值化取值")
    private BigDecimal numValue;
}
