package com.fruitmall.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.common.enums.CategoryStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.product.domain.FmCategory;
import com.fruitmall.product.domain.FmProductSpu;
import com.fruitmall.product.dto.CategorySaveDTO;
import com.fruitmall.product.mapper.FmCategoryMapper;
import com.fruitmall.product.mapper.FmProductSpuMapper;
import com.fruitmall.product.service.IFmCategoryService;
import com.fruitmall.product.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 分类服务实现。 */
@Service
@RequiredArgsConstructor
public class FmCategoryServiceImpl extends ServiceImpl<FmCategoryMapper, FmCategory>
        implements IFmCategoryService {

    private final FmProductSpuMapper fmProductSpuMapper;

    @Override
    public List<CategoryVO> listTree(boolean onlyEnabled) {
        List<FmCategory> categories = this.list(Wrappers.<FmCategory>lambdaQuery()
                .eq(onlyEnabled, FmCategory::getStatus, CategoryStatusEnum.ENABLED.getCode())
                .orderByAsc(FmCategory::getSort)
                .orderByAsc(FmCategory::getId));

        Map<Long, CategoryVO> voMap = new LinkedHashMap<>();
        for (FmCategory category : categories) {
            voMap.put(category.getId(), toVO(category));
        }

        List<CategoryVO> roots = new ArrayList<>();
        for (CategoryVO vo : voMap.values()) {
            CategoryVO parent = voMap.get(vo.getParentId());
            if (parent == null) {
                roots.add(vo);
            } else {
                parent.getChildren().add(vo);
            }
        }
        sortTree(roots);
        return roots;
    }

    @Override
    public Long create(CategorySaveDTO dto) {
        FmCategory category = new FmCategory();
        category.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        category.setLevel(resolveLevel(category.getParentId()));
        fillFields(category, dto);
        this.save(category);
        return category.getId();
    }

    @Override
    public void update(Long id, CategorySaveDTO dto) {
        FmCategory exists = this.getById(id);
        if (exists == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
        // 只更新可编辑字段，父级与层级保持不变，避免出现环形结构
        FmCategory update = new FmCategory();
        update.setId(id);
        fillFields(update, dto);
        this.updateById(update);
    }

    @Override
    public void delete(Long id) {
        FmCategory exists = this.getById(id);
        if (exists == null) {
            throw new BizException(ResultCode.NOT_FOUND, "分类不存在");
        }
        Long childCount = this.count(Wrappers.<FmCategory>lambdaQuery().eq(FmCategory::getParentId, id));
        if (childCount > 0) {
            throw new BizException(ResultCode.CONFLICT, "该分类下还有子分类，请先处理子分类");
        }
        Long productCount = fmProductSpuMapper.selectCount(
                Wrappers.<FmProductSpu>lambdaQuery().eq(FmProductSpu::getCategoryId, id));
        if (productCount > 0) {
            throw new BizException(ResultCode.CONFLICT, "该分类下还有商品，不能删除");
        }
        this.removeById(id);
    }

    private void fillFields(FmCategory category, CategorySaveDTO dto) {
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryCode(dto.getCategoryCode());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        category.setIcon(dto.getIcon());
        category.setStatus(dto.getStatus());
        category.setRemark(dto.getRemark());
    }

    private Integer resolveLevel(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return 1;
        }
        FmCategory parent = this.getById(parentId);
        if (parent == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "父级分类不存在");
        }
        return parent.getLevel() + 1;
    }

    private void sortTree(List<CategoryVO> nodes) {
        nodes.sort(Comparator.comparing(CategoryVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(CategoryVO::getId));
        for (CategoryVO node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private CategoryVO toVO(FmCategory category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());
        vo.setCategoryName(category.getCategoryName());
        vo.setCategoryCode(category.getCategoryCode());
        vo.setLevel(category.getLevel());
        vo.setSort(category.getSort());
        vo.setIcon(category.getIcon());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
