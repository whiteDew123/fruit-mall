package com.fruitmall.auth.service;

import com.fruitmall.auth.dto.AdminLoginDTO;
import com.fruitmall.auth.vo.LoginUserVO;
import com.fruitmall.auth.vo.LoginVO;

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
}
