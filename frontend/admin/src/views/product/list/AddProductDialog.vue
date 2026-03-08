<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="720px"
    :close-on-click-modal="false"
    :show-close="currentStep < 4"
    destroy-on-close
    @close="handleClose"
  >
    <!-- 步骤指示器 -->
    <el-steps :active="currentStep - 1" align-center class="step-indicator">
      <el-step title="选择类型" />
      <el-step title="填写信息" />
      <el-step title="发货地址" />
      <el-step title="上传图片" />
    </el-steps>

    <!-- 步骤一：选择商品类型 -->
    <div v-if="currentStep === 1" class="type-selection">
      <p class="selection-tip">请选择要新增的商品类型：</p>
      <div class="type-cards">
        <div class="type-card" :class="{ active: selectedType === 1 }" @click="selectedType = 1">
          <el-icon class="type-icon"><Chicken /></el-icon>
          <span class="type-name">宠物</span>
          <span class="type-desc">猫、狗、鸟类等活体宠物</span>
        </div>
        <div class="type-card" :class="{ active: selectedType === 2 }" @click="selectedType = 2">
          <el-icon class="type-icon"><ShoppingBag /></el-icon>
          <span class="type-name">宠物用品</span>
          <span class="type-desc">宠物食品、玩具、用品等</span>
        </div>
      </div>
    </div>

    <!-- 步骤二：填写商品信息 -->
    <el-form
      v-else-if="currentStep === 2"
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="left"
    >
      <el-divider content-position="left">基础信息</el-divider>
      <el-form-item label="商品名称" prop="productName">
        <el-input v-model="formData.productName" placeholder="请输入商品名称" maxlength="50" show-word-limit />
      </el-form-item>
      <el-form-item label="商品描述" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入商品描述" maxlength="2048" show-word-limit />
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="一级分类" prop="mainCategoryId">
            <el-select v-model="formData.mainCategoryId" placeholder="请选择一级分类" @change="handleMainCategoryChange" style="width: 100%">
              <el-option v-for="item in mainCategoryOptions" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="二级分类" prop="subCategoryId">
            <el-select v-model="formData.subCategoryId" placeholder="请先选择一级分类" :disabled="!formData.mainCategoryId" style="width: 100%">
              <el-option v-for="item in subCategoryOptions" :key="item.categoryId" :label="item.name" :value="item.categoryId" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="价格" prop="price">
            <el-input-number v-model="formData.price" :min="0" :precision="2" :controls="false" placeholder="请输入价格" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="库存" prop="stock">
            <el-input-number v-model="formData.stock" :min="0" :controls="false" placeholder="请输入库存" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 宠物特有字段 -->
      <template v-if="selectedType === 1">
        <el-divider content-position="left">宠物信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="健康状态" prop="healthStatus">
              <el-select v-model="formData.healthStatus" placeholder="请选择健康状态" style="width: 100%">
                <el-option label="健康" :value="1" />
                <el-option label="良好" :value="2" />
                <el-option label="治疗中" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="疫苗接种" prop="vaccineFlag">
              <el-select v-model="formData.vaccineFlag" placeholder="请选择疫苗状态" style="width: 100%">
                <el-option label="已接种" :value="1" />
                <el-option label="未接种" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="驯养须知" prop="trainNote">
          <el-input v-model="formData.trainNote" type="textarea" :rows="2" placeholder="请输入驯养须知" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="领养须知" prop="raiseNote">
          <el-input v-model="formData.raiseNote" type="textarea" :rows="2" placeholder="请输入领养须知" maxlength="255" show-word-limit />
        </el-form-item>
      </template>

      <!-- 宠物用品特有字段 -->
      <template v-if="selectedType === 2">
        <el-divider content-position="left">用品信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="formData.brand" placeholder="请输入品牌" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产公司" prop="company">
              <el-input v-model="formData.company" placeholder="请输入生产公司" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用年龄" prop="fitAge">
              <el-input v-model="formData.fitAge" placeholder="如：幼年/成年/全年龄" maxlength="255" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用品种" prop="fitVariety">
              <el-input v-model="formData.fitVariety" placeholder="如：全品种/小型犬" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产日期" prop="manufactureDate">
              <el-date-picker v-model="formData.manufactureDate" type="date" placeholder="请选择生产日期" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保质期至" prop="guaranteeDate">
              <el-date-picker v-model="formData.guaranteeDate" type="date" placeholder="请选择保质期" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </el-form>

    <!-- 步骤三：地图选址 -->
    <div v-else-if="currentStep === 3" class="address-step">
      <div class="address-form">
        <div class="address-input-wrapper">
          <el-input v-model="formData.shipAddress" placeholder="请输入地址进行搜索，或在地图上点击选择" clearable @keyup.enter="handleSearchAddress">
            <template #prefix><el-icon><Location /></el-icon></template>
          </el-input>
          <el-button type="primary" :loading="mapSearching" @click="handleSearchAddress">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button :loading="gettingLocation" @click="getCurrentLocation">
            <el-icon><Aim /></el-icon>定位
          </el-button>
        </div>
        <div v-if="showSuggestions && addressSuggestions.length > 0" class="address-suggestions">
          <div class="suggestions-header">
            <span>找到 {{ addressSuggestions.length }} 个相关地址</span>
            <el-button text size="small" @click="closeSuggestions"><el-icon><Close /></el-icon></el-button>
          </div>
          <div v-for="(item, index) in addressSuggestions" :key="index" class="suggestion-item" @click="selectAddressSuggestion(item)">
            <el-icon class="suggestion-icon"><Location /></el-icon>
            <div class="suggestion-content">
              <div class="suggestion-title">{{ item.title }}</div>
              <div class="suggestion-address">{{ item.address }}</div>
            </div>
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
      <div class="address-preview" v-if="formData.shipAddress">
        <el-icon><Location /></el-icon>
        <span>当前选择：{{ formData.shipAddress }}</span>
      </div>
    </div>

    <!-- 步骤四：上传图片 -->
    <div v-else-if="currentStep === 4" class="upload-step">
      <div class="upload-section">
        <div class="upload-title">
          主图（16:9横向比例，可选，最多1张）
        </div>
        <div class="upload-tip">主图将作为商品列表展示图，建议尺寸 1920x1080 或 1280x720</div>
        <el-upload
          class="main-image-upload"
          :show-file-list="false"
          :before-upload="handleMainImageBeforeUpload"
          :http-request="handleMainImageUpload"
          accept="image/*"
        >
          <div v-if="mainImageUrl" class="main-image-preview">
            <img :src="mainImageUrl" alt="主图" />
            <div class="preview-mask" @click.stop>
              <span class="action-icon" title="查看大图" @click="handlePreviewMainImage"><el-icon><ZoomIn /></el-icon></span>
              <span class="action-icon" title="更换图片" @click="triggerMainImageUpload"><el-icon><Plus /></el-icon></span>
              <span class="action-icon delete-icon" title="删除图片" @click="handleDeleteMainImage"><el-icon><Delete /></el-icon></span>
            </div>
          </div>
          <div v-else class="upload-placeholder main-placeholder">
            <el-icon><Plus /></el-icon>
            <span>上传主图</span>
          </div>
        </el-upload>
      </div>

      <div class="upload-section">
        <div class="upload-title">详情图（必填，至少1张，最多9张）</div>
        <div class="upload-tip">详情图将在商品详情页展示，支持任意比例</div>
        <div class="detail-images-grid">
          <!-- 已选择的图片 -->
          <div 
            v-for="item in detailImageList" 
            :key="item.id" 
            class="detail-image-item"
          >
            <img :src="item.url" alt="详情图" />
            <div class="detail-image-mask">
              <span class="action-btn" @click="handlePreviewDetailImageById(item.id)"><el-icon><ZoomIn /></el-icon></span>
              <span class="action-btn delete-btn" @click="removeDetailImage(item.id)"><el-icon><Delete /></el-icon></span>
            </div>
          </div>
          <!-- 上传按钮 -->
          <el-upload
            v-if="detailImageList.length < 9"
            class="detail-image-uploader"
            :show-file-list="false"
            :before-upload="handleDetailImageBeforeUpload"
            :http-request="handleDetailImageUpload"
            accept="image/*"
          >
            <div class="upload-placeholder">
              <el-icon><Plus /></el-icon>
            </div>
          </el-upload>
        </div>
      </div>

      <!-- 图片预览 -->
      <el-image-viewer
        v-if="showImageViewer"
        :url-list="previewImageList"
        :initial-index="previewImageIndex"
        @close="showImageViewer = false"
      />
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="currentStep < 4" @click="handleClose">取消</el-button>
        <el-button v-if="currentStep > 1 && currentStep < 4" @click="handlePrevStep">上一步</el-button>
        <el-button v-if="currentStep < 3" type="primary" :disabled="!canGoNext" @click="handleNextStep">下一步</el-button>
        <el-button v-else-if="currentStep === 3" type="primary" :disabled="!formData.shipAddress" @click="handleNextStep">
          下一步
        </el-button>
        <el-button v-else type="primary" :loading="uploading" :disabled="detailImageList.length === 0" @click="handleFinish">
          {{ uploading ? '上传中...' : '完成' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { Chicken, ShoppingBag, Location, Search, Close, Aim, Loading, Plus, ZoomIn, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { fetchAllMainCategories, fetchAllSubCategories, type SuperCategoryVO, type SubCategoryVO } from '@/api/category'
import { addPetProduct, addSupplyProduct, validateMainImageRatio, type PetProductAddDTO, type SupplyProductAddDTO, type ProductImageItem } from '@/api/product'

declare const BMap: any
declare const BMAP_STATUS_SUCCESS: number

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const userStore = useUserStore()
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const currentStep = ref(1)
const selectedType = ref<number | null>(null)
const formRef = ref<FormInstance>()

// 分类选项
const mainCategoryOptions = ref<SuperCategoryVO[]>([])
const subCategoryOptions = ref<SubCategoryVO[]>([])

// 百度地图相关
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

// 图片上传相关
const mainImageUrl = ref('')
const mainImageFile = ref<File | null>(null) // 存储主图文件
const mainImageUploading = ref(false)
// 详情图列表：{ id, file, url }
const detailImageList = ref<Array<{ id: number; file: File; url: string }>>([])
const detailImageUploading = ref(false)
const uploading = ref(false) // 统一上传状态

// 图片预览相关
const showImageViewer = ref(false)
const previewImageList = ref<string[]>([])
const previewImageIndex = ref(0)

// 表单数据
const formData = reactive({
  productName: '',
  description: '',
  mainCategoryId: null as number | null,
  subCategoryId: null as number | null,
  price: null as number | null,
  stock: null as number | null,
  shipAddress: '',
  healthStatus: null as number | null,
  vaccineFlag: null as number | null,
  trainNote: '',
  raiseNote: '',
  brand: '',
  company: '',
  fitAge: '',
  fitVariety: '',
  manufactureDate: '',
  guaranteeDate: ''
})

// 表单验证规则
const formRules = computed<FormRules>(() => {
  const notEmptyValidator = (rule: any, value: string, callback: any) => {
    if (!value || value.trim() === '') callback(new Error(rule.message))
    else callback()
  }
  const baseRules: FormRules = {
    productName: [
      { required: true, message: '请输入商品名称', trigger: 'blur' },
      { validator: notEmptyValidator, message: '商品名称不能为空', trigger: 'blur' }
    ],
    mainCategoryId: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
    subCategoryId: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
  }
  if (selectedType.value === 2) {
    return {
      ...baseRules,
      manufactureDate: [{ required: true, message: '请选择生产日期', trigger: 'change' }],
      guaranteeDate: [{ required: true, message: '请选择保质期', trigger: 'change' }]
    }
  }
  return baseRules
})

const dialogTitle = computed(() => {
  if (currentStep.value === 1) return '新增商品 - 选择类型'
  if (currentStep.value === 2) return selectedType.value === 1 ? '新增商品 - 宠物信息' : '新增商品 - 宠物用品信息'
  if (currentStep.value === 3) return '新增商品 - 设置发货地址'
  return '新增商品 - 上传图片'
})

const canGoNext = computed(() => {
  if (currentStep.value === 1) return !!selectedType.value
  return true
})

const trimOrUndefined = (str: string | undefined | null): string | undefined => {
  if (str === null || str === undefined) return undefined
  const trimmed = str.trim()
  return trimmed === '' ? undefined : trimmed
}

// 加载一级分类
const loadMainCategories = async () => {
  try {
    mainCategoryOptions.value = await fetchAllMainCategories()
  } catch (error) {
    console.error('获取一级分类失败:', error)
  }
}

// 一级分类变化时加载二级分类
const handleMainCategoryChange = async (mainCategoryId: number) => {
  formData.subCategoryId = null
  subCategoryOptions.value = []
  if (mainCategoryId) {
    try {
      subCategoryOptions.value = await fetchAllSubCategories(mainCategoryId)
    } catch (error) {
      console.error('获取二级分类失败:', error)
    }
  }
}

// 初始化百度地图
const initBaiduMap = () => {
  if (!mapContainer.value || typeof BMap === 'undefined') {
    console.warn('百度地图API未加载')
    return
  }
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
      setTimeout(() => { mapSearching.value = false; mapLoading.value = false }, 500)
      if (localSearch.getStatus() === BMAP_STATUS_SUCCESS && results.getCurrentNumPois() > 0) {
        const numPois = Math.min(results.getCurrentNumPois(), 5)
        const suggestions: any[] = []
        for (let i = 0; i < numPois; i++) {
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
  baiduMap.addEventListener('click', (e: any) => {
    mapMarker.setPosition(e.point)
    mapLoading.value = true
    geoCoder.getLocation(e.point, (result: any) => {
      mapLoading.value = false
      if (result) formData.shipAddress = result.address || ''
    })
  })
  mapMarker.addEventListener('dragend', (e: any) => {
    mapLoading.value = true
    geoCoder.getLocation(e.point, (result: any) => {
      mapLoading.value = false
      if (result) formData.shipAddress = result.address || ''
    })
  })
}

const handleSearchAddress = () => {
  if (!formData.shipAddress || !localSearch) {
    ElMessage.warning('请输入地址进行搜索')
    return
  }
  mapSearching.value = true
  mapLoading.value = true
  localSearch.search(formData.shipAddress)
}

const selectAddressSuggestion = (suggestion: any) => {
  if (!suggestion || !baiduMap) return
  if (suggestion.point) {
    baiduMap.centerAndZoom(suggestion.point, 16)
    mapMarker.setPosition(suggestion.point)
  }
  formData.shipAddress = suggestion.fullAddress || suggestion.address || suggestion.title
  showSuggestions.value = false
  addressSuggestions.value = []
}

const closeSuggestions = () => {
  showSuggestions.value = false
  addressSuggestions.value = []
}

const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持定位功能')
    return
  }
  if (typeof BMap === 'undefined' || !geoCoder) {
    ElMessage.warning('地图服务未就绪，请稍后再试')
    return
  }
  gettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords
      const wgs84Point = new BMap.Point(longitude, latitude)
      const convertor = new BMap.Convertor()
      convertor.translate([wgs84Point], 1, 5, (data: any) => {
        if (data.status === 0 && data.points?.length > 0) {
          const bdPoint = data.points[0]
          if (baiduMap && mapMarker) {
            baiduMap.centerAndZoom(bdPoint, 16)
            mapMarker.setPosition(bdPoint)
          }
          geoCoder.getLocation(bdPoint, (result: any) => {
            gettingLocation.value = false
            if (result?.address) {
              formData.shipAddress = result.address
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
    (error) => {
      gettingLocation.value = false
      const msgs: Record<number, string> = {
        1: '您拒绝了位置访问请求',
        2: '无法获取位置信息',
        3: '获取位置超时'
      }
      ElMessage.warning(msgs[error.code] || '获取位置失败')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

// 主图上传前校验
const handleMainImageBeforeUpload = async (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  const key = `${file.name}_${file.size}_${file.lastModified}`
  if (detailImageList.value.some(item => `${item.file.name}_${item.file.size}_${item.file.lastModified}` === key)) {
    ElMessage.warning('该图片已在详情图中，请勿重复上传')
    return false
  }
  const isValidRatio = await validateMainImageRatio(file)
  if (!isValidRatio) {
    ElMessage.warning('主图需要16:9左右的横向比例（如1920x1080、1280x720）')
    return false
  }
  return true
}

// 主图选择（仅本地预览，不上传）
const handleMainImageUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  mainImageFile.value = file
  mainImageUrl.value = URL.createObjectURL(file)
}

// 详情图上传前校验
const handleDetailImageBeforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  const key = `${file.name}_${file.size}_${file.lastModified}`
  if (detailImageList.value.some(item => `${item.file.name}_${item.file.size}_${item.file.lastModified}` === key)) {
    ElMessage.warning('该图片已存在，请勿重复上传')
    return false
  }
  if (mainImageFile.value) {
    const mainKey = `${mainImageFile.value.name}_${mainImageFile.value.size}_${mainImageFile.value.lastModified}`
    if (mainKey === key) {
      ElMessage.warning('该图片已作为主图，请勿重复上传')
      return false
    }
  }
  return true
}

// 详情图选择（仅本地预览，不上传）
const handleDetailImageUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const id = Date.now() + Math.random()
  const url = URL.createObjectURL(file)
  detailImageList.value.push({ id, file, url })
}

// 删除详情图
const removeDetailImage = (id: number) => {
  const index = detailImageList.value.findIndex(item => item.id === id)
  if (index > -1) {
    URL.revokeObjectURL(detailImageList.value[index].url)
    detailImageList.value.splice(index, 1)
  }
}

// 预览详情图
const handlePreviewDetailImageById = (id: number) => {
  const urls = detailImageList.value.map(item => item.url)
  const index = detailImageList.value.findIndex(item => item.id === id)
  previewImageList.value = urls
  previewImageIndex.value = index > -1 ? index : 0
  showImageViewer.value = true
}

// 预览主图
const handlePreviewMainImage = () => {
  if (mainImageUrl.value) {
    previewImageList.value = [mainImageUrl.value]
    previewImageIndex.value = 0
    showImageViewer.value = true
  }
}

// 触发主图上传（点击更换按钮时）
const triggerMainImageUpload = () => {
  const uploadEl = document.querySelector('.main-image-upload input[type="file"]') as HTMLInputElement
  if (uploadEl) {
    uploadEl.click()
  }
}

// 删除主图
const handleDeleteMainImage = () => {
  mainImageUrl.value = ''
  mainImageFile.value = null
}

// 预览详情图
const handlePreviewDetailImage = (file: any) => {
  const urls = detailImageList.value.map(item => item.url).filter(Boolean)
  const index = urls.indexOf(file.url)
  if (urls.length > 0) {
    previewImageList.value = urls
    previewImageIndex.value = index >= 0 ? index : 0
    showImageViewer.value = true
  }
}

const handlePrevStep = () => { currentStep.value-- }

const handleNextStep = async () => {
  if (currentStep.value === 1) {
    currentStep.value = 2
    loadMainCategories()
  } else if (currentStep.value === 2) {
    if (!formRef.value) return
    await formRef.value.validate((valid) => {
      if (valid) {
        currentStep.value = 3
        nextTick(() => setTimeout(() => initBaiduMap(), 100))
      }
    })
  } else if (currentStep.value === 3) {
    if (!formData.shipAddress || formData.shipAddress.trim() === '') {
      ElMessage.warning('请选择发货地址')
      return
    }
    currentStep.value = 4
  }
}

// 步骤4：一次性提交商品信息 + 图片
const handleFinish = async () => {
  if (detailImageList.value.length === 0) {
    ElMessage.warning('至少需要上传一张详情图')
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }

  // 组装图片列表
  const images: ProductImageItem[] = []
  if (mainImageFile.value) {
    images.push({ file: mainImageFile.value, isMain: 1 })
  }
  detailImageList.value.forEach(item => images.push({ file: item.file, isMain: 0 }))

  uploading.value = true
  try {
    if (selectedType.value === 1) {
      const petData: PetProductAddDTO = {
        userId: Number(userId),
        productName: formData.productName.trim(),
        description: trimOrUndefined(formData.description),
        shipAddress: formData.shipAddress.trim(),
        mainCategoryId: formData.mainCategoryId!,
        subCategoryId: formData.subCategoryId!,
        price: formData.price!,
        stock: formData.stock || undefined,
        healthStatus: formData.healthStatus || undefined,
        vaccineFlag: formData.vaccineFlag ?? undefined,
        trainNote: trimOrUndefined(formData.trainNote),
        raiseNote: trimOrUndefined(formData.raiseNote)
      }
      await addPetProduct(petData, images)
    } else {
      const supplyData: SupplyProductAddDTO = {
        userId: Number(userId),
        productName: formData.productName.trim(),
        description: trimOrUndefined(formData.description),
        shipAddress: formData.shipAddress.trim(),
        mainCategoryId: formData.mainCategoryId!,
        subCategoryId: formData.subCategoryId!,
        price: formData.price!,
        stock: formData.stock || undefined,
        brand: trimOrUndefined(formData.brand),
        fitAge: trimOrUndefined(formData.fitAge),
        fitVariety: trimOrUndefined(formData.fitVariety),
        manufactureDate: formData.manufactureDate,
        guaranteeDate: formData.guaranteeDate,
        company: trimOrUndefined(formData.company)
      }
      await addSupplyProduct(supplyData, images)
    }
    ElMessage.success('商品新增完成')
    emit('success')
    handleClose()
  } catch (error: any) {
    ElMessage.error(error.message || '新增失败')
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  currentStep.value = 1
  selectedType.value = null
  mainImageUrl.value = ''
  mainImageFile.value = null
  // 释放详情图 URL
  detailImageList.value.forEach(item => URL.revokeObjectURL(item.url))
  detailImageList.value = []
  Object.assign(formData, {
    productName: '', description: '', mainCategoryId: null, subCategoryId: null, price: null, stock: null, shipAddress: '',
    healthStatus: null, vaccineFlag: null, trainNote: '', raiseNote: '',
    brand: '', company: '', fitAge: '', fitVariety: '', manufactureDate: '', guaranteeDate: ''
  })
  subCategoryOptions.value = []
  addressSuggestions.value = []
  showSuggestions.value = false
  if (baiduMap) { baiduMap = null; mapMarker = null; localSearch = null; geoCoder = null }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

watch(visible, (val) => { if (val) resetForm() })
</script>

<style scoped lang="scss">
.step-indicator { margin-bottom: 24px; padding: 0 20px; }
.type-selection { padding: 20px 0; }
.selection-tip { text-align: center; color: #606266; margin-bottom: 24px; font-size: 15px; }
.type-cards { display: flex; gap: 24px; justify-content: center; }
.type-card {
  width: 200px; padding: 32px 24px; border: 2px solid #e4e7ed; border-radius: 12px;
  cursor: pointer; transition: all 0.3s; display: flex; flex-direction: column; align-items: center; gap: 12px;
  &:hover { border-color: var(--el-color-primary-light-3); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); }
  &.active { border-color: var(--el-color-primary); background-color: var(--el-color-primary-light-9); }
  .type-icon { font-size: 48px; color: var(--el-color-primary); }
  .type-name { font-size: 18px; font-weight: 600; color: #303133; }
  .type-desc { font-size: 13px; color: #909399; text-align: center; }
}
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
:deep(.el-divider__text) { font-weight: 600; color: #303133; }

.address-step { padding: 0 10px; }
.address-form { margin-bottom: 16px; }
.address-input-wrapper { display: flex; gap: 12px; .el-input { flex: 1; } }
.address-suggestions {
  margin-top: 8px; border: 1px solid #e4e7ed; border-radius: 8px; background: #fff; max-height: 240px; overflow-y: auto;
  .suggestions-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-bottom: 1px solid #f0f0f0; font-size: 13px; color: #909399; }
  .suggestion-item {
    display: flex; align-items: flex-start; gap: 8px; padding: 10px 12px; cursor: pointer; transition: background 0.2s;
    &:hover { background: #f5f7fa; }
    .suggestion-icon { color: var(--el-color-primary); margin-top: 2px; flex-shrink: 0; }
    .suggestion-content { flex: 1; min-width: 0;
      .suggestion-title { font-size: 14px; color: #303133; margin-bottom: 2px; }
      .suggestion-address { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }
  }
}
.map-wrapper {
  position: relative; border-radius: 8px; overflow: hidden; border: 1px solid #e4e7ed;
  .map-container { width: 100%; height: 320px; }
  .map-loading {
    position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255, 255, 255, 0.8);
    display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
    color: var(--el-color-primary); font-size: 14px;
    .el-icon { font-size: 24px; }
  }
}
.address-preview {
  margin-top: 12px; padding: 12px 16px; background: #f0f9eb; border-radius: 8px;
  display: flex; align-items: center; gap: 8px; color: #67c23a; font-size: 14px;
  .el-icon { font-size: 16px; }
}

// 图片上传步骤样式
.upload-step { padding: 0 10px; }
.upload-section { margin-bottom: 24px; }
.upload-title { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 8px; .required { color: #f56c6c; margin-right: 4px; } }
.upload-tip { font-size: 13px; color: #909399; margin-bottom: 12px; }
.main-image-upload {
  :deep(.el-upload) { width: 320px; height: 180px; border: 2px dashed #e4e7ed; border-radius: 8px; overflow: hidden; transition: border-color 0.3s;
    &:hover { border-color: var(--el-color-primary); }
  }
}
.main-image-preview {
  width: 100%; height: 100%; position: relative; cursor: default;
  img { width: 100%; height: 100%; object-fit: cover; }
  .preview-mask {
    position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5);
    display: flex; align-items: center; justify-content: center; gap: 32px;
    color: #fff; opacity: 0; transition: opacity 0.3s;
    .action-icon { 
      display: flex; align-items: center; justify-content: center;
      width: 56px; height: 56px; cursor: pointer; border-radius: 50%; 
      transition: all 0.2s; background: rgba(255, 255, 255, 0.15);
      .el-icon { font-size: 28px; }
      &:hover { background: rgba(255, 255, 255, 0.3); transform: scale(1.1); }
      &.delete-icon:hover { background: rgba(245, 108, 108, 0.8); }
    }
  }
  &:hover .preview-mask { opacity: 1; }
}
.upload-placeholder {
  width: 100%; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  color: #909399; font-size: 14px;
  .el-icon { font-size: 32px; }
}
.main-placeholder { width: 320px; height: 180px; }
.detail-images-grid {
  display: flex; flex-wrap: wrap; gap: 12px;
  .detail-image-item {
    width: 120px; height: 120px; border-radius: 8px; overflow: hidden; position: relative;
    img { width: 100%; height: 100%; object-fit: cover; }
    .detail-image-mask {
      position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5);
      display: flex; align-items: center; justify-content: center; gap: 16px;
      opacity: 0; transition: opacity 0.3s;
      .action-btn {
        display: flex; align-items: center; justify-content: center;
        width: 40px; height: 40px; cursor: pointer; border-radius: 50%; 
        background: rgba(255, 255, 255, 0.15); transition: all 0.2s;
        .el-icon { font-size: 22px; color: #fff; }
        &:hover { background: rgba(255, 255, 255, 0.3); transform: scale(1.1); }
        &.delete-btn:hover { background: rgba(245, 108, 108, 0.8); }
      }
    }
    &:hover .detail-image-mask { opacity: 1; }
  }
  .detail-image-uploader {
    :deep(.el-upload) { width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 8px; cursor: pointer; transition: border-color 0.3s;
      &:hover { border-color: var(--el-color-primary); }
    }
    .upload-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #909399;
      .el-icon { font-size: 28px; }
    }
  }
}
.upload-progress {
  display: flex; align-items: center; gap: 8px; margin-top: 8px; color: var(--el-color-primary); font-size: 14px;
}

// 暗黑模式适配
:root[data-theme='dark'] {
  .type-card { border-color: #3d3d3f;
    &:hover { border-color: var(--el-color-primary-light-3); }
    &.active { background-color: rgba(var(--el-color-primary-rgb), 0.1); }
    .type-name { color: #e5e6eb; }
  }
  .selection-tip { color: #a9aeb8; }
  .address-suggestions { background: #1d1d1f; border-color: #3d3d3f;
    .suggestions-header { border-color: #3d3d3f; }
    .suggestion-item { &:hover { background: #2a2a2b; } .suggestion-title { color: #e5e6eb; } }
  }
  .map-wrapper { border-color: #3d3d3f; .map-loading { background: rgba(29, 29, 31, 0.8); } }
  .address-preview { background: rgba(103, 194, 58, 0.1); }
  .upload-title { color: #e5e6eb; }
  .main-image-upload :deep(.el-upload) { border-color: #3d3d3f; }
}
</style>
