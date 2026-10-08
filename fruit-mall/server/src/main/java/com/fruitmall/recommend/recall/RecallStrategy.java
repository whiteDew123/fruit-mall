package com.fruitmall.recommend.recall;

import com.fruitmall.recommend.feature.RecommendContext;

import java.util.List;

/**
 * 召回策略：推荐第一阶段，从全量商品中快速筛出候选集合。
 * 每个通道只负责一种召回思路，新增通道只需实现本接口并交给 Spring 管理，编排逻辑无需改动。
 */
public interface RecallStrategy {

    /** 通道编码，如 CONTENT / SEASON / HOT / FALLBACK */
    String code();

    /** 通道名称，用于前台展示推荐来源与留痕 */
    String name();

    /** 返回召回的商品ID列表，按优先级从高到低排序 */
    List<Long> recall(RecommendContext context);
}
