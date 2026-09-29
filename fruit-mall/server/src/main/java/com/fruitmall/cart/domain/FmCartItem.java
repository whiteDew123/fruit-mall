package com.fruitmall.cart.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 购物车项。购物车不存价格，价格以 SKU 实时价为准。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_cart_item")
public class FmCartItem extends BaseEntity {

    /** 会员ID */
    private Long memberId;

    /** 商品SPU ID（冗余，便于列表展示） */
    private Long spuId;

    /** 规格ID */
    private Long skuId;

    /** 数量 */
    private Integer quantity;

    /** 是否选中：0 否 / 1 是 */
    private Integer selected;

    /** 状态，见 CartItemStatusEnum */
    private Integer status;
}
