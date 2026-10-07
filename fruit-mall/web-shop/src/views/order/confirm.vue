<template>
  <div>
    <van-nav-bar title="确认订单" left-arrow @click-left="$router.back()" />

    <div class="fm-page" style="padding-bottom: 80px">
      <div class="fm-card" @click="$router.push('/user/address')">
        <template v-if="preview.address">
          <div class="fm-row">
            <strong>{{ preview.address.receiverName }} {{ preview.address.receiverPhone }}</strong>
            <span v-if="preview.address.isDefault === 1" class="fm-muted">默认</span>
          </div>
          <div class="fm-muted" style="margin-top: 4px">{{ preview.address.fullAddress }}</div>
        </template>
        <div v-else class="fm-row">
          <span>请先添加收货地址</span>
          <span class="fm-muted">去添加 ›</span>
        </div>
      </div>

      <div class="fm-card">
        <strong>商品清单</strong>
        <div v-for="item in preview.items" :key="item.cartItemId" class="line">
          <img class="fm-thumb" :src="item.image" />
          <div style="flex: 1">
            <div>{{ item.spuName }}</div>
            <div class="fm-muted">{{ item.specName }}</div>
            <div class="fm-row">
              <span class="fm-price">￥{{ item.price.toFixed(2) }}</span>
              <span class="fm-muted">× {{ item.quantity }}</span>
            </div>
          </div>
        </div>
        <van-empty v-if="preview.items.length === 0" description="没有可结算的商品" image-size="60" />
      </div>

      <div class="fm-card">
        <van-field v-model="memberRemark" label="订单备注" placeholder="选填，如配送时间要求" maxlength="100" />
      </div>

      <div class="fm-card">
        <div class="fm-row"><span>商品金额</span><span>￥{{ preview.totalAmount.toFixed(2) }}</span></div>
        <div class="fm-row" style="margin-top: 6px"><span>运费</span><span>￥{{ preview.freightAmount.toFixed(2) }}</span></div>
        <div class="fm-row" style="margin-top: 6px">
          <strong>应付金额</strong>
          <span class="fm-price" style="font-size: 18px">￥{{ preview.payAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <van-submit-bar :price="Math.round(preview.payAmount * 100)" button-text="提交订单"
                    :loading="submitting" @submit="onSubmit" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getOrderPreview, submitOrder } from '../../api/order'

const router = useRouter()
const preview = ref({ items: [], totalAmount: 0, freightAmount: 0, payAmount: 0, address: null })
const memberRemark = ref('')
const submitting = ref(false)

const load = async () => {
  preview.value = await getOrderPreview()
}

const onSubmit = async () => {
  if (!preview.value.address) {
    showToast('请先添加收货地址')
    return
  }
  if (preview.value.items.length === 0) {
    showToast('没有可结算的商品')
    return
  }
  submitting.value = true
  try {
    const data = await submitOrder({
      addressId: preview.value.address.id,
      memberRemark: memberRemark.value
    })
    showToast('下单成功，请完成支付')
    router.replace(`/pay/${data.orderId}`)
  } finally {
    submitting.value = false
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
</style>
