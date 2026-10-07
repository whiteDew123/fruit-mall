import { defineStore } from 'pinia'

const TOKEN_KEY = 'fm_token'
const USER_KEY = 'fm_user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
    anonymousId: localStorage.getItem('fm_anonymous_id') || ''
  }),
  getters: {
    isLogin: (state) => !!state.token
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
    ensureAnonymousId() {
      if (!this.anonymousId) {
        this.anonymousId = 'anon-' + Math.random().toString(36).slice(2) + Date.now()
        localStorage.setItem('fm_anonymous_id', this.anonymousId)
      }
      return this.anonymousId
    },
    clear() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
