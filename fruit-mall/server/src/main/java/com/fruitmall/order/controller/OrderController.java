package com.fruitmall.order.controller;

import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.order.dto.OrderSubmitDTO;
import com.fruitmall.order.query.OrderQuery;
import com.fruitmall.order.service.IFmOrdersService;
import com.fruitmall.order.vo.OrderDetailVO;
import com.fruitmall.order.vo.OrderListVO;
import com.fruitmall.order.vo.OrderPreviewVO;
import com.fruitmall.order.vo.OrderSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 消费者端 —— 订单。需要会员登录。 */
@Tag(name = "消费者端-订单")
@RestController
@RequestMapping("/api/shop/order")
@RequiredArgsConstructor
public class OrderController {

    private final IFmOrdersService fmOrdersService;

    @Operation(summary = "确认订单页数据", description = "返回默认地址、已勾选商品与金额；金额按实时价重算")
    @GetMapping("/preview")
    public Result<OrderPreviewVO> preview() {
        return Result.ok(fmOrdersService.preview());
    }

    @Operation(summary = "提交订单", description = "库存条件预占 + 订单落库 + 库存流水，任一商品库存不足整单失败")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@Valid @RequestBody OrderSubmitDTO dto) {
        return Result.ok("下单成功", fmOrdersService.submit(dto));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<PageResult<OrderListVO>> list(@Valid OrderQuery query) {
        return Result.ok(fmOrdersService.pageMine(query));
    }

    @Operation(summary = "我的订单详情", description = "含商品明细与状态时间轴")
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(fmOrdersService.detailMine(id));
    }

    @Operation(summary = "取消订单", description = "仅待支付订单可取消，取消后释放预占库存")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                              @RequestParam(required = false) String reason) {
        fmOrdersService.cancelByMember(id, reason);
        return Result.ok("订单已取消", null);
    }
}
