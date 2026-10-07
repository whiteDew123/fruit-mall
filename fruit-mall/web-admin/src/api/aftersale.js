import request from '../utils/request'

/** 售后单列表 */
export const getAfterSaleList = (params) => request.get('/admin/aftersale/list', { params })

/** 售后单详情 */
export const getAfterSaleDetail = (id) => request.get(`/admin/aftersale/${id}`)

/** 审核：pass=true 通过 / false 驳回 */
export const auditAfterSale = (id, pass, remark) =>
  request.post(`/admin/aftersale/${id}/audit`, null, { params: { pass, remark } })

/** 确认收到退货 */
export const receiveAfterSale = (id, remark) =>
  request.post(`/admin/aftersale/${id}/receive`, null, { params: { remark } })

/** 确认退款完成 */
export const refundAfterSale = (id, remark) =>
  request.post(`/admin/aftersale/${id}/refund`, null, { params: { remark } })
