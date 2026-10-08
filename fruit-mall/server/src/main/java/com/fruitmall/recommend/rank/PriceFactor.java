package com.fruitmall.recommend.rank;

import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import org.springframework.stereotype.Component;

/**
 * 价格匹配：商品最低售价与用户价格偏好中心的接近程度。
 * 用户尚未形成价格偏好（无正向行为）或商品无报价时返回 null。
 */
@Component
public class PriceFactor implements ScoreFactor {

    @Override
    public String code() {
        return "PRICE";
    }

    @Override
    public String name() {
        return "价格匹配";
    }

    @Override
    public Double value(UserProfile profile, ItemFeature item, RecommendContext context) {
        if (profile == null || profile.getPriceCenter() <= 0D || item.getMinPrice() == null) {
            return null;
        }
        double center = profile.getPriceCenter();
        double deviation = Math.abs(item.getMinPrice().doubleValue() - center) / center;
        return SimilarityUtil.clamp01(1D - deviation);
    }

    @Override
    public String reasonText(double value, ItemFeature item) {
        return "价位符合你的消费习惯";
    }
}
