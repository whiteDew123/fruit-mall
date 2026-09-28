package com.fruitmall.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台用户登录入参。
 */
@Data
@Schema(description = "后台用户登录入参")
public class AdminLoginDTO {

    @Schema(description = "登录名", example = "admin")
    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过 50")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码长度不能超过 64")
    private String password;
}
