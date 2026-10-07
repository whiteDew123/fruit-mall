<template>
  <div>
    <van-nav-bar title="鲜果优选" />

    <van-search v-model="keyword" placeholder="搜索水果、产地" @search="goSearch" />

    <div class="fm-page">
      <div class="fm-card">
        <div class="fm-row" style="margin-bottom: 10px">
          <strong>全部分类</strong>
          <span class="fm-muted" @click="$router.push('/category')">查看全部 ›</span>
        </div>
        <div class="categories">
          <div v-for="item in topCategories" :key="item.id" class="category-item"
               @click="goCategory(item.id)">
            <img class="category-icon" :src="item.icon || placeholder" :alt="item.categoryName" />
            <span>{{ item.categoryName }}</span>
          </div>
        </div>
      </div>

      <div class="fm-card">
        <div class="fm-row" style="margin-bottom: 6px">
          <strong>猜你喜欢</strong>
          <!--
            当前先用"销量排序"作为推荐位的兜底数据，
            推荐模块上线后把数据源换成 GET /api/shop/recommend/home 即可，页面结构不用改。
          -->
          <span class="fm-muted">热销推荐</span>
        </div>
        <van-loading v-if="loading" size="20" style="padding: 20px 0" />
        <ProductCard v-for="item in recommendList" :key="item.id" :product="item" />
        <van-empty v-if="!loading && recommendList.length === 0" description="暂无商品，请先在后台建档" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { getCategoryTree } from '../../api/category'
import { getProductList } from '../../api/product'

const router = useRouter()
const keyword = ref('')
const topCategories = ref([])
const recommendList = ref([])
const loading = ref(false)
const placeholder = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg" width="48" height="48"%3E%3Crect width="48" height="48" fill="%23f2f3f5"/%3E%3C/svg%3E'

const loadData = async () => {
  loading.value = true
  try {
    const [tree, page] = await Promise.all([
      getCategoryTree(),
      getProductList({ pageNum: 1, pageSize: 6, sortBy: 'sales' })
    ])
    topCategories.value = (tree || []).slice(0, 8)
    recommendList.value = page?.list || []
  } finally {
    loading.value = false
  }
}

const goSearch = () => {
  router.push({ path: '/category', query: { keyword: keyword.value } })
}

const goCategory = (categoryId) => {
  router.push({ path: '/category', query: { categoryId } })
}

onMounted(loadData)
</script>

<style scoped>
.categories {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  object-fit: cover;
  background: #f7f8fa;
}
</style>
