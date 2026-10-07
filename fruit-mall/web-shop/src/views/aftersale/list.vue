<template>
  <div>
    <van-nav-bar title="我的售后" left-arrow @click-left="$router.back()" />
    <div class="fm-page">
      <van-empty v-if="!loading && list.length === 0" description="没有售后记录" />
      <div v-for="item in list" :key="item.id" class="fm-card">
        <div class="fm-row">
          <span class="fm-muted">{{ item.afterSaleNo }}</span>
          <strong>{{ item.statusDesc }}</strong>
        </div>
        <div style="margin-top: 6px">
          {{ item.typeDesc }} ｜ 数量 {{ item.quantity }} ｜ 退款 ￥{{ item.refundAmount.toFixed(2) }}
        </div>
        <div class="fm-muted">原因：{{ item.reason }}</div>
        <div class="fm-muted">{{ item.createTime }}</div>
        <div v-if="item.status === 10" style="text-align: right; margin-top: 8px">
          <van-button size="mini" round @click="onCancel(item)">撤销申请</van-button>
        </div>
      </div>
      <van-loading v-if="loading" size="20" style="text-align: center; padding: 12px" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { cancelAfterSale, getAfterSaleList } from '../../api/aftersale'

const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const page = await getAfterSaleList({ pageNum: 1, pageSize: 20 })
    list.value = page?.list || []
  } finally {
    loading.value = false
  }
}

const onCancel = async (item) => {
  await showConfirmDialog({ title: '撤销申请', message: '确认撤销该售后申请？' })
  await cancelAfterSale(item.id, '会员撤销申请')
  showToast('已撤销')
  load()
}

onMounted(load)
</script>
