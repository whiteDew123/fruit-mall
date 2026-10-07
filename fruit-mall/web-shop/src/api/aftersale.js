import request from '../utils/request'

/** 申请售后 */
export const applyAfterSale = (data) => request.post('/shop/aftersale', data)

/** 我的售后单列表 */
export const getAfterSaleList = (params) => request.get('/shop/aftersale/list', { params })

/** 售后单详情 */
export const getAfterSaleDetail = (id) => request.get(`/shop/aftersale/${id}`)

/** 撤销申请 */
export const cancelAfterSale = (id, reason) =>
  request.post(`/shop/aftersale/${id}/cancel`, null, { params: { reason } })
