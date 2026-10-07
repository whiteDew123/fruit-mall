<template>
  <div>
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />

    <div class="fm-page" v-if="order">
      <div class="fm-card">
        <div class="fm-row">
          <strong style="font-size: 16px">{{ order.statusDesc }}</strong>
          <span class="fm-muted">{{ order.orderNo }}</span>
        </div>
        <div v-if="order.status === 10" class="fm-muted" style="margin-top: 6px">
          请在 {{ order.expireTime }} 前完成支付，超时订单将自动关闭
        </div>
      </div>

      <div class="fm-card">
        <strong>配送地址</strong>
        <div style="margin-top: 6px">{{ order.receiverName }} {{ order.receiverPhone }}</div>
        <div class="fm-muted">{{ order.fullAddress }}</div>
      </div>

      <div class="fm-card">
        <strong>商品明细</strong>
        <div v-for="item in order.items" :key="item.id" class="line">
          <img class="fm-thumb" :src="item.image" />
          <div style="flex: 1">
            <div>{{ item.spuName }}</div>
            <div class="fm-muted">{{ item.skuName }}</div>
            <div class="fm-row">
              <span class="fm-price">￥{{ item.price.toFixed(2) }}</span>
              <span class="fm-muted">× {{ item.quantity }}</span>
            </div>
          </div>
          <div class="item-actions">
            <van-button v-if="order.status === 50 && item.reviewed === 0" size="mini" type="primary" round
                        @click="$router.push(`/review/${item.id}`)">评价</van-button>
            <van-button v-if="canApplyAfterSale(item)" size="mini" round
                        @click="$router.push(`/aftersale/apply/${item.id}`)">申请售后</van-button>
          </div>
        </div>
      </div>

      <div class="fm-card">
        <div class="fm-row"><span>商品金额</span><span>￥{{ order.totalAmount.toFixed(2) }}</span></div>
        <div class="fm-row" style="margin-top: 6px"><span>运费</span><span>￥{{ order.freightAmount.toFixed(2) }}</span></div>
        <div class="fm-row" style="margin-top: 6px">
          <strong>实付金额</strong>
          <span class="fm-price" style="font-size: 18px">￥{{ order.payAmount.toFixed(2) }}</span>
        </div>
        <div v-if="order.memberRemark" class="fm-muted" style="margin-top: 6px">备注：{{ order.memberRemark }}</div>
        <div v-if="order.adminRemark" class="fm-muted">商家备注：{{ order.adminRemark }}</div>
      </div>

      <div v-if="fulfillment" class="fm-card">
        <div class="fm-row">
          <strong>配送进度</strong>
          <span class="fm-muted">{{ fulfillment.statusDesc }}</span>
        </div>
        <van-steps direction="vertical" :active="fulfillment.traces.length - 1" style="margin-top: 10px">
          <van-step v-for="trace in fulfillment.traces" :key="trace.createTime + trace.node">
            <div>{{ trace.nodeName }}</div>
            <div class="fm-muted">{{ trace.remark }}</div>
            <div class="fm-muted">{{ trace.createTime }}</div>
          </van-step>
        </van-steps>
      </div>

      <div class="fm-card">
        <strong>订单流程</strong>
        <van-steps direction="vertical" :active="order.statusLogs.length - 1" style="margin-top: 10px">
          <van-step v-for="log in order.statusLogs" :key="log.createTime + log.action">
            <div>{{ log.actionDesc }}</div>
            <div class="fm-muted">{{ log.operatorName }} ｜ {{ log.createTime }}</div>
          </van-step>
        </van-steps>
      </div>
    </div>

    <van-submit-bar v-if="order && order.status === 10" :price="Math.round(order.payAmount * 100)"
                    button-text="去支付" @submit="$router.push(`/pay/${order.id}`)">
      <van-button size="small" round @click="onCancel">取消订单</van-button>
    </van-submit-bar>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { cancelOrder, getFulfillment, getOrderDetail } from '../../api/order'

const route = useRoute()
const order = ref(null)
const fulfillment = ref(null)

const canApplyAfterSale = (item) => {
  if (!order.value) return false
  const statusAllowed = order.value.status === 20 || order.value.status === 50
  const quantityLeft = item.quantity - (item.afterSaleQuantity || 0)
  return statusAllowed && quantityLeft > 0
}

const onCancel = async () => {
  await showConfirmDialog({ title: '取消订单', message: '取消后库存会立即释放，确认取消？' })
  await cancelOrder(order.value.id, '用户取消')
  showToast('订单已取消')
  await load()
}

const load = async () => {
  order.value = await getOrderDetail(route.params.id)
  // 只有支付成功之后才有履约单
  if (order.value.status >= 20) {
    fulfillment.value = await getFulfillment(order.value.id).catch(() => null)
  }
}

onMounted(load)
</script>

<style scoped>
.line {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  justify-content: center;
}
</style>
