<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <el-button @click="$router.back()">返回</el-button>
      <el-button v-if="userStore.hasPerm('order:remark')" type="primary" @click="onRemark">修改备注</el-button>
    </div>

    <el-descriptions v-if="order" :column="3" border>
      <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag>{{ order.statusDesc }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
      <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
      <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
      <el-descriptions-item label="应付金额">
        <span class="price">￥{{ order.payAmount?.toFixed(2) }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="收货地址" :span="3">{{ order.fullAddress }}</el-descriptions-item>
      <el-descriptions-item label="会员备注" :span="3">{{ order.memberRemark || '-' }}</el-descriptions-item>
      <el-descriptions-item label="商家备注" :span="3">{{ order.adminRemark || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">商品明细</el-divider>
    <el-table :data="order?.items || []" border>
      <el-table-column prop="spuName" label="商品" min-width="160" />
      <el-table-column prop="skuName" label="规格" width="120" />
      <el-table-column label="单价" width="110">
        <template #default="{ row }">￥{{ row.price?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="quantity" label="数量" width="80" />
      <el-table-column label="小计" width="110">
        <template #default="{ row }">￥{{ row.amount?.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="afterSaleQuantity" label="已售后" width="90" />
      <el-table-column label="已评价" width="90">
        <template #default="{ row }">{{ row.reviewed === 1 ? '是' : '否' }}</template>
      </el-table-column>
    </el-table>

    <el-divider content-position="left">状态时间轴</el-divider>
    <el-timeline>
      <el-timeline-item v-for="log in order?.statusLogs || []" :key="log.createTime + log.action"
                        :timestamp="log.createTime">
        {{ log.actionDesc }} ｜ {{ log.operatorName }} ｜ {{ log.remark }}
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, updateOrderRemark } from '../../api/order'
import { useUserStore } from '../../store/user'

const route = useRoute()
const userStore = useUserStore()
const order = ref(null)
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    order.value = await getOrderDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

const onRemark = async () => {
  const { value } = await ElMessageBox.prompt('请输入商家备注', '修改备注', {
    inputValue: order.value?.adminRemark || ''
  })
  await updateOrderRemark(order.value.id, value)
  ElMessage.success('备注已保存')
  load()
}

onMounted(load)
</script>
