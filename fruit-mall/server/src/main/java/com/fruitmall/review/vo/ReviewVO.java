package com.fruitmall.review.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 评价出参。 */
@Data
@Schema(description = "评价")
public class ReviewVO {

    @Schema(description = "评价ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "商品SPU ID")
    private Long spuId;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "规格ID")
    private Long skuId;

    @Schema(description = "星级 1-5")
    private Integer star;

    @Schema(description = "评价内容")
    private String content;

    @Schema(description = "评价图片 JSON 数组")
    private String images;

    @Schema(description = "是否匿名：0 否 / 1 是")
    private Integer isAnonymous;

    @Schema(description = "评价人昵称，匿名评价显示为匿名用户")
    private String memberNickname;

    @Schema(description = "状态：10 显示 / 20 隐藏")
    private Integer status;

    @Schema(description = "商家回复")
    private String replyContent;

    @Schema(description = "回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "评价时间")
    private LocalDateTime createTime;
}
