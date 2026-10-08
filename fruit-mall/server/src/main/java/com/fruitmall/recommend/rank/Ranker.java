package com.fruitmall.recommend.rank;

import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.recommend.config.RecommendProperties;
import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import com.fruitmall.recommend.vo.RecommendFactorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 多因素加权排序器。
 * 核心公式：score = Σ(权重 × 因素分) / Σ权重 − 惩罚项；
 * 关键细节：因素返回 null（数据缺失）或权重为 0 时，该维度既不计入分子也不计入分母，
 * 相当于自动重新归一化，避免把"缺失"当成"0 分"而误伤新用户与新商品。
 */
@Component
@RequiredArgsConstructor
public class Ranker {

    /** 复购惩罚系数：水果是消耗品，复购是常态，因此只降权不剔除 */
    private static final double REPURCHASE_PENALTY = 0.08D;

    private final List<ScoreFactor> factors;
    private final RecommendProperties properties;

    /** 给单个商品打分并输出因素明细 */
    public RankedItem rank(UserProfile profile, ItemFeature item, RecommendContext context) {
        double weightedSum = 0D;
        double weightSum = 0D;
        List<RecommendFactorVO> details = new ArrayList<>();

        for (ScoreFactor factor : factors) {
            Double raw = factor.value(profile, item, context);
            double weight = properties.weightOf(factor.code());
            if (raw == null || weight <= 0D) {
                // 数据缺失或未配置权重：跳过，不参与分母
                continue;
            }
            double value = SimilarityUtil.clamp01(raw);
            double contribution = weight * value;
            weightedSum += contribution;
            weightSum += weight;

            RecommendFactorVO detail = new RecommendFactorVO();
            detail.setCode(factor.code());
            detail.setName(factor.name());
            detail.setValue(BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP));
            detail.setWeight(BigDecimal.valueOf(weight).setScale(4, RoundingMode.HALF_UP));
            detail.setContribution(BigDecimal.valueOf(contribution).setScale(4, RoundingMode.HALF_UP));
            detail.setText(factor.reasonText(value, item));
            details.add(detail);
        }

        double score = weightSum == 0D ? 0D : weightedSum / weightSum;
        score -= penalty(profile, item);

        RankedItem ranked = new RankedItem();
        ranked.setFeature(item);
        ranked.setScore(Math.max(score, 0D));
        details.sort(Comparator.comparing(RecommendFactorVO::getContribution).reversed());
        ranked.setFactors(details);
        return ranked;
    }

    /** 惩罚项：已购买过的商品降权，但不剔除 */
    private double penalty(UserProfile profile, ItemFeature item) {
        if (profile == null || profile.getPurchasedSpuIds() == null) {
            return 0D;
        }
        return profile.getPurchasedSpuIds().contains(item.getSpuId()) ? REPURCHASE_PENALTY : 0D;
    }
}
