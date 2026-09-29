package com.fruitmall.cart.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.cart.domain.FmCartItem;
import com.fruitmall.cart.dto.CartAddDTO;
import com.fruitmall.cart.mapper.FmCartItemMapper;
import com.fruitmall.cart.service.IFmCartItemService;
import com.fruitmall.cart.vo.CartItemVO;
import com.fruitmall.cart.vo.CartSummaryVO;
import com.fruitmall.cart.vo.CartVO;
import com.fruitmall.common.enums.CartItemStatusEnum;
import com.fruitmall.common.enums.ProductStatusEnum;
import com.fruitmall.common.enums.SkuStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.product.domain.FmProductSku;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.mapper.FmProductSkuMapper;
import com.fruitmall.product.mapper.FmProductSpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** 购物车服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmCartItemServiceImpl extends ServiceImpl<FmCartItemMapper, FmCartItem>
        implements IFmCartItemService {

    private static final int SELECTED_YES = 1;
    private static final int SELECTED_NO = 0;

    private final FmProductSkuMapper skuMapper;
    private final FmProductSpuMapper spuMapper;

    @Override
    public CartVO getCart() {
        Long memberId = UserContext.getRequiredMemberId();
        List<CartItemVO> items = decorate(memberId);
        CartVO vo = new CartVO();
        vo.setItems(items);
        vo.setSummary(summarize(items));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long addToCart(CartAddDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();
        FmProductSku sku = skuMapper.selectById(dto.getSkuId());
        if (sku == null || !SkuStatusEnum.ENABLED.getCode().equals(sku.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "商品规格不存在或已停用");
        }
        FmProductSpu spu = spuMapper.selectById(sku.getSpuId());
        if (spu == null || !ProductStatusEnum.ON_SALE.getCode().equals(spu.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "商品已下架");
        }
        int available = availableStock(sku);
        if (available <= 0) {
            throw new BizException(ResultCode.CONFLICT, "商品库存不足");
        }

        FmCartItem exists = this.getOne(Wrappers.<FmCartItem>lambdaQuery()
                .eq(FmCartItem::getMemberId, memberId)
                .eq(FmCartItem::getSkuId, dto.getSkuId()));
        int targetQuantity = dto.getQuantity() + (exists == null ? 0 : exists.getQuantity());
        if (targetQuantity > available) {
            throw new BizException(ResultCode.CONFLICT, "库存不足，最多可购 " + available + " 件");
        }

        if (exists != null) {
            FmCartItem update = new FmCartItem();
            update.setId(exists.getId());
            update.setQuantity(targetQuantity);
            update.setSelected(SELECTED_YES);
            update.setStatus(CartItemStatusEnum.NORMAL.getCode());
            this.updateById(update);
            return exists.getId();
        }

        FmCartItem item = new FmCartItem();
        item.setMemberId(memberId);
        item.setSpuId(spu.getId());
        item.setSkuId(sku.getId());
        item.setQuantity(dto.getQuantity());
        item.setSelected(SELECTED_YES);
        item.setStatus(CartItemStatusEnum.NORMAL.getCode());
        this.save(item);
        return item.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateQuantity(Long cartItemId, Integer quantity) {
        FmCartItem item = requireMine(cartItemId);
        FmProductSku sku = skuMapper.selectById(item.getSkuId());
        if (sku == null || !SkuStatusEnum.ENABLED.getCode().equals(sku.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "该规格已停用，请先移除");
        }
        int available = availableStock(sku);
        if (quantity > available) {
            throw new BizException(ResultCode.CONFLICT, "库存不足，最多可购 " + available + " 件");
        }
        FmCartItem update = new FmCartItem();
        update.setId(item.getId());
        update.setQuantity(quantity);
        this.updateById(update);
    }

    @Override
    public void updateSelected(Long cartItemId, boolean selected) {
        FmCartItem item = requireMine(cartItemId);
        FmCartItem update = new FmCartItem();
        update.setId(item.getId());
        update.setSelected(selected ? SELECTED_YES : SELECTED_NO);
        this.updateById(update);
    }

    @Override
    public void selectAll(boolean selected) {
        Long memberId = UserContext.getRequiredMemberId();
        FmCartItem update = new FmCartItem();
        update.setSelected(selected ? SELECTED_YES : SELECTED_NO);
        this.update(update, Wrappers.<FmCartItem>lambdaUpdate()
                .eq(FmCartItem::getMemberId, memberId)
                .eq(FmCartItem::getStatus, CartItemStatusEnum.NORMAL.getCode()));
    }

    @Override
    public void remove(Long cartItemId) {
        requireMine(cartItemId);
        baseMapper.deleteByIdAndMember(cartItemId, UserContext.getRequiredMemberId());
    }

    @Override
    public void clearInvalid() {
        baseMapper.deleteInvalidByMember(UserContext.getRequiredMemberId());
    }

    @Override
    public Integer countMine() {
        Integer count = baseMapper.sumValidQuantity(UserContext.getRequiredMemberId());
        return count == null ? 0 : count;
    }

    @Override
    public List<CartItemVO> listSelectedForOrder(Long memberId) {
        return decorate(memberId).stream()
                .filter(item -> SELECTED_YES == (item.getSelected() == null ? SELECTED_NO : item.getSelected()))
                .filter(item -> CartItemStatusEnum.NORMAL.getCode().equals(item.getStatus()))
                .toList();
    }

    /** 校验购物车项属于当前登录会员，防止越权操作他人购物车 */
    private FmCartItem requireMine(Long cartItemId) {
        Long memberId = UserContext.getRequiredMemberId();
        FmCartItem item = this.getById(cartItemId);
        if (item == null || !memberId.equals(item.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "购物车项不存在");
        }
        return item;
    }

    /**
     * 补齐展示字段并同步失效状态：
     * 商品下架或规格停用则标记失效；库存不足仍然有效，由结算环节拦截。
     */
    private List<CartItemVO> decorate(Long memberId) {
        List<CartItemVO> items = baseMapper.selectCartItems(memberId);
        for (CartItemVO item : items) {
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            item.setSubtotal(item.getPrice() == null
                    ? BigDecimal.ZERO
                    : item.getPrice().multiply(BigDecimal.valueOf(quantity)));

            String reason = null;
            if (item.getSpuName() == null) {
                reason = "商品已删除";
            } else if (!ProductStatusEnum.ON_SALE.getCode().equals(item.getSpuStatus())) {
                reason = "商品已下架";
            } else if (!SkuStatusEnum.ENABLED.getCode().equals(item.getSkuStatus())) {
                reason = "规格已停用";
            }
            int status = reason == null
                    ? CartItemStatusEnum.NORMAL.getCode()
                    : CartItemStatusEnum.INVALID.getCode();
            item.setStatus(status);
            item.setInvalidReason(reason);
            syncStatus(item.getId(), status);
        }
        return items;
    }

    /** 展示时发现的失效状态回写数据库，保证购物车数据与展示一致 */
    private void syncStatus(Long cartItemId, int status) {
        FmCartItem item = this.getById(cartItemId);
        if (item != null && !Integer.valueOf(status).equals(item.getStatus())) {
            FmCartItem update = new FmCartItem();
            update.setId(cartItemId);
            update.setStatus(status);
            this.updateById(update);
        }
    }

    private CartSummaryVO summarize(List<CartItemVO> items) {
        CartSummaryVO summary = new CartSummaryVO();
        summary.setItemCount(items.size());
        int selectedQuantity = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;
        int validCount = 0;
        int selectedValidCount = 0;
        for (CartItemVO item : items) {
            if (!CartItemStatusEnum.NORMAL.getCode().equals(item.getStatus())) {
                continue;
            }
            validCount++;
            if (SELECTED_YES == (item.getSelected() == null ? SELECTED_NO : item.getSelected())) {
                selectedValidCount++;
                selectedQuantity += item.getQuantity() == null ? 0 : item.getQuantity();
                selectedAmount = selectedAmount.add(item.getSubtotal() == null
                        ? BigDecimal.ZERO : item.getSubtotal());
            }
        }
        summary.setSelectedQuantity(selectedQuantity);
        summary.setSelectedAmount(selectedAmount);
        summary.setAllSelected(validCount > 0 && validCount == selectedValidCount);
        return summary;
    }

    private int availableStock(FmProductSku sku) {
        int stock = sku.getStock() == null ? 0 : sku.getStock();
        int locked = sku.getLockedStock() == null ? 0 : sku.getLockedStock();
        return stock - locked;
    }
}
