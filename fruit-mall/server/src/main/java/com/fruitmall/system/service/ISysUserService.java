package com.fruitmall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.system.domain.SysUser;
import com.fruitmall.system.query.SysUserQuery;
import com.fruitmall.system.vo.SysUserVO;

/**
 * 系统用户服务。
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 按登录名查询用户，登录时使用（需要返回密码密文用于校验）
     */
    SysUser getByUsername(String username);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 分页查询用户列表，手机号脱敏
     */
    PageResult<SysUserVO> pageUsers(SysUserQuery query);
}
