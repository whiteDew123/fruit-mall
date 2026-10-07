package com.fruitmall.aftersale.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 售后单。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_after_sale")
public class FmAfterSale extends BaseEntity {

    /** 售后单号 */
    private String afterSaleNo;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 会员ID */
    private Long memberId;

    /** 类型，见 AfterSaleTypeEnum */
    private Integer type;

    /** 状态，见 AfterSaleStatusEnum */
    private Integer status;

    /** 售后商品总数量 */
    private Integer quantity;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 申请原因 */
    private String reason;

    /** 凭证图片（JSON 数组字符串） */
    private String evidenceImages;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人ID */
    private Long auditorId;

    /** 审核意见 */
    private String auditRemark;

    /** 会员寄回时间 */
    private LocalDateTime returnTime;

    /** 商家收货时间 */
    private LocalDateTime receiveTime;

    /** 退款完成时间 */
    private LocalDateTime refundTime;

    /** 取消原因 */
    private String cancelReason;
}
