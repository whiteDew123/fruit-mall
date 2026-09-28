package com.fruitmall.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.util.MaskUtil;
import com.fruitmall.system.domain.SysUser;
import com.fruitmall.system.mapper.SysUserMapper;
import com.fruitmall.system.query.SysUserQuery;
import com.fruitmall.system.service.ISysUserService;
import com.fruitmall.system.vo.SysUserVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户服务实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

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
}
