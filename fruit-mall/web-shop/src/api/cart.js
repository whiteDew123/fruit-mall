import request from '../utils/request'

/** 购物车明细与汇总 */
export const getCart = () => request.get('/shop/cart/list')

/** 购物车角标数量 */
export const getCartCount = () => request.get('/shop/cart/count')

/** 加入购物车 */
export const addToCart = (data) => request.post('/shop/cart/add', data)

/** 修改数量 */
export const updateCartQuantity = (id, quantity) =>
  request.put(`/shop/cart/${id}/quantity`, { quantity })

/** 修改选中状态 */
export const updateCartSelected = (id, selected) =>
  request.put(`/shop/cart/${id}/selected`, null, { params: { selected } })

/** 全选 / 取消全选 */
export const selectAllCart = (selected) =>
  request.put('/shop/cart/selected/all', null, { params: { selected } })

/** 删除购物车项 */
export const removeCartItem = (id) => request.delete(`/shop/cart/${id}`)

/** 清空失效商品 */
export const clearInvalidCart = () => request.delete('/shop/cart/invalid')
