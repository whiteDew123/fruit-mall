<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 220px"
                @keyup.enter="reload" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" @click="reload">查询</el-button>
      <span class="hint">推进顺序：分拣完成 → 打包待配送 → 出库配送 → 送达 → 签收，不得跳级</span>
    </div>

    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="fulfillmentNo" label="履约单号" width="200" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="receiverPhone" label="联系电话" width="130" />
      <el-table-column prop="receiverAddress" label="收货地址" min-width="200" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.statusDesc }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="330" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 10 && can('fulfillment:pick')" link type="primary"
                     @click="advance(row, 'pick', '分拣完成')">分拣完成</el-button>
          <el-button v-if="row.status === 20 && can('fulfillment:pick')" link type="primary"
                     @click="advance(row, 'ready', '打包待配送')">打包待配送</el-button>
          <el-button v-if="row.status === 30 && can('fulfillment:deliver')" link type="primary"
                     @click="advance(row, 'deliver', '出库配送')">出库配送</el-button>
          <el-button v-if="row.status === 40 && can('fulfillment:deliver')" link type="primary"
                     @click="advance(row, 'arrive', '确认送达')">确认送达</el-button>
          <el-button v-if="row.status === 50 && can('fulfillment:sign')" link type="success"
                     @click="advance(row, 'sign', '确认签收')">确认签收</el-button>
          <el-button v-if="row.status !== 60 && can('fulfillment:exception')" link type="danger"
                     @click="onException(row)">异常</el-button>
          <el-button link @click="showDetail(row)">轨迹</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.pageSize"
                     :current-page="query.pageNum" @current-change="onPageChange" />
    </div>

    <el-dialog v-model="detailVisible" title="履约轨迹" width="560px">
      <el-timeline v-if="detail">
        <el-timeline-item v-for="trace in detail.traces" :key="trace.createTime + trace.node"
                          :timestamp="trace.createTime">
          <strong>{{ trace.nodeName }}</strong> ｜ {{ trace.operatorName }}
          <div class="hint">{{ trace.remark }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { advanceFulfillment, getFulfillmentDetail, getFulfillmentList } from '../../api/fulfillment'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)
const query = reactive({ pageNum: 1, pageSize: 10, orderNo: '', status: null })

const statusOptions = [
  { label: '待分拣', value: 10 },
  { label: '分拣完成', value: 20 },
  { label: '待配送', value: 30 },
  { label: '配送中', value: 40 },
  { label: '已送达', value: 50 },
  { label: '已签收', value: 60 },
  { label: '异常', value: 90 }
]

const statusType = (status) =>
  ({ 10: 'warning', 20: '', 30: '', 40: 'primary', 50: 'primary', 60: 'success', 90: 'danger' }[status] || '')

const can = (perm) => userStore.hasPerm(perm)

const load = async () => {
  loading.value = true
  try {
    const page = await getFulfillmentList({ ...query })
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

const advance = async (row, action, label) => {
  const { value } = await ElMessageBox.prompt(`确认执行「${label}」？可填写备注`, label, {
    inputValue: '',
    inputPlaceholder: '选填'
  })
  await advanceFulfillment(row.id, action, { remark: value || null })
  ElMessage.success(`${label}成功`)
  load()
}

const onException = async (row) => {
  const { value } = await ElMessageBox.prompt('请填写异常原因（必填）', '异常登记', {
    inputPlaceholder: '如 破损 / 缺货 / 拒收 / 改期'
  })
  if (!value) {
    ElMessage.warning('异常原因不能为空')
    return
  }
  await advanceFulfillment(row.id, 'exception', { remark: value })
  ElMessage.success('异常已登记')
  load()
}

const showDetail = async (row) => {
  detail.value = await getFulfillmentDetail(row.id)
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
