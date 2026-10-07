package com.fruitmall.fulfillment.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.fulfillment.dto.FulfillmentActionDTO;
import com.fruitmall.fulfillment.query.FulfillmentQuery;
import com.fruitmall.fulfillment.service.IFmFulfillmentOrderService;
import com.fruitmall.fulfillment.vo.FulfillmentListVO;
import com.fruitmall.fulfillment.vo.FulfillmentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家后台 —— 履约管理。
 * 推进顺序：分拣完成 → 打包待配送 → 出库配送 → 送达 → 签收，不得跳级；
 * 任意环节可登记异常。每次推进都会同步更新订单状态。
 */
@Tag(name = "履约管理")
@RestController
@RequestMapping("/api/admin/fulfillment")
@RequiredArgsConstructor
public class FmFulfillmentOrderController {

    private final IFmFulfillmentOrderService fmFulfillmentOrderService;

    @Operation(summary = "履约单列表", description = "支持状态与订单号筛选，列表页手机号脱敏")
    @RequiresPermission("fulfillment:list")
    @GetMapping("/list")
    public Result<PageResult<FulfillmentListVO>> list(@Valid FulfillmentQuery query) {
        return Result.ok(fmFulfillmentOrderService.pageAll(query));
    }

    @Operation(summary = "履约单详情", description = "含履约时间轴")
    @RequiresPermission("fulfillment:list")
    @GetMapping("/{id}")
    public Result<FulfillmentVO> detail(@PathVariable Long id) {
        return Result.ok(fmFulfillmentOrderService.detailForAdmin(id));
    }

    @Operation(summary = "分拣完成", description = "履约转分拣完成，订单转备货中")
    @RequiresPermission("fulfillment:pick")
    @OperLog(module = "履约管理", action = "分拣完成")
    @PostMapping("/{id}/pick")
    public Result<Void> pick(@PathVariable Long id,
                            @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.pick(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("分拣完成", null);
    }

    @Operation(summary = "打包待配送", description = "履约转待配送")
    @RequiresPermission("fulfillment:pick")
    @OperLog(module = "履约管理", action = "打包待配送")
    @PostMapping("/{id}/ready")
    public Result<Void> ready(@PathVariable Long id,
                             @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.ready(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("已打包，等待配送", null);
    }

    @Operation(summary = "出库配送", description = "履约转配送中，订单转配送中")
    @RequiresPermission("fulfillment:deliver")
    @OperLog(module = "履约管理", action = "出库配送")
    @PostMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id,
                               @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.deliver(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("已出库配送", null);
    }

    @Operation(summary = "确认送达", description = "履约转已送达")
    @RequiresPermission("fulfillment:deliver")
    @OperLog(module = "履约管理", action = "确认送达")
    @PostMapping("/{id}/arrive")
    public Result<Void> arrive(@PathVariable Long id,
                              @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.arrive(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("已送达", null);
    }

    @Operation(summary = "确认签收", description = "履约转已签收，订单转已完成；完成后可评价与申请售后")
    @RequiresPermission("fulfillment:sign")
    @OperLog(module = "履约管理", action = "确认签收")
    @PostMapping("/{id}/sign")
    public Result<Void> sign(@PathVariable Long id,
                            @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.sign(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("已签收", null);
    }

    @Operation(summary = "异常登记", description = "破损/缺货/拒收/改期，必须填写原因")
    @RequiresPermission("fulfillment:exception")
    @OperLog(module = "履约管理", action = "异常登记")
    @PostMapping("/{id}/exception")
    public Result<Void> exception(@PathVariable Long id,
                                 @Valid @RequestBody(required = false) FulfillmentActionDTO dto) {
        fmFulfillmentOrderService.exception(id, remarkOf(dto), imagesOf(dto));
        return Result.ok("异常已登记", null);
    }

    private String remarkOf(FulfillmentActionDTO dto) {
        return dto == null ? null : dto.getRemark();
    }

    private String imagesOf(FulfillmentActionDTO dto) {
        return dto == null ? null : dto.getImages();
    }
}
