package com.fruitmall.order.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 订单项。商品名称、规格、单价均为下单时的快照。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_order_item")
public class FmOrderItem extends BaseEntity {

    /** 订单ID */
    private Long orderId;

    /** 订单号（冗余，便于按单号查询） */
    private String orderNo;

    /** 商品SPU ID */
    private Long spuId;

    /** 规格ID */
    private Long skuId;

    /** 商品名称（快照） */
    private String spuName;

    /** 规格名称（快照） */
    private String skuName;

    /** 规格与属性快照（JSON） */
    private String skuSnapshot;

    /** 商品图片（快照） */
    private String image;

    /** 成交单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal amount;

    /** 累计已售后数量 */
    private Integer afterSaleQuantity;

    /** 是否已评价：0 否 / 1 是 */
    private Integer reviewed;
}
