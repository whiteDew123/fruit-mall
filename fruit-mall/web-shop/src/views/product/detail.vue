<template>
  <div>
    <van-nav-bar title="商品详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="!detail" size="24" style="padding: 40px 0; text-align: center" />

    <div v-else>
      <van-swipe :autoplay="4000" class="gallery">
        <van-swipe-item v-for="(img, index) in gallery" :key="index">
          <img :src="img" class="gallery-img" :alt="detail.spuName" />
        </van-swipe-item>
      </van-swipe>

      <div class="fm-card">
        <div class="fm-row">
          <span class="fm-price" style="font-size: 20px">￥{{ currentSku ? currentSku.price.toFixed(2) : '--' }}</span>
          <span class="fm-muted">已售 {{ detail.salesCount || 0 }}</span>
        </div>
        <h3 style="margin: 8px 0 4px">{{ detail.spuName }}</h3>
        <div class="fm-muted">{{ detail.subtitle }}</div>
        <div class="fm-muted" style="margin-top: 6px">
          产地：{{ detail.originPlace || '-' }} ｜ 应季：{{ detail.seasonMonths || '-' }} ｜ 储存：{{ detail.storageCondition || '-' }}
        </div>
      </div>

      <div class="fm-card">
        <strong>选择规格</strong>
        <div class="skus">
          <van-button v-for="sku in detail.skus" :key="sku.id" size="small"
                      :type="currentSku && currentSku.id === sku.id ? 'primary' : 'default'"
                      :disabled="sku.status !== 10 || sku.availableStock <= 0"
                      @click="currentSku = sku">
            {{ sku.specName }}（{{ sku.availableStock > 0 ? '剩 ' + sku.availableStock : '缺货' }}）
          </van-button>
        </div>
        <div class="fm-row" style="margin-top: 12px">
          <span>购买数量</span>
          <van-stepper v-model="quantity" :min="1" :max="maxQuantity" integer />
        </div>
      </div>

      <div v-if="detail.attrs && detail.attrs.length" class="fm-card">
        <strong>特色属性</strong>
        <div class="attrs">
          <div v-for="attr in detail.attrs" :key="attr.attrDefId" class="attr-item">
            <span class="fm-muted">{{ attr.attrName }}</span>
            <span>{{ attr.attrValue }}{{ attr.unit || '' }}</span>
          </div>
        </div>
      </div>

      <div class="fm-card">
        <strong>商品评价（{{ reviewTotal }}）</strong>
        <van-empty v-if="reviews.length === 0" description="还没有评价" image-size="60" />
        <div v-for="review in reviews" :key="review.id" class="review">
          <div class="fm-row">
            <span>{{ review.memberNickname }}</span>
            <van-rate :model-value="review.star" readonly size="12" />
          </div>
          <div style="margin: 4px 0">{{ review.content }}</div>
          <div class="fm-muted">{{ review.createTime }}</div>
          <div v-if="review.replyContent" class="reply">商家回复：{{ review.replyContent }}</div>
        </div>
      </div>

      <div class="fm-card">
        <strong>图文详情</strong>
        <div v-if="detail.detail" v-html="detail.detail"></div>
        <div v-else class="fm-muted">暂无详情</div>
      </div>

      <van-goods-action>
        <van-goods-action-icon icon="cart-o" text="购物车" :badge="cartCount || ''" @click="$router.push('/cart')" />
        <van-goods-action-button type="warning" text="加入购物车" @click="onAddCart" />
        <van-goods-action-button type="danger" text="立即购买" @click="onBuyNow" />
      </van-goods-action>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getProductDetail, getProductReviews } from '../../api/product'
import { addToCart, getCartCount, selectAllCart } from '../../api/cart'
import { reportBehavior } from '../../api/behavior'
import { useUserStore } from '../../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const detail = ref(null)
const currentSku = ref(null)
const quantity = ref(1)
const reviews = ref([])
const reviewTotal = ref(0)
const cartCount = ref(0)

const gallery = computed(() => {
  if (!detail.value) return []
  const images = [detail.value.mainImage, ...(detail.value.detailImages || [])]
    .filter((url) => !!url)
  return images.map((url) => url.replace('/api/files', '/api/files'))
})

const maxQuantity = computed(() => {
  const stock = currentSku.value?.availableStock || 1
  return Math.max(stock, 1)
})

const loadReviews = async () => {
  const page = await getProductReviews(route.params.id, { pageNum: 1, pageSize: 5 })
  reviews.value = page?.list || []
  reviewTotal.value = page?.total || 0
}

const refreshCartCount = async () => {
  if (!userStore.isLogin) return
  try {
    cartCount.value = (await getCartCount()) || 0
  } catch (e) {
    cartCount.value = 0
  }
}

const requireSku = () => {
  if (!currentSku.value) {
    showToast('请先选择规格')
    return false
  }
  if (currentSku.value.availableStock < quantity.value) {
    showToast('库存不足')
    return false
  }
  return true
}

const onAddCart = async () => {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!requireSku()) return
  await addToCart({ skuId: currentSku.value.id, quantity: quantity.value })
  showToast('已加入购物车')
  refreshCartCount()
}

const onBuyNow = async () => {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!requireSku()) return
  // 立即购买：先取消其它商品的勾选，再加购当前规格并进入确认订单
  await selectAllCart(false)
  await addToCart({ skuId: currentSku.value.id, quantity: quantity.value })
  router.push('/order/confirm')
}

onMounted(async () => {
  detail.value = await getProductDetail(route.params.id)
  currentSku.value = (detail.value.skus || []).find((sku) => sku.status === 10 && sku.availableStock > 0)
    || detail.value.skus?.[0] || null
  await loadReviews()
  refreshCartCount()
  // 浏览详情埋点，是推荐系统的核心行为数据
  reportBehavior('VIEW', 'SPU', Number(route.params.id), { scene: 'DETAIL' })
})
</script>

<style scoped>
.gallery-img {
  width: 100%;
  height: 260px;
  object-fit: cover;
}

.skus {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.attrs {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-top: 10px;
}

.attr-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.review {
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
  font-size: 13px;
}

.reply {
  margin-top: 6px;
  padding: 6px 8px;
  background: #f7f8fa;
  border-radius: 6px;
  font-size: 12px;
  color: #646566;
}
</style>
