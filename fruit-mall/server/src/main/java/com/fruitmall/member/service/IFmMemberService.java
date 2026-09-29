package com.fruitmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.member.domain.FmMember;
import com.fruitmall.member.dto.MemberRegisterDTO;
import com.fruitmall.member.vo.MemberInfoVO;

/** 会员服务。 */
public interface IFmMemberService extends IService<FmMember> {

    /** 按登录名查询会员 */
    FmMember getByUsername(String username);

    /** 注册会员，密码以 BCrypt 密文入库 */
    Long register(MemberRegisterDTO dto);

    /** 更新最后登录时间 */
    void updateLastLoginTime(Long memberId);

    /** 当前登录会员信息 */
    MemberInfoVO currentMember();

    /** 修改昵称与头像 */
    void updateProfile(String nickname, String avatar);
}
