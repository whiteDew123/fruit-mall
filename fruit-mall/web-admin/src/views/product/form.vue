<template>
  <div class="page-card" v-loading="loading">
    <div class="toolbar">
      <strong>{{ isEdit ? '编辑商品' : '商品建档' }}</strong>
      <el-button @click="$router.back()">返回</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </div>

    <el-form :model="form" label-width="100px">
      <el-divider content-position="left">基本信息</el-divider>
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="商品编码" required>
            <el-input v-model="form.spuCode" placeholder="如 SPU-MANGO-001" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="商品名称" required>
            <el-input v-model="form.spuName" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="分类" required>
            <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option v-for="item in flatCategories" :key="item.id" :label="item.label" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="产地">
            <el-input v-model="form.originPlace" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="应季月份">
            <el-input v-model="form.seasonMonths" placeholder="如 5,6,7" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="储存条件">
            <el-input v-model="form.storageCondition" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="保质期（天）">
            <el-input-number v-model="form.shelfLifeDays" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="计量单位">
            <el-input v-model="form.unit" placeholder="如 斤 / 箱" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="副标题">
            <el-input v-model="form.subtitle" placeholder="卖点，如 树上熟 甜度高" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="图文详情">
            <el-input v-model="form.detail" type="textarea" :rows="3" placeholder="支持 HTML 片段" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-divider content-position="left">商品图片</el-divider>
      <el-form-item label="主图">
        <el-upload :show-file-list="false" :http-request="(options) => onUpload(options, 'main')">
          <el-button>上传主图</el-button>
        </el-upload>
        <el-image v-if="form.mainImage" :src="form.mainImage" style="width: 80px; height: 80px; margin-left: 12px" />
      </el-form-item>
      <el-form-item label="详情图">
        <el-upload :show-file-list="false" :http-request="(options) => onUpload(options, 'detail')">
          <el-button>上传详情图</el-button>
        </el-upload>
        <el-image v-for="(url, index) in form.detailImages" :key="url" :src="url"
                  style="width: 80px; height: 80px; margin-left: 12px"
                  @click="form.detailImages.splice(index, 1)" />
      </el-form-item>

      <el-divider content-position="left">商品规格（至少一个，售价与库存挂在规格上）</el-divider>
      <el-table :data="form.skus" border>
        <el-table-column label="规格编码" width="160">
          <template #default="{ row }"><el-input v-model="row.skuCode" /></template>
        </el-table-column>
        <el-table-column label="规格名" width="140">
          <template #default="{ row }"><el-input v-model="row.specName" placeholder="如 5斤装" /></template>
        </el-table-column>
        <el-table-column label="规格键值 JSON" width="200">
          <template #default="{ row }">
            <el-input v-model="row.specJson" placeholder='{"净重":"5斤"}' />
          </template>
        </el-table-column>
        <el-table-column label="售价" width="130">
          <template #default="{ row }"><el-input-number v-model="row.price" :min="0.01" :precision="2" /></template>
        </el-table-column>
        <el-table-column label="划线价" width="130">
          <template #default="{ row }"><el-input-number v-model="row.originalPrice" :min="0" :precision="2" /></template>
        </el-table-column>
        <el-table-column label="库存" width="120">
          <template #default="{ row }"><el-input-number v-model="row.stock" :min="0" /></template>
        </el-table-column>
        <el-table-column label="预警值" width="120">
          <template #default="{ row }"><el-input-number v-model="row.warnStock" :min="0" /></template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ $index }">
            <el-button link type="danger" @click="form.skus.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button style="margin-top: 10px" @click="addSku">新增规格</el-button>

      <el-divider content-position="left">特色属性（上架前必填项必须填写）</el-divider>
      <el-row :gutter="16">
        <el-col v-for="attr in attrs" :key="attr.id" :span="8">
          <el-form-item :label="attr.attrName + (attr.required === 1 ? ' *' : '')">
            <el-input v-model="attrValues[attr.id].attrValue" :placeholder="attr.unit || ''"
                      @change="onAttrChange(attr)" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createProduct, getAttrList, getCategoryTree, getProductDetail, updateProduct, uploadImage
} from '../../api/product'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const categories = ref([])
const attrs = ref([])
const attrValues = reactive({})
const form = reactive({
  spuCode: '', spuName: '', categoryId: null, subtitle: '', originPlace: '', seasonMonths: '',
  storageCondition: '', shelfLifeDays: 7, unit: '斤', mainImage: '', detail: '', sort: 0,
  remark: '', skus: [], detailImages: []
})

