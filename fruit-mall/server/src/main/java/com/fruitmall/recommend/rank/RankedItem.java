package com.fruitmall.recommend.rank;

import com.fruitmall.recommend.feature.ItemFeature;
import com.fruitmall.recommend.vo.RecommendFactorVO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 排序结果：商品、综合得分、各因素明细与命中的召回通道。 */
@Data
public class RankedItem {

    private ItemFeature feature;

    private double score;

    private List<RecommendFactorVO> factors = new ArrayList<>();

    private List<String> recallChannels = new ArrayList<>();
}
