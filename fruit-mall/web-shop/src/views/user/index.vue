<template>
  <div>
    <van-nav-bar title="个人中心" />
    <div class="fm-page">
      <div class="fm-card">
        <template v-if="profile">
          <div class="fm-row">
            <div>
              <div style="font-size: 16px; font-weight: 500">{{ profile.nickname }}</div>
              <div class="fm-muted">{{ profile.phone || profile.username }}</div>
            </div>
            <van-icon name="setting-o" size="20" @click="onEditNickname" />
          </div>
          <div class="fm-muted" style="margin-top: 6px">注册时间：{{ profile.createTime }}</div>
        </template>
        <van-empty v-else description="未获取到会员信息" image-size="60" />
      </div>

      <van-cell-group inset>
        <van-cell title="我的订单" is-link to="/order/list" icon="orders-o" />
        <van-cell title="收货地址" is-link to="/user/address" icon="location-o" />
        <van-cell title="我的售后" is-link to="/aftersale/list" icon="after-sale" />
      </van-cell-group>

      <div style="margin: 16px">
        <van-button round block plain type="danger" @click="onLogout">退出登录</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getProfile, updateProfile } from '../../api/member'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const profile = ref(null)

const load = async () => {
  profile.value = await getProfile()
  userStore.setUser({ userId: profile.value.memberId, username: profile.value.username, nickname: profile.value.nickname })
}

const onEditNickname = async () => {
  const nickname = window.prompt('修改昵称', profile.value?.nickname || '')
  if (!nickname) return
  await updateProfile({ nickname })
  showToast('修改成功')
  load()
}

const onLogout = async () => {
  await showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
  userStore.clear()
  router.replace('/login')
}

onMounted(load)
</script>
