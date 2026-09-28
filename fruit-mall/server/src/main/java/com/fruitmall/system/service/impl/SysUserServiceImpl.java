package com.fruitmall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.MaskUtil;
import com.fruitmall.common.util.PasswordUtil;
import com.fruitmall.system.domain.SysUser;
import com.fruitmall.system.mapper.SysUserMapper;
import com.fruitmall.system.mapper.SysUserRoleMapper;
import com.fruitmall.system.dto.SysUserCreateDTO;
import com.fruitmall.system.query.SysUserQuery;
import com.fruitmall.system.service.ISysUserService;
import com.fruitmall.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public SysUser getByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setLastLoginTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    public PageResult<SysUserVO> pageUsers(SysUserQuery query) {
        // 只取列表需要的列，密码密文不参与查询
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .select(SysUser::getId, SysUser::getUsername, SysUser::getNickname, SysUser::getRealName,
                        SysUser::getPhone, SysUser::getStatus, SysUser::getLastLoginTime, SysUser::getCreateTime)
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<SysUserVO> list = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }

    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRealName(user.getRealName());
        // 手机号在列表页脱敏，需要完整信息时走单独接口并记录操作日志
        vo.setPhone(MaskUtil.maskPhone(user.getPhone()));
        vo.setStatus(user.getStatus());
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createUser(SysUserCreateDTO dto) {
        Long exists = baseMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.CONFLICT, "登录名已存在：" + dto.getUsername());
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        // 密码只以 BCrypt 密文入库，任何位置都不保存明文
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        // 审计字段由 MyMetaObjectHandler 自动填充
        this.save(user);

        dto.getRoleIds().forEach(roleId -> sysUserRoleMapper.insertUserRole(user.getId(), roleId));
        return user.getId();
    }
}
