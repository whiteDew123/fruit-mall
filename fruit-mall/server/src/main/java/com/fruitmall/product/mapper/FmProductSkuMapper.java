package com.fruitmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.product.domain.FmProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 SKU Mapper。
 * 库存相关的写操作一律走条件更新（WHERE 中带库存判断），通过影响行数判断是否成功，
 * 这是本项目防止超卖的核心手段，禁止"先查再减"。
 */
@Mapper
public interface FmProductSkuMapper extends BaseMapper<FmProductSku> {

    /**
     * 下单预占：可售库存减少、预占库存增加。
     * `stock - locked_stock >= 数量` 与更新在同一条 SQL 中完成，由数据库保证原子性；
     * 返回 0 表示库存不足，调用方必须整单回滚。
     */
    @Update("""
            UPDATE fm_product_sku
               SET stock = stock - #{quantity},
                   locked_stock = locked_stock + #{quantity}
             WHERE id = #{skuId}
               AND deleted = 0
               AND status = 10
               AND stock - locked_stock >= #{quantity}
            """)
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 支付成功实扣：核销预占库存并累加销量。
     */
    @Update("""
            UPDATE fm_product_sku
               SET locked_stock = locked_stock - #{quantity},
                   sales_count = sales_count + #{quantity}
             WHERE id = #{skuId}
               AND deleted = 0
               AND locked_stock >= #{quantity}
            """)
    int confirmDeduct(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /**
     * 取消或超时关单：释放预占，可售库存恢复。
     */
    @Update("""
            UPDATE fm_product_sku
               SET stock = stock + #{quantity},
                   locked_stock = locked_stock - #{quantity}
             WHERE id = #{skuId}
               AND deleted = 0
               AND locked_stock >= #{quantity}
            """)
    int releaseStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
