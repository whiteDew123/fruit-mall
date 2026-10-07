import request from '../utils/request'

/** 销售趋势 */
export const getSalesTrend = (params) => request.get('/admin/stat/sales-trend', { params })

/** 品类销售占比 */
export const getCategoryRatio = () => request.get('/admin/stat/category-ratio')

/** 商品销量排行 */
export const getProductTop = (params) => request.get('/admin/stat/product-top', { params })
