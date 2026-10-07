package com.fruitmall.review.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 订单评价。一个订单项只能评价一次，唯一索引 uk_order_item 兜底。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_review")
public class FmReview extends BaseEntity {

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 订单项ID */
    private Long orderItemId;

    /** 商品SPU ID */
    private Long spuId;

    /** 规格ID */
    private Long skuId;

    /** 会员ID */
    private Long memberId;

    /** 星级 1-5 */
    private Integer star;

    /** 评价内容 */
    private String content;

    /** 评价图片（JSON 数组字符串） */
    private String images;

    /** 是否匿名：0 否 / 1 是 */
    private Integer isAnonymous;

    /** 状态，见 ReviewStatusEnum */
    private Integer status;

    /** 商家回复 */
    private String replyContent;

    /** 回复时间 */
    private LocalDateTime replyTime;
}
