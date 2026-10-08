package com.fruitmall.recommend.feature;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一次推荐请求的上下文。
 * 把商品特征、用户画像、热度与当前时间集中在一处供各策略共享，
 * 避免每个策略各自查库、各自归一化，导致同一请求内口径不一致。
 */
@Data
public class RecommendContext {

    /** 用户画像，游客为空画像 */
    private UserProfile profile;

    /** 全部上架商品特征 */
    private List<ItemFeature> features;

    /** 商品特征索引 */
    private Map<Long, ItemFeature> featureMap = new HashMap<>();

    /** 归一化后的热销得分，key 为 SPU ID */
    private Map<Long, Double> hotScoreMap = new HashMap<>();

    /** 请求时间 */
    private LocalDateTime now = LocalDateTime.now();

    /** 召回候选上限 */
    private int recallLimit = 100;

    /** 本次请求标识，用于留痕关联 */
    private String requestId;

    /** 数值型属性总数，用于计算单个商品的信息完备度 */
    private int attrTotalCount;
}
