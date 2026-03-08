<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Close } from '@element-plus/icons-vue'

// 注入刷新购物车数量的方法
const refreshCartCount = inject('refreshCartCount', () => {})

const props = defineProps({
  // 是否登录
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  // 用户信息
  userInfo: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits(['openLogin'])

// 使用路由
const router = useRouter()
const route = useRoute()

// API基础URL
const API_BASE_URL = 'http://localhost:8080'

// API基础路径 - 统一使用首页接口
const apiBase = `${API_BASE_URL}/api/user/index`

// 数据状态
const loading = ref(false)
const categories = ref([])
const carousels = ref([])
const latestProducts = ref([])
const hoveredCategoryId = ref(null)
const subCategoriesMap = ref({}) // 改为Map存储，key为categoryId，value为二级分类数组

// 猜你喜欢
const recommendProducts = ref([])       // 推荐商品列表
const isNewUser = ref(false)            // true=新用户，不显示板块
const recommendLoading = ref(false)

// 从 localStorage 读取登录凭证，作为是否显示猜你喜欢的唯一依据
const getStoredUser = () => {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return null
    const user = JSON.parse(raw)
    return (user && user.userId && user.token) ? user : null
  } catch {
    return null
  }
}
const storedUser = ref(getStoredUser())



// 获取当前悬浮分类的二级分类
const currentSubCategories = computed(() => {
  if (!hoveredCategoryId.value) return []
  return subCategoriesMap.value[hoveredCategoryId.value] || []
})

// 分类菜单高度配置
const MENU_HEIGHT = 540 - 32 // 菜单总高度减去上下padding
const MIN_ITEM_HEIGHT = 29 // 每个分类项最小高度（阈值，低于此值会遮挡文字）
const MAX_ITEM_HEIGHT = 56 // 每个分类项最大高度

// 计算分类菜单样式（是否需要滚动条）
const categoryMenuStyle = computed(() => {
  const count = categories.value.length
  if (count === 0) return {}
  
  const calculatedHeight = MENU_HEIGHT / count
  // 如果计算出的高度小于最小高度，需要显示滚动条
  const needScroll = calculatedHeight < MIN_ITEM_HEIGHT
  
  return {
    overflowY: needScroll ? 'auto' : 'hidden'
  }
})

// 计算每个分类项的高度
const categoryItemStyle = computed(() => {
  const count = categories.value.length
  if (count === 0) return {}
  
  const calculatedHeight = MENU_HEIGHT / count
  
  // 如果计算高度小于最小高度，使用最小高度（此时会出现滚动条）
  // 如果计算高度大于最大高度，使用最大高度
  // 否则使用计算高度
  if (calculatedHeight < MIN_ITEM_HEIGHT) {
    return {
      flex: 'none',
      height: `${MIN_ITEM_HEIGHT}px`
    }
  } else if (calculatedHeight > MAX_ITEM_HEIGHT) {
    return {
      flex: '1',
      maxHeight: `${MAX_ITEM_HEIGHT}px`
    }
  } else {
    return {
      flex: '1'
    }
  }
})

// 获取轮播图数据
const fetchCarousels = async () => {
  try {
    const url = `${apiBase}/getCarousels`
    console.log('请求轮播图:', url)
    const response = await fetch(url)
    const result = await response.json()
    console.log('轮播图响应:', result)
    if (result.code === 0) {
      carousels.value = result.data
    } else {
      console.error('轮播图业务错误:', result.message)
    }
  } catch (error) {
    console.error('获取轮播图失败:', error)
  }
}

// 获取一级分类
const fetchCategories = async () => {
  try {
    const url = `${apiBase}/getSuperCategories`
    console.log('请求一级分类:', url)
    const response = await fetch(url)
    const result = await response.json()
    console.log('一级分类响应:', result)
    if (result.code === 0) {
      categories.value = result.data
    } else {
      console.error('分类业务错误:', result.message)
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取二级分类（鼠标悬浯时）
const fetchSubCategories = async (categoryId) => {
  try {
    // 如果已经缓存，直接返回
    if (subCategoriesMap.value[categoryId]) {
      console.log('💾 使用缓存的二级分类:', categoryId, subCategoriesMap.value[categoryId])
      return
    }
    
    const url = `${apiBase}/getSubCategories?mainCategoryId=${categoryId}`
    console.log('🔍 请求二级分类:', url)
    const response = await fetch(url)
    const result = await response.json()
    console.log('📦 二级分类完整响应:', result)
    console.log('📊 result.code:', result.code)
    console.log('📊 result.data:', result.data)
    console.log('📊 result.data类型:', Array.isArray(result.data) ? '数组' : typeof result.data)
    
    if (result.code === 0 && result.data) {
      // 存储到Map中
      subCategoriesMap.value[categoryId] = Array.isArray(result.data) ? result.data : []
      console.log('✅ 存储二级分类到Map:', categoryId, subCategoriesMap.value[categoryId])
      console.log('✅ Map当前内容:', subCategoriesMap.value)
    } else {
      console.error('❌ 二级分类业务错误:', result.message)
      subCategoriesMap.value[categoryId] = []
    }
  } catch (error) {
    console.error('❌ 获取二级分类失败:', error)
    subCategoriesMap.value[categoryId] = []
  }
}

// 获取最新商品
const fetchLatestProducts = async () => {
  try {
    const url = `${apiBase}/getLatestProducts`
    console.log('请求最新商品:', url)
    const response = await fetch(url)
    const result = await response.json()
    console.log('最新商品响应:', result)
    if (result.code === 0) {
      latestProducts.value = result.data
    } else {
      console.error('最新商品业务错误:', result.message)
    }
  } catch (error) {
    console.error('获取最新商品失败:', error)
  }
}

// 获取猜你喜欢推荐（协同过滤）
// data=null  → 新用户，不显示板块
// data=[]    → 老用户但暂无推荐，不显示板块
// data=[...] → 正常展示
const fetchRecommendations = async () => {
  const user = getStoredUser()
  if (!user) return   // 无登录凭证，直接跳过

  recommendLoading.value = true
  try {
    const url = `${API_BASE_URL}/api/user/index/getRecommendations?userId=${user.userId}`
    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${user.token}` }
    })
    const result = await response.json()
    if (result.code === 0) {
      if (result.data === null) {
        isNewUser.value = true
        recommendProducts.value = []
      } else {
        isNewUser.value = false
        recommendProducts.value = result.data
      }
    }
  } catch (error) {
    console.error('获取推荐商品失败:', error)
  } finally {
    recommendLoading.value = false
  }
}

// 处理分类悬浯
const handleCategoryHover = async (categoryId) => {
  console.log('👁️ 鼠标悬浯分类，ID:', categoryId)
  hoveredCategoryId.value = categoryId
  console.log('🎯 设置hoveredCategoryId:', hoveredCategoryId.value)
  await fetchSubCategories(categoryId)
  console.log('📦 fetchSubCategories执行完毕')
  console.log('📊 当前hoveredCategoryId:', hoveredCategoryId.value)
  console.log('📊 currentSubCategories:', currentSubCategories.value)
  console.log('✅ 面板应显示:', hoveredCategoryId.value === categoryId && currentSubCategories.value.length > 0)
}

// 处理分类离开
const handleCategoryLeave = () => {
  console.log('🚫 鼠标离开分类')
  hoveredCategoryId.value = null
}

// 商品点击 - 跳转到详情页
const handleProductClick = (product) => {
  console.log('点击商品:', product)
  if (!props.isLoggedIn) {
    ElMessage.warning('请先登录再操作')
    emit('openLogin')
    return
  }
  // 通过 query 参数传递商品类型：1=宠物，2=用品
  router.push({
    path: `/product/${product.productId}`,
    query: { type: product.productType }
  })
}

// 一级分类点击 - 跳转到分类列表页
const handleCategoryClick = (categoryId) => {
  if (!props.isLoggedIn) {
    ElMessage.warning('请先登录再操作')
    emit('openLogin')
    return
  }
  router.push({
    path: '/category',
    query: { categoryId }
  })
}

// 二级分类点击 - 跳转到分类列表页并选中该二级分类
const handleSubCategoryClick = (mainCategoryId, subCategoryId) => {
  if (!props.isLoggedIn) {
    ElMessage.warning('请先登录再操作')
    emit('openLogin')
    return
  }
  router.push({
    path: '/category',
    query: { 
      categoryId: mainCategoryId,
      subCategoryId: subCategoryId
    }
  })
}

// ===== 快速加入购物车模态框 =====
const showCartModal = ref(false)
const cartModalMouseDown = ref(false)
const selectedProduct = ref(null)
const cartQuantity = ref(1)
const addingToCart = ref(false)

// 打开加入购物车模态框
const openCartModal = (product, event) => {
  event.stopPropagation() // 阻止冒泡，不进入商品详情
  
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  
  selectedProduct.value = product
  cartQuantity.value = 1
  showCartModal.value = true
}

// 关闭模态框
const closeCartModal = () => {
  showCartModal.value = false
  selectedProduct.value = null
  cartQuantity.value = 1
}

// 确认加入购物车
const confirmAddToCart = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('用户信息异常，请重新登录')
    return
  }
  
  if (!selectedProduct.value) return
  
  addingToCart.value = true
  try {
    const response = await fetch(`${API_BASE_URL}/api/user/cart/add`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getStoredUser()?.token || ''}`
      },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        productId: selectedProduct.value.productId,
        totalCount: cartQuantity.value
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success(`已将 ${cartQuantity.value} 件商品加入购物车`)
      closeCartModal()
      // 刷新导航栏购物车数量
      refreshCartCount()
    } else {
      ElMessage.error(result.message || '加入购物车失败')
    }
  } catch (error) {
    console.error('加入购物车异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    addingToCart.value = false
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  loading.value = true
  await Promise.all([
    fetchCarousels(),
    fetchCategories(),
    fetchLatestProducts()
  ])
  loading.value = false
}

// 页面加载
onMounted(async () => {
  loading.value = true
  console.log('API基础路径:', apiBase)
  
  await Promise.all([
    fetchCarousels(),
    fetchCategories(),
    fetchLatestProducts()
  ])
  
  // 推荐接口单独请求，不阻塞主流程
  fetchRecommendations()
  
  loading.value = false
  console.log('数据加载完成')
  console.log('轮播图数量:', carousels.value.length)
  console.log('分类数量:', categories.value.length)
  console.log('商品数量:', latestProducts.value.length)
})

// 登录状态变化时重新拉取推荐
watch(() => props.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    storedUser.value = getStoredUser()
    fetchRecommendations()
  } else {
    // 退出登录：清空推荐数据，板块消失
    storedUser.value = null
    recommendProducts.value = []
    isNewUser.value = false
  }
})
</script>

<template>
  <div class="product-layout">
    <!-- Banner区域 - 全宽轮播图 -->
    <section class="banner-section">
      <!-- 轮播图 - 全宽显示 -->
      <div class="banner-carousel">
        <el-carousel 
          v-if="carousels.length > 0"
          :interval="4000" 
          arrow="never"
          height="620px"
        >
          <el-carousel-item 
            v-for="item in carousels" 
            :key="item.productId"
          >
            <div 
              class="carousel-item"
              @click="handleProductClick(item)"
            >
              <img :src="item.imageUrl" :alt="`商品${item.productId}`" />
            </div>
          </el-carousel-item>
        </el-carousel>
        <div v-else class="carousel-placeholder">
          <p>暂无轮播图</p>
        </div>
      </div>

      <!-- 分类菜单 - 悬浮在轮播图上 -->
      <div class="category-overlay">
        <div class="container-base">
          <div 
            class="category-menu"
            :class="{ 'sub-expanded': hoveredCategoryId && currentSubCategories.length > 0 }"
            @mouseleave="handleCategoryLeave"
          >
            <!-- 一级分类列表 -->
            <div class="category-menu-inner" :style="categoryMenuStyle">
              <div
                v-for="category in categories"
                :key="category.categoryId"
                class="category-item"
                :style="categoryItemStyle"
                :class="{ active: hoveredCategoryId === category.categoryId }"
                @mouseenter="handleCategoryHover(category.categoryId)"
                @click="handleCategoryClick(category.categoryId)"
              >
                <span class="category-name">{{ category.name }}</span>
                <span class="category-arrow">›</span>
              </div>
            </div>

            <!-- 二级分类面板 - 固定位置 -->
            <transition name="slide-fade">
              <div 
                v-show="hoveredCategoryId && currentSubCategories.length > 0"
                class="sub-category-panel"
              >
                <div class="panel-header">
                  <span class="panel-title">{{ categories.find(c => c.categoryId === hoveredCategoryId)?.name }}</span>
                </div>
                <div class="sub-category-grid">
                  <div
                    v-for="subCat in currentSubCategories"
                    :key="subCat.categoryId"
                    class="sub-category-item"
                    @click.stop="handleSubCategoryClick(hoveredCategoryId, subCat.categoryId)"
                  >
                    {{ subCat.name }}
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </section>

    <!-- 最近上新板块 -->
    <section class="latest-section">
      <div class="container-base">
        <div class="section-header">
          <h2 class="section-title">最近上新</h2>
          <p class="section-subtitle">新鲜出炉的优质商品</p>
        </div>
        
        <div v-if="latestProducts.length > 0" class="product-grid">
          <div
            v-for="product in latestProducts"
            :key="product.productId"
            class="product-card"
            @click="handleProductClick(product)"
          >
            <div class="product-badge">新上架</div>
            <div class="product-image">
              <img :src="product.imageUrl" :alt="product.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="product-price">¥{{ product.price }}</span>
                <el-button type="primary" size="small" class="add-cart-btn" @click="openCartModal(product, $event)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无新品</p>
        </div>
      </div>
    </section>

    <!-- 精品推荐板块（仅登录且非新用户且有推荐结果时显示） -->
    <section v-if="storedUser && !isNewUser && recommendProducts.length > 0" class="recommend-section">
      <div class="container-base">
        <div class="section-header">
          <h2 class="section-title">猜你喜欢</h2>
          <p class="section-subtitle">根据您的购买偏好为您精选</p>
        </div>
        
        <div v-if="recommendLoading" class="empty-state">
          <p>推荐加载中...</p>
        </div>
        <div v-else class="product-grid">
          <div
            v-for="product in recommendProducts"
            :key="'rec-' + product.productId"
            class="product-card recommend-card"
            @click="handleProductClick(product)"
          >
            <div class="product-badge hot">推荐</div>
            <div class="product-image">
              <img :src="product.imageUrl" :alt="product.name" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="product-price">¥{{ product.price }}</span>
                <el-button type="primary" size="small" class="add-cart-btn" @click="openCartModal(product, $event)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 快速加入购物车模态框 -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="showCartModal" class="cart-modal-overlay" @mousedown.self="cartModalMouseDown = true" @mouseup.self="cartModalMouseDown && closeCartModal(); cartModalMouseDown = false">
          <div class="cart-modal" @mousedown="cartModalMouseDown = false">
            <!-- 模态框头部 -->
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </h3>
              <button class="modal-close" @click="closeCartModal">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <!-- 商品信息 -->
            <div class="modal-body" v-if="selectedProduct">
              <div class="product-preview">
                <img :src="selectedProduct.imageUrl" :alt="selectedProduct.name" class="preview-image" />
                <div class="preview-info">
                  <h4 class="preview-name">{{ selectedProduct.name }}</h4>
                  <p class="preview-desc">{{ selectedProduct.description }}</p>
                  <div class="preview-price">¥{{ selectedProduct.price }}</div>
                </div>
              </div>
              
              <!-- 数量选择 -->
              <div class="quantity-row">
                <span class="quantity-label">购买数量</span>
                <el-input-number
                  v-model="cartQuantity"
                  :min="1"
                  :max="99"
                  size="large"
                />
              </div>
              
              <!-- 小计 -->
              <div class="subtotal-row">
                <span class="subtotal-label">小计</span>
                <span class="subtotal-price">¥{{ (selectedProduct.price * cartQuantity).toFixed(2) }}</span>
              </div>
            </div>
            
            <!-- 模态框底部 -->
            <div class="modal-footer">
              <el-button size="large" @click="closeCartModal">取消</el-button>
              <el-button 
                type="primary" 
                size="large" 
                :loading="addingToCart"
                @click="confirmAddToCart"
              >
                确认加入
              </el-button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.product-layout {
  background: var(--color-bg-base);
  min-height: 100vh;
}

// Banner区域 - 全宽布局
.banner-section {
  position: relative;
  background: var(--color-bg-base);
}

// 轮播图 - 全宽显示
.banner-carousel {
  width: 100%;
  height: 620px;

  :deep(.el-carousel) {
    height: 100%;
  }

  :deep(.el-carousel__container) {
    height: 100%;
  }
  
  // 优化指示器样式
  :deep(.el-carousel__indicators) {
    bottom: 30px;
    
    .el-carousel__indicator {
      .el-carousel__button {
        width: 30px;
        height: 4px;
        border-radius: 2px;
        background: rgba(255, 255, 255, 0.5);
        transition: all var(--transition-base);
      }
      
      &.is-active .el-carousel__button {
        width: 40px;
        background: white;
      }
    }
  }
}

.carousel-item {
  width: 100%;
  height: 100%;
  cursor: pointer;
  overflow: hidden;
  position: relative;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s var(--ease-out);
  }
  
  &:hover img {
    transform: scale(1.02);
  }
}

.carousel-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-lg);
}

