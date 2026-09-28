package com.fruitmall.auth.controller;

import com.fruitmall.auth.dto.AdminLoginDTO;
import com.fruitmall.auth.service.IAuthService;
import com.fruitmall.auth.vo.LoginUserVO;
import com.fruitmall.auth.vo.LoginVO;
import com.fruitmall.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录注册接口。
 * 登录接口在鉴权白名单内；其余 /api/auth/** 接口需要携带令牌。
 */
@Tag(name = "登录注册")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @Operation(summary = "后台用户登录", description = "登录成功返回 JWT 令牌与用户权限，后续请求放入请求头 Authorization")
    @PostMapping("/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.ok("登录成功", authService.adminLogin(dto));
    }

    @Operation(summary = "获取当前登录用户信息", description = "用于前端刷新页面后恢复登录态与权限")
    @GetMapping("/profile")
    public Result<LoginUserVO> profile() {
        return Result.ok(authService.currentUser());
    }
}
