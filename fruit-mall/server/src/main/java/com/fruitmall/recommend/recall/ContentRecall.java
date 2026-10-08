package com.fruitmall.recommend.recall;

import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.recommend.feature.RecommendContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 内容相似召回：用用户画像向量与商品特征向量做余弦相似度，取相似度最高的若干商品。
 * 冷启动用户画像为空时直接返回空集合，由热销与兜底通道补齐候选。
 */
@Component
public class ContentRecall implements RecallStrategy {

    private static final int CHANNEL_LIMIT = 50;

    @Override
    public String code() {
        return "CONTENT";
    }

    @Override
    public String name() {
        return "内容相似";
    }

    @Override
    public List<Long> recall(RecommendContext context) {
        if (context.getProfile() == null || context.getProfile().isEmpty()) {
            return List.of();
        }
        double[] profileVector = context.getProfile().getVector();
        return context.getFeatures().stream()
                .filter(feature -> feature.getVector() != null)
                .map(feature -> new ScoredItem(feature.getSpuId(),
                        SimilarityUtil.cosine(profileVector, feature.getVector())))
                .filter(scored -> scored.score() > 0D)
                .sorted(Comparator.comparingDouble(ScoredItem::score).reversed())
                .limit(Math.min(CHANNEL_LIMIT, context.getRecallLimit()))
                .map(ScoredItem::spuId)
                .toList();
    }

    /** 通道内部使用的临时结构，避免为单次排序单独建类 */
    private record ScoredItem(Long spuId, double score) {
    }
}
