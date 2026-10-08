package com.fruitmall.recommend.recall;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 时令召回：命应当前月份的商品优先。
 * 水果是强时令品类，当季本身就是最有说服力的推荐理由，因此单独设一个通道。
 */
@Component
public class SeasonRecall implements RecallStrategy {

    private static final int CHANNEL_LIMIT = 30;

    @Override
    public String code() {
        return "SEASON";
    }

    @Override
    public String name() {
        return "时令匹配";
    }

    @Override
    public List<Long> recall(RecommendContext context) {
        int month = context.getNow().getMonthValue();
        return context.getFeatures().stream()
                .filter(feature -> feature.getSeasonMonthSet().contains(month))
                .sorted(Comparator.comparingInt((ItemFeature feature) ->
                        feature.getSalesCount() == null ? 0 : feature.getSalesCount()).reversed())
                .limit(Math.min(CHANNEL_LIMIT, context.getRecallLimit()))
                .map(ItemFeature::getSpuId)
                .toList();
    }
}
