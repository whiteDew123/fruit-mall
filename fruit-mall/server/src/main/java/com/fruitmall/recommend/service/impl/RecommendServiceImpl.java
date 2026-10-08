package com.fruitmall.recommend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.behavior.mapper.FmUserBehaviorMapper;
import com.fruitmall.behavior.vo.BehaviorCountVO;
import com.fruitmall.common.enums.BehaviorTypeEnum;
import com.fruitmall.recommend.config.RecommendProperties;
import com.fruitmall.recommend.diversify.Diversifier;
import com.fruitmall.recommend.domain.FmRecommendTrace;
import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.feature.ItemFeatureService;
import com.fruitmall.recommend.feature.RecommendContext;
import com.fruitmall.recommend.feature.UserProfile;
import com.fruitmall.recommend.feature.UserProfileService;
import com.fruitmall.recommend.mapper.FmRecommendTraceMapper;
import com.fruitmall.recommend.rank.RankedItem;
import com.fruitmall.recommend.rank.Ranker;
import com.fruitmall.recommend.recall.RecallStrategy;
import com.fruitmall.recommend.reason.ReasonGenerator;
import com.fruitmall.recommend.service.IRecommendService;
import com.fruitmall.recommend.vo.RecommendItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 推荐服务实现：编排"召回 → 过滤 → 打分 → 打散 → 理由 → 留痕"六个环节。
 *
 * 四个工程要点：
 * 1）召回通道由 Spring 注入的策略列表驱动，新增通道不必改本类；
 * 2）热度、特征、画像在同一请求内只算一次，保证各阶段口径一致；
 * 3）过滤掉下架、无库存与用户明确不感兴趣的商品；
 * 4）留痕写入失败只记日志，绝不影响推荐结果返回。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements IRecommendService {

    private static final String SCENE_HOME = "HOME";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private final ItemFeatureService itemFeatureService;
    private final UserProfileService userProfileService;
    private final FmUserBehaviorMapper behaviorMapper;
    private final FmRecommendTraceMapper traceMapper;
    private final List<RecallStrategy> recallStrategies;
    private final Ranker ranker;
    private final Diversifier diversifier;
    private final ReasonGenerator reasonGenerator;
    private final RecommendProperties properties;

    @Override
    public List<RecommendItemVO> recommendHome(int limit, String anonymousId) {
        // 游客的 memberId 为 null，画像服务会返回空画像，走冷启动分支
        Long memberId = UserContext.getUserId();
        UserProfile profile = userProfileService.buildProfile(memberId);
        RecommendContext context = buildContext(profile, memberId);

        // ① 召回：合并各通道结果，同时记录每个商品命中了哪些通道
        Map<Long, Set<String>> channelHits = new LinkedHashMap<>();
        for (RecallStrategy strategy : recallStrategies) {
            for (Long spuId : strategy.recall(context)) {
                channelHits.computeIfAbsent(spuId, key -> new LinkedHashSet<>()).add(strategy.name());
            }
        }

        // ② 过滤：无库存、已下架（特征里已只含上架商品）、用户明确不感兴趣
        List<ItemFeature> candidates = channelHits.keySet().stream()
                .map(context.getFeatureMap()::get)
                .filter(Objects::nonNull)
                .filter(feature -> feature.getAvailableStock() != null && feature.getAvailableStock() > 0)
                .filter(feature -> !profile.getDislikedSpuIds().contains(feature.getSpuId()))
                .toList();

        // ③ 打分并按分数降序
        List<RankedItem> rankedItems = new ArrayList<>();
        for (ItemFeature feature : candidates) {
            RankedItem ranked = ranker.rank(profile, feature, context);
            ranked.setRecallChannels(new ArrayList<>(
                    channelHits.getOrDefault(feature.getSpuId(), Set.of())));
            rankedItems.add(ranked);
        }
        rankedItems.sort(Comparator.comparingDouble(RankedItem::getScore).reversed());

        // ④ 多样性打散，避免结果集中在单一品类
        List<RankedItem> picked = diversifier.diversify(rankedItems, limit);

        // ⑤ 生成理由并转成出参
        List<RecommendItemVO> result = new ArrayList<>(picked.size());
        for (RankedItem ranked : picked) {
            result.add(toVO(ranked));
        }

        // ⑥ 留痕：失败不影响推荐结果
        saveTrace(context, memberId, anonymousId, result);
        if (log.isDebugEnabled()) {
            log.debug("首页推荐完成：memberId={}, 画像样本={}, 候选={}, 返回={}",
                    memberId, profile.getBehaviorCount(), candidates.size(), result.size());
        }
        return result;
    }

    private RecommendContext buildContext(UserProfile profile, Long memberId) {
        RecommendContext context = new RecommendContext();
        List<ItemFeature> features = itemFeatureService.listFeatures();
        context.setFeatures(features);
        context.setProfile(profile);
        context.setNow(LocalDateTime.now());
        context.setRecallLimit(properties.getRecallLimit());
        context.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        context.setAttrTotalCount(itemFeatureService.getNumericAttrCount());

        Map<Long, ItemFeature> featureMap = new HashMap<>();
        for (ItemFeature feature : features) {
            featureMap.put(feature.getSpuId(), feature);
        }
        context.setFeatureMap(featureMap);
        context.setHotScoreMap(buildHotScoreMap());
        return context;
    }

    /** 近 N 天的商品热度：行为次数 × 行为权重，再按最大值归一化到 [0,1] */
    private Map<Long, Double> buildHotScoreMap() {
        LocalDateTime since = LocalDateTime.now().minusDays(properties.getHotDays());
        List<BehaviorCountVO> rows = behaviorMapper.countByTargetAndBehavior(since);
        Map<Long, Double> raw = new HashMap<>();
        for (BehaviorCountVO row : rows) {
            BehaviorTypeEnum type = BehaviorTypeEnum.of(row.getBehavior());
            // 负向行为（不感兴趣）不算热度
            if (type == null || type.getWeight() <= 0D || row.getTargetId() == null) {
                continue;
            }
            raw.merge(row.getTargetId(), type.getWeight() * row.getCnt(), Double::sum);
        }
        if (raw.isEmpty()) {
            return Map.of();
        }
        double max = raw.values().stream().mapToDouble(Double::doubleValue).max().orElse(1D);
        Map<Long, Double> normalized = new HashMap<>();
        raw.forEach((spuId, value) -> normalized.put(spuId, max <= 0D ? 0D : value / max));
        return normalized;
    }

    private RecommendItemVO toVO(RankedItem ranked) {
        ItemFeature feature = ranked.getFeature();
        RecommendItemVO vo = new RecommendItemVO();
        vo.setSpuId(feature.getSpuId());
        vo.setSpuName(feature.getSpuName());
        vo.setSubtitle(feature.getSubtitle());
        vo.setMainImage(feature.getMainImage());
        vo.setCategoryId(feature.getCategoryId());
        vo.setCategoryName(feature.getCategoryName());
        vo.setOriginPlace(feature.getOriginPlace());
        vo.setSeasonMonths(feature.getSeasonMonths());
        vo.setMinPrice(feature.getMinPrice());
        vo.setAvailableStock(feature.getAvailableStock());
        vo.setScore(BigDecimal.valueOf(ranked.getScore()).setScale(4, RoundingMode.HALF_UP));
        vo.setReasonText(reasonGenerator.generate(ranked.getFactors()));
        vo.setFactors(ranked.getFactors());
        vo.setRecallChannels(ranked.getRecallChannels());
        return vo;
    }

    /** 写入推荐留痕，供"推荐可解释"与论文实验使用；失败只记日志 */
    private void saveTrace(RecommendContext context, Long memberId, String anonymousId,
                           List<RecommendItemVO> items) {
        if (items.isEmpty()) {
            return;
        }
        try {
            int rankNo = 1;
            for (RecommendItemVO item : items) {
                FmRecommendTrace trace = new FmRecommendTrace();
                trace.setRequestId(context.getRequestId());
                trace.setMemberId(memberId);
                trace.setAnonymousId(anonymousId);
                trace.setSpuId(item.getSpuId());
                trace.setScene(SCENE_HOME);
                trace.setRankNo(rankNo++);
                trace.setScore(item.getScore());
                trace.setFactorsJson(OBJECT_MAPPER.writeValueAsString(item.getFactors()));
                trace.setReasonText(item.getReasonText());
                trace.setConfigVersion(properties.getVersion());
                trace.setExposed(1);
                trace.setClicked(0);
                trace.setCreateTime(LocalDateTime.now());
                traceMapper.insert(trace);
            }
        } catch (Exception e) {
            log.warn("推荐留痕写入失败，不影响推荐结果", e);
        }
    }
}
