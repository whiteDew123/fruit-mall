package com.fruitmall.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 会员注册入参。 */
@Data
@Schema(description = "会员注册入参")
public class MemberRegisterDTO {

    @Schema(description = "登录名", example = "fruitfan")
    @NotBlank(message = "登录名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "登录名只能包含字母、数字、下划线，长度 4-20")
    private String username;

    @Schema(description = "密码", example = "Fruit@2026")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 之间")
    private String password;

    @Schema(description = "昵称，不填默认与登录名相同")
    @Size(max = 50, message = "昵称长度不能超过 50")
    private String nickname;

    @Schema(description = "手机号", example = "13900000001")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
