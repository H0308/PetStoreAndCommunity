<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="720px"
    :close-on-click-modal="false"
    :show-close="!submitting && !uploading"
    destroy-on-close
    @close="handleClose"
  >
    <el-steps :active="currentStep - 1" align-center class="step-indicator">
      <el-step title="基础信息" />
      <el-step title="附加信息" />
      <el-step title="发货地址" />
      <el-step title="图片管理" />
    </el-steps>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载商品信息中...</span>
    </div>

    <template v-else>
      <!-- 步骤一：基础信息 -->
      <el-form v-if="currentStep === 1" ref="baseFormRef" :model="formData" :rules="baseFormRules" label-width="100px" label-position="left" class="edit-form">
        <div class="form-header">
          <el-tag type="info" size="small">编号: {{ formData.identifier }}</el-tag>
          <el-tag :type="formData.status === 1 ? 'success' : formData.status === 2 ? 'danger' : 'info'" size="small">{{ statusText }}</el-tag>
        </div>
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="formData.productName" placeholder="请输入商品名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="商品类型" prop="type">
          <el-radio-group v-model="formData.type" @change="handleTypeChange">
            <el-radio :value="1"><el-icon><Chicken /></el-icon> 宠物</el-radio>
            <el-radio :value="2"><el-icon><ShoppingBag /></el-icon> 宠物用品</el-radio>
          </el-radio-group>
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
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入商品描述" maxlength="2048" show-word-limit />
        </el-form-item>
      </el-form>

      <!-- 步骤二：附加信息 -->
      <el-form v-else-if="currentStep === 2" ref="extraFormRef" :model="formData" :rules="extraFormRules" label-width="100px" label-position="left" class="edit-form">
        <template v-if="formData.type === 1">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="健康状态" prop="healthStatus">
                <el-select v-model="formData.healthStatus" placeholder="请选择健康状态" style="width: 100%">
                  <el-option label="健康" :value="1" /><el-option label="良好" :value="2" /><el-option label="治疗中" :value="3" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="疫苗接种" prop="vaccineFlag">
                <el-select v-model="formData.vaccineFlag" placeholder="请选择疫苗状态" style="width: 100%">
                  <el-option label="已接种" :value="1" /><el-option label="未接种" :value="0" />
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
        <template v-if="formData.type === 2">
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="品牌" prop="brand"><el-input v-model="formData.brand" placeholder="请输入品牌" maxlength="50" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="生产公司" prop="company"><el-input v-model="formData.company" placeholder="请输入生产公司" maxlength="255" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="适用年龄" prop="fitAge"><el-input v-model="formData.fitAge" placeholder="如：幼年/成年/全年龄" maxlength="255" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="适用品种" prop="fitVariety"><el-input v-model="formData.fitVariety" placeholder="如：全品种/小型犬" maxlength="255" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12"><el-form-item label="生产日期" prop="manufactureDate"><el-date-picker v-model="formData.manufactureDate" type="date" placeholder="请选择生产日期" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="保质期至" prop="guaranteeDate"><el-date-picker v-model="formData.guaranteeDate" type="date" placeholder="请选择保质期" style="width: 100%" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item></el-col>
          </el-row>
        </template>
      </el-form>

      <!-- 步骤三：发货地址 -->
      <div v-else-if="currentStep === 3" class="address-step">
        <div class="address-form">
          <div class="address-input-wrapper">
            <el-input v-model="formData.shipAddress" placeholder="请输入地址进行搜索，或在地图上点击选择" clearable @keyup.enter="handleSearchAddress">
              <template #prefix><el-icon><Location /></el-icon></template>
            </el-input>
            <el-button type="primary" :loading="mapSearching" @click="handleSearchAddress"><el-icon><Search /></el-icon>搜索</el-button>
            <el-button :loading="gettingLocation" @click="getCurrentLocation"><el-icon><Aim /></el-icon>定位</el-button>
          </div>
          <div v-if="showSuggestions && addressSuggestions.length > 0" class="address-suggestions">
            <div class="suggestions-header"><span>找到 {{ addressSuggestions.length }} 个相关地址</span><el-button text size="small" @click="closeSuggestions"><el-icon><Close /></el-icon></el-button></div>
            <div v-for="(item, index) in addressSuggestions" :key="index" class="suggestion-item" @click="selectAddressSuggestion(item)">
              <el-icon class="suggestion-icon"><Location /></el-icon>
              <div class="suggestion-content"><div class="suggestion-title">{{ item.title }}</div><div class="suggestion-address">{{ item.address }}</div></div>
            </div>
          </div>
        </div>
        <div class="map-wrapper">
          <div ref="mapContainer" class="map-container"></div>
          <div v-if="mapLoading || gettingLocation" class="map-loading"><el-icon class="is-loading"><Loading /></el-icon><span>{{ gettingLocation ? '正在获取位置...' : '地址解析中...' }}</span></div>
        </div>
        <div class="address-preview" v-if="formData.shipAddress"><el-icon><Location /></el-icon><span>当前地址：{{ formData.shipAddress }}</span></div>
      </div>

      <!-- 步骤四：图片管理 -->
      <div v-else-if="currentStep === 4" class="image-step">
        <!-- 已有图片 -->
        <div class="image-section">
          <div class="section-title">已有图片</div>
          <div class="existing-images" v-if="existingImages.length > 0">
            <div v-for="img in existingImages" :key="img.id" class="image-item" :class="{ 'marked-delete': img.markedDelete }">
              <img :src="img.url" alt="商品图片" />
              <!-- 未标记删除：显示放大和删除按钮 -->
              <div v-if="!img.markedDelete" class="image-mask">
                <span class="action-btn" @click="handlePreviewImage(img.url)"><el-icon><ZoomIn /></el-icon></span>
                <span class="action-btn delete-btn" @click="toggleImageDelete(img)"><el-icon><Delete /></el-icon></span>
              </div>
              <!-- 已标记删除：默认显示提示文字，悬浮时显示撤回按钮 -->
              <template v-else>
                <div class="delete-text-overlay"><span>已标记删除</span></div>
                <div class="delete-hover-mask">
                  <span class="action-btn restore-btn" @click="toggleImageDelete(img)"><el-icon><RefreshRight /></el-icon></span>
                </div>
              </template>
            </div>
          </div>
          <el-empty v-else description="暂无已有图片" :image-size="60" />
        </div>

        <!-- 上传新主图 -->
        <div class="upload-section">
          <div class="section-title">新主图（16:9横向比例，可选）</div>
          <div class="upload-tip">主图将作为商品列表展示图，建议尺寸 1920x1080 或 1280x720</div>
          <el-upload class="main-image-upload" :show-file-list="false" :before-upload="handleMainImageBeforeUpload" :http-request="handleMainImageUpload" accept="image/*">
            <div v-if="newMainImageUrl" class="main-image-preview">
              <img :src="newMainImageUrl" alt="新主图" />
              <div class="preview-mask" @click.stop>
                <span class="action-icon" title="查看大图" @click="handlePreviewImage(newMainImageUrl)"><el-icon><ZoomIn /></el-icon></span>
                <span class="action-icon" title="更换图片" @click="triggerMainImageUpload"><el-icon><Plus /></el-icon></span>
                <span class="action-icon delete-icon" title="删除图片" @click="handleDeleteNewMainImage"><el-icon><Delete /></el-icon></span>
              </div>
            </div>
            <div v-else class="upload-placeholder main-placeholder"><el-icon><Plus /></el-icon><span>上传主图</span></div>
          </el-upload>
        </div>

        <!-- 上传新详情图 -->
        <div class="upload-section">
          <div class="section-title">新详情图（可选，最多9张）</div>
          <div class="upload-tip">详情图将在商品详情页展示，支持任意比例</div>
          <div class="detail-images-grid">
            <div v-for="item in newDetailImageList" :key="item.id" class="detail-image-item">
              <img :src="item.url" alt="详情图" />
              <div class="detail-image-mask">
                <span class="action-btn" @click="handlePreviewImage(item.url)"><el-icon><ZoomIn /></el-icon></span>
                <span class="action-btn delete-btn" @click="removeNewDetailImage(item.id)"><el-icon><Delete /></el-icon></span>
              </div>
            </div>
            <el-upload v-if="newDetailImageList.length < 9" class="detail-image-uploader" :show-file-list="false" :before-upload="handleDetailImageBeforeUpload" :http-request="handleDetailImageUpload" accept="image/*">
              <div class="upload-placeholder"><el-icon><Plus /></el-icon></div>
            </el-upload>
          </div>
        </div>

        <el-image-viewer v-if="showImageViewer" :url-list="[previewImageUrl]" @close="showImageViewer = false" />
      </div>
    </template>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" :disabled="submitting || uploading">取消</el-button>
        <el-button v-if="currentStep > 1" @click="handlePrevStep" :disabled="submitting || uploading">上一步</el-button>
        <el-button v-if="currentStep < 4" type="primary" @click="handleNextStep" :disabled="loading">下一步</el-button>
        <el-button v-else type="primary" :loading="submitting || uploading" @click="handleSubmit">{{ uploading ? '上传图片中...' : submitting ? '保存中...' : '完成修改' }}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { Chicken, ShoppingBag, Location, Search, Close, Aim, Loading, Plus, ZoomIn, Delete, RefreshRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import { fetchAllMainCategories, fetchAllSubCategories, type SuperCategoryVO, type SubCategoryVO } from '@/api/category'
import { fetchAdminPetDetail, fetchAdminSupplyDetail, changeProduct, validateMainImageRatio, type AdminPetDetailVO, type AdminSupplyDetailVO, type MainProductChangeDTO, type ProductImageItem } from '@/api/product'

declare const BMap: any
declare const BMAP_STATUS_SUCCESS: number

interface Props { modelValue: boolean; productId: number | null; productType: number | null }
const props = defineProps<Props>()
const emit = defineEmits<{ (e: 'update:modelValue', value: boolean): void; (e: 'success'): void }>()

const userStore = useUserStore()
const visible = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) })

