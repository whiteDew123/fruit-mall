package com.fruitmall.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录成功出参。
 */
@Data
@Builder
@Schema(description = "登录结果")
public class LoginVO {

    @Schema(description = "访问令牌，后续请求放入请求头 Authorization: Bearer <token>")
    private String token;

    @Schema(description = "令牌类型固定为 Bearer")
    private String tokenType;

    @Schema(description = "令牌有效期（秒）")
    private Long expiresIn;

    @Schema(description = "登录用户信息")
    private LoginUserVO user;
}
