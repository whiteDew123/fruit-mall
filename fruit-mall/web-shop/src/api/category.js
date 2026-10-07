import request from '../utils/request'

/** 分类树（只含启用分类） */
export const getCategoryTree = () => request.get('/shop/category/tree')
