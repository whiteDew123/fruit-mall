<template>
  <div>
    <van-nav-bar title="我的订单" />
    <van-tabs v-model:active="activeStatus" @change="reload">
      <van-tab title="全部" :name="0" />
      <van-tab title="待支付" :name="10" />
      <van-tab title="已支付" :name="20" />
      <van-tab title="配送中" :name="40" />
      <van-tab title="已完成" :name="50" />
      <van-tab title="退款/取消" :name="60" />
    </van-tabs>

    <div class="fm-page">
      <van-empty v-if="!loading && list.length === 0" description="还没有订单" />

      <div v-for="order in list" :key="order.id" class="fm-card" @click="$router.push(`/order/${order.id}`)">
        <div class="fm-row">
          <span class="fm-muted">{{ order.orderNo }}</span>
          <strong>{{ order.statusDesc }}</strong>
        </div>
        <div class="line">
          <img class="fm-thumb" :src="order.firstItemImage" />
          <div style="flex: 1">
            <div>{{ order.firstItemName }}</div>
            <div class="fm-muted">共 {{ order.itemCount }} 种商品，{{ order.totalQuantity }} 件</div>
          </div>
          <span class="fm-price">￥{{ order.payAmount.toFixed(2) }}</span>
        </div>
        <div class="fm-muted">{{ order.createTime }}</div>

        <div class="actions" @click.stop>
          <van-button v-if="order.status === 10" size="small" type="primary" round
                      @click="$router.push(`/pay/${order.id}`)">去支付</van-button>
          <van-button v-if="order.status === 10" size="small" round
                      @click="onCancel(order)">取消订单</van-button>
          <van-button v-if="order.status === 50" size="small" round type="primary"
                      @click="$router.push(`/order/${order.id}`)">去评价 / 售后</van-button>
        </div>
      </div>

      <van-loading v-if="loading" size="20" style="text-align: center; padding: 12px" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { cancelOrder, getOrderList } from '../../api/order'

const activeStatus = ref(0)
const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    const page = await getOrderList({
      pageNum: 1,
      pageSize: 20,
      status: activeStatus.value || undefined
    })
    list.value = page?.list || []
  } finally {
    loading.value = false
  }
}

const reload = () => load()

const onCancel = async (order) => {
  await showConfirmDialog({ title: '取消订单', message: '取消后库存会立即释放，确认取消？' })
  await cancelOrder(order.id, '用户取消')
  showToast('订单已取消')
  load()
}

onMounted(load)
</script>

<style scoped>
.line {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 0;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
