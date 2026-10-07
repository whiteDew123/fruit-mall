<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="商品名称 / 产地" clearable style="width: 200px"
                @keyup.enter="reload" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px">
        <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="草稿" :value="10" />
        <el-option label="上架" :value="20" />
        <el-option label="下架" :value="30" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <el-button v-if="userStore.hasPerm('product:create')" type="success" @click="$router.push('/product/form')">
        商品建档
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="图片" width="90">
        <template #default="{ row }">
          <el-image :src="row.mainImage" fit="cover" style="width: 60px; height: 60px" />
        </template>
      </el-table-column>
      <el-table-column prop="spuCode" label="商品编码" width="150" />
      <el-table-column prop="spuName" label="商品名称" min-width="160" />
      <el-table-column prop="categoryName" label="分类" width="110" />
      <el-table-column label="价格区间" width="150">
        <template #default="{ row }">
          <span class="price">
            ￥{{ row.minPrice?.toFixed(2) }} ~ ￥{{ row.maxPrice?.toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="availableStock" label="可售库存" width="100" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPerm('product:update')" link type="primary"
                     @click="$router.push(`/product/form/${row.id}`)">编辑</el-button>
          <el-button v-if="userStore.hasPerm('product:status')" link type="success"
                     @click="onToggleStatus(row)">
            {{ row.status === 20 ? '下架' : '上架' }}
          </el-button>
          <el-button v-if="userStore.hasPerm('product:delete')" link type="danger"
                     @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNum" @current-change="onPageChange" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changeProductStatus, getCategoryTree, getProductList, removeProduct } from '../../api/product'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null })

const flatCategories = computed(() => {
  const result = []
  categories.value.forEach((top) => {
    result.push({ id: top.id, label: top.categoryName })
    ;(top.children || []).forEach((child) => result.push({ id: child.id, label: `　${child.categoryName}` }))
  })
  return result
})

const statusText = (status) => ({ 10: '草稿', 20: '上架', 30: '下架' }[status] || '-')
const statusType = (status) => ({ 10: 'info', 20: 'success', 30: 'warning' }[status] || '')

const load = async () => {
  loading.value = true
  try {
    const page = await getProductList({ ...query })
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

const onToggleStatus = async (row) => {
  const target = row.status === 20 ? 30 : 20
  await changeProductStatus(row.id, target)
  ElMessage.success(target === 20 ? '已上架' : '已下架')
  load()
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除商品「${row.spuName}」？`, '提示', { type: 'warning' })
  await removeProduct(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  categories.value = (await getCategoryTree()) || []
  load()
})
</script>
