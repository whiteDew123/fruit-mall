package com.fruitmall.review.admin;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.review.query.ReviewQuery;
import com.fruitmall.review.service.IFmReviewService;
import com.fruitmall.review.vo.ReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商家后台 —— 评价管理。 */
@Tag(name = "评价管理")
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class FmReviewController {

    private final IFmReviewService fmReviewService;

    @Operation(summary = "评价列表", description = "支持商品、星级、状态筛选")
    @RequiresPermission("review:list")
    @GetMapping("/list")
    public Result<PageResult<ReviewVO>> list(@Valid ReviewQuery query) {
        return Result.ok(fmReviewService.pageAll(query));
    }

    @Operation(summary = "回复评价")
    @RequiresPermission("review:reply")
    @OperLog(module = "评价管理", action = "回复评价")
    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @RequestParam String replyContent) {
        fmReviewService.reply(id, replyContent);
        return Result.ok("回复成功", null);
    }

    @Operation(summary = "显示/隐藏评价", description = "status：10 显示 / 20 隐藏")
    @RequiresPermission("review:status")
    @OperLog(module = "评价管理", action = "评价显示状态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        fmReviewService.changeStatus(id, status);
        return Result.ok("操作成功", null);
    }
}
