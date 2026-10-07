import request from '../utils/request'

/** 提交评价 */
export const createReview = (data) => request.post('/shop/review', data)
