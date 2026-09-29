package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 商品特色属性定义：甜度、酸度、果径、应季月份等。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_product_attr_def")
public class FmProductAttrDef extends BaseEntity {

    /** 属性编码 */
    private String attrCode;

    /** 属性名称 */
    private String attrName;

    /** 数据类型，见 AttrDataTypeEnum */
    private Integer dataType;

    /** 单位 */
    private String unit;

    /** 枚举取值数组（JSON） */
    private String enumOptions;

    /** 数值下限 */
    private BigDecimal minValue;

    /** 数值上限 */
    private BigDecimal maxValue;

    /** 是否上架必填：0 否 / 1 是 */
    private Integer required;

    /** 排序 */
    private Integer sort;

    /** 状态：10 正常 / 20 停用 */
    private Integer status;
}
