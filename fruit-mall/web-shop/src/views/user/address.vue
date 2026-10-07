<template>
  <div>
    <van-nav-bar title="收货地址" left-arrow @click-left="$router.back()" />

    <div class="fm-page">
      <div v-for="item in list" :key="item.id" class="fm-card">
        <div class="fm-row">
          <div>
            <span>{{ item.receiverName }}</span>
            <span class="fm-muted" style="margin-left: 8px">{{ item.receiverPhone }}</span>
            <van-tag v-if="item.isDefault === 1" type="primary" style="margin-left: 8px">默认</van-tag>
          </div>
          <van-icon name="edit" @click="startEdit(item)" />
        </div>
        <div class="fm-muted" style="margin-top: 4px">{{ item.fullAddress }}</div>
        <div class="actions">
          <van-button v-if="item.isDefault !== 1" size="mini" round @click="onSetDefault(item)">设为默认</van-button>
          <van-button size="mini" round type="danger" plain @click="onRemove(item)">删除</van-button>
        </div>
      </div>

      <van-empty v-if="list.length === 0" description="还没有收货地址" image-size="60" />
    </div>

    <div v-if="editing" class="fm-page" style="border-top: 8px solid var(--fm-bg)">
      <div class="fm-card">
        <strong>{{ form.id ? '编辑地址' : '新增地址' }}</strong>
        <van-field v-model="form.receiverName" label="收货人" placeholder="必填" />
        <van-field v-model="form.receiverPhone" label="手机号" placeholder="必填" />
        <van-field v-model="form.province" label="省" placeholder="如 海南省" />
        <van-field v-model="form.city" label="市" placeholder="如 三亚市" />
        <van-field v-model="form.district" label="区县" placeholder="如 吉阳区" />
        <van-field v-model="form.detailAddress" label="详细地址" placeholder="必填" />
        <van-field v-model="form.tag" label="标签" placeholder="家 / 公司" />
        <van-cell title="设为默认地址">
          <template #right-icon>
            <van-switch v-model="isDefault" size="20" />
          </template>
        </van-cell>
        <div style="margin-top: 12px; display: flex; gap: 10px">
          <van-button round block type="primary" :loading="saving" @click="onSave">保存</van-button>
          <van-button round block @click="editing = false">取消</van-button>
        </div>
      </div>
    </div>

    <van-button v-else round block type="primary" style="margin: 0 12px" @click="startCreate">
      新增收货地址
    </van-button>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import {
  createAddress, getAddressList, removeAddress, setDefaultAddress, updateAddress
} from '../../api/member'

const list = ref([])
const editing = ref(false)
const saving = ref(false)
const isDefault = ref(false)
const form = reactive({
  id: null, receiverName: '', receiverPhone: '', province: '',
  city: '', district: '', detailAddress: '', tag: ''
})

const load = async () => {
  list.value = (await getAddressList()) || []
}

const resetForm = () => {
  Object.assign(form, {
    id: null, receiverName: '', receiverPhone: '', province: '',
    city: '', district: '', detailAddress: '', tag: ''
  })
  isDefault.value = false
}

const startCreate = () => {
  resetForm()
  editing.value = true
}

const startEdit = (item) => {
  Object.assign(form, item)
  isDefault.value = item.isDefault === 1
  editing.value = true
}

const onSave = async () => {
  if (!form.receiverName || !form.receiverPhone || !form.detailAddress) {
    showToast('请填写收货人、手机号与详细地址')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, isDefault: isDefault.value ? 1 : 0 }
    if (form.id) {
      await updateAddress(form.id, payload)
    } else {
      await createAddress(payload)
    }
    showToast('保存成功')
    editing.value = false
    resetForm()
    load()
  } finally {
    saving.value = false
  }
}

const onSetDefault = async (item) => {
  await setDefaultAddress(item.id)
  showToast('已设为默认地址')
  load()
}

const onRemove = async (item) => {
  await showConfirmDialog({ title: '删除地址', message: '确认删除该收货地址？' })
  await removeAddress(item.id)
  showToast('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
