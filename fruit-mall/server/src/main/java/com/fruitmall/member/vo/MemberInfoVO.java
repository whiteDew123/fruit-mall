package com.fruitmall.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 会员信息出参。本人查看时手机号展示完整，不做脱敏。 */
@Data
@Builder
@Schema(description = "会员信息")
public class MemberInfoVO {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
