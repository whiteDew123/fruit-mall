package com.fruitmall.behavior.controller;

import com.fruitmall.behavior.dto.BehaviorReportDTO;
import com.fruitmall.behavior.service.IBehaviorService;
import com.fruitmall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 消费者端 —— 行为埋点（公开接口，游客也可上报）。 */
@Slf4j
@Tag(name = "消费者端-行为埋点")
@RestController
@RequestMapping("/api/shop/behavior")
@RequiredArgsConstructor
public class BehaviorController {

    private final IBehaviorService behaviorService;

    @Operation(summary = "上报行为", description = "浏览详情、搜索、加购、下单、评价、不感兴趣时调用，是推荐系统的数据源")
    @PostMapping("/report")
    public Result<Void> report(@Valid @RequestBody BehaviorReportDTO dto) {
        try {
            behaviorService.report(dto);
        } catch (Exception e) {
            // 埋点失败不影响前端流程
            log.warn("行为埋点上报失败：behavior={}", dto.getBehavior(), e);
        }
        return Result.ok();
    }
}
