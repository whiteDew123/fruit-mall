package com.fruitmall.recommend.rank;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 时令匹配：当季 1.0、相邻月份 0.5、反季 0.1。
 * 商品未维护应季月份时返回 null，不把"没填"当成"反季"。
 */
@Component
public class SeasonFactor implements ScoreFactor {

    @Override
    public String code() {
        return "SEASON";
    }

    @Override
    public String name() {
        return "时令匹配";
    }

    @Override
    public Double value(UserProfile profile, ItemFeature item, RecommendContext context) {
        Set<Integer> months = item.getSeasonMonthSet();
        if (months == null || months.isEmpty()) {
            return null;
        }
        int current = context.getNow().getMonthValue();
        if (months.contains(current)) {
            return 1.0D;
        }
        // 前后一个月算临近季节，注意 12 月与 1 月的环绕
        int previous = current == 1 ? 12 : current - 1;
        int next = current == 12 ? 1 : current + 1;
        if (months.contains(previous) || months.contains(next)) {
            return 0.5D;
        }
        return 0.1D;
    }

    @Override
    public String reasonText(double value, ItemFeature item) {
        return value >= 1.0D ? "当季上市，正当季" : "临近应季";
    }
}
