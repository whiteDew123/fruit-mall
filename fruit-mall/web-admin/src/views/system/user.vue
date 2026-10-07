<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="登录名" clearable style="width: 180px"
                @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="正常" :value="10" />
        <el-option label="禁用" :value="20" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button v-if="userStore.hasPerm('system:user:create')" type="success" @click="dialogVisible = true">
        新增用户
      </el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="username" label="登录名" width="140" />
      <el-table-column prop="nickname" label="显示名" width="140" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="手机号（脱敏）" width="150" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 10 ? 'success' : 'info'">
            {{ row.status === 10 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="新增后台用户" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="登录名"><el-input v-model="form.username" placeholder="4-20 位字母数字下划线" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="显示名"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width: 100%">
            <el-option label="系统管理员" :value="1" />
            <el-option label="商家运营人员" :value="2" />
            <el-option label="履约与售后人员" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createUser, getUserList } from '../../api/system'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, username: '', status: null })
const form = reactive({ username: '', password: '', nickname: '', realName: '', phone: '', status: 10, roleIds: [] })

const load = async () => {
  loading.value = true
  try {
    const page = await getUserList({ ...query })
    list.value = page?.list || []
    total.value = page?.total || 0
  } finally {
    loading.value = false
  }
}

const reload = () => {
  query.pageNum = 1
  load()
}

const onPageChange = (pageNum) => {
  query.pageNum = pageNum
  load()
}

const onSave = async () => {
  if (!form.username || !form.password || !form.nickname) {
    ElMessage.warning('请填写登录名、密码与显示名')
    return
  }
  if (!form.roleIds.length) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  saving.value = true
  try {
    await createUser({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    Object.assign(form, { username: '', password: '', nickname: '', realName: '', phone: '', status: 10, roleIds: [] })
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
