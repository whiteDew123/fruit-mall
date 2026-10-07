import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后台开发端口 5174，/api 代理到后端 8080
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
