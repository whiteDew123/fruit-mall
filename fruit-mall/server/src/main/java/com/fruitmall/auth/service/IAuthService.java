package com.fruitmall.auth.service;

import com.fruitmall.auth.dto.AdminLoginDTO;
import com.fruitmall.auth.vo.LoginUserVO;
import com.fruitmall.auth.vo.LoginVO;
import com.fruitmall.member.dto.MemberLoginDTO;
import com.fruitmall.member.dto.MemberRegisterDTO;

/**
 * 认证服务。
 */
public interface IAuthService {

    /**
     * 后台用户登录
     *
     * @param dto 登录入参
     * @return 令牌与登录用户信息
     */
    LoginVO adminLogin(AdminLoginDTO dto);

    /**
     * 获取当前登录用户信息
     */
    LoginUserVO currentUser();

    /**
     * 会员注册
     */
    Long memberRegister(MemberRegisterDTO dto);

    /**
     * 会员登录
     */
    LoginVO memberLogin(MemberLoginDTO dto);
}
