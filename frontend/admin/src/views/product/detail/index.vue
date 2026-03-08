<template>
  <ElDialog
    v-model="visible"
    width="960px"
    :show-close="false"
    :close-on-click-modal="false"
    class="product-detail-dialog"
    @close="handleClose"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <span class="header-title">商品详情</span>
          <ElTag v-if="productDetail" :class="getStatusClass(productDetail.status)" size="small">
            {{ getStatusText(productDetail.status) }}
          </ElTag>
        </div>
        <button class="close-btn" @click="handleClose">
          <ArtSvgIcon icon="ri:close-line" />
        </button>
      </div>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="skeleton-layout">
        <ElSkeleton animated>
          <template #template>
            <div class="skeleton-left">
              <ElSkeletonItem variant="image" style="width: 100%; height: 360px; border-radius: 12px;" />
              <div class="skeleton-thumbs">
                <ElSkeletonItem v-for="i in 4" :key="i" variant="image" style="width: 64px; height: 64px; border-radius: 8px;" />
              </div>
            </div>
            <div class="skeleton-right">
              <ElSkeletonItem variant="h1" style="width: 70%; height: 28px;" />
              <ElSkeletonItem variant="text" style="width: 30%; height: 36px; margin-top: 16px;" />
              <ElSkeletonItem variant="text" style="width: 100%; height: 120px; margin-top: 24px; border-radius: 12px;" />
              <ElSkeletonItem variant="text" style="width: 100%; height: 80px; margin-top: 16px; border-radius: 12px;" />
            </div>
          </template>
        </ElSkeleton>
      </div>
    </div>

    <!-- 商品详情内容 -->
    <div v-else-if="productDetail" class="detail-wrapper">
      <div class="detail-layout">
        <!-- 左侧：图片展示 -->
        <div class="gallery-section">
          <div class="main-image-wrapper">
            <ElImage
              :src="currentImage"
              fit="contain"
              :preview-src-list="productDetail.productImages"
              :initial-index="currentImageIndex"
              class="main-image"
            >
              <template #error>
                <div class="image-error">
                  <ArtSvgIcon icon="ri:image-2-line" class="error-icon" />
                  <span>暂无图片</span>
                </div>
              </template>
            </ElImage>
            <div class="image-counter" v-if="productDetail.productImages?.length > 1">
              {{ currentImageIndex + 1 }} / {{ productDetail.productImages.length }}
            </div>
          </div>
          <div v-if="productDetail.productImages?.length > 1" class="thumbnail-strip-wrapper">
            <button 
              v-if="productDetail.productImages.length > 4" 
              class="thumb-nav-btn prev"
              :disabled="thumbScrollIndex <= 0"
              @click="scrollThumbnails('prev')"
            >
              <ArtSvgIcon icon="ri:arrow-left-s-line" />
            </button>
            <div class="thumbnail-strip" ref="thumbnailStripRef">
              <div 
                class="thumbnail-track" 
                :style="{ transform: `translateX(-${thumbScrollIndex * 72}px)` }"
              >
                <div
                  v-for="(img, index) in productDetail.productImages"
                  :key="index"
                  class="thumb-item"
                  :class="{ active: currentImageIndex === index }"
                  @click="currentImageIndex = index"
                >
                  <img :src="img" :alt="`图片${index + 1}`" />
                </div>
              </div>
            </div>
            <button 
              v-if="productDetail.productImages.length > 4" 
              class="thumb-nav-btn next"
              :disabled="thumbScrollIndex >= maxThumbScrollIndex"
              @click="scrollThumbnails('next')"
            >
              <ArtSvgIcon icon="ri:arrow-right-s-line" />
            </button>
          </div>
        </div>

        <!-- 右侧：商品信息 -->
        <div class="info-section">
          <!-- 商品名称和价格 -->
          <div class="product-header">
            <h2 class="product-title">{{ productDetail.name }}</h2>
            <div class="price-row">
              <span class="price-label">售价</span>
              <span class="price-value">
                <span class="currency">¥</span>
                <span class="amount">{{ formatPrice(productDetail.price) }}</span>
              </span>
            </div>
          </div>

          <!-- 基础信息卡片 -->
          <div class="info-card">
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">商品编号</span>
                <span class="cell-value code">{{ productDetail.identifier }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">商品类型</span>
                <span class="cell-value">
                  <ElTag :type="productDetail.productType === 1 ? 'success' : 'primary'" size="small" effect="plain">
                    {{ productDetail.productType === 1 ? '宠物' : '宠物用品' }}
                  </ElTag>
                </span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">商品分类</span>
                <span class="cell-value">{{ productDetail.mainCategoryName }} / {{ productDetail.subCategoryName }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">库存数量</span>
                <span class="cell-value" :class="{ 'stock-warning': productDetail.stock <= 10, 'stock-danger': productDetail.stock <= 0 }">
                  {{ productDetail.stock }} 件
                </span>
              </div>
            </div>
            <div class="info-row single">
              <div class="info-cell full">
                <span class="cell-label">发货地址</span>
                <span class="cell-value">{{ productDetail.deliverAddress }}</span>
              </div>
            </div>
          </div>

          <!-- 宠物特有信息 -->
          <div v-if="isPet && petDetail" class="info-card highlight">
            <div class="card-title">
              <ArtSvgIcon icon="ri:heart-pulse-line" />
              <span>宠物信息</span>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">健康状态</span>
                <span class="cell-value">
                  <span class="status-dot" :class="getHealthClass(petDetail.healthStatus)"></span>
                  {{ getHealthStatusText(petDetail.healthStatus) }}
                </span>
              </div>
              <div class="info-cell">
                <span class="cell-label">疫苗接种</span>
                <span class="cell-value">
                  <span class="status-dot" :class="petDetail.vaccineFlag === 1 ? 'success' : 'default'"></span>
                  {{ petDetail.vaccineFlag === 1 ? '已接种' : '未接种' }}
                </span>
              </div>
            </div>
            <div v-if="petDetail.trainNote" class="note-block">
              <span class="note-label">驯养说明</span>
              <p class="note-content">{{ petDetail.trainNote }}</p>
            </div>
            <div v-if="petDetail.raiseNote" class="note-block">
              <span class="note-label">饲养说明</span>
              <p class="note-content">{{ petDetail.raiseNote }}</p>
            </div>
          </div>

          <!-- 宠物用品特有信息 -->
          <div v-if="!isPet && supplyDetail" class="info-card highlight">
            <div class="card-title">
              <ArtSvgIcon icon="ri:shopping-basket-2-line" />
              <span>用品信息</span>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">品牌</span>
                <span class="cell-value">{{ supplyDetail.brand || '-' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">生产公司</span>
                <span class="cell-value">{{ supplyDetail.company || '-' }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">生产日期</span>
                <span class="cell-value">{{ formatDate(supplyDetail.manufactureDate) }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">保质期至</span>
                <span class="cell-value">{{ formatDate(supplyDetail.guaranteeDate) }}</span>
              </div>
            </div>
            <div class="info-row">
              <div class="info-cell">
                <span class="cell-label">适用年龄</span>
                <span class="cell-value">{{ supplyDetail.fitAge || '-' }}</span>
              </div>
              <div class="info-cell">
                <span class="cell-label">适用品种</span>
                <span class="cell-value">{{ supplyDetail.figVariety || '-' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="description-section">
        <div class="section-title">
          <ArtSvgIcon icon="ri:file-text-line" />
          <span>商品描述</span>
        </div>
        <div class="description-content">
          {{ productDetail.description || '暂无商品描述' }}
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-container">
      <ElEmpty description="商品不存在或已被删除" />
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
import { 
  fetchPetProductDetail, 
  fetchSupplyProductDetail,
  type PetProductDetail,
  type SupplyProductDetail,
  type BaseProductDetail
} from '@/api/product'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'ProductDetailDialog' })

const props = defineProps<{
  modelValue: boolean
  productId: number | null
  productType: number | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const productDetail = ref<BaseProductDetail | null>(null)
const petDetail = ref<PetProductDetail | null>(null)
const supplyDetail = ref<SupplyProductDetail | null>(null)
const currentImageIndex = ref(0)
const thumbnailStripRef = ref<HTMLElement | null>(null)
const thumbScrollIndex = ref(0)

// 计算最大滚动索引
const maxThumbScrollIndex = computed(() => {
  if (!productDetail.value?.productImages?.length) return 0
  return Math.max(0, productDetail.value.productImages.length - 4)
})

// 滚动缩略图
const scrollThumbnails = (direction: 'prev' | 'next') => {
  if (direction === 'prev') {
    thumbScrollIndex.value = Math.max(0, thumbScrollIndex.value - 1)
  } else {
    thumbScrollIndex.value = Math.min(maxThumbScrollIndex.value, thumbScrollIndex.value + 1)
  }
}

const isPet = computed(() => productDetail.value?.productType === 1)

const currentImage = computed(() => {
  if (!productDetail.value?.productImages?.length) return ''
  return productDetail.value.productImages[currentImageIndex.value]
})

const getStatusClass = (status: number) => {
  const map: Record<number, string> = { 1: 'status-online', 2: 'status-outstock', 3: 'status-offline' }
  return map[status] || 'status-offline'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '在售', 2: '缺货', 3: '下架' }
  return map[status] || '未知'
}

const getHealthClass = (status: number) => {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'default'
}

const getHealthStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '健康', 2: '良好', 3: '治疗中' }
  return map[status] || '未知'
}

const formatPrice = (price: number) => {
  return price?.toFixed(2) || '0.00'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

const handleClose = () => {
  visible.value = false
  productDetail.value = null
  petDetail.value = null
  supplyDetail.value = null
  currentImageIndex.value = 0
  thumbScrollIndex.value = 0
}

const loadProductDetail = async () => {
  if (!props.productId || !props.productType) return

  try {
    loading.value = true
    productDetail.value = null
    petDetail.value = null
    supplyDetail.value = null

    if (props.productType === 1) {
      const data = await fetchPetProductDetail(props.productId)
      productDetail.value = data
      petDetail.value = data
    } else if (props.productType === 2) {
      const data = await fetchSupplyProductDetail(props.productId)
      productDetail.value = data
      supplyDetail.value = data
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val && props.productId && props.productType) {
    loadProductDetail()
  }
})
</script>


<style lang="scss" scoped>
.product-detail-dialog {
  :deep(.el-dialog) {
    border-radius: 16px;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    padding: 0;
    margin: 0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    max-height: 80vh;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }
    &::-webkit-scrollbar-thumb {
      background: var(--el-border-color);
      border-radius: 3px;
    }
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .close-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    border-radius: 8px;
    cursor: pointer;
    color: var(--el-text-color-secondary);
    transition: all 0.2s;

    &:hover {
      background: var(--el-fill-color);
      color: var(--el-text-color-primary);
    }
  }
}

.status-online {
  background-color: #d4edda !important;
  color: #155724 !important;
  border-color: #c3e6cb !important;
}

.status-outstock {
  background-color: #f8d7da !important;
  color: #721c24 !important;
  border-color: #f5c6cb !important;
}

.status-offline {
  background-color: #e2e3e5 !important;
  color: #383d41 !important;
  border-color: #d6d8db !important;
}

.loading-container {
  padding: 32px;
}

.skeleton-layout {
  display: flex;
  gap: 32px;

  .skeleton-left {
    width: 380px;
    flex-shrink: 0;
  }

  .skeleton-thumbs {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }

  .skeleton-right {
    flex: 1;
  }
}

.detail-wrapper {
  padding: 24px;
}

.detail-layout {
  display: flex;
  gap: 32px;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.gallery-section {
  width: 380px;
  flex-shrink: 0;

  @media (max-width: 768px) {
    width: 100%;
  }

  .main-image-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 3 / 4;
    border-radius: 12px;
    overflow: hidden;
    background: #f8fafc;
    border: 1px solid var(--el-border-color-lighter);

    .main-image {
      width: 100%;
      height: 100%;

      :deep(img) {
        transition: transform 0.3s;
      }

      &:hover :deep(img) {
        transform: scale(1.02);
      }
    }

    .image-error {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: var(--el-text-color-placeholder);

      .error-icon {
        font-size: 48px;
      }
    }

    .image-counter {
      position: absolute;
      bottom: 12px;
      right: 12px;
      padding: 4px 10px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-size: 12px;
      border-radius: 12px;
    }
  }

  .thumbnail-strip-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 12px;

    .thumb-nav-btn {
      flex-shrink: 0;
      width: 28px;
      height: 28px;
      border: none;
      background: var(--el-fill-color);
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-secondary);
      transition: all 0.2s;

      &:hover:not(:disabled) {
        background: var(--el-color-primary-light-9);
        color: var(--el-color-primary);
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }

      svg {
        width: 18px;
        height: 18px;
      }
    }
  }

  .thumbnail-strip {
    flex: 1;
    overflow: hidden;
    width: calc(4 * 64px + 3 * 8px); // 4个缩略图 + 3个间距

    .thumbnail-track {
      display: flex;
      gap: 8px;
      transition: transform 0.3s ease;
    }

    .thumb-item {
      flex-shrink: 0;
      width: 64px;
      height: 64px;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      border: 2px solid transparent;
      transition: all 0.2s;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &:hover {
        border-color: var(--el-color-primary-light-5);
      }

      &.active {
        border-color: var(--el-color-primary);
        box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
      }
    }
  }
}

.info-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-header {
  .product-title {
    margin: 0 0 12px;
    font-size: 22px;
    font-weight: 600;
    line-height: 1.4;
    color: var(--el-text-color-primary);
  }

  .price-row {
    display: flex;
    align-items: baseline;
    gap: 8px;

    .price-label {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .price-value {
      color: #dc2626;

      .currency {
        font-size: 16px;
        font-weight: 500;
      }

      .amount {
        font-size: 28px;
        font-weight: 700;
      }
    }
  }
}

.info-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;

  &.highlight {
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    border: 1px solid #bae6fd;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .info-row {
    display: flex;
    gap: 16px;

    &:not(:last-child) {
      margin-bottom: 12px;
    }

    &.single {
      .info-cell {
        flex: 1;
      }
    }
  }

  .info-cell {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4px;

    &.full {
      flex: none;
      width: 100%;
    }

    .cell-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .cell-value {
      font-size: 14px;
      color: var(--el-text-color-primary);
      display: flex;
      align-items: center;
      gap: 6px;

      &.code {
        font-family: 'SF Mono', Monaco, monospace;
        color: var(--el-color-primary);
      }

      &.stock-warning {
        color: #d97706;
      }

      &.stock-danger {
        color: #dc2626;
        font-weight: 500;
      }
    }
  }

  .note-block {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px dashed var(--el-border-color);

    .note-label {
      display: block;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 6px;
    }

    .note-content {
      margin: 0;
      font-size: 13px;
      line-height: 1.6;
      color: var(--el-text-color-regular);
      white-space: pre-wrap;
    }
  }
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;

  &.success {
    background: #10b981;
  }

  &.warning {
    background: #f59e0b;
  }

  &.danger {
    background: #ef4444;
  }

  &.default {
    background: #9ca3af;
  }
}

.description-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .description-content {
    font-size: 14px;
    line-height: 1.8;
    color: var(--el-text-color-regular);
    white-space: pre-wrap;
    background: #f8fafc;
    padding: 16px;
    border-radius: 12px;
  }
}

.empty-container {
  padding: 60px 20px;
}
</style>
