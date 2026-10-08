package com.fruitmall.recommend.rank;

import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import org.springframework.stereotype.Component;

/**
 * 内容偏好匹配：用户画像向量与商品特征向量的余弦相似度。
 * 新用户或游客没有画像，返回 null，该因素自动退出打分。
 */
@Component
public class ContentFactor implements ScoreFactor {

    @Override
    public String code() {
        return "CONTENT";
    }

    @Override
    public String name() {
        return "内容偏好匹配";
    }

    @Override
    public Double value(UserProfile profile, ItemFeature item, RecommendContext context) {
        if (profile == null || profile.isEmpty() || item.getVector() == null) {
            return null;
        }
        return SimilarityUtil.clamp01(SimilarityUtil.cosine(profile.getVector(), item.getVector()));
    }

    @Override
    public String reasonText(double value, ItemFeature item) {
        return "口味匹配：与你常买的口味相近";
    }
}