const isEdit = computed(() => !!route.params.id)

const flatCategories = computed(() => {
  const result = []
  categories.value.forEach((top) => {
    result.push({ id: top.id, label: top.categoryName })
    ;(top.children || []).forEach((child) => result.push({ id: child.id, label: `　${child.categoryName}` }))
  })
  return result
})

const addSku = () => {
  form.skus.push({
    id: null, skuCode: '', specName: '', specJson: '', price: 0.01,
    originalPrice: null, stock: 0, warnStock: 0, status: 10
  })
}

const onUpload = async (options, type) => {
  const url = await uploadImage(options.file)
  if (type === 'main') {
    form.mainImage = url
  } else {
    form.detailImages.push(url)
  }
  ElMessage.success('上传成功')
}

const onAttrChange = (attr) => {
  // 数值型属性同时写入 num_value，供推荐特征向量直接使用
  const value = attrValues[attr.id].attrValue
  if (attr.dataType === 10 && value !== '' && !Number.isNaN(Number(value))) {
    attrValues[attr.id].numValue = Number(value)
  } else {
    attrValues[attr.id].numValue = null
  }
}

const onSave = async () => {
  if (!form.spuCode || !form.spuName || !form.categoryId) {
    ElMessage.warning('请填写商品编码、名称与分类')
    return
  }
  if (!form.skus.length) {
    ElMessage.warning('至少填写一个商品规格')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      attrs: Object.entries(attrValues)
        .filter(([, value]) => value.attrValue !== '' && value.attrValue != null)
        .map(([attrDefId, value]) => ({
          attrDefId: Number(attrDefId),
          attrValue: String(value.attrValue),
          numValue: value.numValue
        }))
    }
    if (isEdit.value) {
      await updateProduct(route.params.id, payload)
    } else {
      await createProduct(payload)
    }
    ElMessage.success('保存成功，商品为草稿状态，请在列表中上架')
    router.push('/product/list')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const [tree, attrList] = await Promise.all([getCategoryTree(), getAttrList()])
    categories.value = tree || []
    attrs.value = attrList || []
    attrs.value.forEach((attr) => {
      attrValues[attr.id] = { attrValue: '', numValue: null }
    })

    if (isEdit.value) {
      const detail = await getProductDetail(route.params.id)
      Object.assign(form, {
        spuCode: detail.spuCode,
        spuName: detail.spuName,
        categoryId: detail.categoryId,
        subtitle: detail.subtitle,
        originPlace: detail.originPlace,
        seasonMonths: detail.seasonMonths,
        storageCondition: detail.storageCondition,
        shelfLifeDays: detail.shelfLifeDays,
        unit: detail.unit,
        mainImage: detail.mainImage,
        detail: detail.detail,
        sort: detail.sort,
        remark: detail.remark,
        detailImages: detail.detailImages || [],
        skus: (detail.skus || []).map((sku) => ({
          id: sku.id, skuCode: sku.skuCode, specName: sku.specName, specJson: sku.specJson,
          price: sku.price, originalPrice: sku.originalPrice, stock: sku.stock,
          warnStock: 0, status: sku.status
        }))
      })
      ;(detail.attrs || []).forEach((attr) => {
        attrValues[attr.attrDefId] = { attrValue: attr.attrValue, numValue: attr.numValue }
      })
    } else {
      addSku()
    }
  } finally {
    loading.value = false
  }
})
</script>
