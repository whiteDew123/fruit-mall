package com.fruitmall.member.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.enums.MemberStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.common.util.PasswordUtil;
import com.fruitmall.member.domain.FmMember;
import com.fruitmall.member.dto.MemberRegisterDTO;
import com.fruitmall.member.mapper.FmMemberMapper;
import com.fruitmall.member.service.IFmMemberService;
import com.fruitmall.member.vo.MemberInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/** 会员服务实现。 */
@Slf4j
@Service
public class FmMemberServiceImpl extends ServiceImpl<FmMemberMapper, FmMember> implements IFmMemberService {

    @Override
    public FmMember getByUsername(String username) {
        return lambdaQuery().eq(FmMember::getUsername, username).one();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long register(MemberRegisterDTO dto) {
        FmMember member = new FmMember();
        member.setUsername(dto.getUsername());
        // 密码只以 BCrypt 密文入库
        member.setPassword(PasswordUtil.encode(dto.getPassword()));
        member.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        member.setPhone(dto.getPhone());
        member.setStatus(MemberStatusEnum.NORMAL.getCode());
        try {
            this.save(member);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "登录名或手机号已被注册");
        }
        log.info("会员注册成功：memberId={}, username={}", member.getId(), member.getUsername());
        return member.getId();
    }

    @Override
    public void updateLastLoginTime(Long memberId) {
        FmMember update = new FmMember();
        update.setId(memberId);
        update.setLastLoginTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    public MemberInfoVO currentMember() {
        FmMember member = requireCurrentMember();
        return MemberInfoVO.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .avatar(member.getAvatar())
                .createTime(member.getCreateTime())
                .build();
    }

    @Override
    public void updateProfile(String nickname, String avatar) {
        FmMember member = requireCurrentMember();
        FmMember update = new FmMember();
        update.setId(member.getId());
        update.setNickname(nickname);
        update.setAvatar(avatar);
        this.updateById(update);
    }

    /** 取当前登录会员，非会员或不存在直接抛异常 */
    private FmMember requireCurrentMember() {
        Long memberId = UserContext.getRequiredMemberId();
        FmMember member = this.getById(memberId);
        if (member == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录会员不存在");
        }
        if (!MemberStatusEnum.NORMAL.getCode().equals(member.getStatus())) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被禁用");
        }
        return member;
    }

    /** 供其他服务校验会员是否可用 */
    public boolean existsNormalMember(Long memberId) {
        FmMember member = this.getById(memberId);
        return member != null && MemberStatusEnum.NORMAL.getCode().equals(member.getStatus());
    }
}
