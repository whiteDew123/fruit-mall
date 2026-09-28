package com.fruitmall.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.system.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
