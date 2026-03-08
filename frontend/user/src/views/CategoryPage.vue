<script>
export default {
  name: 'CategoryPage'
}
</script>

<script setup>
import { ref, computed, onMounted, watch, onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, SortUp, SortDown, View } from '@element-plus/icons-vue'
import { request } from '@/api/config.js'

const route = useRoute()
const router = useRouter()

const props = defineProps({
  isLoggedIn: { type: Boolean, default: false }
})
const emit = defineEmits(['openLogin'])

// API基础URL
const API_BASE_URL = 'http://localhost:8080'
const apiBase = `${API_BASE_URL}/api/user/index`

// sessionStorage key - 使用分类ID区分不同分类的筛选条件
const getStorageKey = () => `categoryFilter_${route.query.categoryId || 'default'}`

// 从 sessionStorage 恢复筛选条件
const loadFilterFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(getStorageKey())
    if (saved) {
      return JSON.parse(saved)
    }
  } catch (e) {
    console.warn('读取筛选条件失败:', e)
  }
  return null
}

// 保存筛选条件到 sessionStorage
const saveFilterToStorage = () => {
  try {
    const data = {
      selectedSubCategory: selectedSubCategory.value,
      sortType: sortType.value,
      priceOrder: priceOrder.value,
      priceRange: priceRange.value,
      currentPage: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    sessionStorage.setItem(getStorageKey(), JSON.stringify(data))
  } catch (e) {
    console.warn('保存筛选条件失败:', e)
  }
}

// 初始化时恢复筛选条件
const savedFilter = loadFilterFromStorage()

// 数据状态
const loading = ref(false)
const products = ref([])
const categories = ref([])        // 一级分类
const subCategories = ref([])     // 二级分类
const currentCategory = ref(null) // 当前一级分类信息
const searchKeyword = ref('')     // 搜索关键词

// 筛选与排序状态 - 从 sessionStorage 恢复或使用默认值
const selectedSubCategory = ref(savedFilter?.selectedSubCategory ?? null)
const sortType = ref(savedFilter?.sortType || 'default')
const priceOrder = ref(savedFilter?.priceOrder || 'asc')
const priceRange = ref(savedFilter?.priceRange || { min: '', max: '' })

// 分页状态 - 从 sessionStorage 恢复或使用默认值
const pagination = ref({
  currentPage: savedFilter?.currentPage || 1,
  pageSize: savedFilter?.pageSize || 20,
  total: 0,
  totalPages: 0
})

// 面包屑路径
const breadcrumbs = computed(() => {
  const crumbs = [
    { name: '首页', path: '/' },
    { name: '商城', path: '/products' }
  ]
  if (currentCategory.value) {
    crumbs.push({ name: currentCategory.value.name, path: '' })
  }
  if (selectedSubCategory.value) {
    const sub = subCategories.value.find(s => s.categoryId === selectedSubCategory.value)
    if (sub) {
      crumbs.push({ name: sub.name, path: '' })
    }
  }
  return crumbs
})

// 构建筛选条件DTO
const buildFilterDTO = () => {
  const filter = {}
  
  // 排序条件
  switch (sortType.value) {
    case 'sales':
      filter.sellCount = 1 // 1-销量降序
      break
    case 'newest':
      filter.latest = 1 // 1-创建时间降序
      break
    case 'price':
      filter.price = priceOrder.value === 'asc' ? 0 : 1 // 0-价格升序，1-价格降序
      break
  }
  
  // 价格区间
  if (priceRange.value.min !== '' && priceRange.value.max !== '') {
    filter.miniPrice = Number(priceRange.value.min)
    filter.maxPrice = Number(priceRange.value.max)
  }
  
  return filter
}

// 筛选后的商品列表（仅用于本地关键词搜索）
const filteredProducts = computed(() => {
  let result = [...products.value]
  
  // 关键词搜索（本地过滤）
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p => 
      p.name.toLowerCase().includes(keyword) || 
      (p.description && p.description.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

// 分页后的商品列表（后端已分页，直接使用）
const paginatedProducts = computed(() => {
  return filteredProducts.value
})

// 获取一级分类列表
const fetchCategories = async () => {
  try {
    const response = await request(`${apiBase}/getSuperCategories`)
    const result = await response.json()
    if (result.code === 0) {
      categories.value = result.data
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 获取二级分类
const fetchSubCategories = async (mainCategoryId) => {
  try {
    const response = await request(`${apiBase}/getSubCategories?mainCategoryId=${mainCategoryId}`)
    const result = await response.json()
    if (result.code === 0) {
      subCategories.value = result.data || []
    }
  } catch (error) {
    console.error('获取二级分类失败:', error)
    subCategories.value = []
  }
}

// 获取一级分类下的商品（支持分页和筛选）
const fetchProducts = async (mainCategoryId) => {
  loading.value = true
  try {
    // 构建请求体 - ProductUnderMainCategoryWithFilterDTO
    const requestBody = {
      productUnderMainCategoryDTO: {
        mainCategoryId: Number(mainCategoryId),
        currentPage: pagination.value.currentPage,
        pageSize: pagination.value.pageSize
      },
      categoryFilterDTO: buildFilterDTO()
    }
    
    const response = await request(`${apiBase}/getProductsUnderMainCategory`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      products.value = result.data.totalRecords || []
      pagination.value.total = Number(result.data.totalCount) || 0
      pagination.value.totalPages = Number(result.data.totalPages) || 0
      pagination.value.currentPage = Number(result.data.currentPage) || 1
    }
  } catch (error) {
    console.error('获取商品失败:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

// 获取二级分类下的商品（支持分页和筛选）
const fetchProductsBySubCategory = async (subCategoryId) => {
  loading.value = true
  try {
    // 构建请求体 - ProductUnderSubCategoryWithFilterDTO
    const requestBody = {
      productUnderSubCategoryDTO: {
        subCategoryId: Number(subCategoryId),
        currentPage: pagination.value.currentPage,
        pageSize: pagination.value.pageSize
      },
      categoryFilterDTO: buildFilterDTO()
    }
    
    const response = await request(`${apiBase}/getProductsUnderSubCategory`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      products.value = result.data.totalRecords || []
      pagination.value.total = Number(result.data.totalCount) || 0
      pagination.value.totalPages = Number(result.data.totalPages) || 0
      pagination.value.currentPage = Number(result.data.currentPage) || 1
    }
  } catch (error) {
    console.error('获取二级分类商品失败:', error)
    products.value = []
  } finally {
    loading.value = false
  }
}

// 初始化页面数据
const initPageData = async () => {
  const categoryId = route.query.categoryId
  const subCategoryId = route.query.subCategoryId
  const keyword = route.query.keyword
  
  // 重新加载该分类的筛选条件
  const savedFilter = loadFilterFromStorage()
  if (savedFilter) {
    selectedSubCategory.value = savedFilter.selectedSubCategory ?? null
    sortType.value = savedFilter.sortType || 'default'
    priceOrder.value = savedFilter.priceOrder || 'asc'
    priceRange.value = savedFilter.priceRange || { min: '', max: '' }
    pagination.value.currentPage = savedFilter.currentPage || 1
    pagination.value.pageSize = savedFilter.pageSize || 20
  }
  
  await fetchCategories()
  
  if (categoryId) {
    currentCategory.value = categories.value.find(c => c.categoryId == categoryId)
    await fetchSubCategories(categoryId)
    
    if (subCategoryId) {
      // URL 明确指定了子分类，直接用
      selectedSubCategory.value = Number(subCategoryId)
      await fetchProductsBySubCategory(subCategoryId)
    } else {
      // 没有指定子分类，显示主分类全部商品，清空子分类选中状态
      selectedSubCategory.value = null
      await fetchProducts(categoryId)
    }
  }
  
  if (keyword) {
    searchKeyword.value = keyword
  }
}

// 选择二级分类
const handleSubCategoryClick = async (categoryId) => {
  const mainCategoryId = route.query.categoryId
  const numId = categoryId === null ? null : Number(categoryId)
  if (numId === null) {
    // 点"全部"，显示一级分类下所有商品
    selectedSubCategory.value = null
    if (mainCategoryId) {
      await fetchProducts(mainCategoryId)
    }
  } else {
    // 选中二级分类
    selectedSubCategory.value = numId
    await fetchProductsBySubCategory(numId)
  }
  pagination.value.currentPage = 1
  saveFilterToStorage()
}

// 切换排序
const handleSortChange = async (type) => {
  if (type === 'price') {
    if (sortType.value === 'price') {
      priceOrder.value = priceOrder.value === 'asc' ? 'desc' : 'asc'
    } else {
      sortType.value = 'price'
      priceOrder.value = 'asc'
    }
  } else {
    sortType.value = type
  }
  pagination.value.currentPage = 1
  saveFilterToStorage()
  
  // 重新请求后端数据
  const mainCategoryId = route.query.categoryId
  if (selectedSubCategory.value) {
    await fetchProductsBySubCategory(selectedSubCategory.value)
  } else if (mainCategoryId) {
    await fetchProducts(mainCategoryId)
  }
}

// 确认价格区间
const handlePriceFilter = async () => {
  pagination.value.currentPage = 1
  saveFilterToStorage()
  
  // 重新请求后端数据
  const mainCategoryId = route.query.categoryId
  if (selectedSubCategory.value) {
    await fetchProductsBySubCategory(selectedSubCategory.value)
  } else if (mainCategoryId) {
    await fetchProducts(mainCategoryId)
  }
}

// 重置价格区间
const resetPriceFilter = async () => {
  priceRange.value = { min: '', max: '' }
  pagination.value.currentPage = 1
  saveFilterToStorage()
  
  // 重新请求后端数据
  const mainCategoryId = route.query.categoryId
  if (selectedSubCategory.value) {
    await fetchProductsBySubCategory(selectedSubCategory.value)
  } else if (mainCategoryId) {
    await fetchProducts(mainCategoryId)
  }
}

// 分页变化
const handlePageChange = async (page) => {
  pagination.value.currentPage = page
  saveFilterToStorage()
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // 重新请求后端数据
  const mainCategoryId = route.query.categoryId
  if (selectedSubCategory.value) {
    await fetchProductsBySubCategory(selectedSubCategory.value)
  } else if (mainCategoryId) {
    await fetchProducts(mainCategoryId)
  }
}

// 每页条数变化
const handleSizeChange = async (size) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1 // 切换每页条数时回到第一页
  saveFilterToStorage()
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // 重新请求后端数据
  const mainCategoryId = route.query.categoryId
  if (selectedSubCategory.value) {
    await fetchProductsBySubCategory(selectedSubCategory.value)
  } else if (mainCategoryId) {
    await fetchProducts(mainCategoryId)
  }
}

// 商品点击
const handleProductClick = (product) => {
  if (!props.isLoggedIn) {
    ElMessage.warning('请先登录再操作')
    emit('openLogin')
    return
  }
  router.push({
    path: `/product/${product.productId}`,
    query: { 
      type: product.productType,
      from: 'category',
      categoryId: route.query.categoryId,
      categoryName: currentCategory.value?.name
    }
  })
}

// 返回商城首页
const goHome = () => {
  router.push('/products')
}

// 监听路由变化（同一组件内切换分类）
watch(() => route.query, (newQuery, oldQuery) => {
  // 只有 query 真正变化时才重新初始化
  if (JSON.stringify(newQuery) !== JSON.stringify(oldQuery)) {
    initPageData()
  }
}, { deep: true, flush: 'post' })

// keep-alive 激活时重新加载（从其他页面返回）
onActivated(() => {
  initPageData()
})

onMounted(() => {
  initPageData()
})
</script>

<template>
  <div class="category-page">
    <div class="page-container">
      <!-- 面包屑导航 -->
      <div class="breadcrumb-wrapper">
        <el-breadcrumb :separator-icon="ArrowRight">
          <el-breadcrumb-item 
            v-for="(crumb, index) in breadcrumbs" 
            :key="index"
            :class="{ 'is-last': index === breadcrumbs.length - 1 }"
          >
            <span 
              v-if="crumb.path" 
              class="crumb-link" 
              @click="router.push(crumb.path)"
            >
              {{ crumb.name }}
            </span>
            <span v-else class="crumb-current">{{ crumb.name }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 筛选与排序栏 -->
      <div class="filter-bar">
        <!-- 子分类标签 -->
        <div v-if="subCategories.length > 0" class="filter-section">
          <span class="filter-label">分类：</span>
          <div class="tag-list">
            <el-tag
              :type="selectedSubCategory === null ? '' : 'info'"
              :effect="selectedSubCategory === null ? 'dark' : 'plain'"
              @click="handleSubCategoryClick(null)"
              style="cursor: pointer;"
            >
              全部
            </el-tag>
            <el-tag
              v-for="sub in subCategories"
              :key="sub.categoryId"
              :type="selectedSubCategory === sub.categoryId || selectedSubCategory === Number(sub.categoryId) ? '' : 'info'"
              :effect="selectedSubCategory === sub.categoryId || selectedSubCategory === Number(sub.categoryId) ? 'dark' : 'plain'"
              @click="handleSubCategoryClick(sub.categoryId)"
              style="cursor: pointer;"
            >
              {{ sub.name }}
            </el-tag>
          </div>
        </div>

        <!-- 排序条件 -->
        <div class="filter-section sort-section">
          <span class="filter-label">排序：</span>
          <div class="sort-options">
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'default' }"
              @click="handleSortChange('default')"
            >
              综合排序
            </span>
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'sales' }"
              @click="handleSortChange('sales')"
            >
              销量
            </span>
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'newest' }"
              @click="handleSortChange('newest')"
            >
              最新上架
            </span>
            <span 
              class="sort-item price-sort" 
              :class="{ active: sortType === 'price' }"
              @click="handleSortChange('price')"
            >
              价格
              <span class="price-arrows">
                <el-icon :class="{ active: sortType === 'price' && priceOrder === 'asc' }">
                  <SortUp />
                </el-icon>
                <el-icon :class="{ active: sortType === 'price' && priceOrder === 'desc' }">
                  <SortDown />
                </el-icon>
              </span>
            </span>
          </div>

          <!-- 价格区间 -->
          <div class="price-filter">
            <el-input
              v-model="priceRange.min"
              placeholder="最低价"
              size="small"
              class="price-input"
              type="number"
            />
            <span class="price-separator">-</span>
            <el-input
              v-model="priceRange.max"
              placeholder="最高价"
              size="small"
              class="price-input"
              type="number"
            />
            <el-button size="small" type="primary" @click="handlePriceFilter">确定</el-button>
            <el-button size="small" @click="resetPriceFilter">重置</el-button>
          </div>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-section">
        <div v-if="loading" class="loading-state">
          <el-skeleton :rows="4" animated />
        </div>

        <template v-else-if="paginatedProducts.length > 0">
          <div class="product-grid">
            <div
              v-for="product in paginatedProducts"
              :key="product.productId"
              class="product-card"
              @click="handleProductClick(product)"
            >
              <div class="product-image">
                <img :src="product.imageUrl" :alt="product.name" />
                <!-- 悬浮操作栏 -->
                <div class="hover-actions">
                  <el-button size="small" type="primary" :icon="View">
                    查看详情
                  </el-button>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-desc">{{ product.description }}</p>
                <div class="product-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ product.price }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页器 -->
          <div class="pagination-wrapper" v-if="pagination.total > 0">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[1, 10, 20, 30, 50]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              popper-class="pagination-size-popper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </template>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <el-empty description="哎呀，这里空空如也~ 换个分类试试？">
            <template #image>
              <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 200 200'%3E%3Ccircle cx='100' cy='100' r='80' fill='%23FFF8F4'/%3E%3Cpath d='M60 120 Q100 80 140 120' stroke='%23FF8A5B' stroke-width='4' fill='none'/%3E%3Ccircle cx='70' cy='90' r='8' fill='%23FF8A5B'/%3E%3Ccircle cx='130' cy='90' r='8' fill='%23FF8A5B'/%3E%3C/svg%3E" alt="empty" />
            </template>
            <div class="empty-actions">
              <el-button type="primary" @click="goHome">返回商城</el-button>
              <el-button @click="router.push('/products')">看看推荐</el-button>
            </div>
          </el-empty>
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped lang="scss">
.category-page {
  background: var(--color-bg-base);
  min-height: 100vh;
  padding: var(--spacing-xl) 0 var(--spacing-4xl);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-xl);
}

// 面包屑导航
.breadcrumb-wrapper {
  margin-bottom: var(--spacing-xl);
  
  :deep(.el-breadcrumb) {
    font-size: var(--font-size-sm);
    
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        color: var(--color-text-tertiary);
      }
      
      &:last-child .el-breadcrumb__inner {
        color: var(--color-text-primary);
        font-weight: var(--font-weight-medium);
      }
    }
    
    .el-breadcrumb__separator {
      color: var(--color-text-tertiary);
    }
  }
  
  .crumb-link {
    cursor: pointer;
    transition: color var(--transition-fast);
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  .crumb-current {
    color: var(--color-text-primary);
    font-weight: var(--font-weight-medium);
  }
}

// 筛选栏
.filter-bar {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.filter-section {
  display: flex;
  align-items: flex-start;
  padding: var(--spacing-md) 0;
  
  &:not(:last-child) {
    border-bottom: 1px solid var(--color-border-light);
  }
}

.filter-label {
  flex-shrink: 0;
  width: 60px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 24px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  
  :deep(.el-tag) {
    cursor: pointer;
    border-radius: var(--radius-base);
    padding: 0 var(--spacing-base);
    height: 32px;
    line-height: 30px;
    border: 1px solid var(--color-border-base);
    background: var(--color-bg-surface);
    color: var(--color-text-secondary);
    transition: all var(--transition-fast);
    
    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
    
    &.el-tag--dark {
      background: var(--color-primary);
      border-color: var(--color-primary);
      color: white;
    }
  }
}

// 排序区域
.sort-section {
  align-items: center;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.sort-options {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  flex: 1;
}

.sort-item {
  cursor: pointer;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  
  &:hover {
    color: var(--color-primary);
  }
  
  &.active {
    color: var(--color-primary);
    font-weight: var(--font-weight-medium);
  }
}

.price-sort {
  display: flex;
  align-items: center;
  gap: 4px;
  
  .price-arrows {
    display: flex;
    flex-direction: column;
    font-size: 10px;
    line-height: 1;
    
    .el-icon {
      color: var(--color-text-placeholder);
      transition: color var(--transition-fast);
      
      &.active {
        color: var(--color-primary);
      }
    }
  }
}

.price-filter {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-left: auto;
  
  .price-input {
    width: 100px;
    
    :deep(.el-input__wrapper) {
      border-radius: var(--radius-sm);
    }
    
    // 隐藏number输入框的上下箭头
    :deep(input[type='number']) {
      appearance: textfield;
      -moz-appearance: textfield;
      
      &::-webkit-outer-spin-button,
      &::-webkit-inner-spin-button {
        appearance: none;
        -webkit-appearance: none;
        margin: 0;
      }
    }
  }
  
  .price-separator {
    color: var(--color-text-tertiary);
  }
  
  :deep(.el-button--primary) {
    background: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover {
      background: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
  }
}

// 商品列表
.product-section {
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--spacing-lg);
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(4, 1fr);
  }
  
  @media (max-width: 992px) {
    grid-template-columns: repeat(3, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
  }
}

.product-card {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  box-shadow: var(--shadow-sm);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--shadow-lg);
    
    .product-image img {
      transform: scale(1.05);
    }
    
    .hover-actions {
      transform: translateY(0);
      opacity: 1;
    }
  }
}

.product-image {
  position: relative;
  width: 100%;
  padding-top: 100%; // 1:1 正方形
  overflow: hidden;
  background: var(--color-bg-base);
  
  img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-base) var(--ease-out);
  }
}

.hover-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--spacing-md);
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  display: flex;
  justify-content: center;
  gap: var(--spacing-sm);
  transform: translateY(100%);
  opacity: 0;
  transition: all var(--transition-base) var(--ease-out);
  
  :deep(.el-button) {
    font-size: var(--font-size-xs);
    
    &.el-button--primary {
      background: var(--color-primary);
      border-color: var(--color-primary);
    }
  }
}

.product-info {
  padding: var(--spacing-md);
}

.product-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
  min-height: 2.8em;
}

