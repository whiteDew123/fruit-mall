package com.fruitmall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单权限 Mapper。
 * 权限标识来自 sys_menu.perm，经角色关联表汇总到用户。
 */
@Mapper
public interface SysMenuMapper {

    /**
     * 查询用户拥有的权限标识集合
     */
    @Select("""
            SELECT DISTINCT m.perm
              FROM sys_menu m
              JOIN sys_role_menu rm ON rm.menu_id = m.id
              JOIN sys_user_role ur ON ur.role_id = rm.role_id
             WHERE ur.user_id = #{userId}
               AND m.deleted = 0
               AND m.status = 10
               AND m.perm IS NOT NULL
               AND m.perm <> ''
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}
