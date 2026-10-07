package com.fruitmall.order.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.order.query.OrderQuery;
import com.fruitmall.order.service.IFmOrdersService;
import com.fruitmall.order.vo.OrderDetailVO;
import com.fruitmall.order.vo.OrderListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商家后台 —— 订单管理。 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class FmOrdersController {

    private final IFmOrdersService fmOrdersService;

    @Operation(summary = "订单列表", description = "支持状态、订单号、会员筛选，列表页手机号脱敏")
    @RequiresPermission("order:list")
    @GetMapping("/list")
    public Result<PageResult<OrderListVO>> list(@Valid OrderQuery query) {
        return Result.ok(fmOrdersService.pageAll(query));
    }

    @Operation(summary = "订单详情", description = "含商品明细、收货信息快照与状态时间轴")
    @RequiresPermission("order:detail")
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(fmOrdersService.detailForAdmin(id));
    }

    @Operation(summary = "订单备注")
    @RequiresPermission("order:remark")
    @OperLog(module = "订单管理", action = "订单备注")
    @PutMapping("/{id}/remark")
    public Result<Void> remark(@PathVariable Long id, @RequestParam String remark) {
        fmOrdersService.updateAdminRemark(id, remark);
        return Result.ok("备注成功", null);
    }

    @Operation(summary = "取消订单", description = "仅待支付订单可取消，取消后释放预占库存")
    @RequiresPermission("order:cancel")
    @OperLog(module = "订单管理", action = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                              @RequestParam(required = false) String reason) {
        fmOrdersService.cancelByAdmin(id, reason);
        return Result.ok("订单已取消", null);
    }
}
