package com.fruitmall.stat.service.impl;

import com.fruitmall.stat.mapper.StatMapper;
import com.fruitmall.stat.query.StatQuery;
import com.fruitmall.stat.service.IStatService;
import com.fruitmall.stat.vo.CategoryRatioVO;
import com.fruitmall.stat.vo.ProductTopVO;
import com.fruitmall.stat.vo.SalesTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** 经营分析服务实现。 */
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements IStatService {

    private final StatMapper statMapper;

    @Override
    public List<SalesTrendVO> salesTrend(StatQuery query) {
        return statMapper.selectSalesTrend(query.getDays());
    }

    @Override
    public List<CategoryRatioVO> categoryRatio() {
        List<CategoryRatioVO> list = statMapper.selectCategoryRatio();
        BigDecimal total = list.stream()
                .map(item -> item.getAmount() == null ? BigDecimal.ZERO : item.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            list.forEach(item -> item.setRatio(BigDecimal.ZERO));
            return list;
        }
        // 占比在服务层计算，避免 SQL 里重复聚合
        for (CategoryRatioVO item : list) {
            BigDecimal amount = item.getAmount() == null ? BigDecimal.ZERO : item.getAmount();
            item.setRatio(amount.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP));
        }
        return list;
    }

    @Override
    public List<ProductTopVO> productTop(StatQuery query) {
        return statMapper.selectProductTop(query.getLimit());
    }
}
