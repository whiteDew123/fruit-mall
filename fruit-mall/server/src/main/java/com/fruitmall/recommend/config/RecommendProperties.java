package com.fruitmall.recommend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 推荐权重方案与算法参数。
 * 权重写在 application.yml 里并带版本号，留痕时记录该版本号，
 * 后续做"人工调整推荐策略"时只需发布新版本配置，不必改代码。
 */
@Data
@Component
@ConfigurationProperties(prefix = "fruit-mall.recommend")
public class RecommendProperties {

    /** 权重方案版本号，写入推荐留痕，便于对比与回溯 */
    private String version = "v1";

    /** 行为时间衰减半衰期（天）：越久远的行为对画像影响越小 */
    private int halfLifeDays = 14;

    /** 参与画像聚合的行为回溯天数 */
    private int behaviorWindowDays = 30;

    /** 热度统计窗口（天） */
    private int hotDays = 7;

    /** 召回阶段候选集上限 */
    private int recallLimit = 100;

    /** 各打分因素的权重，key 为因素编码 */
    private Map<String, Double> weights = new LinkedHashMap<>();

    /** 取某个因素的权重，未配置返回 0（等价于该因素不参与打分） */
    public double weightOf(String factorCode) {
        return weights.getOrDefault(factorCode, 0D);
    }
}
