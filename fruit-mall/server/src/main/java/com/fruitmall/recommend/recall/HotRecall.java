package com.fruitmall.recommend.recall;

import com.fruitmall.recommend.feature.RecommendContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 热销召回：按近 N 天的行为热度（下单、加购、浏览加权）取前若干商品。
 * 热度得分在上下文中统一归一化，保证所有策略共用同一份口径。
 */
@Component
public class HotRecall implements RecallStrategy {

    private static final int CHANNEL_LIMIT = 30;

    @Override
    public String code() {
        return "HOT";
    }

    @Override
    public String name() {
        return "热销榜单";
    }

    @Override
    public List<Long> recall(RecommendContext context) {
        return context.getHotScoreMap().entrySet().stream()
                .filter(entry -> entry.getValue() > 0D)
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(Math.min(CHANNEL_LIMIT, context.getRecallLimit()))
                .map(Map.Entry::getKey)
                .toList();
    }
}
