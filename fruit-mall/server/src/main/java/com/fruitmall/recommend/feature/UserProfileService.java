package com.fruitmall.recommend.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fruitmall.behavior.domain.FmUserBehavior;
import com.fruitmall.behavior.mapper.FmUserBehaviorMapper;
import com.fruitmall.common.enums.BehaviorTargetTypeEnum;
import com.fruitmall.common.enums.BehaviorTypeEnum;
import com.fruitmall.recommend.config.RecommendProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户画像服务。
 * 聚合公式：偏好维度 d 的值 = Σ(行为权重 × 时间衰减 × 商品在维度 d 的取值) / Σ|行为权重 × 时间衰减|。
 * 三个要点：行为权重来自 BehaviorTypeEnum（负向行为取负值）；时间衰减用半衰期公式 λ = ln2 / 半衰期天数；
 * 分母取绝对值之和，保证"不感兴趣"这类负向行为能真正把偏好拉低，而不是被正负行为互相抵消。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final FmUserBehaviorMapper behaviorMapper;
    private final ItemFeatureService itemFeatureService;
    private final RecommendProperties properties;

    /** 构建用户画像；memberId 传 null 表示游客，直接返回空画像走冷启动分支 */
    public UserProfile buildProfile(Long memberId) {
        UserProfile profile = new UserProfile();
        profile.setMemberId(memberId);
        if (memberId == null) {
            return profile;
        }

        List<ItemFeature> features = itemFeatureService.listFeatures();
        if (features.isEmpty()) {
            return profile;
        }
        int dimension = features.get(0).getVector().length;
        double[] vector = new double[dimension];
        double priceWeightedSum = 0D;
        double priceWeight = 0D;
        double weightSum = 0D;

        LocalDateTime since = LocalDateTime.now().minusDays(properties.getBehaviorWindowDays());
        List<FmUserBehavior> behaviors = behaviorMapper.selectList(Wrappers.<FmUserBehavior>lambdaQuery()
                .eq(FmUserBehavior::getMemberId, memberId)
                .ge(FmUserBehavior::getCreateTime, since)
                .orderByDesc(FmUserBehavior::getCreateTime));

        double lambda = Math.log(2) / Math.max(properties.getHalfLifeDays(), 1);
        for (FmUserBehavior behavior : behaviors) {
            BehaviorTypeEnum type = BehaviorTypeEnum.of(behavior.getBehavior());
            if (type == null || behavior.getTargetId() == null) {
                continue;
            }
            // 只有针对商品的行为参与画像；关键词、分类行为的建模留作后续扩展
            boolean isSpu = behavior.getTargetType() == null
                    || BehaviorTargetTypeEnum.SPU.getCode().equals(behavior.getTargetType());
            if (!isSpu) {
                continue;
            }
            ItemFeature feature = itemFeatureService.getFeature(behavior.getTargetId());
            if (feature == null) {
                continue;
            }
            long days = behavior.getCreateTime() == null ? 0
                    : Duration.between(behavior.getCreateTime(), LocalDateTime.now()).toDays();
            double decay = Math.exp(-lambda * Math.max(days, 0));
            double effective = type.getWeight() * decay;

            for (int d = 0; d < dimension; d++) {
                vector[d] += effective * feature.getVector()[d];
            }
            weightSum += Math.abs(effective);

            if (feature.getMinPrice() != null && type.getWeight() > 0) {
                priceWeightedSum += feature.getMinPrice().doubleValue() * effective;
                priceWeight += effective;
            }
            if (BehaviorTypeEnum.DISLIKE == type) {
                profile.getDislikedSpuIds().add(feature.getSpuId());
            }
            if (BehaviorTypeEnum.ORDER == type) {
                profile.getPurchasedSpuIds().add(feature.getSpuId());
            }
        }

        profile.setBehaviorCount(behaviors.size());
        if (weightSum > 0D) {
            for (int d = 0; d < dimension; d++) {
                vector[d] = vector[d] / weightSum;
            }
            profile.setVector(vector);
            profile.setEmpty(false);
        } else {
            profile.setVector(new double[dimension]);
        }
        if (priceWeight > 0D) {
            profile.setPriceCenter(priceWeightedSum / priceWeight);
        }
        return profile;
    }
}
