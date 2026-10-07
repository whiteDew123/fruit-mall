<template>
  <div>
    <van-nav-bar title="购物车" />
    <div class="fm-page">
      <van-empty v-if="items.length === 0" description="购物车还是空的" image="search">
        <van-button round type="primary" size="small" @click="$router.push('/')">去逛逛</van-button>
      </van-empty>

      <template v-else>
        <div v-for="item in validItems" :key="item.id" class="fm-card">
          <div class="fm-row" style="align-items: flex-start; gap: 10px">
            <van-checkbox :model-value="item.selected === 1" @click="toggleSelected(item)" />
            <img class="fm-thumb" :src="item.skuImage || item.mainImage" @click="$router.push(`/product/${item.spuId}`)" />
            <div style="flex: 1; min-width: 0">
              <div>{{ item.spuName }}</div>
              <div class="fm-muted">{{ item.specName }}</div>
              <div class="fm-row" style="margin-top: 8px">
                <span class="fm-price">￥{{ item.price.toFixed(2) }}</span>
                <van-stepper :model-value="item.quantity" :max="item.availableStock" integer
                             @change="(value) => changeQuantity(item, value)" />
              </div>
              <div class="fm-muted" style="margin-top: 4px">
                小计 ￥{{ item.subtotal.toFixed(2) }}
                <span v-if="item.availableStock <= 0" style="color: #ee0a24"> ｜ 库存不足</span>
              </div>
            </div>
            <van-icon name="delete-o" size="18" @click="onRemove(item)" />
          </div>
        </div>

        <div v-if="invalidItems.length" class="fm-card">
          <div class="fm-row">
            <strong>失效商品（{{ invalidItems.length }}）</strong>
            <van-button size="mini" @click="onClearInvalid">清空失效</van-button>
          </div>
          <div v-for="item in invalidItems" :key="item.id" class="fm-muted" style="padding: 6px 0">
            {{ item.spuName }} × {{ item.quantity }} —— {{ item.invalidReason }}
          </div>
        </div>
      </template>
    </div>

    <van-submit-bar v-if="items.length" :price="Math.round(summary.selectedAmount * 100)"
                    button-text="去结算" @submit="goConfirm">
      <van-checkbox :model-value="summary.allSelected" @click="onSelectAll">全选</van-checkbox>
    </van-submit-bar>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import {
  clearInvalidCart, getCart, removeCartItem, selectAllCart,
  updateCartQuantity, updateCartSelected
} from '../../api/cart'

const router = useRouter()
const items = ref([])
const summary = ref({ itemCount: 0, selectedQuantity: 0, selectedAmount: 0, allSelected: false })

const validItems = computed(() => items.value.filter((item) => item.status === 10))
const invalidItems = computed(() => items.value.filter((item) => item.status !== 10))

const load = async () => {
  const data = await getCart()
  items.value = data?.items || []
  summary.value = data?.summary || summary.value
}

const toggleSelected = async (item) => {
  await updateCartSelected(item.id, item.selected !== 1)
  load()
}

const changeQuantity = async (item, value) => {
  if (!value || value === item.quantity) return
  try {
    await updateCartQuantity(item.id, value)
    load()
  } catch (e) {
    load()
  }
}

const onSelectAll = async () => {
  await selectAllCart(!summary.value.allSelected)
  load()
}

const onRemove = async (item) => {
  await showConfirmDialog({ title: '提示', message: `确认移出「${item.spuName}」？` })
  await removeCartItem(item.id)
  showToast('已移出购物车')
  load()
}

const onClearInvalid = async () => {
  await clearInvalidCart()
  showToast('已清空失效商品')
  load()
}

const goConfirm = () => {
  if (summary.value.selectedQuantity === 0) {
    showToast('请先选择要结算的商品')
    return
  }
  router.push('/order/confirm')
}

onMounted(load)
</script>
