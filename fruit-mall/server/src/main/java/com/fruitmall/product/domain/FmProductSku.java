package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 商品 SKU：5 斤装、10 斤装等规格，价格与库存挂在这里。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_product_sku")
public class FmProductSku extends BaseEntity {

    /** 商品SPU ID */
    private Long spuId;

    /** 规格编码 */
    private String skuCode;

    /** 规格名，如 5斤装 */
    private String specName;

    /** 规格键值 JSON，如 {"净重":"5斤","果径":"80mm"} */
    private String specJson;

    /** 规格图片 */
    private String image;

    /** 划线价 */
    private BigDecimal originalPrice;

    /** 售价 */
    private BigDecimal price;

    /** 可售库存总量 */
    private Integer stock;

    /** 预占库存 */
    private Integer lockedStock;

    /** 低库存预警阈值 */
    private Integer warnStock;

    /** 累计销量 */
    private Integer salesCount;

    /** 状态，见 SkuStatusEnum */
    private Integer status;

    /** 乐观锁版本 */
    private Integer version;
}
