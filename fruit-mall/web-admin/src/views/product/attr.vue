<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button v-if="userStore.hasPerm('product:attr:update')" type="success" @click="openDialog(null)">
        新增属性
      </el-button>
      <span class="hint">标记为"上架必填"的属性，商品未填写时无法上架</span>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="attrCode" label="属性编码" width="150" />
      <el-table-column prop="attrName" label="属性名称" width="130" />
      <el-table-column label="数据类型" width="110">
        <template #default="{ row }">{{ typeText(row.dataType) }}</template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="90" />
      <el-table-column label="取值范围" width="150">
        <template #default="{ row }">
          <span v-if="row.minValue != null">{{ row.minValue }} ~ {{ row.maxValue }}</span>
          <span v-else class="hint">{{ row.enumOptions || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="上架必填" width="100">
        <template #default="{ row }">
          <el-tag :type="row.required === 1 ? 'danger' : 'info'">
            {{ row.required === 1 ? '必填' : '选填' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPerm('product:attr:update')" link type="primary"
                     @click="openDialog(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑属性' : '新增属性'" width="460px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="属性编码"><el-input v-model="form.attrCode" /></el-form-item>
        <el-form-item label="属性名称"><el-input v-model="form.attrName" /></el-form-item>
        <el-form-item label="数据类型">
          <el-select v-model="form.dataType" style="width: 100%">
            <el-option label="数值" :value="10" />
            <el-option label="枚举" :value="20" />
            <el-option label="文本" :value="30" />
            <el-option label="布尔" :value="40" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="如 级 / Brix" /></el-form-item>
        <el-form-item label="枚举取值">
          <el-input v-model="form.enumOptions" placeholder='JSON 数组，如 ["一级","二级"]' />
        </el-form-item>
        <el-form-item label="数值范围">
          <el-input-number v-model="form.minValue" /> ~ <el-input-number v-model="form.maxValue" />
        </el-form-item>
        <el-form-item label="上架必填">
          <el-switch v-model="required" />
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import { createAttr, getAttrList, updateAttr } from '../../api/product'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const required = ref(false)
const form = reactive({
  id: null, attrCode: '', attrName: '', dataType: 10, unit: '',
  enumOptions: '', minValue: null, maxValue: null, sort: 0
})

const typeText = (type) => ({ 10: '数值', 20: '枚举', 30: '文本', 40: '布尔' }[type] || '-')

const load = async () => {
  loading.value = true
  try {
    list.value = (await getAttrList()) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  Object.assign(form, {
    id: row?.id || null,
    attrCode: row?.attrCode || '',
    attrName: row?.attrName || '',
    dataType: row?.dataType ?? 10,
    unit: row?.unit || '',
    enumOptions: row?.enumOptions || '',
    minValue: row?.minValue ?? null,
    maxValue: row?.maxValue ?? null,
    sort: row?.sort ?? 0
  })
  required.value = row?.required === 1
  dialogVisible.value = true
}

const onSave = async () => {
  if (!form.attrCode || !form.attrName) {
    ElMessage.warning('请填写属性编码与名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, required: required.value ? 1 : 0, status: 10 }
    if (form.id) {
      await updateAttr(form.id, payload)
    } else {
      await createAttr(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hint {
  color: #909399;
  font-size: 12px;
}
</style>
