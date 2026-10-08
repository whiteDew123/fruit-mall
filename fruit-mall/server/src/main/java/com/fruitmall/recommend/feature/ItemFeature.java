package com.fruitmall.recommend.feature;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 商品特征：推荐算法的物品侧输入。
 * vector 为定长数值向量，含义是"各数值型特色属性的归一化值 + 价格档位"，
 * 维度顺序在一次特征构建内保持一致，因此可以直接做余弦相似度比较。
 */
@Data
public class ItemFeature {

    /** 商品SPU ID */
    private Long spuId;

    /** 商品名称 */
    private String spuName;

    /** 副标题 */
    private String subtitle;

    /** 主图 */
    private String mainImage;

    /** 产地 */
    private String originPlace;

    /** 应季月份原始值，如 5,6,7 */
    private String seasonMonths;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 最低售价 */
    private BigDecimal minPrice;

    /** 可售库存合计 */
    private Integer availableStock;

    /** 累计销量 */
    private Integer salesCount;

    /** 保质期天数，用于品质与新鲜度打分 */
    private Integer shelfLifeDays;

    /** 已填写的数值型属性个数，用于品质因素里的信息完备度 */
    private int attrFilledCount;

    /** 归一化后的价格档位，[0,1]，越高越贵 */
    private double priceNorm;

    /** 应季月份集合，用于时令匹配 */
    private Set<Integer> seasonMonthSet = new HashSet<>();

    /** 特征向量（定长） */
    private double[] vector;
}
