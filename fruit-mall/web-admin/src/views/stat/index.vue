<template>
  <div>
    <div class="page-card" style="margin-bottom: 12px">
      <div class="toolbar">
        <span>统计区间：</span>
        <el-radio-group v-model="days" @change="loadTrend">
          <el-radio-button :value="7">近 7 天</el-radio-button>
          <el-radio-button :value="30">近 30 天</el-radio-button>
          <el-radio-button :value="90">近 90 天</el-radio-button>
        </el-radio-group>
        <el-button @click="loadAll">刷新</el-button>
        <span class="hint">
          统计口径：只计入已支付及之后（备货中/配送中/已完成）的订单，退款与取消不计入
        </span>
      </div>

      <el-row :gutter="12">
        <el-col :span="8">
          <div class="metric">
            <div class="metric-label">区间销售额</div>
            <div class="metric-value price">￥{{ totalAmount.toFixed(2) }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric">
            <div class="metric-label">区间订单数</div>
            <div class="metric-value">{{ totalOrders }}</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="metric">
            <div class="metric-label">客单价</div>
            <div class="metric-value price">￥{{ avgAmount.toFixed(2) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-row :gutter="12">
      <el-col :span="24">
        <div class="page-card">
          <strong>销售趋势</strong>
          <div ref="trendRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card" style="margin-top: 12px">
          <strong>品类销售占比</strong>
          <div ref="ratioRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card" style="margin-top: 12px">
          <strong>商品销量 TOP10</strong>
          <div ref="topRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getCategoryRatio, getProductTop, getSalesTrend } from '../../api/stat'

const days = ref(30)
const trendRef = ref(null)
const ratioRef = ref(null)
const topRef = ref(null)
const trendData = ref([])
const ratioData = ref([])
const topData = ref([])
let trendChart = null
let ratioChart = null
let topChart = null

const totalAmount = computed(() =>
  trendData.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
)
const totalOrders = computed(() =>
  trendData.value.reduce((sum, item) => sum + Number(item.orderCount || 0), 0)
)
const avgAmount = computed(() => (totalOrders.value ? totalAmount.value / totalOrders.value : 0))

const renderTrend = () => {
  if (!trendChart) return
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'] },
    grid: { left: 50, right: 50, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: trendData.value.map((item) => item.statDate) },
    yAxis: [
      { type: 'value', name: '销售额' },
      { type: 'value', name: '订单数' }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: trendData.value.map((item) => Number(item.amount || 0))
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        barWidth: 14,
        data: trendData.value.map((item) => item.orderCount)
      }
    ]
  })
}

const renderRatio = () => {
  if (!ratioChart) return
  ratioChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ￥{c} ({d}%)' },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        data: ratioData.value.map((item) => ({ name: item.categoryName, value: Number(item.amount || 0) }))
      }
    ]
  })
}

const renderTop = () => {
  if (!topChart) return
  const sorted = [...topData.value].reverse()
  topChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 120, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: sorted.map((item) => item.spuName) },
    series: [
      {
        type: 'bar',
        data: sorted.map((item) => item.quantity),
        label: { show: true, position: 'right' }
      }
    ]
  })
}

const loadTrend = async () => {
  trendData.value = (await getSalesTrend({ days: days.value })) || []
  renderTrend()
}

const loadAll = async () => {
  const [trend, ratio, top] = await Promise.all([
    getSalesTrend({ days: days.value }),
    getCategoryRatio(),
    getProductTop({ limit: 10 })
  ])
  trendData.value = trend || []
  ratioData.value = ratio || []
  topData.value = top || []
  renderTrend()
  renderRatio()
  renderTop()
}

const onResize = () => {
  trendChart?.resize()
  ratioChart?.resize()
  topChart?.resize()
}

onMounted(async () => {
  trendChart = echarts.init(trendRef.value)
  ratioChart = echarts.init(ratioRef.value)
  topChart = echarts.init(topRef.value)
  window.addEventListener('resize', onResize)
  await loadAll()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  ratioChart?.dispose()
  topChart?.dispose()
})
</script>

<style scoped>
.chart {
  height: 300px;
  margin-top: 10px;
}

.metric {
  padding: 6px 10px;
}

.metric-label {
  color: #909399;
  font-size: 13px;
}

.metric-value {
  font-size: 22px;
  font-weight: 600;
  margin-top: 4px;
}

.hint {
  color: #909399;
  font-size: 12px;
}
</style>
