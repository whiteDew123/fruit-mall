package com.fruitmall.fulfillment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 履约推进入参。异常登记必须填写备注。 */
@Data
@Schema(description = "履约推进入参")
public class FulfillmentActionDTO {

    @Schema(description = "备注或异常原因", example = "客户要求下午配送")
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    @Schema(description = "图片地址 JSON 数组，如 [\"/api/files/a.jpg\"]")
    @Size(max = 1000, message = "图片地址长度不能超过 1000")
    private String images;
}
