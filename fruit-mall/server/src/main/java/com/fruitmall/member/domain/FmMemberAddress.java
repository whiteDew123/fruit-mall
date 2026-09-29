package com.fruitmall.member.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 收货地址。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_member_address")
public class FmMemberAddress extends BaseEntity {

    /** 会员ID */
    private Long memberId;

    /** 收货人 */
    private String receiverName;

    /** 收货电话 */
    private String receiverPhone;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区县 */
    private String district;

    /** 详细地址 */
    private String detailAddress;

    /** 是否默认：0 否 / 1 是 */
    private Integer isDefault;

    /** 标签：家/公司等 */
    private String tag;
}
