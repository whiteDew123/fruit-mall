package com.fruitmall.fulfillment.controller;

import com.fruitmall.common.result.Result;
import com.fruitmall.fulfillment.service.IFmFulfillmentOrderService;
import com.fruitmall.fulfillment.vo.FulfillmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 消费者端 —— 履约进度。需要会员登录，只能查看自己订单的履约信息。 */
@Tag(name = "消费者端-履约")
@RestController
@RequestMapping("/api/shop/fulfillment")
@RequiredArgsConstructor
public class FulfillmentController {

    private final IFmFulfillmentOrderService fmFulfillmentOrderService;

    @Operation(summary = "订单履约进度", description = "返回履约状态与配送时间轴，供前台展示进度条")
    @GetMapping("/{orderId}")
    public Result<FulfillmentVO> getByOrderId(@PathVariable Long orderId) {
        return Result.ok(fmFulfillmentOrderService.getByOrderIdForMember(orderId));
    }
}
