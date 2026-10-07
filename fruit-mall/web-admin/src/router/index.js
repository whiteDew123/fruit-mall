import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由与菜单一体化定义：permission 为进入该页所需权限标识，与 sys_menu.perm 保持一致。
 * 侧边栏据此过滤，前端隐藏入口只是体验优化，真正的权限校验在后端。
 */
export const menuRoutes = [
  {
    path: '/product',
    title: '商品管理',
    children: [
      { path: '/product/list', title: '商品列表', permission: 'product:list' },
      { path: '/product/category', title: '分类管理', permission: 'category:list' },
      { path: '/product/attr', title: '特色属性', permission: 'product:list' }
    ]
  },
  {
    path: '/order',
    title: '订单管理',
    children: [{ path: '/order/list', title: '订单列表', permission: 'order:list' }]
  },
  {
    path: '/fulfillment',
    title: '履约管理',
    children: [{ path: '/fulfillment/list', title: '履约单', permission: 'fulfillment:list' }]
  },
  {
    path: '/aftersale',
    title: '售后管理',
    children: [{ path: '/aftersale/list', title: '售后单', permission: 'aftersale:list' }]
  },
  {
    path: '/review',
    title: '评价管理',
    children: [{ path: '/review/list', title: '评价列表', permission: 'review:list' }]
  },
  {
    path: '/stat',
    title: '经营分析',
    children: [{ path: '/stat/index', title: '数据看板', permission: 'stat:sales' }]
  },
  {
    path: '/system',
    title: '系统管理',
    children: [{ path: '/system/user', title: '用户管理', permission: 'system:user:list' }]
  }
]

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/login/index.vue') },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/product/list',
    children: [
      { path: 'product/list', component: () => import('../views/product/list.vue') },
      { path: 'product/form/:id?', component: () => import('../views/product/form.vue') },
      { path: 'product/category', component: () => import('../views/product/category.vue') },
      { path: 'product/attr', component: () => import('../views/product/attr.vue') },
      { path: 'order/list', component: () => import('../views/order/list.vue') },
      { path: 'order/detail/:id', component: () => import('../views/order/detail.vue') },
      { path: 'fulfillment/list', component: () => import('../views/fulfillment/list.vue') },
      { path: 'aftersale/list', component: () => import('../views/aftersale/list.vue') },
      { path: 'review/list', component: () => import('../views/review/list.vue') },
      { path: 'stat/index', component: () => import('../views/stat/index.vue') },
      { path: 'system/user', component: () => import('../views/system/user.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('fm_admin_token')
  if (to.path !== '/login' && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
