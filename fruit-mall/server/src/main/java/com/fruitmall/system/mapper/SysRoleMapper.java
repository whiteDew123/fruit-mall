package com.fruitmall.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 * sys_user_role 为物理删除的关联表，故不需要 deleted 条件。
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 查询用户拥有的角色编码，仅统计正常状态的角色
     */
    @Select("""
            SELECT r.role_code
              FROM sys_role r
              JOIN sys_user_role ur ON ur.role_id = r.id
             WHERE ur.user_id = #{userId}
               AND r.deleted = 0
               AND r.status = 10
             ORDER BY r.sort
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
