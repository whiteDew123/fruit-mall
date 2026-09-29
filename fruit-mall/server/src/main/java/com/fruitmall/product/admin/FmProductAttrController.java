package com.fruitmall.product.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.Result;
import com.fruitmall.product.domain.FmProductAttrDef;
import com.fruitmall.product.dto.AttrDefSaveDTO;
import com.fruitmall.product.service.IFmProductAttrDefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 商家后台 —— 商品特色属性定义。 */
@Tag(name = "商品管理-特色属性")
@RestController
@RequestMapping("/api/admin/product/attr")
@RequiredArgsConstructor
public class FmProductAttrController {

    private final IFmProductAttrDefService fmProductAttrDefService;

    @Operation(summary = "属性定义列表")
    @RequiresPermission("product:list")
    @GetMapping("/list")
    public Result<List<FmProductAttrDef>> list() {
        return Result.ok(fmProductAttrDefService.listEnabled());
    }

    @Operation(summary = "新增属性定义")
    @RequiresPermission("product:attr:update")
    @OperLog(module = "商品管理", action = "新增特色属性")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody AttrDefSaveDTO dto) {
        return Result.ok("新增成功", fmProductAttrDefService.create(dto));
    }

    @Operation(summary = "编辑属性定义")
    @RequiresPermission("product:attr:update")
    @OperLog(module = "商品管理", action = "编辑特色属性")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AttrDefSaveDTO dto) {
        fmProductAttrDefService.update(id, dto);
        return Result.ok("修改成功", null);
    }
}
