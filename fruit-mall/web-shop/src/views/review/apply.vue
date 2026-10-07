<template>
  <div>
    <van-nav-bar title="发表评价" left-arrow @click-left="$router.back()" />
    <div class="fm-page">
      <div class="fm-card" style="text-align: center">
        <div class="fm-muted">商品满意度</div>
        <van-rate v-model="form.star" size="28" style="margin: 10px 0" />
      </div>

      <div class="fm-card">
        <van-field v-model="form.content" type="textarea" rows="4" maxlength="500" show-word-limit
                   label="评价内容" placeholder="说说口感、新鲜度与物流体验" />
      </div>

      <div class="fm-card">
        <van-field v-model="form.images" label="图片地址" placeholder='选填，JSON 数组，如 ["/api/files/a.jpg"]' />
      </div>

      <div class="fm-card">
        <van-switch v-model="anonymous" size="20" /> <span style="margin-left: 8px">匿名评价</span>
      </div>

      <van-button round block type="primary" :loading="submitting" @click="onSubmit">提交评价</van-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { createReview } from '../../api/review'
import { reportBehavior } from '../../api/behavior'

const route = useRoute()
const router = useRouter()
const anonymous = ref(false)
const submitting = ref(false)
const form = reactive({ star: 5, content: '', images: '' })

const onSubmit = async () => {
  submitting.value = true
  try {
    await createReview({
      orderItemId: Number(route.params.orderItemId),
      star: form.star,
      content: form.content,
      images: form.images || null,
      isAnonymous: anonymous.value ? 1 : 0
    })
    reportBehavior('REVIEW', 'SPU', null, { scene: 'ORDER' })
    showToast('评价成功')
    router.back()
  } finally {
    submitting.value = false
  }
}
</script>
