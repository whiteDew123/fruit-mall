import request from '../utils/request'

/** 分类树（含已停用） */
export const getCategoryTree = () => request.get('/admin/product/category/tree')

/** 新增分类 */
export const createCategory = (data) => request.post('/admin/product/category', data)

/** 编辑分类 */
export const updateCategory = (id, data) => request.put(`/admin/product/category/${id}`, data)

/** 删除分类 */
export const removeCategory = (id) => request.delete(`/admin/product/category/${id}`)

/** 商品列表 */
export const getProductList = (params) => request.get('/admin/product/spu/list', { params })

/** 商品详情 */
export const getProductDetail = (id) => request.get(`/admin/product/spu/${id}`)

/** 商品建档 */
export const createProduct = (data) => request.post('/admin/product/spu', data)

/** 商品编辑 */
export const updateProduct = (id, data) => request.put(`/admin/product/spu/${id}`, data)

/** 商品上下架：status 20 上架 / 30 下架 */
export const changeProductStatus = (id, status) =>
  request.put(`/admin/product/spu/${id}/status`, null, { params: { status } })

/** 删除商品 */
export const removeProduct = (id) => request.delete(`/admin/product/spu/${id}`)

/** 特色属性定义列表 */
export const getAttrList = () => request.get('/admin/product/attr/list')

/** 新增特色属性 */
export const createAttr = (data) => request.post('/admin/product/attr', data)

/** 编辑特色属性 */
export const updateAttr = (id, data) => request.put(`/admin/product/attr/${id}`, data)

/** 上传图片，返回可访问地址 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/file/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
