package com.fruitmall.aftersale.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 售后明细：记录涉及的订单项、数量与退货暂存批次。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_after_sale_item")
public class FmAfterSaleItem extends BaseEntity {

    /** 售后单ID */
    private Long afterSaleId;

    /** 订单项ID */
    private Long orderItemId;

    /** 规格ID */
    private Long skuId;

    /** 售后数量 */
    private Integer quantity;

    /** 该明细退款金额 */
    private BigDecimal refundAmount;

    /** 退货暂存批次ID，生鲜退货不可二次销售，回补到暂存批次 */
    private Long returnBatchId;
}
