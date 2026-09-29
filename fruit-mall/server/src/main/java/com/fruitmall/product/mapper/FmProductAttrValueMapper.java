package com.fruitmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.product.domain.FmProductAttrValue;
import com.fruitmall.product.vo.ProductAttrVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 特色属性值 Mapper。 */
@Mapper
public interface FmProductAttrValueMapper extends BaseMapper<FmProductAttrValue> {

    /**
     * 物理删除商品的属性值。
     * 属性值是"整体替换"的子表数据，没有外部引用；
     * 若用逻辑删除，旧行仍会占用唯一索引 uk_spu_attr，导致重新写入同一属性时报重复键。
     */
    @Delete("DELETE FROM fm_product_attr_value WHERE spu_id = #{spuId}")
    int deleteBySpuId(@Param("spuId") Long spuId);

    /**
     * 查询商品的特色属性取值，连带属性定义信息（名称、单位、数据类型）一起返回。
     */
    @Select("""
            SELECT v.attr_def_id, d.attr_code, d.attr_name, d.data_type, d.unit,
                   v.attr_value, v.num_value
              FROM fm_product_attr_value v
              JOIN fm_product_attr_def d ON d.id = v.attr_def_id AND d.deleted = 0
             WHERE v.spu_id = #{spuId} AND v.deleted = 0
             ORDER BY d.sort, d.id
            """)
    List<ProductAttrVO> selectAttrsBySpuId(@Param("spuId") Long spuId);
}
