package com.fruitmall.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 会员登录入参。 */
@Data
@Schema(description = "会员登录入参")
public class MemberLoginDTO {

    @Schema(description = "登录名", example = "fruitfan")
    @NotBlank(message = "登录名不能为空")
    @Size(max = 50, message = "登录名长度不能超过 50")
    private String username;

    @Schema(description = "密码", example = "Fruit@2026")
    @NotBlank(message = "密码不能为空")
    @Size(max = 64, message = "密码长度不能超过 64")
    private String password;
}
