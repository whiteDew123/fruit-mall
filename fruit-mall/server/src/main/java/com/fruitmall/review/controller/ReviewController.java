package com.fruitmall.review.controller;

import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.review.dto.ReviewCreateDTO;
import com.fruitmall.review.query.ReviewQuery;
import com.fruitmall.review.service.IFmReviewService;
import com.fruitmall.review.vo.ReviewVO;
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

/** 消费者端 —— 评价。提交需登录，商品评价列表为公开接口。 */
@Tag(name = "消费者端-评价")
@RestController
@RequestMapping("/api/shop/review")
@RequiredArgsConstructor
public class ReviewController {

    private final IFmReviewService fmReviewService;

    @Operation(summary = "提交评价", description = "订单完成后才能评价，一个订单项只能评价一次")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody ReviewCreateDTO dto) {
        return Result.ok("评价成功", fmReviewService.create(dto));
    }

    @Operation(summary = "商品评价列表", description = "公开接口，供商品详情页展示，只返回显示中的评价")
    @GetMapping("/spu/{spuId}")
    public Result<PageResult<ReviewVO>> pageBySpu(@PathVariable Long spuId, @Valid ReviewQuery query) {
        query.setSpuId(spuId);
        return Result.ok(fmReviewService.pageBySpu(query));
    }
}
