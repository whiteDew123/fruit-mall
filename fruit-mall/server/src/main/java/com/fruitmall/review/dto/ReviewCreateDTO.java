package com.fruitmall.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 提交评价入参。 */
@Data
@Schema(description = "提交评价入参")
public class ReviewCreateDTO {

    @Schema(description = "订单项ID", example = "1")
    @NotNull(message = "订单项ID不能为空")
    private Long orderItemId;

    @Schema(description = "星级 1-5", example = "5")
    @NotNull(message = "请选择星级")
    @Min(value = 1, message = "星级最低为 1")
    @Max(value = 5, message = "星级最高为 5")
    private Integer star;

    @Schema(description = "评价内容", example = "芒果很甜，果核薄，物流快")
    @Size(max = 1000, message = "评价内容长度不能超过 1000")
    private String content;

    @Schema(description = "评价图片地址 JSON 数组")
    @Size(max = 1000, message = "图片地址长度不能超过 1000")
    private String images;

    @Schema(description = "是否匿名：0 否 / 1 是", example = "0")
    private Integer isAnonymous = 0;
}
