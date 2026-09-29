package com.fruitmall.cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.cart.domain.FmCartItem;
import com.fruitmall.cart.dto.CartAddDTO;
import com.fruitmall.cart.vo.CartItemVO;
import com.fruitmall.cart.vo.CartVO;

import java.util.List;

/** 购物车服务。所有方法只操作当前登录会员自己的购物车。 */
public interface IFmCartItemService extends IService<FmCartItem> {

    /** 购物车明细与汇总 */
    CartVO getCart();

    /** 加入购物车，同一规格累加数量 */
    Long addToCart(CartAddDTO dto);

    /** 修改数量 */
    void updateQuantity(Long cartItemId, Integer quantity);

    /** 修改选中状态 */
    void updateSelected(Long cartItemId, boolean selected);

    /** 全选 / 取消全选（只影响有效项） */
    void selectAll(boolean selected);

    /** 删除购物车项 */
    void remove(Long cartItemId);

    /** 清空失效项 */
    void clearInvalid();

    /** 有效商品件数，供购物车角标使用 */
    Integer countMine();

    /** 当前会员选中的有效项，供下单流程使用 */
    List<CartItemVO> listSelectedForOrder(Long memberId);
}
