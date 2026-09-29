package com.fruitmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.query.ProductQuery;
import com.fruitmall.product.vo.ProductListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品 SPU Mapper。
 * 列表查询用标量子查询取出该商品的最低/最高售价与可售库存合计，
 * 避免 GROUP BY 在 ONLY_FULL_GROUP_BY 模式下的分组完整性限制。
 */
@Mapper
public interface FmProductSpuMapper extends BaseMapper<FmProductSpu> {

    /**
     * 商品分页列表，消费者端与商家后台共用。
     * 消费者端调用时由 Service 强制 status = 上架。
     */
    @Select("""
            <script>
            SELECT s.id, s.spu_code, s.spu_name, s.subtitle, s.category_id, c.category_name,
                   s.origin_place, s.season_months, s.unit, s.main_image, s.sales_count,
                   s.status, s.sort, s.create_time,
                   (SELECT MIN(k.price) FROM fm_product_sku k
                     WHERE k.spu_id = s.id AND k.deleted = 0 AND k.status = 10) AS min_price,
                   (SELECT MAX(k.price) FROM fm_product_sku k
                     WHERE k.spu_id = s.id AND k.deleted = 0 AND k.status = 10) AS max_price,
                   (SELECT IFNULL(SUM(k.stock - k.locked_stock), 0) FROM fm_product_sku k
                     WHERE k.spu_id = s.id AND k.deleted = 0 AND k.status = 10) AS available_stock
              FROM fm_product_spu s
              LEFT JOIN fm_category c ON c.id = s.category_id AND c.deleted = 0
             WHERE s.deleted = 0
               <if test="q.status != null">AND s.status = #{q.status}</if>
               <if test="q.categoryId != null">
                 AND (s.category_id = #{q.categoryId}
                      OR s.category_id IN (SELECT id FROM fm_category
                                            WHERE parent_id = #{q.categoryId} AND deleted = 0))
               </if>
               <if test="q.keyword != null and q.keyword != ''">
                 AND (s.spu_name LIKE CONCAT('%', #{q.keyword}, '%')
                      OR s.subtitle LIKE CONCAT('%', #{q.keyword}, '%')
                      OR s.origin_place LIKE CONCAT('%', #{q.keyword}, '%'))
               </if>
               <if test="q.minPrice != null">
                 AND EXISTS (SELECT 1 FROM fm_product_sku k2
                              WHERE k2.spu_id = s.id AND k2.deleted = 0 AND k2.status = 10
                                AND k2.price &gt;= #{q.minPrice})
               </if>
               <if test="q.maxPrice != null">
                 AND EXISTS (SELECT 1 FROM fm_product_sku k2
                              WHERE k2.spu_id = s.id AND k2.deleted = 0 AND k2.status = 10
                                AND k2.price &lt;= #{q.maxPrice})
               </if>
             <choose>
               <when test="q.sortBy == 'priceAsc'">ORDER BY min_price ASC, s.sort ASC, s.id DESC</when>
               <when test="q.sortBy == 'priceDesc'">ORDER BY min_price DESC, s.sort ASC, s.id DESC</when>
               <when test="q.sortBy == 'new'">ORDER BY s.create_time DESC, s.id DESC</when>
               <otherwise>ORDER BY s.sales_count DESC, s.sort ASC, s.id DESC</otherwise>
             </choose>
            </script>
            """)
    IPage<ProductListVO> selectProductPage(IPage<ProductListVO> page, @Param("q") ProductQuery query);
}