const loading = ref(false)
const currentStep = ref(1)
const submitting = ref(false)
const uploading = ref(false)
const baseFormRef = ref<FormInstance>()
const extraFormRef = ref<FormInstance>()

const originalData = ref<AdminPetDetailVO | AdminSupplyDetailVO | null>(null)
const originalType = ref<number | null>(null)
const typeChangeWarned = ref(false)

const mainCategoryOptions = ref<SuperCategoryVO[]>([])
const subCategoryOptions = ref<SubCategoryVO[]>([])

const formData = reactive({
  identifier: '', productName: '', description: '', type: 1 as number,
  mainCategoryId: null as number | null, subCategoryId: null as number | null,
  price: null as number | null, stock: null as number | null, shipAddress: '', status: 0,
  healthStatus: null as number | null, vaccineFlag: null as number | null, trainNote: '', raiseNote: '',
  brand: '', company: '', fitAge: '', fitVariety: '', manufactureDate: '', guaranteeDate: ''
})

interface ExistingImage { id: number; url: string; markedDelete: boolean }
const existingImages = ref<ExistingImage[]>([])

// 新主图
const newMainImageUrl = ref('')
const newMainImageFile = ref<File | null>(null)

// 新详情图列表
interface NewDetailImage { id: number; file: File; url: string }
const newDetailImageList = ref<NewDetailImage[]>([])

