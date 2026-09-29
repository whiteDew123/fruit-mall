package com.fruitmall.member.controller;

import com.fruitmall.common.result.Result;
import com.fruitmall.member.dto.MemberAddressSaveDTO;
import com.fruitmall.member.service.IFmMemberAddressService;
import com.fruitmall.member.service.IFmMemberService;
import com.fruitmall.member.vo.MemberAddressVO;
import com.fruitmall.member.vo.MemberInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 消费者端 —— 个人中心与收货地址。需要会员登录。 */
@Tag(name = "消费者端-会员")
@RestController
@RequestMapping("/api/shop/member")
@RequiredArgsConstructor
public class MemberController {

    private final IFmMemberService fmMemberService;
    private final IFmMemberAddressService fmMemberAddressService;

    @Operation(summary = "我的信息")
    @GetMapping("/profile")
    public Result<MemberInfoVO> profile() {
        return Result.ok(fmMemberService.currentMember());
    }

    @Operation(summary = "修改我的信息", description = "只支持修改昵称与头像")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestParam(required = false) String nickname,
                                      @RequestParam(required = false) String avatar) {
        fmMemberService.updateProfile(nickname, avatar);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "收货地址列表")
    @GetMapping("/address/list")
    public Result<List<MemberAddressVO>> addressList() {
        return Result.ok(fmMemberAddressService.listMine());
    }

    @Operation(summary = "新增收货地址")
    @PostMapping("/address")
    public Result<Long> createAddress(@Valid @RequestBody MemberAddressSaveDTO dto) {
        return Result.ok("新增成功", fmMemberAddressService.create(dto));
    }

    @Operation(summary = "编辑收货地址")
    @PutMapping("/address/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @Valid @RequestBody MemberAddressSaveDTO dto) {
        fmMemberAddressService.update(id, dto);
        return Result.ok("修改成功", null);
    }

    @Operation(summary = "删除收货地址")
    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        fmMemberAddressService.delete(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/address/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        fmMemberAddressService.setDefault(id);
        return Result.ok("设置成功", null);
    }
}
