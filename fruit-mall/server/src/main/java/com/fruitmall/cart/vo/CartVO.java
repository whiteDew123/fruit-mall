package com.fruitmall.cart.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 购物车出参：明细 + 汇总，一次返回避免前端两次请求。 */
@Data
@Schema(description = "购物车")
public class CartVO {

    @Schema(description = "购物车明细")
    private List<CartItemVO> items = new ArrayList<>();

    @Schema(description = "汇总信息")
    private CartSummaryVO summary = new CartSummaryVO();
}