const showImageViewer = ref(false)
const previewImageUrl = ref('')

const mapContainer = ref<HTMLElement | null>(null)
let baiduMap: any = null, mapMarker: any = null, localSearch: any = null, geoCoder: any = null
const mapSearching = ref(false)
const mapLoading = ref(false)
const gettingLocation = ref(false)
const addressSuggestions = ref<any[]>([])
const showSuggestions = ref(false)

const statusText = computed(() => ({ 1: '在售', 2: '缺货', 3: '下架' }[formData.status] || '未知'))
const dialogTitle = computed(() => ['编辑商品 - 基础信息', '编辑商品 - 附加信息', '编辑商品 - 发货地址', '编辑商品 - 图片管理'][currentStep.value - 1])

const baseFormRules: FormRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (!value || value.trim() === '') {
          callback(new Error('商品名称不能为空或纯空格'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  type: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  mainCategoryId: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
  subCategoryId: [{ required: true, message: '请选择二级分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}
const extraFormRules = computed<FormRules>(() => formData.type === 2 ? {
  manufactureDate: [{ required: true, message: '请选择生产日期', trigger: 'change' }],
  guaranteeDate: [{ required: true, message: '请选择保质期', trigger: 'change' }]
} : {})

const loadProductDetail = async () => {
  if (!props.productId || !props.productType) return
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息获取失败'); return }
  loading.value = true
  try {
    const detail = props.productType === 1 ? await fetchAdminPetDetail(props.productId, Number(userId)) : await fetchAdminSupplyDetail(props.productId, Number(userId))
    originalData.value = JSON.parse(JSON.stringify(detail))
    originalType.value = detail.type
    formData.identifier = detail.identifier; formData.productName = detail.productName; formData.description = detail.description || ''
    formData.type = detail.type; formData.shipAddress = detail.shipAddress; formData.mainCategoryId = detail.mainCategoryId
    formData.subCategoryId = detail.subCategoryId; formData.price = detail.price; formData.stock = detail.stock; formData.status = detail.status
    if (detail.type === 1) {
      const pet = detail as AdminPetDetailVO
      formData.healthStatus = pet.healthStatus; formData.vaccineFlag = pet.vaccineFlag; formData.trainNote = pet.trainNote || ''; formData.raiseNote = pet.raiseNote || ''
    } else {
      const supply = detail as AdminSupplyDetailVO
      formData.brand = supply.brand || ''; formData.company = supply.company || ''; formData.fitAge = supply.fitAge || ''
      formData.fitVariety = supply.fitVariety || ''; formData.manufactureDate = supply.manufactureDate || ''; formData.guaranteeDate = supply.guaranteeDate || ''
    }
    existingImages.value = []
    if (detail.productImageUrls) {
      for (const [id, url] of Object.entries(detail.productImageUrls)) {
        existingImages.value.push({ id: Number(id), url: url as string, markedDelete: false })
      }
    }
    await loadMainCategories()
    if (formData.mainCategoryId) await loadSubCategories(formData.mainCategoryId)
  } catch (error: any) { ElMessage.error(error.message || '获取商品详情失败'); handleClose() }
  finally { loading.value = false }
}

const loadMainCategories = async () => { try { mainCategoryOptions.value = await fetchAllMainCategories() } catch (e) { console.error(e) } }
const loadSubCategories = async (mainCategoryId: number) => { try { subCategoryOptions.value = await fetchAllSubCategories(mainCategoryId) } catch (e) { console.error(e) } }
const handleMainCategoryChange = async (mainCategoryId: number) => { formData.subCategoryId = null; subCategoryOptions.value = []; if (mainCategoryId) await loadSubCategories(mainCategoryId) }

const handleTypeChange = async (newType: string | number | boolean | undefined) => {
  if (typeof newType !== 'number') return
  if (originalType.value !== null && newType !== originalType.value && !typeChangeWarned.value) {
    try {
      await ElMessageBox.confirm('修改商品类型后，原有的商品附加信息（如宠物的健康状态、用品的品牌等）将会全部清空，确定要修改吗？', '类型修改警告', { confirmButtonText: '确定修改', cancelButtonText: '取消', type: 'warning' })
      typeChangeWarned.value = true; clearExtraFields(); formData.mainCategoryId = null; formData.subCategoryId = null; subCategoryOptions.value = []
    } catch { formData.type = originalType.value }
  } else if (newType !== originalType.value) { clearExtraFields(); formData.mainCategoryId = null; formData.subCategoryId = null; subCategoryOptions.value = [] }
}

const clearExtraFields = () => {
  formData.healthStatus = null; formData.vaccineFlag = null; formData.trainNote = ''; formData.raiseNote = ''
  formData.brand = ''; formData.company = ''; formData.fitAge = ''; formData.fitVariety = ''; formData.manufactureDate = ''; formData.guaranteeDate = ''
}

const initBaiduMap = () => {
  if (!mapContainer.value || typeof BMap === 'undefined') return
  baiduMap = new BMap.Map(mapContainer.value)
  const defaultPoint = new BMap.Point(116.404, 39.915)
  baiduMap.centerAndZoom(defaultPoint, 15); baiduMap.enableScrollWheelZoom(true)
  baiduMap.addControl(new BMap.NavigationControl()); baiduMap.addControl(new BMap.ScaleControl())
  mapMarker = new BMap.Marker(defaultPoint); baiduMap.addOverlay(mapMarker); mapMarker.enableDragging()
  geoCoder = new BMap.Geocoder()
  localSearch = new BMap.LocalSearch(baiduMap, {
    onSearchComplete: (results: any) => {
      setTimeout(() => { mapSearching.value = false; mapLoading.value = false }, 500)
      if (localSearch.getStatus() === BMAP_STATUS_SUCCESS && results.getCurrentNumPois() > 0) {
        const suggestions: any[] = []
        for (let i = 0; i < Math.min(results.getCurrentNumPois(), 5); i++) {
          const poi = results.getPoi(i)
          if (poi) suggestions.push({ title: poi.title || '', address: poi.address || '', point: poi.point, fullAddress: poi.address ? (poi.address + (poi.title ? ' ' + poi.title : '')) : poi.title })
        }
        if (suggestions.length > 0) { addressSuggestions.value = suggestions; showSuggestions.value = true; if (suggestions.length === 1) selectAddressSuggestion(suggestions[0]) }
      } else { addressSuggestions.value = []; showSuggestions.value = false }
    }
  })
  baiduMap.addEventListener('click', (e: any) => { mapMarker.setPosition(e.point); mapLoading.value = true; geoCoder.getLocation(e.point, (r: any) => { mapLoading.value = false; if (r) formData.shipAddress = r.address || '' }) })
  mapMarker.addEventListener('dragend', (e: any) => { mapLoading.value = true; geoCoder.getLocation(e.point, (r: any) => { mapLoading.value = false; if (r) formData.shipAddress = r.address || '' }) })
  if (formData.shipAddress) geoCoder.getPoint(formData.shipAddress, (point: any) => { if (point) { baiduMap.centerAndZoom(point, 16); mapMarker.setPosition(point) } })
}

const handleSearchAddress = () => { if (!formData.shipAddress || !localSearch) { ElMessage.warning('请输入地址进行搜索'); return }; mapSearching.value = true; mapLoading.value = true; localSearch.search(formData.shipAddress) }
const selectAddressSuggestion = (s: any) => { if (!s || !baiduMap) return; if (s.point) { baiduMap.centerAndZoom(s.point, 16); mapMarker.setPosition(s.point) }; formData.shipAddress = s.fullAddress || s.address || s.title; showSuggestions.value = false; addressSuggestions.value = [] }
const closeSuggestions = () => { showSuggestions.value = false; addressSuggestions.value = [] }

const getCurrentLocation = () => {
  if (!navigator.geolocation) { ElMessage.warning('您的浏览器不支持定位功能'); return }
  if (typeof BMap === 'undefined' || !geoCoder) { ElMessage.warning('地图服务未就绪'); return }
  gettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const wgs84Point = new BMap.Point(pos.coords.longitude, pos.coords.latitude)
      new BMap.Convertor().translate([wgs84Point], 1, 5, (data: any) => {
        if (data.status === 0 && data.points?.length > 0) {
          const bdPoint = data.points[0]
          if (baiduMap && mapMarker) { baiduMap.centerAndZoom(bdPoint, 16); mapMarker.setPosition(bdPoint) }
          geoCoder.getLocation(bdPoint, (r: any) => { gettingLocation.value = false; if (r?.address) { formData.shipAddress = r.address; ElMessage.success('已获取当前位置') } else ElMessage.warning('无法解析当前位置地址') })
        } else { gettingLocation.value = false; ElMessage.warning('坐标转换失败') }
      })
    },
    (err) => { gettingLocation.value = false; ElMessage.warning({ 1: '您拒绝了位置访问请求', 2: '无法获取位置信息', 3: '获取位置超时' }[err.code] || '获取位置失败') },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

const handlePreviewImage = (url: string) => { previewImageUrl.value = url; showImageViewer.value = true }
const toggleImageDelete = (img: ExistingImage) => { img.markedDelete = !img.markedDelete }

// 新主图上传
const handleMainImageBeforeUpload = async (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片大小不能超过 5MB'); return false }
  const key = `${file.name}_${file.size}_${file.lastModified}`
  if (newDetailImageList.value.some(item => `${item.file.name}_${item.file.size}_${item.file.lastModified}` === key)) {
    ElMessage.warning('该图片已在详情图中，请勿重复上传'); return false
  }
  const isValidRatio = await validateMainImageRatio(file)
  if (!isValidRatio) { ElMessage.warning('主图需要16:9左右的横向比例（如1920x1080、1280x720）'); return false }
  return true
}
const handleMainImageUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  if (newMainImageUrl.value) URL.revokeObjectURL(newMainImageUrl.value)
  newMainImageFile.value = file
  newMainImageUrl.value = URL.createObjectURL(file)
}
const triggerMainImageUpload = () => {
  const uploadEl = document.querySelector('.main-image-upload input[type="file"]') as HTMLInputElement
  if (uploadEl) uploadEl.click()
}
const handleDeleteNewMainImage = () => { 
  if (newMainImageUrl.value) URL.revokeObjectURL(newMainImageUrl.value)
  newMainImageUrl.value = ''; newMainImageFile.value = null 
}

// 新详情图上传
const handleDetailImageBeforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size / 1024 / 1024 > 5) { ElMessage.error('图片大小不能超过 5MB'); return false }
  const key = `${file.name}_${file.size}_${file.lastModified}`
  if (newDetailImageList.value.some(item => `${item.file.name}_${item.file.size}_${item.file.lastModified}` === key)) {
    ElMessage.warning('该图片已存在，请勿重复上传'); return false
  }
  if (newMainImageFile.value) {
    const mainKey = `${newMainImageFile.value.name}_${newMainImageFile.value.size}_${newMainImageFile.value.lastModified}`
    if (mainKey === key) { ElMessage.warning('该图片已作为主图，请勿重复上传'); return false }
  }
  return true
}
const handleDetailImageUpload = async (options: UploadRequestOptions) => {
  const file = options.file as File
  const url = URL.createObjectURL(file)
  newDetailImageList.value.push({ id: Date.now() + Math.random(), file, url })
}
const removeNewDetailImage = (id: number) => {
  const idx = newDetailImageList.value.findIndex(item => item.id === id)
  if (idx > -1) { URL.revokeObjectURL(newDetailImageList.value[idx].url); newDetailImageList.value.splice(idx, 1) }
}

const handlePrevStep = () => { currentStep.value-- }
const handleNextStep = async () => {
  if (currentStep.value === 1) { if (!baseFormRef.value) return; await baseFormRef.value.validate((valid) => { if (valid) currentStep.value = 2 }) }
  else if (currentStep.value === 2) { if (extraFormRef.value) { await extraFormRef.value.validate((valid) => { if (valid) { currentStep.value = 3; nextTick(() => setTimeout(initBaiduMap, 100)) } }) } else { currentStep.value = 3; nextTick(() => setTimeout(initBaiduMap, 100)) } }
  else if (currentStep.value === 3) { if (!formData.shipAddress?.trim()) { ElMessage.warning('请选择发货地址'); return }; currentStep.value = 4 }
}

const buildChangeData = (): MainProductChangeDTO => {
  const userId = Number(userStore.info?.userId)
  const orig = originalData.value!
  
  // 构建图片删除标记
  const productImageUrls: Record<number, number> = {}
  existingImages.value.forEach(img => { productImageUrls[img.id] = img.markedDelete ? 1 : 0 })

  const result: MainProductChangeDTO = { 
    userId,
    productId: props.productId!,
    baseProductChangeDTO: null
  }

  // 基础信息变更检测，只传递发生变化的字段
  const baseDTO: any = {}
  
  if (formData.type !== orig.type) baseDTO.type = formData.type
  if (formData.productName.trim() !== orig.productName) baseDTO.productName = formData.productName.trim()
  if ((formData.description?.trim() || '') !== (orig.description || '')) baseDTO.description = formData.description.trim()
  if (formData.shipAddress.trim() !== orig.shipAddress) baseDTO.shipAddress = formData.shipAddress.trim()
  if (formData.mainCategoryId !== orig.mainCategoryId) baseDTO.mainCategoryId = formData.mainCategoryId
  if (formData.subCategoryId !== orig.subCategoryId) baseDTO.subCategoryId = formData.subCategoryId
  if (formData.price !== orig.price) baseDTO.price = formData.price
  if (formData.stock !== orig.stock) baseDTO.stock = formData.stock
  if (Object.keys(productImageUrls).length > 0) baseDTO.productImageUrls = productImageUrls
  
  result.baseProductChangeDTO = Object.keys(baseDTO).length > 0 ? baseDTO : null

  // 附加信息变更检测 - 根据当前类型（formData.type）决定发送哪个DTO
  const typeChanged = formData.type !== orig.type
  
  if (formData.type === 1) {
    // 当前是宠物类型
    const petDTO: any = {}
    if (typeChanged) {
      // 类型发生了变化（从用品改为宠物），发送所有非空的宠物字段
      if (formData.healthStatus != null) petDTO.healthStatus = formData.healthStatus
      if (formData.trainNote?.trim()) petDTO.trainNote = formData.trainNote.trim()
      if (formData.raiseNote?.trim()) petDTO.raiseNote = formData.raiseNote.trim()
      if (formData.vaccineFlag != null) petDTO.vaccineFlag = formData.vaccineFlag
    } else {
      // 类型没变，只发送变化的字段
      const origPet = orig as AdminPetDetailVO
      if (formData.healthStatus !== origPet.healthStatus) petDTO.healthStatus = formData.healthStatus
      if ((formData.trainNote?.trim() || '') !== (origPet.trainNote || '')) petDTO.trainNote = formData.trainNote?.trim() || undefined
      if ((formData.raiseNote?.trim() || '') !== (origPet.raiseNote || '')) petDTO.raiseNote = formData.raiseNote?.trim() || undefined
      if (formData.vaccineFlag !== origPet.vaccineFlag) petDTO.vaccineFlag = formData.vaccineFlag ?? undefined
    }
    result.petProductChangeDTO = Object.keys(petDTO).length > 0 ? petDTO : null
    result.supplyProductChangeDTO = null
  } else {
    // 当前是用品类型
    const supplyDTO: any = {}
    if (typeChanged) {
      // 类型发生了变化（从宠物改为用品），发送所有非空的用品字段
      if (formData.brand?.trim()) supplyDTO.brand = formData.brand.trim()
      if (formData.fitAge?.trim()) supplyDTO.fitAge = formData.fitAge.trim()
      if (formData.fitVariety?.trim()) supplyDTO.fitVariety = formData.fitVariety.trim()
      if (formData.manufactureDate) supplyDTO.manufactureDate = formData.manufactureDate
      if (formData.guaranteeDate) supplyDTO.guaranteeDate = formData.guaranteeDate
      if (formData.company?.trim()) supplyDTO.company = formData.company.trim()
    } else {
      // 类型没变，只发送变化的字段
      const origSupply = orig as AdminSupplyDetailVO
      if ((formData.brand?.trim() || '') !== (origSupply.brand || '')) supplyDTO.brand = formData.brand?.trim() || undefined
      if ((formData.fitAge?.trim() || '') !== (origSupply.fitAge || '')) supplyDTO.fitAge = formData.fitAge?.trim() || undefined
      if ((formData.fitVariety?.trim() || '') !== (origSupply.fitVariety || '')) supplyDTO.fitVariety = formData.fitVariety?.trim() || undefined
      if (formData.manufactureDate !== origSupply.manufactureDate) supplyDTO.manufactureDate = formData.manufactureDate || undefined
      if (formData.guaranteeDate !== origSupply.guaranteeDate) supplyDTO.guaranteeDate = formData.guaranteeDate || undefined
      if ((formData.company?.trim() || '') !== (origSupply.company || '')) supplyDTO.company = formData.company?.trim() || undefined
    }
    result.supplyProductChangeDTO = Object.keys(supplyDTO).length > 0 ? supplyDTO : null
    result.petProductChangeDTO = null
  }

  return result
}

const handleSubmit = async () => {
  const userId = userStore.info?.userId
  if (!userId || !props.productId) { ElMessage.error('信息异常，无法提交'); return }
  
  // 验证：至少需要一张非主图
  const remainingExistingImages = existingImages.value.filter(img => !img.markedDelete).length
  const newDetailCount = newDetailImageList.value.length
  if (remainingExistingImages + newDetailCount === 0) {
    ElMessage.warning('至少需要保留或上传一张非主图')
    return
  }
  
  submitting.value = true
  try {
    // 组装新图片列表
    const newImages: ProductImageItem[] = []
    if (newMainImageFile.value) newImages.push({ file: newMainImageFile.value, isMain: 1 })
    newDetailImageList.value.forEach(item => newImages.push({ file: item.file, isMain: 0 }))

    const result = await changeProduct(buildChangeData(), newImages.length > 0 ? newImages : undefined)
    if (!result.successFlag) throw new Error('修改失败')
    ElMessage.success('商品修改成功'); emit('success')
  } catch (error: any) { ElMessage.error(error.message || '修改失败') }
  finally { submitting.value = false; uploading.value = false }
}

const resetState = () => {
  currentStep.value = 1; loading.value = false; submitting.value = false; uploading.value = false
  originalData.value = null; originalType.value = null; typeChangeWarned.value = false
  Object.assign(formData, { identifier: '', productName: '', description: '', type: 1, mainCategoryId: null, subCategoryId: null, price: null, stock: null, shipAddress: '', status: 0, healthStatus: null, vaccineFlag: null, trainNote: '', raiseNote: '', brand: '', company: '', fitAge: '', fitVariety: '', manufactureDate: '', guaranteeDate: '' })
  existingImages.value = []
  if (newMainImageUrl.value) URL.revokeObjectURL(newMainImageUrl.value)
  newMainImageUrl.value = ''; newMainImageFile.value = null
  newDetailImageList.value.forEach(item => URL.revokeObjectURL(item.url)); newDetailImageList.value = []
  subCategoryOptions.value = []; addressSuggestions.value = []; showSuggestions.value = false
  if (baiduMap) { baiduMap = null; mapMarker = null; localSearch = null; geoCoder = null }
}

const handleClose = () => { visible.value = false; resetState() }
watch(visible, (val) => { 
  if (val) { 
    resetState()
    // 使用 nextTick 确保 props 已更新
    nextTick(() => loadProductDetail())
  } 
})
</script>

<style scoped lang="scss">
.step-indicator { margin-bottom: 24px; padding: 0 20px; }
.loading-container { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 60px 0; color: #909399; .el-icon { font-size: 32px; margin-bottom: 12px; } }
.edit-form { padding: 0 10px; }
.form-header { display: flex; gap: 8px; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #ebeef5; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.address-step { padding: 0 10px; }
.address-form { margin-bottom: 16px; }
.address-input-wrapper { display: flex; gap: 12px; .el-input { flex: 1; } }
.address-suggestions { margin-top: 8px; background: #fff; border: 1px solid #e4e7ed; border-radius: 4px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); max-height: 240px; overflow-y: auto; }
.suggestions-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; border-bottom: 1px solid #ebeef5; font-size: 12px; color: #909399; }
.suggestion-item { display: flex; align-items: flex-start; padding: 10px 12px; cursor: pointer; transition: background-color 0.2s; &:hover { background-color: #f5f7fa; } }
.suggestion-icon { margin-right: 8px; margin-top: 2px; color: var(--el-color-primary); }
.suggestion-content { flex: 1; min-width: 0; }
.suggestion-title { font-size: 14px; color: #303133; margin-bottom: 2px; }
.suggestion-address { font-size: 12px; color: #909399; }
.map-wrapper { position: relative; margin-bottom: 12px; }
.map-container { width: 100%; height: 300px; border-radius: 8px; border: 1px solid #e4e7ed; }
.map-loading { position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; background: rgba(255, 255, 255, 0.8); border-radius: 8px; .el-icon { font-size: 24px; margin-bottom: 8px; color: var(--el-color-primary); } span { font-size: 13px; color: #606266; } }
.address-preview { display: flex; align-items: center; gap: 6px; padding: 10px 12px; background: #f0f9eb; border-radius: 4px; color: #67c23a; font-size: 13px; }
.image-step { padding: 0 10px; }
.image-section, .upload-section { margin-bottom: 20px; }
.section-title { font-weight: 600; margin-bottom: 8px; color: #303133; }
.upload-tip { font-size: 12px; color: #909399; margin-bottom: 12px; }
.existing-images { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.image-item { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; border: 1px solid #e4e7ed; img { width: 100%; height: 100%; object-fit: cover; } &.marked-delete { opacity: 0.6; } }
.image-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; gap: 8px; opacity: 0; transition: opacity 0.2s; }
.image-item:hover .image-mask { opacity: 1; }
.action-btn { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: rgba(255, 255, 255, 0.9); border-radius: 50%; cursor: pointer; transition: all 0.2s; &:hover { transform: scale(1.1); } &.delete-btn { color: #f56c6c; } &.restore-btn { color: #67c23a; } }

// 标记删除图片的样式
.delete-text-overlay { 
  position: absolute; top: 0; left: 0; right: 0; bottom: 0; 
  display: flex; align-items: center; justify-content: center; 
  background: rgba(0, 0, 0, 0.6); color: #fff; font-size: 12px; 
  transition: opacity 0.2s;
}
.delete-hover-mask { 
  position: absolute; top: 0; left: 0; right: 0; bottom: 0; 
  display: flex; align-items: center; justify-content: center; 
  background: rgba(0, 0, 0, 0.5); opacity: 0; transition: opacity 0.2s;
}
.image-item.marked-delete:hover .delete-text-overlay { opacity: 0; }
.image-item.marked-delete:hover .delete-hover-mask { opacity: 1; }

// 主图上传样式
.main-image-upload { width: 200px; height: 112px; }
.main-image-preview { width: 100%; height: 100%; position: relative; border-radius: 8px; overflow: hidden; border: 1px solid #e4e7ed;
  img { width: 100%; height: 100%; object-fit: cover; }
  .preview-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; gap: 12px; opacity: 0; transition: opacity 0.2s; }
  &:hover .preview-mask { opacity: 1; }
}
.action-icon { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: rgba(255, 255, 255, 0.9); border-radius: 50%; cursor: pointer; transition: all 0.2s; &:hover { transform: scale(1.1); } &.delete-icon { color: #f56c6c; } }
.main-placeholder { width: 200px; height: 112px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; color: #909399; font-size: 12px; cursor: pointer; transition: border-color 0.2s; &:hover { border-color: var(--el-color-primary); } .el-icon { font-size: 24px; } }

// 详情图上传样式
.detail-images-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px; }
.detail-image-item { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; border: 1px solid #e4e7ed;
  img { width: 100%; height: 100%; object-fit: cover; }
  .detail-image-mask { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; gap: 8px; opacity: 0; transition: opacity 0.2s; }
  &:hover .detail-image-mask { opacity: 1; }
}
.detail-image-uploader { 
  aspect-ratio: 1;
  :deep(.el-upload) { 
    width: 100%; height: 100%; border: 2px dashed #dcdfe6; border-radius: 8px; 
    display: flex; align-items: center; justify-content: center; cursor: pointer; 
    transition: border-color 0.2s; 
    &:hover { border-color: var(--el-color-primary); } 
  }
}
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 4px; color: #909399; font-size: 12px; .el-icon { font-size: 24px; } }
</style>
