<template>
  <div>
    <van-nav-bar title="商品列表" />
    <van-search v-model="query.keyword" placeholder="搜索商品、产地" @search="reload" />

    <van-tabs v-model:active="activeCategory" @change="reload">
      <van-tab title="全部" :name="0" />
      <van-tab v-for="item in categories" :key="item.id" :title="item.categoryName" :name="item.id" />
    </van-tabs>

    <van-dropdown-menu>
      <van-dropdown-item v-model="query.sortBy" :options="sortOptions" @change="reload" />
      <van-dropdown-item v-model="priceRange" :options="priceOptions" @change="onPriceChange" />
    </van-dropdown-menu>

    <div class="fm-page">
      <ProductCard v-for="item in list" :key="item.id" :product="item" />
      <van-empty v-if="!loading && list.length === 0" description="没有找到符合条件的商品" />
      <div v-if="finished && list.length > 0" class="fm-muted" style="text-align: center">没有更多了</div>
      <van-loading v-if="loading" size="20" style="text-align: center; padding: 12px" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { getCategoryTree } from '../../api/category'
import { getProductList } from '../../api/product'

const route = useRoute()
const categories = ref([])
const list = ref([])
const loading = ref(false)
const finished = ref(false)
const activeCategory = ref(0)
const priceRange = ref('')

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: route.query.keyword || '',
  sortBy: 'sales',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  minPrice: null,
  maxPrice: null
})

const sortOptions = [
  { text: '销量优先', value: 'sales' },
  { text: '最新上架', value: 'new' },
  { text: '价格从低到高', value: 'priceAsc' },
  { text: '价格从高到低', value: 'priceDesc' }
]

const priceOptions = [
  { text: '价格不限', value: '' },
  { text: '50 元以下', value: '0-50' },
  { text: '50-100 元', value: '50-100' },
  { text: '100 元以上', value: '100-' }
]

const onPriceChange = (value) => {
  if (!value) {
    query.minPrice = null
    query.maxPrice = null
  } else {
    const [min, max] = value.split('-')
    query.minPrice = min === '' ? null : Number(min)
    query.maxPrice = max === '' ? null : Number(max)
  }
  reload()
}

const loadPage = async () => {
  loading.value = true
  try {
    const page = await getProductList({ ...query, categoryId: query.categoryId || undefined })
    list.value = page?.list || []
    finished.value = (page?.list || []).length < query.pageSize
  } finally {
    loading.value = false
  }
}

const reload = () => {
  query.pageNum = 1
  query.categoryId = activeCategory.value || null
  loadPage()
}

onMounted(async () => {
  categories.value = (await getCategoryTree()) || []
  if (query.categoryId) {
    activeCategory.value = query.categoryId
  }
  loadPage()
})
</script>
