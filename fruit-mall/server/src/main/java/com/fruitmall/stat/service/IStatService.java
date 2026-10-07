package com.fruitmall.stat.service;

import com.fruitmall.stat.query.StatQuery;
import com.fruitmall.stat.vo.CategoryRatioVO;
import com.fruitmall.stat.vo.ProductTopVO;
import com.fruitmall.stat.vo.SalesTrendVO;

import java.util.List;

/** 经营分析服务。 */
public interface IStatService {

    /** 近 N 天销售趋势 */
    List<SalesTrendVO> salesTrend(StatQuery query);

    /** 品类销售占比 */
    List<CategoryRatioVO> categoryRatio();

    /** 商品销量排行 */
    List<ProductTopVO> productTop(StatQuery query);
}
