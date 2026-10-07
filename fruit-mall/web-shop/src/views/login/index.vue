<template>
  <div>
    <van-nav-bar :title="mode === 'login' ? '会员登录' : '会员注册'" />
    <div class="fm-page">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field v-model="form.username" name="username" label="登录名"
                     placeholder="4-20 位字母、数字或下划线"
                     :rules="[{ required: true, message: '请填写登录名' }]" />
          <van-field v-model="form.password" type="password" name="password" label="密码"
                     placeholder="6-32 位"
                     :rules="[{ required: true, message: '请填写密码' }]" />
          <template v-if="mode === 'register'">
            <van-field v-model="form.nickname" name="nickname" label="昵称" placeholder="不填默认与登录名相同" />
            <van-field v-model="form.phone" name="phone" label="手机号" placeholder="选填" />
          </template>
        </van-cell-group>

        <div style="margin: 16px">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            {{ mode === 'login' ? '登录' : '注册并登录' }}
          </van-button>
        </div>
      </van-form>

      <div style="text-align: center">
        <van-button size="small" plain type="primary" @click="toggleMode">
          {{ mode === 'login' ? '没有账号？去注册' : '已有账号？去登录' }}
        </van-button>
      </div>

      <div class="fm-muted" style="text-align: center; margin-top: 20px">
        演示账号：fruitfan / Fruit@2026
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login, register } from '../../api/auth'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const mode = ref('login')
const submitting = ref(false)
const form = reactive({ username: '', password: '', nickname: '', phone: '' })

const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'register' : 'login'
}

const onSubmit = async () => {
  submitting.value = true
  try {
    if (mode.value === 'register') {
      await register({ ...form })
      showToast('注册成功')
    }
    const data = await login({ username: form.username, password: form.password })
    userStore.setLogin(data.token, data.user)
    showToast('登录成功')
    router.replace(route.query.redirect || '/')
  } finally {
    submitting.value = false
  }
}
</script>
