import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发服务器把 /api 代理到后端，避免跨域；生产环境由 Nginx 做同样的转发
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
