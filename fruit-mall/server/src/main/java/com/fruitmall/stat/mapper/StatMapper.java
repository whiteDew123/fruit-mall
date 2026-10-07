package com.fruitmall.stat.mapper;

import com.fruitmall.stat.vo.CategoryRatioVO;
import com.fruitmall.stat.vo.ProductTopVO;
import com.fruitmall.stat.vo.SalesTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 经营分析统计 Mapper。
 *
 * 统计口径：只统计**有效成交**订单，即状态为已支付 20 / 备货中 30 / 配送中 40 / 已完成 50 的订单；
 * 待支付 10、已取消 60、退款中 70、已退款 80 的订单不计入销售额与销量。
 * 该口径与论文第 6 章的统计说明保持一致。
 */
@Mapper
public interface StatMapper {

    /** 近 N 天销售趋势：按支付日期聚合订单数与销售额 */
    @Select("""
            SELECT DATE_FORMAT(o.pay_time, '%Y-%m-%d') AS stat_date,
                   COUNT(DISTINCT o.id) AS order_count,
                   IFNULL(SUM(o.pay_amount), 0) AS amount
              FROM fm_orders o
             WHERE o.deleted = 0
               AND o.pay_time IS NOT NULL
               AND o.status IN (20, 30, 40, 50)
               AND o.pay_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
             GROUP BY DATE_FORMAT(o.pay_time, '%Y-%m-%d')
             ORDER BY stat_date
            """)
    List<SalesTrendVO> selectSalesTrend(@Param("days") Integer days);

    /** 品类销售占比：按分类聚合销售件数与金额 */
    @Select("""
            SELECT c.category_name AS category_name,
                   IFNULL(SUM(oi.quantity), 0) AS quantity,
                   IFNULL(SUM(oi.amount), 0) AS amount
              FROM fm_order_item oi
              JOIN fm_orders o ON o.id = oi.order_id
                              AND o.deleted = 0
                              AND o.status IN (20, 30, 40, 50)
              JOIN fm_product_spu s ON s.id = oi.spu_id
              JOIN fm_category c ON c.id = s.category_id
             WHERE oi.deleted = 0
             GROUP BY c.id, c.category_name
             ORDER BY amount DESC
            """)
    List<CategoryRatioVO> selectCategoryRatio();

    /** 商品销量 TOP：按商品聚合销售件数与金额 */
    @Select("""
            SELECT oi.spu_id AS spu_id,
                   oi.spu_name AS spu_name,
                   IFNULL(SUM(oi.quantity), 0) AS quantity,
                   IFNULL(SUM(oi.amount), 0) AS amount
              FROM fm_order_item oi
              JOIN fm_orders o ON o.id = oi.order_id
                              AND o.deleted = 0
                              AND o.status IN (20, 30, 40, 50)
             WHERE oi.deleted = 0
             GROUP BY oi.spu_id, oi.spu_name
             ORDER BY quantity DESC, amount DESC
             LIMIT #{limit}
            """)
    List<ProductTopVO> selectProductTop(@Param("limit") Integer limit);
}
