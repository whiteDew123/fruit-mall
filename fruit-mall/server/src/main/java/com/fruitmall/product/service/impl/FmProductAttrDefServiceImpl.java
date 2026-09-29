package com.fruitmall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.product.domain.FmProductAttrDef;
import com.fruitmall.product.dto.AttrDefSaveDTO;
import com.fruitmall.product.mapper.FmProductAttrDefMapper;
import com.fruitmall.product.service.IFmProductAttrDefService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/** 特色属性定义服务实现。 */
@Service
public class FmProductAttrDefServiceImpl extends ServiceImpl<FmProductAttrDefMapper, FmProductAttrDef>
        implements IFmProductAttrDefService {

    @Override
    public List<FmProductAttrDef> listEnabled() {
        return this.list(Wrappers.<FmProductAttrDef>lambdaQuery()
                .eq(FmProductAttrDef::getStatus, 10)
                .orderByAsc(FmProductAttrDef::getSort)
                .orderByAsc(FmProductAttrDef::getId));
    }

    @Override
    public Long create(AttrDefSaveDTO dto) {
        FmProductAttrDef def = new FmProductAttrDef();
        fill(def, dto);
        try {
            this.save(def);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "属性编码已存在：" + dto.getAttrCode());
        }
        return def.getId();
    }

    @Override
    public void update(Long id, AttrDefSaveDTO dto) {
        FmProductAttrDef exists = this.getById(id);
        if (exists == null) {
            throw new BizException(ResultCode.NOT_FOUND, "属性定义不存在");
        }
        FmProductAttrDef update = new FmProductAttrDef();
        update.setId(id);
        fill(update, dto);
        try {
            this.updateById(update);
        } catch (DuplicateKeyException e) {
            throw new BizException(ResultCode.CONFLICT, "属性编码已存在：" + dto.getAttrCode());
        }
    }

    private void fill(FmProductAttrDef def, AttrDefSaveDTO dto) {
        def.setAttrCode(dto.getAttrCode());
        def.setAttrName(dto.getAttrName());
        def.setDataType(dto.getDataType());
        def.setUnit(dto.getUnit());
        def.setEnumOptions(dto.getEnumOptions());
        def.setMinValue(dto.getMinValue());
        def.setMaxValue(dto.getMaxValue());
        def.setRequired(dto.getRequired() == null ? 0 : dto.getRequired());
        def.setSort(dto.getSort() == null ? 0 : dto.getSort());
        def.setStatus(dto.getStatus() == null ? 10 : dto.getStatus());
    }
}
