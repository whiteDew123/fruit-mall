<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 220px"
                @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <span class="hint">流程：待审核 → 已同意 → 退货中 → 退款中 → 已完成</span>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="afterSaleNo" label="售后单号" width="200" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="typeDesc" label="类型" width="100" />
      <el-table-column prop="quantity" label="数量" width="70" />
      <el-table-column label="退款金额" width="110">
        <template #default="{ row }"><span class="price">￥{{ row.refundAmount?.toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="reason" label="申请原因" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 10 && can('aftersale:audit')" link type="primary"
                     @click="onAudit(row)">审核</el-button>
          <el-button v-if="row.status === 40 && can('aftersale:receive')" link type="primary"
                     @click="onReceive(row)">确认收货</el-button>
          <el-button v-if="row.status === 50 && can('aftersale:refund')" link type="success"
                     @click="onRefund(row)">确认退款</el-button>
          <el-button link @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <el-dialog v-model="detailVisible" title="售后详情" width="600px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="售后单号">{{ detail.afterSaleNo }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.typeDesc }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.statusDesc }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">￥{{ detail.refundAmount?.toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="退款单号">{{ detail.refundNo || '尚未发起' }}</el-descriptions-item>
        <el-descriptions-item label="退款状态">{{ detail.refundStatusDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">{{ detail.applyTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">售后商品</el-divider>
      <el-table :data="detail?.items || []" border size="small">
        <el-table-column prop="spuName" label="商品" min-width="140" />
        <el-table-column prop="skuName" label="规格" width="110" />
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="退款金额" width="110">
          <template #default="{ row }">￥{{ row.refundAmount?.toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditAfterSale, getAfterSaleDetail, getAfterSaleList, receiveAfterSale, refundAfterSale
} from '../../api/aftersale'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', status: null })

const statusOptions = [
  { label: '待审核', value: 10 },
  { label: '已同意', value: 20 },
  { label: '已驳回', value: 30 },
  { label: '退货中', value: 40 },
  { label: '退款中', value: 50 },
  { label: '已完成', value: 60 },
  { label: '已取消', value: 70 }
]

const statusType = (status) =>
  ({ 10: 'warning', 20: 'primary', 30: 'danger', 40: '', 50: 'primary', 60: 'success', 70: 'info' }[status] || '')

const can = (perm) => userStore.hasPerm(perm)

const load = async () => {
  loading.value = true
  try {
    const page = await getAfterSaleList({ ...query })
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

const onAudit = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入审核意见（通过或驳回都会记录）', '审核售后', {
    inputValue: '同意退款'
  })
  const action = await ElMessageBox.confirm('点击「确定」为通过，点击「取消」为驳回', '审核结果', {
    confirmButtonText: '审核通过',
    cancelButtonText: '驳回',
    distinguishCancelAndClose: true
  }).then(() => true).catch((action) => (action === 'cancel' ? false : null))
  if (action === null) return
  await auditAfterSale(row.id, action, value)
  ElMessage.success(action ? '已通过' : '已驳回')
  load()
}

const onReceive = async (row) => {
  const { value } = await ElMessageBox.prompt('确认收到退货？可填写备注', '确认收货', { inputValue: '已收到退货' })
  await receiveAfterSale(row.id, value)
  ElMessage.success('已确认收货（退货进入暂存，不回补可售库存）')
  load()
}

const onRefund = async (row) => {
  await ElMessageBox.confirm(`确认退款 ￥${row.refundAmount.toFixed(2)}？`, '确认退款', { type: 'warning' })
  await refundAfterSale(row.id, '退款完成')
  ElMessage.success('退款完成')
  load()
}

const showDetail = async (row) => {
  detail.value = await getAfterSaleDetail(row.id)
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.hint {
  color: #909399;
  font-size: 12px;
}
</style>
