package com.fruitmall.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增后台用户入参。
 */
@Data
@Schema(description = "新增后台用户入参")
public class SysUserCreateDTO {

    @Schema(description = "登录名", example = "warehouse01")
    @NotBlank(message = "登录名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "登录名只能包含字母、数字、下划线，长度 4-20")
    private String username;

    @Schema(description = "密码，入库前以 BCrypt 加密", example = "Fruit@2026")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 之间")
    private String password;

    @Schema(description = "显示名", example = "仓库管理员")
    @NotBlank(message = "显示名不能为空")
    @Size(max = 50, message = "显示名长度不能超过 50")
    private String nickname;

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "真实姓名长度不能超过 50")
    private String realName;

    @Schema(description = "手机号", example = "13800000009")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过 100")
    private String email;

    @Schema(description = "状态：10 正常 / 20 禁用", example = "10")
    @NotNull(message = "状态不能为空")
    @Min(value = 10, message = "状态取值不正确")
    @Max(value = 20, message = "状态取值不正确")
    private Integer status = 10;

    @Schema(description = "角色ID列表，至少一个", example = "[3]")
    @NotEmpty(message = "至少选择一个角色")
    private List<Long> roleIds;
}
