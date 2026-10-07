import request from '../utils/request'

/** 订单列表 */
export const getOrderList = (params) => request.get('/admin/order/list', { params })

/** 订单详情 */
export const getOrderDetail = (id) => request.get(`/admin/order/${id}`)

/** 订单备注 */
export const updateOrderRemark = (id, remark) =>
  request.put(`/admin/order/${id}/remark`, null, { params: { remark } })

/** 取消订单 */
export const cancelOrder = (id, reason) =>
  request.post(`/admin/order/${id}/cancel`, null, { params: { reason } })
