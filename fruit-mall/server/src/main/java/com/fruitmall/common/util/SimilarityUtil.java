package com.fruitmall.common.util;

/**
 * 相似度与归一化工具。
 * 推荐模块的向量运算全部在这里，纯 Java 实现，不引入任何机器学习库。
 */
public final class SimilarityUtil {

    private SimilarityUtil() {
    }

    /**
     * 余弦相似度，含防除零处理。
     * 任一侧为零向量（例如新用户没有行为、新商品没有属性）时返回 0，
     * 由调用方决定该维度是否参与打分，而不是把 0 当成"不相似"。
     */
    public static double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return 0D;
        }
        double dot = 0D;
        double normA = 0D;
        double normB = 0D;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0D || normB == 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 把一组数值线性映射到 [0,1]。全部相等时统一返回 1（都算最高热度），
     * 避免除零，也避免"只有一个商品"时热度恒为 0。
     */
    public static double[] minMaxNormalize(double[] values) {
        if (values == null || values.length == 0) {
            return new double[0];
        }
        double min = values[0];
        double max = values[0];
        for (double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double[] result = new double[values.length];
        if (max - min == 0D) {
            for (int i = 0; i < values.length; i++) {
                result[i] = 1D;
            }
            return result;
        }
        for (int i = 0; i < values.length; i++) {
            result[i] = (values[i] - min) / (max - min);
        }
        return result;
    }

    /** 把数值限制在 [0,1]，打分因子统一走这个方法，避免出现越界分值 */
    public static double clamp01(double value) {
        if (Double.isNaN(value)) {
            return 0D;
        }
        return Math.max(0D, Math.min(1D, value));
    }
}
