package com.fruitmall.product.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.product.dto.ProductSaveDTO;
import com.fruitmall.product.query.ProductQuery;
import com.fruitmall.product.service.IFmProductSpuService;
import com.fruitmall.product.vo.ProductDetailVO;
import com.fruitmall.product.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商家后台 —— 商品管理。 */
@Tag(name = "商品管理-商品")
@RestController
@RequestMapping("/api/admin/product/spu")
@RequiredArgsConstructor
public class FmProductSpuController {

    private final IFmProductSpuService fmProductSpuService;

    @Operation(summary = "商品列表", description = "支持分类、关键词、价格区间、状态筛选与排序")
    @RequiresPermission("product:list")
    @GetMapping("/list")
    public Result<PageResult<ProductListVO>> list(@Valid ProductQuery query) {
        return Result.ok(fmProductSpuService.pageProducts(query));
    }

    @Operation(summary = "商品详情", description = "含规格、特色属性与详情图，供编辑页回填")
    @RequiresPermission("product:list")
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        return Result.ok(fmProductSpuService.getDetail(id));
    }

    @Operation(summary = "商品建档", description = "SPU、规格、特色属性、图片一次性提交，新建后为草稿状态")
    @RequiresPermission("product:create")
    @OperLog(module = "商品管理", action = "商品建档")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProductSaveDTO dto) {
        return Result.ok("建档成功", fmProductSpuService.createProduct(dto));
    }

    @Operation(summary = "商品编辑")
    @RequiresPermission("product:update")
    @OperLog(module = "商品管理", action = "商品编辑")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        fmProductSpuService.updateProduct(id, dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "商品上下架", description = "上架前校验主图、规格、售价、库存与必填特色属性")
    @RequiresPermission("product:status")
    @OperLog(module = "商品管理", action = "商品上下架")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        fmProductSpuService.changeStatus(id, status);
        return Result.ok("操作成功", null);
    }

    @Operation(summary = "删除商品", description = "逻辑删除商品及其规格、属性与图片")
    @RequiresPermission("product:delete")
    @OperLog(module = "商品管理", action = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fmProductSpuService.deleteProduct(id);
        return Result.ok("删除成功", null);
    }
}
