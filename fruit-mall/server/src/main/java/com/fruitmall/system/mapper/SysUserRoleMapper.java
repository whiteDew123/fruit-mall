package com.fruitmall.system.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper。
 * sys_user_role 为物理删除的关联表，重新授权时先删后插，故使用 INSERT IGNORE 保证幂等。
 */
@Mapper
public interface SysUserRoleMapper {

    /**
     * 新增用户角色关联，重复关联不报错
     */
    @Insert("INSERT IGNORE INTO sys_user_role (user_id, role_id, create_time) "
            + "VALUES (#{userId}, #{roleId}, NOW())")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
