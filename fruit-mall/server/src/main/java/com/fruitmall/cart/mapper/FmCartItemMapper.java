package com.fruitmall.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.cart.domain.FmCartItem;
import com.fruitmall.cart.vo.CartItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 购物车 Mapper。 */
@Mapper
public interface FmCartItemMapper extends BaseMapper<FmCartItem> {

    /**
     * 查询会员购物车，连带商品与规格信息，供前端直接展示。
     * 商品或规格被删除时以左连接保留购物车行，由 Service 标记为失效并提示用户。
     */
    @Select("""
            SELECT ci.id, ci.spu_id, ci.sku_id, ci.quantity, ci.selected, ci.status,
                   s.spu_name, s.main_image, s.unit, s.status AS spu_status,
                   k.sku_code, k.spec_name, k.spec_json, k.price,
                   (k.stock - k.locked_stock) AS available_stock,
                   k.image AS sku_image, k.status AS sku_status
              FROM fm_cart_item ci
              LEFT JOIN fm_product_spu s ON s.id = ci.spu_id AND s.deleted = 0
              LEFT JOIN fm_product_sku k ON k.id = ci.sku_id AND k.deleted = 0
             WHERE ci.member_id = #{memberId} AND ci.deleted = 0
             ORDER BY ci.create_time DESC, ci.id DESC
            """)
    List<CartItemVO> selectCartItems(@Param("memberId") Long memberId);

    /**
     * 物理删除购物车项。
     * 购物车是临时数据，没有审计价值；若用逻辑删除，旧行会继续占用唯一索引
     * uk_member_sku，导致删除后无法再次加入同一规格。
     */
    @Delete("DELETE FROM fm_cart_item WHERE id = #{id} AND member_id = #{memberId}")
    int deleteByIdAndMember(@Param("id") Long id, @Param("memberId") Long memberId);

    /** 物理删除该会员的失效购物车项 */
    @Delete("DELETE FROM fm_cart_item WHERE member_id = #{memberId} AND status = 20")
    int deleteInvalidByMember(@Param("memberId") Long memberId);

    /** 统计有效且上架在售的商品件数，供购物车角标使用 */
    @Select("""
            SELECT IFNULL(SUM(ci.quantity), 0)
              FROM fm_cart_item ci
              JOIN fm_product_spu s ON s.id = ci.spu_id AND s.deleted = 0 AND s.status = 20
              JOIN fm_product_sku k ON k.id = ci.sku_id AND k.deleted = 0 AND k.status = 10
             WHERE ci.member_id = #{memberId} AND ci.deleted = 0
            """)
    Integer sumValidQuantity(@Param("memberId") Long memberId);
}
