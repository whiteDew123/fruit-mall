package com.fruitmall.cart.controller;

import com.fruitmall.cart.dto.CartAddDTO;
import com.fruitmall.cart.dto.CartQuantityDTO;
import com.fruitmall.cart.service.IFmCartItemService;
import com.fruitmall.cart.vo.CartVO;
import com.fruitmall.common.result.Result;
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

/** 消费者端 —— 购物车。需要会员登录。 */
@Tag(name = "消费者端-购物车")
@RestController
@RequestMapping("/api/shop/cart")
@RequiredArgsConstructor
public class CartController {

    private final IFmCartItemService fmCartItemService;

    @Operation(summary = "购物车明细与汇总")
    @GetMapping("/list")
    public Result<CartVO> list() {
        return Result.ok(fmCartItemService.getCart());
    }

    @Operation(summary = "购物车角标数量")
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.ok(fmCartItemService.countMine());
    }

    @Operation(summary = "加入购物车", description = "同一规格重复加购会累加数量")
    @PostMapping("/add")
    public Result<Long> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.ok("已加入购物车", fmCartItemService.addToCart(dto));
    }

    @Operation(summary = "修改数量")
    @PutMapping("/{id}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityDTO dto) {
        fmCartItemService.updateQuantity(id, dto.getQuantity());
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "修改选中状态")
    @PutMapping("/{id}/selected")
    public Result<Void> updateSelected(@PathVariable Long id, @RequestParam Boolean selected) {
        fmCartItemService.updateSelected(id, selected);
        return Result.ok("操作成功", null);
    }

    @Operation(summary = "全选 / 取消全选", description = "只影响有效项")
    @PutMapping("/selected/all")
    public Result<Void> selectAll(@RequestParam Boolean selected) {
        fmCartItemService.selectAll(selected);
        return Result.ok("操作成功", null);
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        fmCartItemService.remove(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "清空失效商品")
    @DeleteMapping("/invalid")
    public Result<Void> clearInvalid() {
        fmCartItemService.clearInvalid();
        return Result.ok("已清空失效商品", null);
    }
}