.product-desc {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-bottom: var(--spacing-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  color: var(--color-accent);
  font-family: var(--font-family-number);
  
  .price-symbol {
    font-size: var(--font-size-sm);
  }
  
  .price-value {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
  }
}

// 分页器
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-3xl);
  
  :deep(.el-pagination) {
    --el-pagination-hover-color: var(--color-primary);
    
    .el-pager li {
      border-radius: var(--radius-sm);
      
      &.is-active {
        background: var(--color-primary);
        color: white;
      }
      
      &:hover:not(.is-active) {
        color: var(--color-primary);
      }
    }
    
    .btn-prev,
    .btn-next {
      border-radius: var(--radius-sm);
      
      &:hover {
        color: var(--color-primary);
      }
    }
    
    // 每页条数选择器样式
    .el-pagination__sizes {
      .el-select {
        width: 110px;
        
        .el-input__wrapper {
          border-radius: 8px !important;
          background: #FFFFFF !important;
          box-shadow: 0 0 0 1px #FF8A5B inset !important;
          padding: 4px 11px !important;
        }
        
        .el-input__inner {
          color: #FF8A5B !important;
          font-weight: 500 !important;
        }
        
        .el-select__suffix {
          color: #FF8A5B !important;
        }
        
        .el-icon {
          color: #FF8A5B !important;
        }
      }
    }
    
    // 总数和跳转输入框
    .el-pagination__total,
    .el-pagination__jump {
      color: var(--color-text-secondary);
    }
    
    .el-pagination__editor {
      .el-input__wrapper {
        border-radius: var(--radius-sm);
        box-shadow: 0 0 0 1px var(--color-border-base) inset;
        
        &:hover,
        &.is-focus {
          box-shadow: 0 0 0 1px var(--color-primary) inset;
        }
      }
    }
  }
}

