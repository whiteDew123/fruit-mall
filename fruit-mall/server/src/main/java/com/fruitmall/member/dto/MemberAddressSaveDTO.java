package com.fruitmall.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 收货地址保存入参。 */
@Data
@Schema(description = "收货地址保存入参")
public class MemberAddressSaveDTO {

    @Schema(description = "收货人", example = "张三")
    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人长度不能超过 50")
    private String receiverName;

    @Schema(description = "收货电话", example = "13900000001")
    @NotBlank(message = "收货电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    @Schema(description = "省", example = "海南省")
    @Size(max = 50, message = "省份长度不能超过 50")
    private String province;

    @Schema(description = "市", example = "三亚市")
    @Size(max = 50, message = "城市长度不能超过 50")
    private String city;

    @Schema(description = "区县", example = "吉阳区")
    @Size(max = 50, message = "区县长度不能超过 50")
    private String district;

    @Schema(description = "详细地址", example = "迎宾路 100 号 3 栋 502")
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过 255")
    private String detailAddress;

    @Schema(description = "是否默认地址：0 否 / 1 是", example = "1")
    private Integer isDefault = 0;

    @Schema(description = "标签", example = "家")
    @Size(max = 20, message = "标签长度不能超过 20")
    private String tag;
}
