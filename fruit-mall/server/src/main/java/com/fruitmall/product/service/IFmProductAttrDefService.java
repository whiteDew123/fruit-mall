package com.fruitmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.product.domain.FmProductAttrDef;
import com.fruitmall.product.dto.AttrDefSaveDTO;

import java.util.List;

/** 特色属性定义服务。 */
public interface IFmProductAttrDefService extends IService<FmProductAttrDef> {

    /** 全部启用中的属性定义，按排序返回 */
    List<FmProductAttrDef> listEnabled();

    /** 新增属性定义 */
    Long create(AttrDefSaveDTO dto);

    /** 编辑属性定义 */
    void update(Long id, AttrDefSaveDTO dto);
}
