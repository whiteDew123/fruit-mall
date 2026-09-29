package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 分类树出参。 */
@Data
@Schema(description = "分类节点")
public class CategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "父级ID，0 为顶级")
    private Long parentId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "分类编码")
    private String categoryCode;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "图标地址")
    private String icon;

    @Schema(description = "状态：10 启用 / 20 停用")
    private Integer status;

    @Schema(description = "子分类")
    private List<CategoryVO> children = new ArrayList<>();
}
