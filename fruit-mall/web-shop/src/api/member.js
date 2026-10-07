import request from '../utils/request'

/** 我的信息 */
export const getProfile = () => request.get('/shop/member/profile')

/** 修改昵称与头像 */
export const updateProfile = (params) => request.put('/shop/member/profile', null, { params })

/** 收货地址列表 */
export const getAddressList = () => request.get('/shop/member/address/list')

/** 新增地址 */
export const createAddress = (data) => request.post('/shop/member/address', data)

/** 编辑地址 */
export const updateAddress = (id, data) => request.put(`/shop/member/address/${id}`, data)

/** 删除地址 */
export const removeAddress = (id) => request.delete(`/shop/member/address/${id}`)

/** 设为默认地址 */
export const setDefaultAddress = (id) => request.put(`/shop/member/address/${id}/default`)
