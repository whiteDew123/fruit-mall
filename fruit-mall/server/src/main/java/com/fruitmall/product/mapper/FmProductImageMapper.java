package com.fruitmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.product.domain.FmProductImage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 商品图片 Mapper。 */
@Mapper
public interface FmProductImageMapper extends BaseMapper<FmProductImage> {

    /** 物理删除商品图片，原因同属性值：子表整体替换，避免占用唯一索引与残留无效行。 */
    @Delete("DELETE FROM fm_product_image WHERE spu_id = #{spuId}")
    int deleteBySpuId(@Param("spuId") Long spuId);
}