// 加载状态
.loading-state {
  padding: var(--spacing-3xl);
}

// 空状态
.empty-state {
  padding: var(--spacing-5xl) 0;
  
  :deep(.el-empty) {
    .el-empty__image {
      width: 160px;
      
      img {
        width: 100%;
      }
    }
    
    .el-empty__description {
      color: var(--color-text-secondary);
      font-size: var(--font-size-base);
      margin-top: var(--spacing-lg);
    }
  }
  
  .empty-actions {
    display: flex;
    gap: var(--spacing-md);
    margin-top: var(--spacing-lg);
    
    :deep(.el-button--primary) {
      background: var(--color-primary);
      border-color: var(--color-primary);
      
      &:hover {
        background: var(--color-primary-dark);
        border-color: var(--color-primary-dark);
      }
    }
  }
}
</style>

<!-- 全局样式 - 用于下拉菜单等 teleport 到 body 的元素 -->
<style lang="scss">
// 分页器每页条数选择器样式
.pagination-wrapper {
  .el-pagination {
    .el-pagination__sizes {
      .el-select {
        width: 110px;
        
        // 新版 Element Plus 使用 el-select__wrapper
        .el-select__wrapper {
          border-radius: 8px !important;
          background: #FFFFFF !important;
          box-shadow: 0 0 0 1px #DCDFE6 inset !important; // 默认灰色边框
          padding: 4px 11px !important;
          transition: all 0.2s ease !important;
          
          // 下拉箭头 - 默认灰色
          .el-select__suffix,
          .el-select__suffix .el-icon,
          .el-select__caret {
            color: #A8ABB2 !important; // 默认灰色箭头
            transition: color 0.2s ease !important;
          }
          
          &:hover,
          &.is-focused,
          &.is-hovering {
            box-shadow: 0 0 0 1px #FF8A5B inset !important; // 悬浮时橘色边框
            
            .el-select__selection .el-select__placeholder {
              color: #FF8A5B !important;
            }
            
            // 悬浮时箭头变橘色
            .el-select__suffix,
            .el-select__suffix .el-icon,
            .el-select__caret {
              color: #FF8A5B !important;
            }
          }
        }
        
        // 选中的文字 - 默认灰色
        .el-select__selection {
          .el-select__placeholder {
            color: #606266 !important; // 默认灰色文字
            font-weight: 400 !important;
            transition: color 0.2s ease !important;
          }
        }
      }
    }
  }
}

.pagination-size-popper {
  &.el-select__popper {
    border-radius: 8px;
    border: 1px solid #F5EBE4;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    
    .el-select-dropdown__item {
      color: #5A5A5A;
      
      &.is-selected {
        color: #FF8A5B;
        font-weight: 500;
      }
      
      &:hover,
      &.is-hovering {
        background-color: #FFF8F4;
      }
    }
  }
}
</style>
