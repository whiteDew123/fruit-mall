import { defineStore } from 'pinia'

const TOKEN_KEY = 'fm_admin_token'
const USER_KEY = 'fm_admin_user'

export const useUserStore = defineStore('adminUser', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  getters: {
    isLogin: (state) => !!state.token,
    permissions: (state) => (state.user && state.user.permissions) || [],
    roleCodes: (state) => (state.user && state.user.roleCodes) || []
  },
  actions: {
    setLogin(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user || null))
    },
    setUser(user) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user || null))
    },
    /** 按钮级权限判断，权限标识与后端 @RequiresPermission 一一对应 */
    hasPerm(code) {
      if (!code) return true
      return this.permissions.includes(code)
    },
    clear() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
