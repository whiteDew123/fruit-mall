import request from '../utils/request'

/** 履约单列表 */
export const getFulfillmentList = (params) => request.get('/admin/fulfillment/list', { params })

/** 履约单详情 */
export const getFulfillmentDetail = (id) => request.get(`/admin/fulfillment/${id}`)

/** 推进履约：action 取 pick / ready / deliver / arrive / sign / exception */
export const advanceFulfillment = (id, action, data) =>
  request.post(`/admin/fulfillment/${id}/${action}`, data || {})
