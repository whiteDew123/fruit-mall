package com.fruitmall.recommend.rank;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import org.springframework.stereotype.Component;

/**
 * 热度：近 N 天行为热度的归一化值。
 * 统计窗口内完全没有行为数据时返回 null——宁可让热度因素退出，也不让所有商品一起拿 0 分。
 */
@Component
public class PopularityFactor implements ScoreFactor {

    @Override
    public String code() {
        return "HOT";
    }

    @Override
    public String name() {
        return "近七日热度";
    }

    @Override
    public Double value(UserProfile profile, ItemFeature item, RecommendContext context) {
        Double hot = context.getHotScoreMap().get(item.getSpuId());
        return hot == null ? null : hot;
    }

    @Override
    public String reasonText(double value, ItemFeature item) {
        return "近七日热销";
    }
}
