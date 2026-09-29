package com.fruitmall.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 商品建档入参（SPU + 规格 + 特色属性 + 图片一次性提交）。 */
@Data
@Schema(description = "商品建档入参")
public class ProductSaveDTO {

    @Schema(description = "商品编码", example = "SPU-MANGO-001")
    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过 50")
    private String spuCode;

    @Schema(description = "分类ID", example = "2")
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @Schema(description = "商品名称", example = "海南小台农芒果")
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过 100")
    private String spuName;

    @Schema(description = "副标题/卖点", example = "树上熟 甜度高 果核薄")
    @Size(max = 200, message = "副标题长度不能超过 200")
    private String subtitle;

    @Schema(description = "产地", example = "海南三亚")
    @Size(max = 100, message = "产地长度不能超过 100")
    private String originPlace;

    @Schema(description = "应季月份，逗号分隔", example = "5,6,7")
    @Size(max = 20, message = "应季月份长度不能超过 20")
    private String seasonMonths;

    @Schema(description = "储存条件", example = "阴凉通风处")
    @Size(max = 100, message = "储存条件长度不能超过 100")
    private String storageCondition;

    @Schema(description = "保质期天数", example = "7")
    @Min(value = 0, message = "保质期不能为负")
    private Integer shelfLifeDays;

    @Schema(description = "计量单位", example = "斤")
    @Size(max = 20, message = "计量单位长度不能超过 20")
    private String unit;

    @Schema(description = "主图地址", example = "/api/files/mango.jpg")
    @Size(max = 255, message = "主图地址长度不能超过 255")
    private String mainImage;

    @Schema(description = "图文详情")
    private String detail;

    @Schema(description = "排序，越小越靠前", example = "0")
    private Integer sort = 0;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过 255")
    private String remark;

    @Schema(description = "商品规格，至少一个")
    @Valid
    @NotEmpty(message = "至少填写一个商品规格")
    private List<SkuSaveDTO> skus;

    @Schema(description = "特色属性取值")
    @Valid
    private List<AttrValueSaveDTO> attrs;

    @Schema(description = "详情图地址列表")
    private List<String> detailImages;
}
