package com.fruitmall.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户（后台账号）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录名 */
    private String username;

    /** 密码密文，禁止在出参中返回 */
    private String password;

    /** 显示名 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 手机号，列表页需脱敏 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像地址 */
    private String avatar;

    /** 状态，见 SysUserStatusEnum */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 备注 */
    private String remark;
}
