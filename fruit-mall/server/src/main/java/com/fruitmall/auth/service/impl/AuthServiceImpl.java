package com.fruitmall.auth.service.impl;

import com.fruitmall.auth.context.LoginUser;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.auth.dto.AdminLoginDTO;
import com.fruitmall.auth.service.IAuthService;
import com.fruitmall.auth.vo.LoginUserVO;
import com.fruitmall.auth.vo.LoginVO;
import com.fruitmall.common.constant.AuthConstant;
import com.fruitmall.common.enums.MemberStatusEnum;
import com.fruitmall.common.enums.SysUserStatusEnum;
import com.fruitmall.common.enums.UserTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.JwtUtil;
import com.fruitmall.common.util.PasswordUtil;
import com.fruitmall.member.domain.FmMember;
import com.fruitmall.member.dto.MemberLoginDTO;
import com.fruitmall.member.dto.MemberRegisterDTO;
import com.fruitmall.member.service.IFmMemberService;
import com.fruitmall.system.domain.SysUser;
import com.fruitmall.system.service.ISysMenuService;
import com.fruitmall.system.service.ISysRoleService;
import com.fruitmall.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 认证服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ISysUserService sysUserService;
    private final ISysRoleService sysRoleService;
    private final ISysMenuService sysMenuService;
    private final IFmMemberService fmMemberService;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO adminLogin(AdminLoginDTO dto) {
        SysUser user = sysUserService.getByUsername(dto.getUsername());
        // 用户不存在与密码错误返回同一提示，避免暴露账号是否存在
        if (user == null || !PasswordUtil.matches(dto.getPassword(), user.getPassword())) {
            log.warn("登录失败，用户名或密码错误：{}", dto.getUsername());
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (!SysUserStatusEnum.NORMAL.getCode().equals(user.getStatus())) {
            log.warn("登录失败，账号已禁用：{}", dto.getUsername());
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        sysUserService.updateLastLoginTime(user.getId());

        List<String> roleCodes = sysRoleService.listRoleCodesByUserId(user.getId());
        Set<String> permissions = new HashSet<>(sysMenuService.listPermsByUserId(user.getId()));
        String token = jwtUtil.generate(user.getId(), user.getUsername(), UserTypeEnum.ADMIN);
        log.info("登录成功：{}，角色 {}，权限 {} 项", user.getUsername(), roleCodes, permissions.size());

        return LoginVO.builder()
                .token(token)
                .tokenType(AuthConstant.TOKEN_PREFIX.trim())
                .expiresIn(jwtUtil.getExpireSeconds())
                .user(buildUserVO(user, roleCodes, permissions))
                .build();
    }

    @Override
    public LoginUserVO currentUser() {
        LoginUser loginUser = UserContext.getRequired();
        SysUser user = sysUserService.getById(loginUser.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录用户不存在");
        }
        return buildUserVO(user, loginUser.getRoleCodes(), loginUser.getPermissions());
    }

    private LoginUserVO buildUserVO(SysUser user, List<String> roleCodes, Set<String> permissions) {
        return LoginUserVO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roleCodes(roleCodes)
                .permissions(permissions)
                .build();
    }

    @Override
    public Long memberRegister(MemberRegisterDTO dto) {
        return fmMemberService.register(dto);
    }

    @Override
    public LoginVO memberLogin(MemberLoginDTO dto) {
        FmMember member = fmMemberService.getByUsername(dto.getUsername());
        // 用户不存在与密码错误返回同一提示，避免暴露账号是否存在
        if (member == null || !PasswordUtil.matches(dto.getPassword(), member.getPassword())) {
            log.warn("会员登录失败，用户名或密码错误：{}", dto.getUsername());
            throw new BizException(ResultCode.BAD_REQUEST, "用户名或密码错误");
        }
        if (!MemberStatusEnum.NORMAL.getCode().equals(member.getStatus())) {
            log.warn("会员登录失败，账号已禁用：{}", dto.getUsername());
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用，请联系客服");
        }
        fmMemberService.updateLastLoginTime(member.getId());

        // 会员端不做功能级权限，数据归属由各业务 Service 各自校验
        String token = jwtUtil.generate(member.getId(), member.getUsername(), UserTypeEnum.MEMBER);
        log.info("会员登录成功：memberId={}, username={}", member.getId(), member.getUsername());
        return LoginVO.builder()
                .token(token)
                .tokenType(AuthConstant.TOKEN_PREFIX.trim())
                .expiresIn(jwtUtil.getExpireSeconds())
                .user(LoginUserVO.builder()
                        .userId(member.getId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .avatar(member.getAvatar())
                        .roleCodes(List.of())
                        .permissions(Set.of())
                        .build())
                .build();
    }
}
