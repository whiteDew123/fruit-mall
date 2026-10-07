import request from '../utils/request'

/** 后台用户登录 */
export const login = (data) => request.post('/auth/admin/login', data)

/** 当前登录用户（用于刷新页面后恢复权限） */
export const getProfile = () => request.get('/auth/profile')
