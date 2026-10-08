package com.fruitmall.recommend.recall;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 兜底召回：按销量排序并让新品靠前，保证候选池永远不为空。
 * 冷启动用户、商品数量很少或其它通道全部为空时，系统仍能给出可用的推荐结果。
 */
@Component
public class FallbackRecall implements RecallStrategy {

    private static final int CHANNEL_LIMIT = 20;

    @Override
    public String code() {
        return "FALLBACK";
    }

    @Override
    public String name() {
        return "新品与热销兜底";
    }

    @Override
    public List<Long> recall(RecommendContext context) {
        return context.getFeatures().stream()
                .sorted(Comparator
                        .comparingInt((ItemFeature feature) ->
                                feature.getSalesCount() == null ? 0 : feature.getSalesCount()).reversed()
                        .thenComparing(Comparator.comparingLong(ItemFeature::getSpuId).reversed()))
                .limit(Math.min(CHANNEL_LIMIT, context.getRecallLimit()))
                .map(ItemFeature::getSpuId)
                .toList();
    }
}
