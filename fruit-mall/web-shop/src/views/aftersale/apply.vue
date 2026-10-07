<template>
  <div>
    <van-nav-bar title="申请售后" left-arrow @click-left="$router.back()" />
    <div class="fm-page">
      <div class="fm-card">
        <van-field label="售后类型">
          <template #input>
            <van-radio-group v-model="form.type" direction="horizontal">
              <van-radio :name="10">仅退款</van-radio>
              <van-radio :name="20">退货退款</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field v-model="form.quantity" type="digit" label="售后数量" placeholder="不超过可售后数量" />
      </div>

      <div class="fm-card">
        <van-field v-model="form.reason" type="textarea" rows="3" maxlength="200" show-word-limit
                   label="申请原因" placeholder="如：收到时有两颗压伤" />
      </div>

      <div class="fm-card">
        <van-field v-model="form.evidenceImages" label="凭证图片" placeholder='选填，JSON 数组，如 ["/api/files/a.jpg"]' />
      </div>

      <div class="fm-muted" style="margin: 10px 0">
        提示：生鲜商品退货不进入可售库存，商家收到退货后按流程退款。
      </div>

      <van-button round block type="primary" :loading="submitting" @click="onSubmit">提交申请</van-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { applyAfterSale } from '../../api/aftersale'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const form = reactive({ type: 10, quantity: '1', reason: '', evidenceImages: '' })

const onSubmit = async () => {
  if (!form.reason) {
    showToast('请填写申请原因')
    return
  }
  submitting.value = true
  try {
    await applyAfterSale({
      orderItemId: Number(route.params.orderItemId),
      type: form.type,
      quantity: Number(form.quantity),
      reason: form.reason,
      evidenceImages: form.evidenceImages || null
    })
    showToast('申请已提交，等待商家审核')
    router.replace('/aftersale/list')
  } finally {
    submitting.value = false
  }
}
</script>
