package com.fruitmall.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 收货地址出参。 */
@Data
@Schema(description = "收货地址")
public class MemberAddressVO {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收货人")
    private String receiverName;

    @Schema(description = "收货电话")
    private String receiverPhone;

    @Schema(description = "省")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String detailAddress;

    @Schema(description = "完整地址，用于展示与下单快照")
    private String fullAddress;

    @Schema(description = "是否默认：0 否 / 1 是")
    private Integer isDefault;

    @Schema(description = "标签")
    private String tag;
}
