import request from '../utils/request'

/** 确认订单页数据 */
export const getOrderPreview = () => request.get('/shop/order/preview')

/** 提交订单 */
export const submitOrder = (data) => request.post('/shop/order/submit', data)

/** 我的订单列表 */
export const getOrderList = (params) => request.get('/shop/order/list', { params })

/** 订单详情 */
export const getOrderDetail = (id) => request.get(`/shop/order/${id}`)

/** 取消订单 */
export const cancelOrder = (id, reason) =>
  request.post(`/shop/order/${id}/cancel`, null, { params: { reason } })

/** 订单履约进度 */
export const getFulfillment = (orderId) => request.get(`/shop/fulfillment/${orderId}`)
