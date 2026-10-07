<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button v-if="userStore.hasPerm('category:create')" type="success" @click="openDialog(null, 0)">
        新增一级分类
      </el-button>
      <span class="hint">提示：编辑不支持调整父级，避免形成环形结构</span>
    </div>

    <el-table :data="tree" row-key="id" border default-expand-all
              :tree-props="{ children: 'children' }" v-loading="loading">
      <el-table-column prop="categoryName" label="分类名称" min-width="180" />
      <el-table-column prop="categoryCode" label="编码" width="140" />
      <el-table-column prop="level" label="层级" width="80" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 10 ? 'success' : 'info'">
            {{ row.status === 10 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPerm('category:create')" link type="primary"
                     @click="openDialog(null, row.id)">新增子类</el-button>
          <el-button v-if="userStore.hasPerm('category:update')" link type="primary"
                     @click="openDialog(row)">编辑</el-button>
          <el-button v-if="userStore.hasPerm('category:delete')" link type="danger"
                     @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="form.categoryCode" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="10">启用</el-radio>
            <el-radio :value="20">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCategory, getCategoryTree, removeCategory, updateCategory } from '../../api/product'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const tree = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, parentId: 0, categoryName: '', categoryCode: '', sort: 0, status: 10, remark: '' })

const load = async () => {
  loading.value = true
  try {
    tree.value = (await getCategoryTree()) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row, parentId) => {
  Object.assign(form, {
    id: row?.id || null,
    parentId: row?.parentId ?? parentId ?? 0,
    categoryName: row?.categoryName || '',
    categoryCode: row?.categoryCode || '',
    sort: row?.sort ?? 0,
    status: row?.status ?? 10,
    remark: ''
  })
  dialogVisible.value = true
}

const onSave = async () => {
  if (!form.categoryName) {
    ElMessage.warning('请填写分类名称')
    return
  }
  saving.value = true
  try {
    if (form.id) {
      await updateCategory(form.id, { ...form })
    } else {
      await createCategory({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除分类「${row.categoryName}」？`, '提示', { type: 'warning' })
  await removeCategory(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.hint {
  color: #909399;
  font-size: 12px;
}
</style>
