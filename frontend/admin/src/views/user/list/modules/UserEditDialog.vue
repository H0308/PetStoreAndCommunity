<template>
  <ElDialog
    v-model="dialogVisible"
    title="编辑用户"
    width="700px"
    :close-on-click-modal="false"
    destroy-on-close
    @close="handleClose"
  >
    <div class="section-title">基础信息</div>
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="80px"
      label-position="left"
      v-loading="loading"
      class="compact-form"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <ElFormItem label="邮箱" prop="email">
            <ElInput v-model="formData.email" placeholder="请输入邮箱" />
          </ElFormItem>
        </el-col>
        <el-col :span="12">
          <ElFormItem label="手机号" prop="phone">
            <ElInput v-model="formData.phone" placeholder="请输入手机号" />
          </ElFormItem>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <ElFormItem label="角色" prop="roleId">
            <ElSelect v-model="formData.roleId" placeholder="请选择角色" style="width: 100%">
              <ElOption label="管理员" :value="0" />
              <ElOption label="普通用户" :value="1" />
            </ElSelect>
          </ElFormItem>
        </el-col>
      </el-row>
    </ElForm>

    <!-- 收货信息独立区块 -->
    <div class="address-block">
      <div class="address-label">收货信息</div>
      <div class="receipt-name-row">
        <span class="field-label">收货人</span>
        <ElInput v-model="formData.receiptName" placeholder="请输入收货人姓名" />
      </div>
      <div class="field-label" style="margin-top: 16px;">收货地址</div>
      <div class="address-section">
        <div class="address-input-wrapper">
          <ElInput 
            v-model="formData.receiptAddress" 
            placeholder="请输入地址进行搜索，或在地图上点击选择" 
            clearable 
            @keyup.enter="handleSearchAddress"
          >
            <template #prefix><el-icon><Location /></el-icon></template>
          </ElInput>
          <ElButton type="primary" :loading="mapSearching" @click="handleSearchAddress">
            <el-icon><Search /></el-icon>搜索
          </ElButton>
          <ElButton :loading="gettingLocation" @click="getCurrentLocation">
            <el-icon><Aim /></el-icon>定位
          </ElButton>
        </div>
        <div v-if="showSuggestions && addressSuggestions.length > 0" class="address-suggestions">
          <div class="suggestions-header">
            <span>找到 {{ addressSuggestions.length }} 个相关地址</span>
            <ElButton text size="small" @click="closeSuggestions">
              <el-icon><Close /></el-icon>
            </ElButton>
          </div>
          <div 
            v-for="(item, index) in addressSuggestions" 
            :key="index" 
            class="suggestion-item" 
            @click="selectAddressSuggestion(item)"
          >
            <el-icon class="suggestion-icon"><Location /></el-icon>
            <div class="suggestion-content">
              <div class="suggestion-title">{{ item.title }}</div>
              <div class="suggestion-address">{{ item.address }}</div>
            </div>
          </div>
        </div>
        <div class="map-wrapper">
          <div ref="mapContainer" class="map-container"></div>
          <div v-if="mapLoading || gettingLocation" class="map-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ gettingLocation ? '正在获取位置...' : '地址解析中...' }}</span>
          </div>
        </div>
        <div class="address-preview" v-if="formData.receiptAddress">
          <el-icon><Location /></el-icon>
          <span>当前地址：{{ formData.receiptAddress }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit" :loading="loading">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Location, Search, Close, Aim, Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { updateUser, type UserDetailListVO, type UserUpdateDTO } from '@/api/user'

declare const BMap: any
declare const BMAP_STATUS_SUCCESS: number

defineOptions({ name: 'UserEditDialog' })

const props = defineProps<{
  modelValue: boolean
  user: UserDetailListVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 原始数据，用于比较变化
const originalData = ref<UserDetailListVO | null>(null)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const formData = ref({
  email: '',
  phone: '',
  roleId: 1,
  receiptName: '',
  receiptAddress: ''
})

// 地图相关
const mapContainer = ref<HTMLElement | null>(null)
let baiduMap: any = null
let mapMarker: any = null
let localSearch: any = null
let geoCoder: any = null
const mapSearching = ref(false)
const mapLoading = ref(false)
const gettingLocation = ref(false)
const addressSuggestions = ref<any[]>([])
const showSuggestions = ref(false)

// 表单验证规则
const rules: FormRules = {
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 初始化百度地图
const initBaiduMap = () => {
  if (!mapContainer.value || typeof BMap === 'undefined') return
  
  baiduMap = new BMap.Map(mapContainer.value)
  const defaultPoint = new BMap.Point(116.404, 39.915)
  baiduMap.centerAndZoom(defaultPoint, 15)
  baiduMap.enableScrollWheelZoom(true)
  baiduMap.addControl(new BMap.NavigationControl())
  baiduMap.addControl(new BMap.ScaleControl())
  
  mapMarker = new BMap.Marker(defaultPoint)
  baiduMap.addOverlay(mapMarker)
  mapMarker.enableDragging()
  
  geoCoder = new BMap.Geocoder()
  
  localSearch = new BMap.LocalSearch(baiduMap, {
    onSearchComplete: (results: any) => {
      setTimeout(() => { 
        mapSearching.value = false
        mapLoading.value = false 
      }, 500)
      
      if (localSearch.getStatus() === BMAP_STATUS_SUCCESS && results.getCurrentNumPois() > 0) {
        const suggestions: any[] = []
        for (let i = 0; i < Math.min(results.getCurrentNumPois(), 5); i++) {
          const poi = results.getPoi(i)
          if (poi) {
            suggestions.push({ 
              title: poi.title || '', 
              address: poi.address || '', 
              point: poi.point, 
              fullAddress: poi.address ? (poi.address + (poi.title ? ' ' + poi.title : '')) : poi.title 
            })
          }
        }
        if (suggestions.length > 0) { 
          addressSuggestions.value = suggestions
          showSuggestions.value = true
          if (suggestions.length === 1) selectAddressSuggestion(suggestions[0]) 
        }
      } else { 
        addressSuggestions.value = []
        showSuggestions.value = false 
      }
    }
  })
  
  // 点击地图选择位置
  baiduMap.addEventListener('click', (e: any) => { 
    mapMarker.setPosition(e.point)
    mapLoading.value = true
    geoCoder.getLocation(e.point, (r: any) => { 
      mapLoading.value = false
      if (r) formData.value.receiptAddress = r.address || '' 
    }) 
  })
  
  // 拖拽标记选择位置
  mapMarker.addEventListener('dragend', (e: any) => { 
    mapLoading.value = true
    geoCoder.getLocation(e.point, (r: any) => { 
      mapLoading.value = false
      if (r) formData.value.receiptAddress = r.address || '' 
    }) 
  })
  
  // 如果已有地址，定位到该地址
  if (formData.value.receiptAddress) {
    geoCoder.getPoint(formData.value.receiptAddress, (point: any) => { 
      if (point) { 
        baiduMap.centerAndZoom(point, 16)
        mapMarker.setPosition(point) 
      } 
    })
  }
}

// 搜索地址
const handleSearchAddress = () => { 
  if (!formData.value.receiptAddress || !localSearch) { 
    ElMessage.warning('请输入地址进行搜索')
    return 
  }
  mapSearching.value = true
  mapLoading.value = true
  localSearch.search(formData.value.receiptAddress) 
}

// 选择地址建议
const selectAddressSuggestion = (s: any) => { 
  if (!s || !baiduMap) return
  if (s.point) { 
    baiduMap.centerAndZoom(s.point, 16)
    mapMarker.setPosition(s.point) 
  }
  formData.value.receiptAddress = s.fullAddress || s.address || s.title
  showSuggestions.value = false
  addressSuggestions.value = [] 
}

// 关闭建议列表
const closeSuggestions = () => { 
  showSuggestions.value = false
  addressSuggestions.value = [] 
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) { 
    ElMessage.warning('您的浏览器不支持定位功能')
    return 
  }
  if (typeof BMap === 'undefined' || !geoCoder) { 
    ElMessage.warning('地图服务未就绪')
    return 
  }
  gettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const wgs84Point = new BMap.Point(pos.coords.longitude, pos.coords.latitude)
      new BMap.Convertor().translate([wgs84Point], 1, 5, (data: any) => {
        if (data.status === 0 && data.points?.length > 0) {
          const bdPoint = data.points[0]
          if (baiduMap && mapMarker) { 
            baiduMap.centerAndZoom(bdPoint, 16)
            mapMarker.setPosition(bdPoint) 
          }
          geoCoder.getLocation(bdPoint, (r: any) => { 
            gettingLocation.value = false
            if (r?.address) { 
              formData.value.receiptAddress = r.address
              ElMessage.success('已获取当前位置') 
            } else {
              ElMessage.warning('无法解析当前位置地址')
            }
          })
        } else { 
          gettingLocation.value = false
          ElMessage.warning('坐标转换失败') 
        }
      })
    },
    (err) => { 
      gettingLocation.value = false
      ElMessage.warning({ 
        1: '您拒绝了位置访问请求', 
        2: '无法获取位置信息', 
        3: '获取位置超时' 
      }[err.code] || '获取位置失败') 
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

// 监听用户数据变化，初始化表单
watch(
  () => props.user,
  (newUser) => {
    if (newUser) {
      originalData.value = { ...newUser }
      formData.value = {
        email: newUser.email || '',
        phone: newUser.phone || '',
        roleId: newUser.roleId ?? 1,
        receiptName: newUser.receiptName || '',
        receiptAddress: newUser.receiptAddress || ''
      }
    }
  },
  { immediate: true }
)

// 监听对话框打开，初始化地图
watch(dialogVisible, (val) => {
  if (val) {
    nextTick(() => setTimeout(initBaiduMap, 100))
  }
})

// 获取变化的字段（只传递发生修改的字段）
const getChangedFields = (): Partial<UserUpdateDTO> => {
  if (!originalData.value) return {}
  
  const changed: Partial<UserUpdateDTO> = {}
  
  if (formData.value.email !== originalData.value.email) {
    changed.email = formData.value.email || null
  }
  if (formData.value.phone !== originalData.value.phone) {
    changed.phone = formData.value.phone || null
  }
  if (formData.value.roleId !== originalData.value.roleId) {
    changed.roleId = formData.value.roleId
  }
  if (formData.value.receiptName !== (originalData.value.receiptName || '')) {
    changed.receiptName = formData.value.receiptName || null
  }
  if (formData.value.receiptAddress !== (originalData.value.receiptAddress || '')) {
    changed.receiptAddress = formData.value.receiptAddress || null
  }
  
  return changed
}

// 关闭对话框时清理地图
const handleClose = () => {
  if (baiduMap) {
    baiduMap = null
    mapMarker = null
    localSearch = null
    geoCoder = null
  }
  addressSuggestions.value = []
  showSuggestions.value = false
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value || !props.user) return
  
  try {
    await formRef.value.validate()
    
    const changedFields = getChangedFields()
    
    // 检查是否有修改
    if (Object.keys(changedFields).length === 0) {
      ElMessage.info('没有修改任何内容')
      return
    }
    
    loading.value = true
    
    const params: UserUpdateDTO = {
      userId: Number(userStore.info?.userId),
      userIdChange: props.user.userId,
      ...changedFields
    }
    
    await updateUser(params)
    
    ElMessage.success('修改成功')
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel' && error !== false) {
      ElMessage.error(error.message || '修改失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.section-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 16px;
  font-weight: 600;
}

.compact-form {
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

.address-block {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid #ebeef5;
}

.address-label {
  font-size: 14px;
  color: #303133;
  margin-bottom: 16px;
  font-weight: 600;
}

.receipt-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .el-input {
    flex: 1;
  }
}

.field-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  min-width: 56px;
}

.address-section {
  width: 100%;
}

.address-input-wrapper {
  display: flex;
  gap: 12px;
  
  .el-input {
    flex: 1;
  }
}

.address-suggestions {
  margin-top: 8px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
}

.suggestions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
  font-size: 12px;
  color: #909399;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #f5f7fa;
  }
}

.suggestion-icon {
  margin-right: 8px;
  margin-top: 2px;
  color: var(--el-color-primary);
}

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 2px;
}

.suggestion-address {
  font-size: 12px;
  color: #909399;
}

.map-wrapper {
  position: relative;
  margin-top: 12px;
}

.map-container {
  width: 100%;
  height: 350px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  
  .el-icon {
    font-size: 24px;
    margin-bottom: 8px;
    color: var(--el-color-primary);
  }
  
  span {
    font-size: 13px;
    color: #606266;
  }
}

.address-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  padding: 10px 12px;
  background: #f0f9eb;
  border-radius: 4px;
  color: #67c23a;
  font-size: 13px;
}
</style>
