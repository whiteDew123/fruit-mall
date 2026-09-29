package com.fruitmall.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.member.domain.FmMember;
import org.apache.ibatis.annotations.Mapper;

/** 会员 Mapper。 */
@Mapper
public interface FmMemberMapper extends BaseMapper<FmMember> {
}
