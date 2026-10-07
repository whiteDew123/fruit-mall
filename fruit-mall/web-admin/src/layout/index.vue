<template>
  <el-container style="height: 100%">
    <el-aside width="200px" :style="{ background: 'var(--fm-sidebar-bg)' }">
      <div class="logo">鲜果优选 · 商家后台</div>
      <el-menu :default-active="route.path" router background-color="#1f2d3d"
               text-color="#c0c4cc" active-text-color="#ffd04b">
        <el-sub-menu v-for="group in visibleMenus" :key="group.path" :index="group.path">
          <template #title>{{ group.title }}</template>
          <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
            {{ item.title }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div>{{ currentTitle }}</div>
        <div class="header-right">
          <span class="role">{{ userStore.user?.nickname }}（{{ roleText }}）</span>
          <el-button link type="primary" @click="onLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main style="background: var(--fm-content-bg)">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { menuRoutes } from '../router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 按当前账号的权限过滤菜单，无权限的分组整体不展示 */
const visibleMenus = computed(() =>
  menuRoutes
    .map((group) => ({
      ...group,
      children: group.children.filter((item) => userStore.hasPerm(item.permission))
    }))
    .filter((group) => group.children.length > 0)
)

const currentTitle = computed(() => {
  for (const group of menuRoutes) {
    const item = group.children.find((child) => route.path.startsWith(child.path))
    if (item) return `${group.title} / ${item.title}`
  }
  return '商家后台'
})

const roleText = computed(() => (userStore.roleCodes || []).join('、') || '未分配角色')

const onLogout = async () => {
  await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
  userStore.clear()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.role {
  color: #606266;
  font-size: 13px;
}
</style>
