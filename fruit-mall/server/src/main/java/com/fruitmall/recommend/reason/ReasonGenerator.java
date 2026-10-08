package com.fruitmall.recommend.reason;

import com.fruitmall.recommend.vo.RecommendFactorVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 推荐理由生成：取贡献值最大的 1-2 个因素，拼成一句中文理由。
 * 这是"推荐可解释"的落地点，前台直接展示，同时写入推荐留痕表供论文取材。
 */
@Component
public class ReasonGenerator {

    /** 一条理由最多包含的因素个数，太多反而不利于阅读 */
    private static final int MAX_FACTORS = 2;

    public String generate(List<RecommendFactorVO> factors) {
        if (factors == null || factors.isEmpty()) {
            return "为你精选";
        }
        return factors.stream()
                .filter(factor -> factor.getContribution() != null
                        && factor.getContribution().doubleValue() > 0D)
                .limit(MAX_FACTORS)
                .map(RecommendFactorVO::getText)
                .reduce((left, right) -> left + "；" + right)
                .orElse("为你精选");
    }
}
