package com.fruitmall.recommend.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fruitmall.common.enums.ProductStatusEnum;
import com.fruitmall.common.util.SimilarityUtil;
import com.fruitmall.product.domain.FmCategory;
import com.fruitmall.product.domain.FmProductAttrDef;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.mapper.FmCategoryMapper;
import com.fruitmall.product.mapper.FmProductAttrDefMapper;
import com.fruitmall.product.mapper.FmProductAttrValueMapper;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import com.fruitmall.product.mapper.FmProductSpuMapper;
import com.fruitmall.product.vo.AttrValueRowVO;
import com.fruitmall.product.vo.SkuSummaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商品特征服务：构建并缓存全部上架商品的特征向量。
 * 1）向量维度 = 数值型特色属性（按属性排序）+ 价格档位，属性越丰富向量越有区分度；
 * 2）各维度用全量商品做 min-max 归一化，保证量纲一致；
 * 3）特征做 30 秒内存缓存，避免每次请求重复查库与重复归一化。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemFeatureService {

    /** 特征缓存有效期（毫秒）：商品建档或调价后最迟 30 秒生效 */
    private static final long CACHE_TTL_MILLIS = 30_000L;

    private final FmProductSpuMapper spuMapper;
    private final FmProductSkuMapper skuMapper;
    private final FmProductAttrDefMapper attrDefMapper;
    private final FmProductAttrValueMapper attrValueMapper;
    private final FmCategoryMapper categoryMapper;

    private volatile List<ItemFeature> cachedFeatures = List.of();
    private volatile long cachedAt = 0L;
    private volatile int numericAttrCount = 0;

    /** 数值型属性总数，用于计算商品的信息完备度 */
    public int getNumericAttrCount() {
        listFeatures();
        return numericAttrCount;
    }

    /** 获取全部上架商品的特征，命中缓存时直接返回 */
    public List<ItemFeature> listFeatures() {
        if (System.currentTimeMillis() - cachedAt < CACHE_TTL_MILLIS && !cachedFeatures.isEmpty()) {
            return cachedFeatures;
        }
        synchronized (this) {
            if (System.currentTimeMillis() - cachedAt < CACHE_TTL_MILLIS && !cachedFeatures.isEmpty()) {
                return cachedFeatures;
            }
            cachedFeatures = buildFeatures();
            cachedAt = System.currentTimeMillis();
            return cachedFeatures;
        }
    }

    /** 按商品ID取特征，不存在返回 null（例如商品已下架） */
    public ItemFeature getFeature(Long spuId) {
        if (spuId == null) {
            return null;
        }
        for (ItemFeature feature : listFeatures()) {
            if (feature.getSpuId().equals(spuId)) {
                return feature;
            }
        }
        return null;
    }

    /** 手工失效缓存：商品建档、编辑、上下架后调用，让特征立即重建 */
    public void invalidate() {
        cachedAt = 0L;
    }

    private List<ItemFeature> buildFeatures() {
        List<FmProductSpu> spus = spuMapper.selectList(Wrappers.<FmProductSpu>lambdaQuery()
                .eq(FmProductSpu::getStatus, ProductStatusEnum.ON_SALE.getCode()));
        if (spus.isEmpty()) {
            return List.of();
        }

        List<String> attrCodes = attrDefMapper.selectList(Wrappers.<FmProductAttrDef>lambdaQuery()
                        .eq(FmProductAttrDef::getDataType, 10)
                        .eq(FmProductAttrDef::getStatus, 10)
                        .orderByAsc(FmProductAttrDef::getSort)
                        .orderByAsc(FmProductAttrDef::getId))
                .stream().map(FmProductAttrDef::getAttrCode).toList();

        Map<Long, String> categoryNames = new HashMap<>();
        for (FmCategory category : categoryMapper.selectList(Wrappers.<FmCategory>lambdaQuery())) {
            categoryNames.put(category.getId(), category.getCategoryName());
        }

        Map<Long, SkuSummaryVO> skuSummary = new HashMap<>();
        for (SkuSummaryVO summary : skuMapper.selectSkuSummary()) {
            skuSummary.put(summary.getSpuId(), summary);
        }

        Map<Long, Map<String, Double>> attrValueMap = new HashMap<>();
        for (AttrValueRowVO row : attrValueMapper.selectNumericAttrValues()) {
            attrValueMap.computeIfAbsent(row.getSpuId(), key -> new HashMap<>())
                    .put(row.getAttrCode(), row.getNumValue().doubleValue());
        }

        Map<String, double[]> rawColumns = new LinkedHashMap<>();
        for (String attrCode : attrCodes) {
            rawColumns.put(attrCode, new double[spus.size()]);
        }
        double[] rawPrices = new double[spus.size()];

        for (int i = 0; i < spus.size(); i++) {
            FmProductSpu spu = spus.get(i);
            Map<String, Double> values = attrValueMap.getOrDefault(spu.getId(), Map.of());
            for (String attrCode : attrCodes) {
                // 缺失的属性值按 0 处理：既不抬高也不虚构该维度
                rawColumns.get(attrCode)[i] = values.getOrDefault(attrCode, 0D);
            }
            SkuSummaryVO summary = skuSummary.get(spu.getId());
            rawPrices[i] = summary == null || summary.getMinPrice() == null
                    ? 0D : summary.getMinPrice().doubleValue();
        }

        Map<String, double[]> normalized = new HashMap<>();
        for (Map.Entry<String, double[]> entry : rawColumns.entrySet()) {
            normalized.put(entry.getKey(), SimilarityUtil.minMaxNormalize(entry.getValue()));
        }
        double[] priceNorm = SimilarityUtil.minMaxNormalize(rawPrices);

        List<ItemFeature> features = new ArrayList<>(spus.size());
        for (int i = 0; i < spus.size(); i++) {
            FmProductSpu spu = spus.get(i);
            SkuSummaryVO summary = skuSummary.get(spu.getId());
            ItemFeature feature = new ItemFeature();
            feature.setSpuId(spu.getId());
            feature.setSpuName(spu.getSpuName());
            feature.setSubtitle(spu.getSubtitle());
            feature.setMainImage(spu.getMainImage());
            feature.setOriginPlace(spu.getOriginPlace());
            feature.setSeasonMonths(spu.getSeasonMonths());
            feature.setCategoryId(spu.getCategoryId());
            feature.setCategoryName(categoryNames.get(spu.getCategoryId()));
            feature.setSalesCount(spu.getSalesCount() == null ? 0 : spu.getSalesCount());
            feature.setShelfLifeDays(spu.getShelfLifeDays());
            // 该商品已填写的数值属性个数，用于品质因素里的信息完备度
            feature.setAttrFilledCount(attrValueMap.getOrDefault(spu.getId(), Map.of()).size());
            feature.setMinPrice(summary == null ? null : summary.getMinPrice());
            feature.setAvailableStock(summary == null || summary.getAvailableStock() == null
                    ? 0 : summary.getAvailableStock());
            feature.setPriceNorm(priceNorm[i]);
            feature.setSeasonMonthSet(parseSeasonMonths(spu.getSeasonMonths()));

            double[] vector = new double[attrCodes.size() + 1];
            for (int d = 0; d < attrCodes.size(); d++) {
                vector[d] = normalized.get(attrCodes.get(d))[i];
            }
            vector[attrCodes.size()] = priceNorm[i];
            feature.setVector(vector);
            features.add(feature);
        }
        numericAttrCount = attrCodes.size();
        log.info("商品特征构建完成：{} 个上架商品，向量维度 {}（{} 个数值属性 + 价格）",
                features.size(), attrCodes.size() + 1, attrCodes.size());
        return features;
    }

    /** 把 "5,6,7" 解析成月份集合 */
    private Set<Integer> parseSeasonMonths(String seasonMonths) {
        Set<Integer> months = new HashSet<>();
        if (!StringUtils.hasText(seasonMonths)) {
            return months;
        }
        for (String part : seasonMonths.split(",")) {
            try {
                months.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
                // 非法月份忽略，不影响其它维度
            }
        }
        return months;
    }
}
