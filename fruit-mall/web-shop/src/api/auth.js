import request from '../utils/request'

/** 会员注册 */
export const register = (data) => request.post('/auth/member/register', data)

/** 会员登录 */
export const login = (data) => request.post('/auth/member/login', data)
