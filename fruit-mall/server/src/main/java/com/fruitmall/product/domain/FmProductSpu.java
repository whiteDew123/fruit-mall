package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品 SPU：芒果、苹果等"款"。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_product_spu")
public class FmProductSpu extends BaseEntity {

    /** 商品编码 */
    private String spuCode;

    /** 分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String spuName;

    /** 副标题/卖点 */
    private String subtitle;

    /** 产地 */
    private String originPlace;

    /** 应季月份，如 5,6,7 */
    private String seasonMonths;

    /** 储存条件 */
    private String storageCondition;

    /** 保质期天数 */
    private Integer shelfLifeDays;

    /** 计量单位 */
    private String unit;

    /** 主图地址 */
    private String mainImage;

    /** 图文详情 */
    private String detail;

    /** 累计销量（冗余，推荐热度用） */
    private Integer salesCount;

    /** 状态，见 ProductStatusEnum */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
