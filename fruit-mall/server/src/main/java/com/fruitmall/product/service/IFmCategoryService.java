package com.fruitmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.product.domain.FmCategory;
import com.fruitmall.product.dto.CategorySaveDTO;
import com.fruitmall.product.vo.CategoryVO;

import java.util.List;

/** 分类服务。 */
public interface IFmCategoryService extends IService<FmCategory> {

    /**
     * 分类树
     *
     * @param onlyEnabled true 只返回启用状态的分类（消费者端用）
     */
    List<CategoryVO> listTree(boolean onlyEnabled);

    /** 新增分类 */
    Long create(CategorySaveDTO dto);

    /** 编辑分类（不支持调整父级，避免形成环） */
    void update(Long id, CategorySaveDTO dto);

    /** 删除分类：存在子分类或已挂商品时拒绝 */
    void delete(Long id);
}
