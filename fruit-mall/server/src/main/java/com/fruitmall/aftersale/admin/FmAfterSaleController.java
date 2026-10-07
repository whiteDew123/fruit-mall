package com.fruitmall.aftersale.admin;

import com.fruitmall.aftersale.query.AfterSaleQuery;
import com.fruitmall.aftersale.service.IFmAfterSaleService;
import com.fruitmall.aftersale.vo.AfterSaleListVO;
import com.fruitmall.aftersale.vo.AfterSaleVO;
import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家后台 —— 售后管理。
 * 流程：待审核 → 审核通过（退货退款进入退货中）→ 收到退货 → 退款中 → 确认退款完成。
 */
@Tag(name = "售后管理")
@RestController
@RequestMapping("/api/admin/aftersale")
@RequiredArgsConstructor
public class FmAfterSaleController {

    private final IFmAfterSaleService fmAfterSaleService;

    @Operation(summary = "售后单列表", description = "支持状态与订单号筛选")
    @RequiresPermission("aftersale:list")
    @GetMapping("/list")
    public Result<PageResult<AfterSaleListVO>> list(@Valid AfterSaleQuery query) {
        return Result.ok(fmAfterSaleService.pageAll(query));
    }

    @Operation(summary = "售后单详情", description = "含售后明细、凭证与退款流水号")
    @RequiresPermission("aftersale:list")
    @GetMapping("/{id}")
    public Result<AfterSaleVO> detail(@PathVariable Long id) {
        return Result.ok(fmAfterSaleService.detailForAdmin(id));
    }

    @Operation(summary = "审核售后", description = "pass=true 通过，pass=false 驳回；通过后订单转入退款流程")
    @RequiresPermission("aftersale:audit")
    @OperLog(module = "售后管理", action = "审核售后")
    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                             @RequestParam Boolean pass,
                             @RequestParam(required = false) String remark) {
        fmAfterSaleService.audit(id, Boolean.TRUE.equals(pass), remark);
        return Result.ok(Boolean.TRUE.equals(pass) ? "审核通过" : "已驳回", null);
    }

    @Operation(summary = "确认收到退货",
            description = "退货退款场景使用；生鲜退货回补至退货暂存，不进入可售库存")
    @RequiresPermission("aftersale:receive")
    @OperLog(module = "售后管理", action = "确认收到退货")
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id,
                               @RequestParam(required = false) String remark) {
        fmAfterSaleService.receive(id, remark);
        return Result.ok("已确认收货", null);
    }

    @Operation(summary = "确认退款完成", description = "售后单转已完成，订单转已退款，支付单转已退款")
    @RequiresPermission("aftersale:refund")
    @OperLog(module = "售后管理", action = "确认退款")
    @PostMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id,
                              @RequestParam(required = false) String remark) {
        fmAfterSaleService.refund(id, remark);
        return Result.ok("退款完成", null);
    }
}
