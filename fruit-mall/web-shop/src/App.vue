<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="[]">
      <component :is="Component" />
    </keep-alive>
  </router-view>

  <van-tabbar v-if="showTabbar" route safe-area-inset-bottom>
    <van-tabbar-item replace to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item replace to="/category" icon="apps-o">分类</van-tabbar-item>
    <van-tabbar-item replace to="/cart" icon="shopping-cart-o" :badge="cartBadge">购物车</van-tabbar-item>
    <van-tabbar-item replace to="/order/list" icon="orders-o">订单</van-tabbar-item>
    <van-tabbar-item replace to="/user" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCartCount } from './api/cart'
import { useUserStore } from './store/user'

const route = useRoute()
const userStore = useUserStore()
const cartBadge = ref('')

const showTabbar = computed(() => route.meta.tabbar === true)

const refreshCartBadge = async () => {
  if (!userStore.isLogin) {
    cartBadge.value = ''
    return
  }
  try {
    const count = await getCartCount()
    cartBadge.value = count > 0 ? String(count) : ''
  } catch (e) {
    cartBadge.value = ''
  }
}

onMounted(refreshCartBadge)
watch(() => route.fullPath, refreshCartBadge)
</script>
