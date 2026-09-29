package com.fruitmall.product.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruitmall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 商品分类（多级）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fm_category")
public class FmCategory extends BaseEntity {

    /** 父级ID，0 为顶级 */
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    /** 分类编码 */
    private String categoryCode;

    /** 层级 */
    private Integer level;

    /** 排序 */
    private Integer sort;

    /** 图标地址 */
    private String icon;

    /** 状态，见 CategoryStatusEnum */
    private Integer status;

    /** 备注 */
    private String remark;
}
