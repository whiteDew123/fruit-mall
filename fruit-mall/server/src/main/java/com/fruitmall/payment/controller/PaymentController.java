package com.fruitmall.payment.controller;

import com.fruitmall.common.result.Result;
import com.fruitmall.payment.dto.PaymentPayDTO;
import com.fruitmall.payment.service.IFmPaymentRecordService;
import com.fruitmall.payment.vo.PaymentResultVO;
import com.fruitmall.payment.vo.PaymentVO;
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

/** 消费者端 —— 模拟支付。需要会员登录。 */
@Tag(name = "消费者端-支付")
@RestController
@RequestMapping("/api/shop/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final IFmPaymentRecordService fmPaymentRecordService;

    @Operation(summary = "模拟支付",
            description = "由前端选择模拟结果：SUCCESS 支付成功 / FAIL 支付失败 / TIMEOUT 支付超时；重复支付同一订单具备幂等性")
    @PostMapping("/pay")
    public Result<PaymentResultVO> pay(@Valid @RequestBody PaymentPayDTO dto) {
        return Result.ok(fmPaymentRecordService.pay(dto));
    }

    @Operation(summary = "查询订单的支付单", description = "供支付结果页展示")
    @GetMapping("/{orderId}")
    public Result<PaymentVO> getByOrderId(@PathVariable Long orderId) {
        return Result.ok(fmPaymentRecordService.getByOrderId(orderId));
    }
}
