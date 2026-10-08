package com.fruitmall.recommend.rank;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;

/**
 * 打分因素：推荐的排序阶段，每个因素只回答"这个商品在这一维度上有多合适"。
 * 约定：数据缺失时返回 null，表示该维度不参与加权求和，而不是返回 0 分；
 * 排序器会据此把该维度的权重从分母中去掉，其余权重自动重新归一化。
 */
public interface ScoreFactor {

    /** 因素编码，与配置里的权重 key 一致 */
    String code();

    /** 因素名称 */
    String name();

    /**
     * 计算原始分，取值建议落在 [0,1]
     *
     * @return null 表示该因素在当前数据下不可用
     */
    Double value(UserProfile profile, ItemFeature item, RecommendContext context);

    /** 该因素命中时展示给用户的说明文案 */
    String reasonText(double value, ItemFeature item);
}
