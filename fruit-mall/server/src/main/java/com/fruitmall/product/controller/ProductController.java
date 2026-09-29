package com.fruitmall.product.controller;

import com.fruitmall.common.enums.ProductStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.product.query.ProductQuery;
import com.fruitmall.product.service.IFmProductSpuService;
import com.fruitmall.product.vo.ProductDetailVO;
import com.fruitmall.product.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 消费者端 —— 商品浏览（公开接口）。 */
@Tag(name = "消费者端-商品")
@RestController
@RequestMapping("/api/shop/product")
@RequiredArgsConstructor
public class ProductController {

    private final IFmProductSpuService fmProductSpuService;

    @Operation(summary = "商品列表", description = "只返回上架商品，支持分类、关键词、价格区间筛选与排序")
    @GetMapping("/list")
    public Result<PageResult<ProductListVO>> list(@Valid ProductQuery query) {
        // 消费者端强制只查上架商品，不信任前端传入的状态
        query.setStatus(ProductStatusEnum.ON_SALE.getCode());
        return Result.ok(fmProductSpuService.pageProducts(query));
    }

    @Operation(summary = "商品详情", description = "含规格、特色属性与详情图；未上架商品前台不可见")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        ProductDetailVO detail = fmProductSpuService.getDetail(id);
        if (!ProductStatusEnum.ON_SALE.getCode().equals(detail.getStatus())) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        return Result.ok(detail);
    }
}