// 分类菜单覆盖层 - 悬浮在轮播图上
.category-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none; // 允许点击穿透到轮播图
  z-index: 10;
}

// 分类菜单 - 参考OPPO设计
.category-menu {
  position: relative;
  display: flex;
  width: 240px;
  height: 540px; // 比轮播图矮，留出上下边距
  margin-top: 40px; // 上边距
  pointer-events: auto; // 恢复鼠标事件
  
  // 当二级分类展开时，调整一级分类的圆角
  &.sub-expanded {
    .category-menu-inner {
      border-radius: 12px 0 0 12px;
      border-right-color: transparent;
    }
  }
}

.category-menu-inner {
  width: 240px;
  height: 100%;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  padding: 16px 0;
  border-radius: 12px;
  border: 2px solid var(--color-primary);
  box-shadow: 0 8px 32px rgba(255, 107, 74, 0.15);
  transition: all 0.3s ease;
  
  // 滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
    margin: 12px 0;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 107, 74, 0);
    border-radius: 3px;
  }
  
  &:hover::-webkit-scrollbar-thumb {
    background: rgba(255, 107, 74, 0.3);
  }
  
  &:hover::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 107, 74, 0.5);
  }
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1; // 默认均匀分布，会被动态样式覆盖
  
  &:hover,
  &.active {
    background: linear-gradient(90deg, rgba(255, 107, 74, 0.1) 0%, transparent 100%);
    
    .category-name {
      color: var(--color-primary);
    }
    
    .category-arrow {
      transform: translateX(4px);
      color: var(--color-primary);
    }
  }

  .category-name {
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
    transition: color 0.2s ease;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .category-arrow {
    font-size: 16px;
    color: var(--color-text-tertiary);
    transition: all 0.2s ease;
    flex-shrink: 0;
    margin-left: 8px;
  }
}

