import axios from 'axios'
import { showToast } from 'vant'
import router from '../router'
import { useUserStore } from '../store/user'

/**
 * 统一请求封装。
 * 页面里不直接写 axios：令牌注入、错误提示、401 跳登录都在这里处理。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    // 后端统一响应：code 与 HTTP 状态码一致，200 才算成功
    if (body && body.code === 200) {
      return body.data
    }
    showToast(body?.message || '请求失败')
    return Promise.reject(new Error(body?.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message
    if (status === 401) {
      const userStore = useUserStore()
      userStore.clear()
      showToast('请先登录')
      router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    } else if (status === 403) {
      showToast(message || '没有操作权限')
    } else {
      showToast(message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
