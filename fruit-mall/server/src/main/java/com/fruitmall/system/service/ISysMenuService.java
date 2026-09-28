package com.fruitmall.system.service;

import java.util.List;

/**
 * 菜单权限服务。
 */
public interface ISysMenuService {

    /**
     * 查询用户拥有的权限标识集合
     */
    List<String> listPermsByUserId(Long userId);
}
