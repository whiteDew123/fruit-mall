package com.fruitmall.system.service.impl;

import com.fruitmall.system.mapper.SysRoleMapper;
import com.fruitmall.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        return sysRoleMapper.selectRoleCodesByUserId(userId);
    }
}
