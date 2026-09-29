package com.fruitmall.member.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 会员（消费者端账号）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_member")
public class FmMember extends BaseEntity {

    /** 登录名 */
    private String username;

    /** 密码密文，禁止在出参中返回 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 头像地址 */
    private String avatar;

    /** 状态，见 MemberStatusEnum */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 备注 */
    private String remark;
}
