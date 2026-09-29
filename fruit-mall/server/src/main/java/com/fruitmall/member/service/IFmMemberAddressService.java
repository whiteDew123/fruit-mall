package com.fruitmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.member.domain.FmMemberAddress;
import com.fruitmall.member.dto.MemberAddressSaveDTO;
import com.fruitmall.member.vo.MemberAddressVO;

import java.util.List;

/** 收货地址服务。所有方法都只能操作当前登录会员自己的地址。 */
public interface IFmMemberAddressService extends IService<FmMemberAddress> {

    /** 我的地址列表 */
    List<MemberAddressVO> listMine();

    /** 新增地址，第一条地址自动为默认地址 */
    Long create(MemberAddressSaveDTO dto);

    /** 编辑地址 */
    void update(Long id, MemberAddressSaveDTO dto);

    /** 删除地址 */
    void delete(Long id);

    /** 设为默认地址 */
    void setDefault(Long id);

    /** 当前会员的默认地址，无默认地址时返回第一条；一条都没有则返回 null */
    MemberAddressVO getDefaultAddress(Long memberId);
}
