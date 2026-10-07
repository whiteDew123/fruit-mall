<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 style="text-align: center; margin-top: 0">鲜果优选 · 商家后台</h2>
      <el-form :model="form" label-width="70px" @keyup.enter="onSubmit">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入后台账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">
          登录
        </el-button>
      </el-form>
      <div class="tip">
        演示账号：admin（系统管理员）/ operator（商家运营）/ fulfillment（履约售后）<br />
        密码统一为 Fruit@2026
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'Fruit@2026' })

const onSubmit = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号与密码')
    return
  }
  loading.value = true
  try {
    const data = await login({ ...form })
    userStore.setLogin(data.token, data.user)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d, #3a5169);
}

.login-card {
  width: 380px;
  padding: 10px;
}

.tip {
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
</style>
