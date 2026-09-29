package com.fruitmall.common.enums;

import lombok.Getter;

/**
 * 用户行为类型，取值与 fm_user_behavior.behavior 一致。
 * 用字符串而非编码：行为类型会被大量用于 SQL 统计与造数据脚本，可读性收益更大；
 * 但代码中一律引用本枚举常量，禁止直接写字符串字面量。
 */
@Getter
public enum BehaviorTypeEnum {

    /** 浏览商品详情 */
    VIEW("VIEW", "浏览", 1.0),
    /** 搜索点击 */
    SEARCH("SEARCH", "搜索", 1.5),
    /** 收藏 */
    FAVORITE("FAVORITE", "收藏", 2.5),
    /** 加入购物车 */
    CART("CART", "加购", 3.0),
    /** 下单 */
    ORDER("ORDER", "下单", 5.0),
    /** 评价 */
    REVIEW("REVIEW", "评价", 4.0),
    /** 不感兴趣 */
    DISLIKE("DISLIKE", "不感兴趣", -5.0);

    private final String code;
    private final String desc;

    /** 画像聚合时的行为权重，见 docs/04-推荐算法实现方案.md */
    private final double weight;

    BehaviorTypeEnum(String code, String desc, double weight) {
        this.code = code;
        this.desc = desc;
        this.weight = weight;
    }

    /** 按编码匹配，未匹配返回 null */
    public static BehaviorTypeEnum of(String code) {
        for (BehaviorTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