// 二级分类面板 - 固定位置，紧贴一级分类右侧
.sub-category-panel {
  position: absolute;
  left: 238px; // 紧贴一级分类右侧，稍微重叠以连接边框
  top: 0;
  width: 700px;
  height: 540px; // 与一级分类同高
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 0 12px 12px 0;
  border: 2px solid var(--color-primary);
  border-left: none; // 左侧不要边框，与一级分类连接
  box-shadow: 8px 8px 32px rgba(255, 107, 74, 0.15);
  padding: 24px 30px;
  overflow-y: auto;
  
  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
    
    &:hover {
      background: rgba(0, 0, 0, 0.2);
    }
  }
}

.panel-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-primary);
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.sub-category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.sub-category-item {
  padding: 10px 12px;
  border-radius: 6px;
  text-align: center;
  font-size: 13px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  background: #f5f7fa;
  
  &:hover {
    background: var(--color-primary);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(255, 107, 74, 0.3);
  }
}

// 板块通用样式
.latest-section,
.recommend-section {
  padding: var(--spacing-4xl) 0;
}

// 最后一个板块增加底部间距
.latest-section:last-child,
.recommend-section:last-child {
  padding-bottom: var(--spacing-5xl);
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-3xl);

  .section-title {
    font-size: var(--font-size-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-sm);
  }

  .section-subtitle {
    font-size: var(--font-size-base);
    color: var(--color-text-tertiary);
  }
}

