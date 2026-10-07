package com.fruitmall.stat.admin;

import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.Result;
import com.fruitmall.stat.query.StatQuery;
import com.fruitmall.stat.service.IStatService;
import com.fruitmall.stat.vo.CategoryRatioVO;
import com.fruitmall.stat.vo.ProductTopVO;
import com.fruitmall.stat.vo.SalesTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商家后台 —— 经营分析。统计口径：只含已支付及之后的订单，退款与取消不计入。 */
@Tag(name = "经营分析")
@RestController
@RequestMapping("/api/admin/stat")
@RequiredArgsConstructor
public class StatController {

    private final IStatService statService;

    @Operation(summary = "销售趋势", description = "近 N 天的订单数与销售额，按支付日期聚合")
    @RequiresPermission("stat:sales")
    @GetMapping("/sales-trend")
    public Result<java.util.List<SalesTrendVO>> salesTrend(@Valid StatQuery query) {
        return Result.ok(statService.salesTrend(query));
    }

    @Operation(summary = "品类销售占比")
    @RequiresPermission("stat:category")
    @GetMapping("/category-ratio")
    public Result<java.util.List<CategoryRatioVO>> categoryRatio() {
        return Result.ok(statService.categoryRatio());
    }

    @Operation(summary = "商品销量排行")
    @RequiresPermission("stat:product")
    @GetMapping("/product-top")
    public Result<java.util.List<ProductTopVO>> productTop(@Valid StatQuery query) {
        return Result.ok(statService.productTop(query));
    }
}
