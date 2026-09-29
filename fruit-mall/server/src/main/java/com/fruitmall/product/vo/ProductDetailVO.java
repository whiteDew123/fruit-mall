package com.fruitmall.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 商品详情出参，含规格、特色属性与详情图。 */
@Data
@Schema(description = "商品详情")
public class ProductDetailVO {

    @Schema(description = "商品SPU ID")
    private Long id;

    @Schema(description = "商品编码")
    private String spuCode;

    @Schema(description = "商品名称")
    private String spuName;

    @Schema(description = "副标题/卖点")
    private String subtitle;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "应季月份")
    private String seasonMonths;

    @Schema(description = "储存条件")
    private String storageCondition;

    @Schema(description = "保质期天数")
    private Integer shelfLifeDays;

    @Schema(description = "计量单位")
    private String unit;

    @Schema(description = "主图地址")
    private String mainImage;

    @Schema(description = "图文详情")
    private String detail;

    @Schema(description = "累计销量")
    private Integer salesCount;

    @Schema(description = "状态：10 草稿 / 20 上架 / 30 下架")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商品规格列表")
    private List<ProductSkuVO> skus = new ArrayList<>();

    @Schema(description = "特色属性列表")
    private List<ProductAttrVO> attrs = new ArrayList<>();

    @Schema(description = "详情图地址列表")
    private List<String> detailImages = new ArrayList<>();
}
