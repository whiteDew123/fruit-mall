<template>
  <div>
    <van-nav-bar title="模拟支付" left-arrow @click-left="$router.replace(`/order/${orderId}`)" />

    <div class="fm-page">
      <div class="fm-card" style="text-align: center">
        <div class="fm-muted">订单号</div>
        <div>{{ order?.orderNo || '-' }}</div>
        <div class="fm-price" style="font-size: 28px; margin: 10px 0">
          ￥{{ order?.payAmount ? order.payAmount.toFixed(2) : '--' }}
        </div>
        <div class="fm-muted">请在 {{ order?.expireTime || '-' }} 前完成支付，超时订单将自动关闭</div>
      </div>

      <div v-if="result" class="fm-card" style="text-align: center">
        <van-icon :name="result.success ? 'checked' : 'clear'" size="48"
                  :color="result.success ? '#07c160' : '#ee0a24'" />
        <div style="margin-top: 8px">{{ result.message }}</div>
        <div class="fm-muted" style="margin-top: 4px">
          订单状态：{{ result.orderStatusDesc }} ｜ 支付单：{{ result.payStatusDesc }}
        </div>
        <van-button round type="primary" size="small" style="margin-top: 12px"
                    @click="$router.replace(`/order/${orderId}`)">
          查看订单
                </van-button>
      </div>

      <div v-else class="fm-card">
        <div class="fm-muted" style="margin-bottom: 10px">
          本课题不接真实支付渠道，请选择模拟结果以演示支付闭环与异常分支。
        </div>
        <van-button round block type="primary" :loading="paying" @click="onPay('SUCCESS')">
          模拟支付成功
        </van-button>
        <van-button round block plain type="warning" style="margin-top: 10px" :loading="paying"
                    @click="onPay('FAIL')">
          模拟支付失败
        </van-button>
        <van-button round block plain type="danger" style="margin-top: 10px" :loading="paying"
                    @click="onPay('TIMEOUT')">
          模拟支付超时（关单并释放库存）
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { payOrder } from '../../api/payment'
import { getOrderDetail } from '../../api/order'

const route = useRoute()
const orderId = route.params.orderId
const order = ref(null)
const result = ref(null)
const paying = ref(false)

const onPay = async (type) => {
  paying.value = true
  try {
    result.value = await payOrder(Number(orderId), type)
  } finally {
    paying.value = false
  }
}

onMounted(async () => {
  order.value = await getOrderDetail(orderId)
})
</script>
