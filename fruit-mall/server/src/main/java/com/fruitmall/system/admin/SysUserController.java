package com.fruitmall.system.admin;

import com.fruitmall.common.annotation.RequiresPermission;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.Result;
import com.fruitmall.system.query.SysUserQuery;
import com.fruitmall.system.service.ISysUserService;
import com.fruitmall.system.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商家后台 —— 系统用户管理接口。
 * 按目录约定，后台接口放在业务包的 admin 包下，路径前缀 /api/admin。
 */
@Tag(name = "系统管理-用户")
@RestController
@RequestMapping("/api/admin/system/user")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;

    @Operation(summary = "用户列表", description = "分页查询后台账号，手机号脱敏展示")
    @RequiresPermission("system:user:list")
    @GetMapping("/list")
    public Result<PageResult<SysUserVO>> list(@Valid SysUserQuery query) {
        return Result.ok(sysUserService.pageUsers(query));
    }
}
