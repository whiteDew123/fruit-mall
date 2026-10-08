package com.fruitmall.recommend.rank;

import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import org.springframework.stereotype.Component;

/**
 * 品质与新鲜度：由"信息完备度"与"新鲜度"两项合成。
 * 信息完备度 = 该商品已填写的数值型属性数 / 属性总数，反映建档质量与可比较性；
 * 新鲜度 = 1 - 保质期/30 天，天数越短越新鲜（生鲜品类的常识性约束）。
 * 两项都拿不到数据时返回 null。
 */
@Component
public class QualityFactor implements ScoreFactor {

    /** 视为"很短保质期"的天数，用于把保质期折算成新鲜度 */
    private static final double FRESH_LIFE_DAYS = 30D;

    @Override
    public String code() {
        return "QUALITY";
    }

    @Override
    public String name() {
        return "品质与新鲜度";
    }

    @Override
    public Double value(UserProfile profile, ItemFeature item, RecommendContext context) {
        Double completeness = null;
        if (context.getAttrTotalCount() > 0) {
            completeness = (double) item.getAttrFilledCount() / context.getAttrTotalCount();
        }
        Double freshness = item.getShelfLifeDays() == null
                ? null
                : SimilarityUtil.clamp01(1D - item.getShelfLifeDays() / FRESH_LIFE_DAYS);

        if (completeness == null && freshness == null) {
            return null;
        }
        if (completeness == null) {
            return freshness;
        }
        if (freshness == null) {
            return completeness;
        }
        return 0.5D * completeness + 0.5D * freshness;
    }

    @Override
    public String reasonText(double value, ItemFeature item) {
        return "品质优选，到货新鲜";
    }
}
