package com.fruitmall.recommend.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 推荐留痕：推荐结果可解释、可验证的证据。
 * 记录每个被推荐商品的各因素原始分、权重、贡献值与最终理由，
 * 只追加不修改，是论文实验数据与"人工调整推荐策略"效果对比的来源。
 */
@Data
@TableName("fm_recommend_trace")
public class FmRecommendTrace {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 请求标识，一次请求的多条留痕共用 */
    private String requestId;

    /** 会员ID，游客为 null */
    private Long memberId;

    /** 匿名标识，游客使用 */
    private String anonymousId;

    /** 被推荐商品SPU ID */
    private Long spuId;

    /** 场景：HOME 首页 / DETAIL 详情页 / CART 购物车 */
    private String scene;

    /** 排名，从 1 开始 */
    private Integer rankNo;

    /** 综合得分 */
    private BigDecimal score;

    /** 各因素原始值、权重与贡献值（JSON） */
    private String factorsJson;

    /** 推荐理由 */
    private String reasonText;

    /** 权重方案版本 */
    private String configVersion;

    /** 是否曝光：0 否 / 1 是 */
    private Integer exposed;

    /** 是否点击：0 否 / 1 是 */
    private Integer clicked;

    /** 生成时间 */
    private LocalDateTime createTime;
}
