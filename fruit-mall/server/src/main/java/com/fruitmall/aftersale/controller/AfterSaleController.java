package com.fruitmall.aftersale.controller;

import com.fruitmall.aftersale.dto.AfterSaleApplyDTO;
import com.fruitmall.aftersale.query.AfterSaleQuery;
import com.fruitmall.aftersale.service.IFmAfterSaleService;
import com.fruitmall.aftersale.vo.AfterSaleListVO;
import com.fruitmall.aftersale.vo.AfterSaleVO;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
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

/** 消费者端 —— 售后。需要会员登录，只能操作自己的售后单。 */
@Tag(name = "消费者端-售后")
@RestController
@RequestMapping("/api/shop/aftersale")
@RequiredArgsConstructor
public class AfterSaleController {

    private final IFmAfterSaleService fmAfterSaleService;

    @Operation(summary = "申请售后",
            description = "仅退款/退货退款两种类型；可售后数量不能超过购买数量减去已申请数量")
    @PostMapping
    public Result<Long> apply(@Valid @RequestBody AfterSaleApplyDTO dto) {
        return Result.ok("申请已提交", fmAfterSaleService.apply(dto));
    }

    @Operation(summary = "我的售后单列表")
    @GetMapping("/list")
    public Result<PageResult<AfterSaleListVO>> list(@Valid AfterSaleQuery query) {
        return Result.ok(fmAfterSaleService.pageMine(query));
    }

    @Operation(summary = "我的售后单详情")
    @GetMapping("/{id}")
    public Result<AfterSaleVO> detail(@PathVariable Long id) {
        return Result.ok(fmAfterSaleService.detailMine(id));
    }

    @Operation(summary = "撤销售后申请", description = "只有待审核状态可以撤销")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                              @RequestParam(required = false) String reason) {
        fmAfterSaleService.cancelByMember(id, reason);
        return Result.ok("已撤销申请", null);
    }
}
