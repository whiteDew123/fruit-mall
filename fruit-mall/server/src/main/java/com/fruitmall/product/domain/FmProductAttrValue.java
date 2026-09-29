package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 商品特色属性值：SPU 在各属性上的取值，推荐特征向量的数据来源。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_product_attr_value")
public class FmProductAttrValue extends BaseEntity {

    /** 商品SPU ID */
    private Long spuId;

    /** 属性定义ID */
    private Long attrDefId;

    /** 属性原始值 */
    private String attrValue;

    /** 数值化取值，供特征向量使用 */
    private BigDecimal numValue;
}
