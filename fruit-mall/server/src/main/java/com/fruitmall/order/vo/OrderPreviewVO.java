package com.fruitmall.order.vo;

import com.fruitmall.member.vo.MemberAddressVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 确认订单页数据。 */
@Data
@Schema(description = "确认订单页数据")
public class OrderPreviewVO {

    @Schema(description = "默认收货地址，没有地址时为 null")
    private MemberAddressVO address;

    @Schema(description = "待下单商品（购物车中已勾选且有效的项）")
    private List<OrderPreviewItemVO> items = new ArrayList<>();

    @Schema(description = "商品金额合计")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Schema(description = "运费")
    private BigDecimal freightAmount = BigDecimal.ZERO;

    @Schema(description = "应付金额")
    private BigDecimal payAmount = BigDecimal.ZERO;
}
