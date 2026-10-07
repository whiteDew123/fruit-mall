package com.fruitmall.aftersale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.aftersale.domain.FmAfterSale;
import com.fruitmall.aftersale.dto.AfterSaleApplyDTO;
import com.fruitmall.aftersale.query.AfterSaleQuery;
import com.fruitmall.aftersale.vo.AfterSaleListVO;
import com.fruitmall.aftersale.vo.AfterSaleVO;
import com.fruitmall.common.result.PageResult;

/** 售后服务。 */
public interface IFmAfterSaleService extends IService<FmAfterSale> {

    /**
     * 会员申请售后。
     * 校验：订单必须已支付（已支付或已完成）、可售后数量 ≤ 订单项数量 − 已售后数量（R-08）。
     */
    Long apply(AfterSaleApplyDTO dto);

    /** 我的售后单分页 */
    PageResult<AfterSaleListVO> pageMine(AfterSaleQuery query);

    /** 我的售后单详情 */
    AfterSaleVO detailMine(Long id);

    /** 会员撤销申请：仅待审核可撤销，撤销后归还已占用的可售后数量 */
    void cancelByMember(Long id, String reason);

    /** 后台售后单分页 */
    PageResult<AfterSaleListVO> pageAll(AfterSaleQuery query);

    /** 后台售后单详情 */
    AfterSaleVO detailForAdmin(Long id);

    /**
     * 商家审核。通过时：仅退款直接进入退款中，退货退款进入退货中；驳回则归还已占用的可售后数量。
     */
    void audit(Long id, boolean pass, String remark);

    /**
     * 商家确认收到退货：退货中 → 退款中，并把退货数量记为退货回补流水（R-09，不进入可售库存）。
     */
    void receive(Long id, String remark);

    /** 商家确认退款完成：退款中 → 已完成，订单转为已退款 */
    void refund(Long id, String remark);
}
