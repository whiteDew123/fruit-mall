<template>
  <div class="product-card" @click="$router.push(`/product/${product.id}`)">
    <img class="thumb" :src="product.mainImage || placeholder" :alt="product.spuName" />
    <div class="info">
      <div class="name">{{ product.spuName }}</div>
      <div class="subtitle van-ellipsis">{{ product.subtitle }}</div>
      <div class="fm-row">
        <span class="fm-price">￥{{ priceText }}</span>
        <span class="fm-muted">已售 {{ product.salesCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: { type: Object, required: true }
})

const placeholder = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="80" height="80"%3E%3Crect width="80" height="80" fill="%23f2f3f5"/%3E%3C/svg%3E'

const priceText = computed(() => {
  const min = props.product.minPrice
  const max = props.product.maxPrice
  if (min == null) return '--'
  return min === max ? min.toFixed(2) : `${min.toFixed(2)} 起`
})
</script>

<style scoped>
.product-card {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f2f3f5;
}

.thumb {
  width: 90px;
  height: 90px;
  border-radius: 8px;
  object-fit: cover;
  background: #f7f8fa;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.name {
  font-size: 15px;
  font-weight: 500;
}

.subtitle {
  font-size: 12px;
  color: #969799;
}
</style>
