import request from '../utils/request'

/** 评价列表 */
export const getReviewList = (params) => request.get('/admin/review/list', { params })

/** 回复评价 */
export const replyReview = (id, replyContent) =>
  request.put(`/admin/review/${id}/reply`, null, { params: { replyContent } })

/** 显示/隐藏评价 */
export const changeReviewStatus = (id, status) =>
  request.put(`/admin/review/${id}/status`, null, { params: { status } })
