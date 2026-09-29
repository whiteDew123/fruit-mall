package com.fruitmall.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 分类新增/编辑入参。 */
@Data
@Schema(description = "分类保存入参")
public class CategorySaveDTO {

    @Schema(description = "父级ID，0 为顶级", example = "0")
    @NotNull(message = "父级ID不能为空")
    private Long parentId = 0L;

    @Schema(description = "分类名称", example = "热带水果")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过 50")
    private String categoryName;

    @Schema(description = "分类编码", example = "TROPICAL")
    @Size(max = 50, message = "分类编码长度不能超过 50")
    private String categoryCode;

    @Schema(description = "排序，越小越靠前", example = "1")
    private Integer sort = 0;

    @Schema(description = "图标地址")
    @Size(max = 255, message = "图标地址长度不能超过 255")
    private String icon;

    @Schema(description = "状态：10 启用 / 20 停用", example = "10")
    @NotNull(message = "状态不能为空")
    private Integer status = 10;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;
}
