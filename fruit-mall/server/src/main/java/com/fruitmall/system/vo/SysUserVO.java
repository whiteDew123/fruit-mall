package com.fruitmall.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户列表出参，不含密码密文，手机号已脱敏。
 */
@Data
@Schema(description = "系统用户列表项")
public class SysUserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "显示名")
    private String nickname;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号（已脱敏）")
    private String phone;

    @Schema(description = "状态：10 正常 / 20 禁用")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
