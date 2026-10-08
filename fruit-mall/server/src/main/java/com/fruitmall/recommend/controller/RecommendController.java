package com.fruitmall.recommend.controller;

import com.fruitmall.common.result.Result;
import com.fruitmall.recommend.service.IRecommendService;
import com.fruitmall.recommend.vo.RecommendItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 消费者端 —— 个性化推荐（公开接口，游客也可访问）。 */
@Tag(name = "消费者端-推荐")
@RestController
@RequestMapping("/api/shop/recommend")
@Validated
@RequiredArgsConstructor
public class RecommendController {

    private final IRecommendService recommendService;

    @Operation(summary = "首页个性化推荐",
            description = "多通道召回 + 多因素加权排序 + 多样性打散，返回商品与推荐理由；"
                    + "已登录会员走个性化，游客与新用户自动走时令热销的冷启动策略")
    @GetMapping("/home")
    public Result<List<RecommendItemVO>> home(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "条数至少为 1")
            @Max(value = 50, message = "条数不能超过 50") Integer limit,
            @RequestParam(required = false) String anonymousId) {
        return Result.ok(recommendService.recommendHome(limit, anonymousId));
    }
}
