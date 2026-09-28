package com.fruitmall.system.service.impl;

import com.fruitmall.system.mapper.SysMenuMapper;
import com.fruitmall.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单权限服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements ISysMenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<String> listPermsByUserId(Long userId) {
        return sysMenuMapper.selectPermsByUserId(userId);
    }
}