// 商品网格
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-xl);
}

// 商品卡片
.product-card {
  position: relative;
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  box-shadow: var(--shadow-base);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);

    .product-image img {
      transform: scale(1.05);
    }
  }

  .product-badge {
    position: absolute;
    top: var(--spacing-base);
    left: var(--spacing-base);
    background: var(--color-primary);
    color: white;
    padding: var(--spacing-xs) var(--spacing-base);
    border-radius: var(--radius-base);
    font-size: var(--font-size-xs);
    font-weight: var(--font-weight-semibold);
    z-index: 1;
    
    &.hot {
      background: var(--color-accent);
    }
  }

  .product-image {
    width: 100%;
    height: 280px;
    overflow: hidden;
    background: var(--color-bg-elevated);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-base) var(--ease-out);
    }
  }

  .product-info {
    padding: var(--spacing-lg);
  }

  .product-name {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-xs);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .product-desc {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
    margin-bottom: var(--spacing-md);
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    min-height: 40px;
  }

  .product-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .product-price {
    font-size: var(--font-size-2xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-accent);
    font-family: var(--font-family-number);
  }

  .add-cart-btn {
    background: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover {
      background: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: var(--spacing-4xl);
  color: var(--color-text-tertiary);
}

// 过渡动画
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s var(--ease-out);
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

// 响应式
@media (max-width: 768px) {
  .banner-container {
    flex-direction: column;
    height: auto;
  }

  .category-menu {
    width: 100%;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: var(--spacing-base);
  }
  
  .cart-modal {
    width: 95%;
    margin: 20px;
  }
}

// 模态框动画
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  
  .cart-modal {
    transform: scale(0.9) translateY(-20px);
  }
}
</style>

