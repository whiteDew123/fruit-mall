import request from '../utils/request'

/** 后台用户列表 */
export const getUserList = (params) => request.get('/admin/system/user/list', { params })

/** 新增后台用户 */
export const createUser = (data) => request.post('/admin/system/user', data)
