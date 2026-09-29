package com.fruitmall.product.controller;

import com.fruitmall.common.result.Result;
import com.fruitmall.product.service.IFmCategoryService;
import com.fruitmall.product.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 消费者端 —— 分类导航（公开接口）。 */
@Tag(name = "消费者端-分类")
@RestController
@RequestMapping("/api/shop/category")
@RequiredArgsConstructor
public class CategoryController {

    private final IFmCategoryService fmCategoryService;

    @Operation(summary = "分类树", description = "只返回启用状态的分类，供首页与商品列表导航使用")
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.ok(fmCategoryService.listTree(true));
    }
}
