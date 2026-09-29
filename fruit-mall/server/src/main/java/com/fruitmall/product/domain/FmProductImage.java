package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品图片：主图、详情图、规格图。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_product_image")
public class FmProductImage extends BaseEntity {

    /** 商品SPU ID */
    private Long spuId;

    /** 规格ID，规格图时填写 */
    private Long skuId;

    /** 图片地址 */
    private String imageUrl;

    /** 类型：10 主图 / 20 详情图 / 30 规格图 */
    private Integer imageType;

    /** 排序 */
    private Integer sort;
}
