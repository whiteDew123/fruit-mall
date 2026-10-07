import request from '../utils/request'

/** 商品列表：只返回上架商品 */
export const getProductList = (params) => request.get('/shop/product/list', { params })

/** 商品详情 */
export const getProductDetail = (id) => request.get(`/shop/product/${id}`)

/** 商品评价列表 */
export const getProductReviews = (spuId, params) => request.get(`/shop/review/spu/${spuId}`, { params })
