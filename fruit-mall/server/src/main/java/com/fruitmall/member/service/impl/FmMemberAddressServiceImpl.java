package com.fruitmall.member.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.member.domain.FmMemberAddress;
import com.fruitmall.member.dto.MemberAddressSaveDTO;
import com.fruitmall.member.mapper.FmMemberAddressMapper;
import com.fruitmall.member.service.IFmMemberAddressService;
import com.fruitmall.member.vo.MemberAddressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 收货地址服务实现。 */
@Service
public class FmMemberAddressServiceImpl extends ServiceImpl<FmMemberAddressMapper, FmMemberAddress>
        implements IFmMemberAddressService {

    /** 默认地址标记 */
    private static final int DEFAULT_YES = 1;
    private static final int DEFAULT_NO = 0;

    @Override
    public List<MemberAddressVO> listMine() {
        Long memberId = UserContext.getRequiredMemberId();
        return this.list(Wrappers.<FmMemberAddress>lambdaQuery()
                        .eq(FmMemberAddress::getMemberId, memberId)
                        .orderByDesc(FmMemberAddress::getIsDefault)
                        .orderByDesc(FmMemberAddress::getId))
                .stream().map(this::toVO).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long create(MemberAddressSaveDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();
        boolean first = this.count(Wrappers.<FmMemberAddress>lambdaQuery()
                .eq(FmMemberAddress::getMemberId, memberId)) == 0;
        boolean asDefault = first || DEFAULT_YES == (dto.getIsDefault() == null ? DEFAULT_NO : dto.getIsDefault());
        if (asDefault) {
            clearDefault(memberId);
        }
        FmMemberAddress address = new FmMemberAddress();
        address.setMemberId(memberId);
        fill(address, dto);
        address.setIsDefault(asDefault ? DEFAULT_YES : DEFAULT_NO);
        this.save(address);
        return address.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Long id, MemberAddressSaveDTO dto) {
        FmMemberAddress exists = requireMine(id);
        FmMemberAddress update = new FmMemberAddress();
        update.setId(exists.getId());
        fill(update, dto);
        if (DEFAULT_YES == (dto.getIsDefault() == null ? DEFAULT_NO : dto.getIsDefault())) {
            clearDefault(exists.getMemberId());
            update.setIsDefault(DEFAULT_YES);
        }
        this.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) {
        FmMemberAddress exists = requireMine(id);
        this.removeById(exists.getId());
        // 删掉的是默认地址时，把剩下最新的一条设为默认，避免下单时找不到地址
        if (DEFAULT_YES == (exists.getIsDefault() == null ? DEFAULT_NO : exists.getIsDefault())) {
            FmMemberAddress fallback = this.getOne(Wrappers.<FmMemberAddress>lambdaQuery()
                    .eq(FmMemberAddress::getMemberId, exists.getMemberId())
                    .orderByDesc(FmMemberAddress::getId)
                    .last("LIMIT 1"));
            if (fallback != null) {
                FmMemberAddress update = new FmMemberAddress();
                update.setId(fallback.getId());
                update.setIsDefault(DEFAULT_YES);
                this.updateById(update);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setDefault(Long id) {
        FmMemberAddress exists = requireMine(id);
        clearDefault(exists.getMemberId());
        FmMemberAddress update = new FmMemberAddress();
        update.setId(exists.getId());
        update.setIsDefault(DEFAULT_YES);
        this.updateById(update);
    }

    @Override
    public MemberAddressVO getDefaultAddress(Long memberId) {
        FmMemberAddress address = this.getOne(Wrappers.<FmMemberAddress>lambdaQuery()
                .eq(FmMemberAddress::getMemberId, memberId)
                .orderByDesc(FmMemberAddress::getIsDefault)
                .orderByDesc(FmMemberAddress::getId)
                .last("LIMIT 1"));
        return address == null ? null : toVO(address);
    }

    @Override
    public MemberAddressVO getMine(Long id) {
        return toVO(requireMine(id));
    }

    /** 校验地址存在且属于当前登录会员，防止越权操作他人地址 */
    private FmMemberAddress requireMine(Long id) {
        Long memberId = UserContext.getRequiredMemberId();
        FmMemberAddress address = this.getById(id);
        if (address == null || !memberId.equals(address.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "地址不存在");
        }
        return address;
    }

    private void clearDefault(Long memberId) {
        FmMemberAddress update = new FmMemberAddress();
        update.setIsDefault(DEFAULT_NO);
        this.update(update, Wrappers.<FmMemberAddress>lambdaUpdate()
                .eq(FmMemberAddress::getMemberId, memberId)
                .eq(FmMemberAddress::getIsDefault, DEFAULT_YES));
    }

    private void fill(FmMemberAddress address, MemberAddressSaveDTO dto) {
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setTag(dto.getTag());
    }

    private MemberAddressVO toVO(FmMemberAddress address) {
        MemberAddressVO vo = new MemberAddressVO();
        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setIsDefault(address.getIsDefault());
        vo.setTag(address.getTag());
        vo.setFullAddress(buildFullAddress(address));
        return vo;
    }

    private String buildFullAddress(FmMemberAddress address) {
        StringBuilder builder = new StringBuilder();
        append(builder, address.getProvince());
        append(builder, address.getCity());
        append(builder, address.getDistrict());
        if (StringUtils.hasText(address.getDetailAddress())) {
            builder.append(address.getDetailAddress());
        }
        return builder.toString();
    }

    private void append(StringBuilder builder, String part) {
        if (StringUtils.hasText(part)) {
            builder.append(part);
        }
    }
}
