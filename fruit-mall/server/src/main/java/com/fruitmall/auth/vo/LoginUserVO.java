package com.fruitmall.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 登录用户信息出参。
 */
@Data
@Builder
@Schema(description = "登录用户信息")
public class LoginUserVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "角色编码列表")
    private List<String> roleCodes;

    @Schema(description = "权限标识集合")
    private Set<String> permissions;
}
