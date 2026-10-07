<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 220px"
                @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="联系电话" width="130" />
      <el-table-column prop="firstItemName" label="商品" min-width="160" />
      <el-table-column prop="totalQuantity" label="件数" width="80" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }"><span class="price">￥{{ row.payAmount?.toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="170" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/order/detail/${row.id}`)">详情</el-button>
          <el-button v-if="row.status === 10 && userStore.hasPerm('order:cancel')" link type="danger"
                     @click="onCancel(row)">取消</el-button>
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
import { cancelOrder, getOrderList } from '../../api/order'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', status: null })

const statusOptions = [
  { label: '待支付', value: 10 },
  { label: '已支付', value: 20 },
  { label: '备货中', value: 30 },
  { label: '配送中', value: 40 },
  { label: '已完成', value: 50 },
  { label: '已取消', value: 60 },
  { label: '退款中', value: 70 },
  { label: '已退款', value: 80 }
]

const statusType = (status) =>
  ({ 10: 'warning', 20: 'primary', 30: '', 40: '', 50: 'success', 60: 'info', 70: 'danger', 80: 'info' }[status] || '')

const load = async () => {
  loading.value = true
  try {
    const page = await getOrderList({ ...query })
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

const onCancel = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { inputValue: '商家取消' })
  await cancelOrder(row.id, value)
  ElMessage.success('订单已取消，库存已释放')
  load()
}

onMounted(load)
</script>
