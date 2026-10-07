import request from '../utils/request'

/** 模拟支付：result 取 SUCCESS / FAIL / TIMEOUT */
export const payOrder = (orderId, result) =>
  request.post('/shop/payment/pay', { orderId, result })

/** 查询订单支付单 */
export const getPayment = (orderId) => request.get(`/shop/payment/${orderId}`)
