package com.fruitmall.system.service;

import java.util.List;

/**
 * 角色服务。
 */
public interface ISysRoleService {

    /**
     * 查询用户拥有的角色编码
     */
    List<String> listRoleCodesByUserId(Long userId);
}
