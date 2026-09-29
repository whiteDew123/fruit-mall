package com.fruitmall.product.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.Result;
import com.fruitmall.product.dto.CategorySaveDTO;
import com.fruitmall.product.service.IFmCategoryService;
import com.fruitmall.product.vo.CategoryVO;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家后台 —— 分类管理。 */
@Tag(name = "商品管理-分类")
@RestController
@RequestMapping("/api/admin/product/category")
@RequiredArgsConstructor
public class FmCategoryController {

    private final IFmCategoryService fmCategoryService;

    @Operation(summary = "分类树", description = "后台展示全部分类，含已停用")
    @RequiresPermission("category:list")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.ok(fmCategoryService.listTree(false));
    }

    @Operation(summary = "新增分类")
    @RequiresPermission("category:create")
    @OperLog(module = "商品管理", action = "新增分类")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CategorySaveDTO dto) {
        return Result.ok("新增成功", fmCategoryService.create(dto));
    }

    @Operation(summary = "编辑分类")
    @RequiresPermission("category:update")
    @OperLog(module = "商品管理", action = "编辑分类")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategorySaveDTO dto) {
        fmCategoryService.update(id, dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除分类", description = "存在子分类或已挂商品时拒绝删除")
    @RequiresPermission("category:delete")
    @OperLog(module = "商品管理", action = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fmCategoryService.delete(id);
        return Result.ok("删除成功", null);
    }
}