<!-- 模态框样式 - 非 scoped -->
<style lang="scss">
.cart-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-modal {
  width: 480px;
  max-width: 90%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

// 模态框进入动画
.modal-fade-enter-active .cart-modal {
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

// 模态框离开动画
.modal-fade-leave-active .cart-modal {
  animation: modalSlideOut 0.25s ease-out forwards;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes modalSlideOut {
  from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
  to {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
  
  .modal-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
    
    .el-icon {
      color: var(--color-primary);
    }
  }
  
  .modal-close {
    width: 32px;
    height: 32px;
    border: none;
    background: #f5f5f5;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    
    &:hover {
      background: #eee;
      transform: rotate(90deg);
    }
    
    .el-icon {
      font-size: 16px;
      color: #666;
    }
  }
}

.modal-body {
  padding: 24px;
}

.product-preview {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
  
  .preview-image {
    width: 120px;
    height: 120px;
    object-fit: cover;
    border-radius: 12px;
    background: #f5f5f5;
  }
  
  .preview-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .preview-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .preview-desc {
    font-size: 13px;
    color: #999;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .preview-price {
    font-size: 20px;
    font-weight: 700;
    color: var(--color-primary);
    margin-top: auto;
  }
}

.quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  .quantity-label {
    font-size: 15px;
    color: #333;
    font-weight: 500;
  }
  
  .el-input-number {
    // 去掉整体边框
    border: none !important;
    box-shadow: none !important;
    background: transparent;
    
    &:focus,
    &:hover,
    &.is-focus {
      border: none !important;
      box-shadow: none !important;
    }
  }
  
  .el-input-number__decrease,
  .el-input-number__increase {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: #f5f5f5;
    border: none;
    color: #666;
    
    &:hover:not(.is-disabled) {
      color: var(--color-primary);
      background: #fff0eb;
    }
    
    &.is-disabled {
      color: #ccc;
      background: #fafafa;
    }
  }
  
  .el-input__wrapper {
    width: 50px;
    height: 36px;
    border: none !important;
    box-shadow: none !important;
    background: transparent;
    padding: 0;
    
    &:focus,
    &:hover,
    &.is-focus {
      box-shadow: none !important;
    }
    
    .el-input__inner {
      text-align: center;
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }
  }
}

.subtotal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px dashed #eee;
  
  .subtotal-label {
    font-size: 14px;
    color: #666;
  }
  
  .subtotal-price {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-accent, #ff4d4f);
  }
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
  
  .el-button {
    flex: 1;
    height: 44px;
    font-size: 15px;
    border-radius: 8px;
    font-weight: 500;
    
    &:first-child {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    &.el-button--primary {
      background: var(--color-primary);
      border-color: var(--color-primary);
      
      &:hover {
        background: var(--color-primary-dark, #e65c00);
        border-color: var(--color-primary-dark, #e65c00);
      }
    }
  }
}
</style>
