import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/home/index.vue'), meta: { tabbar: true } },
  { path: '/category', name: 'category', component: () => import('../views/product/list.vue'), meta: { tabbar: true } },
  { path: '/product/:id', name: 'productDetail', component: () => import('../views/product/detail.vue') },
  { path: '/cart', name: 'cart', component: () => import('../views/cart/index.vue'), meta: { tabbar: true, auth: true } },
  { path: '/order/confirm', name: 'orderConfirm', component: () => import('../views/order/confirm.vue'), meta: { auth: true } },
  { path: '/order/list', name: 'orderList', component: () => import('../views/order/list.vue'), meta: { tabbar: true, auth: true } },
  { path: '/order/:id', name: 'orderDetail', component: () => import('../views/order/detail.vue'), meta: { auth: true } },
  { path: '/pay/:orderId', name: 'pay', component: () => import('../views/pay/index.vue'), meta: { auth: true } },
  { path: '/review/:orderItemId', name: 'review', component: () => import('../views/review/apply.vue'), meta: { auth: true } },
  { path: '/aftersale/apply/:orderItemId', name: 'aftersaleApply', component: () => import('../views/aftersale/apply.vue'), meta: { auth: true } },
  { path: '/aftersale/list', name: 'aftersaleList', component: () => import('../views/aftersale/list.vue'), meta: { auth: true } },
  { path: '/user', name: 'user', component: () => import('../views/user/index.vue'), meta: { tabbar: true, auth: true } },
  { path: '/user/address', name: 'address', component: () => import('../views/user/address.vue'), meta: { auth: true } },
  { path: '/login', name: 'login', component: () => import('../views/login/index.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  const token = localStorage.getItem('fm_token')
  if (to.meta.auth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
