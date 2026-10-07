<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.spuId" placeholder="商品SPU ID" clearable style="width: 160px" />
      <el-select v-model="query.star" placeholder="全部星级" clearable style="width: 130px">
        <el-option v-for="star in [1, 2, 3, 4, 5]" :key="star" :label="`${star} 星`" :value="star" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="显示" :value="10" />
        <el-option label="隐藏" :value="20" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="spuName" label="商品" min-width="150" />
      <el-table-column prop="memberNickname" label="评价人" width="110" />
      <el-table-column label="星级" width="140">
        <template #default="{ row }">
          <el-rate :model-value="row.star" disabled />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="200" />
      <el-table-column prop="replyContent" label="商家回复" min-width="160" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 10 ? 'success' : 'info'">
            {{ row.status === 10 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="评价时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="userStore.hasPerm('review:reply')" link type="primary"
                     @click="onReply(row)">回复</el-button>
          <el-button v-if="userStore.hasPerm('review:status')" link
                     @click="onToggleStatus(row)">
            {{ row.status === 10 ? '隐藏' : '显示' }}
          </el-button>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changeReviewStatus, getReviewList, replyReview } from '../../api/review'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, spuId: null, star: null, status: null })

const load = async () => {
  loading.value = true
  try {
    const page = await getReviewList({ ...query })
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

const onReply = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入回复内容', '回复评价', {
    inputValue: row.replyContent || '感谢支持，欢迎复购'
  })
  await replyReview(row.id, value)
  ElMessage.success('回复成功')
  load()
}

const onToggleStatus = async (row) => {
  const target = row.status === 10 ? 20 : 10
  await changeReviewStatus(row.id, target)
  ElMessage.success(target === 20 ? '已隐藏（前台不再展示）' : '已恢复显示')
  load()
}

onMounted(load)
</script>
