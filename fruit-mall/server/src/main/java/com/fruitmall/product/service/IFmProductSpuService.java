package com.fruitmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.dto.ProductSaveDTO;
import com.fruitmall.product.query.ProductQuery;
import com.fruitmall.product.vo.ProductDetailVO;
import com.fruitmall.product.vo.ProductListVO;
import com.fruitmall.product.vo.ProductSkuVO;

import java.util.List;

/** 商品服务。 */
public interface IFmProductSpuService extends IService<FmProductSpu> {

    /** 商品分页列表 */
    PageResult<ProductListVO> pageProducts(ProductQuery query);

    /** 商品详情，商品规格与特色属性一并返回 */
    ProductDetailVO getDetail(Long spuId);

    /** 商品规格列表 */
    List<ProductSkuVO> listSkus(Long spuId);

    /** 商品建档，SPU、规格、特色属性、图片一次性写入 */
    Long createProduct(ProductSaveDTO dto);

    /** 商品编辑 */
    void updateProduct(Long id, ProductSaveDTO dto);

    /**
     * 上下架。上架前校验：主图不为空、至少一个启用规格、售价大于 0、
     * 可售库存大于 0、必填特色属性已填写（对应 docs 中的一致性规则 R-01）。
     */
    void changeStatus(Long id, Integer status);

    /** 逻辑删除商品及其规格、属性、图片 */
    void deleteProduct(Long id);
}
