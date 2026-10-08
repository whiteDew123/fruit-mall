package com.fruitmall.recommend.feature;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户画像：推荐算法的用户侧输入。
 * 由行为日志按"行为权重 × 时间衰减"加权聚合得到，冷启动用户为空画像。
 */
@Data
public class UserProfile {

    /** 会员ID，游客为 null */
    private Long memberId;

    /** 偏好向量，维度与商品特征向量一致 */
    private double[] vector;

    /** 价格偏好中心（元），无行为时为 0，表示尚未形成价格偏好 */
    private double priceCenter;

    /** 是否空画像：新用户或游客，此时内容匹配因素不参与打分 */
    private boolean empty = true;

    /** 明确标记"不感兴趣"的商品，召回后直接过滤 */
    private Set<Long> dislikedSpuIds = new HashSet<>();

    /** 已下单商品，用于复购降权 */
    private Set<Long> purchasedSpuIds = new HashSet<>();

    /** 行为样本数，便于排查"为什么推荐没效果" */
    private int behaviorCount;
}
