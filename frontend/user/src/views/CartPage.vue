<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Delete, Search, ShoppingCart, Close, ArrowLeft, Warning, Clock } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CART_API, API_BASE_URL, request } from '@/api/config.js'
import { getPageSource, clearPageSource, markAsBackNavigation } from '@/router'

const router = useRouter()
const route = useRoute()

// Props
const props = defineProps({
  isLoggedIn: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => null }
})

const emit = defineEmits(['openLogin'])

// 注入刷新购物车数量的方法
const refreshCartCount = inject('refreshCartCount', () => {})

// 实名认证提示弹窗
const showAuthTipDialog = ref(false)

// 信息缺失提示弹窗（手机号/收货地址）
const showInfoTipDialog = ref(false)
const infoTipType = ref('') // 'phone' 或 'address'
const infoTipMessage = ref('')

// 数据状态
const loading = ref(false)
const cartItems = ref([])
const searchKeyword = ref('')
const selectAll = ref(false)

// 修改数量对话框相关
const showQuantityDialog = ref(false)
const quantityModalMouseDown = ref(false)
const editingItem = ref(null)
const newQuantity = ref('1')
const quantitySaving = ref(false)
const quantityError = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)
const totalPages = ref(0)

// 分页大小选项
const pageSizes = [5, 10, 20, 50]

// 已选商品（所有选中的，用于批量删除）
const allSelectedItems = computed(() => cartItems.value.filter(item => item.selected))

// 可结算商品（只有出售中的商品才参与结算）
const selectedItems = computed(() => cartItems.value.filter(item => item.selected && item.status === 1))

// 已选商品数量（所有选中的）
const selectedCount = computed(() => allSelectedItems.value.length)

// 可结算商品数量
const checkoutCount = computed(() => selectedItems.value.length)

// 总价（只计算出售中的商品）
const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 全选/取消全选
const handleSelectAll = (val) => {
  cartItems.value.forEach(item => { item.selected = val })
}

// 监听单个选择变化
const handleItemSelect = () => {
  selectAll.value = cartItems.value.length > 0 && cartItems.value.every(item => item.selected)
}

// 打开修改数量对话框
const openQuantityDialog = (item) => {
  if (isItemUnavailable(item)) return
  editingItem.value = item
  newQuantity.value = String(item.quantity)
  quantityError.value = ''
  showQuantityDialog.value = true
}

// 处理数量输入（只允许输入数字）
const handleQuantityInput = (e) => {
  // 只保留数字
  const val = e.target ? e.target.value : e
  newQuantity.value = String(val).replace(/[^0-9]/g, '')
  quantityError.value = ''
}

// 验证数量
const validateQuantity = () => {
  const num = parseInt(newQuantity.value)
  
  if (!newQuantity.value || isNaN(num)) {
    quantityError.value = '请输入有效数量'
    return false
  }
  if (num < 1) {
    quantityError.value = '数量不能小于1'
    return false
  }
  return true
}

// 确认修改数量 - 调用后端API
const confirmQuantityChange = async () => {
  if (!editingItem.value || !editingItem.value.cartId) {
    ElMessage.error('购物车信息异常，请刷新页面')
    return
  }
  
  // 验证数量
  if (!validateQuantity()) return
  
  const quantity = parseInt(newQuantity.value)
  
  // 数量未变化，直接关闭
  if (quantity === editingItem.value.quantity) {
    showQuantityDialog.value = false
    return
  }
  
  quantitySaving.value = true
  const item = editingItem.value
  const oldQuantity = item.quantity
  
  try {
    const response = await request(CART_API.CHANGE_CART, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        cartId: item.cartId,
        totalCount: quantity
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 更新数量和小计金额
      item.quantity = result.data.totalCount
      item.totalPrice = parseFloat(result.data.totalPrice)
      // 使用后端返回的库存更新最大可购数量
      if (result.data.stock) {
        item.stock = result.data.stock
        item.maxQuantity = result.data.stock
      }
      ElMessage.success('数量修改成功')
      showQuantityDialog.value = false
    } else {
      // 库存不足或商品下架，设置最大可购买数量为当前数量（只能减少，不能增加）
      item.maxQuantity = oldQuantity
      ElMessage.error(result.message || '修改数量失败')
    }
  } catch (error) {
    console.error('修改数量异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    quantitySaving.value = false
  }
}

// 数量变化 - 调用后端API修改购物车商品数量
const handleQuantityChange = async (item, val) => {
  if (!item.cartId) {
    ElMessage.error('购物车信息异常，请刷新页面')
    return
  }
  
  const oldQuantity = item.quantity
  item.quantity = val
  
  try {
    const response = await request(CART_API.CHANGE_CART, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        cartId: item.cartId,
        totalCount: val
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 更新小计金额
      item.totalPrice = parseFloat(result.data.totalPrice)
      item.quantity = result.data.totalCount
      // 成功后清除最大值限制
      item.maxQuantity = null
    } else {
      // 恢复原数量
      item.quantity = oldQuantity
      // 库存不足或商品下架，设置最大可购买数量为当前数量（只能减少，不能增加）
      item.maxQuantity = oldQuantity
      ElMessage.error(result.message || '修改数量失败')
    }
  } catch (error) {
    console.error('修改数量异常:', error)
    // 恢复原数量
    item.quantity = oldQuantity
    // ElMessage.error('网络错误，请稍后重试')
  }
}

// 删除单个商品 - 调用后端API
const handleDelete = async (item) => {
  if (!item.cartId) {
    ElMessage.error('购物车信息异常，请刷新页面')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除"${item.name}"吗？`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request(`${CART_API.DELETE_CART}?cartId=${item.cartId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      const index = cartItems.value.findIndex(i => i.cartId === item.cartId)
      if (index > -1) {
        cartItems.value.splice(index, 1)
        totalCount.value = Math.max(0, totalCount.value - 1)
      }
      ElMessage.success('删除成功')
      
      // 刷新导航栏购物车角标数量
      refreshCartCount()
      
      // 如果当前页没有数据了且不是第一页，跳转到上一页
      if (cartItems.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        fetchCartData()
      }
    } else {
      ElMessage.error(result.message || '删除失败，请稍后重试')
    }
  } catch (error) {
    // 用户取消操作不处理
    if (error !== 'cancel' && error?.toString() !== 'cancel') {
      console.error('删除商品异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 批量删除 - 调用后端API
const handleBatchDelete = async () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  // 获取选中商品的 cartIds
  const cartIds = allSelectedItems.value.map(item => item.cartId).filter(id => id)
  if (cartIds.length === 0) {
    ElMessage.error('购物车信息异常，请刷新页面')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的${selectedCount.value}件商品吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request(`${CART_API.BATCH_DELETE_CART}?cartIds=${cartIds.join(',')}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 统计成功删除的数量
      const successCount = result.data.filter(success => success).length
      const failCount = result.data.length - successCount
      
      if (successCount > 0) {
        // 从列表中移除成功删除的商品
        const successCartIds = cartIds.filter((_, index) => result.data[index])
        cartItems.value = cartItems.value.filter(item => !successCartIds.includes(item.cartId))
        totalCount.value = Math.max(0, totalCount.value - successCount)
        selectAll.value = false
        
        if (failCount > 0) {
          ElMessage.warning(`成功删除 ${successCount} 件商品，${failCount} 件删除失败`)
        } else {
          ElMessage.success('删除成功')
        }
        
        // 刷新导航栏购物车角标数量
        refreshCartCount()
        
        // 如果当前页没有数据了且不是第一页，跳转到上一页
        if (cartItems.value.length === 0 && currentPage.value > 1) {
          currentPage.value--
          fetchCartData()
        }
      } else {
        ElMessage.error('删除失败，请稍后重试')
      }
    } else {
      ElMessage.error(result.message || '删除失败，请稍后重试')
    }
  } catch (error) {
    // 用户取消操作不处理
    if (error !== 'cancel' && error?.toString() !== 'cancel') {
      console.error('批量删除商品异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 跳转商品详情
const goToDetail = (item) => {
  router.push({ path: `/product/${item.productId}`, query: { type: item.type } })
}

// 结算中状态
const settling = ref(false)

// 去结算 - 调用后端批量结算接口
const handleCheckout = async () => {
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  if (checkoutCount.value === 0) {
    ElMessage.warning('请先选择可结算的商品')
    return
  }

  settling.value = true
  try {
    // 获取选中商品的 cartIds
    const cartIds = selectedItems.value.map(item => item.cartId)

    // 调用批量结算接口，返回预创建订单详情列表
    const response = await request(`${CART_API.BATCH_SETTLE_CART}?cartIds=${cartIds.join(',')}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()

    if (result.code !== 0 || !result.data) {
      // 检查是否是实名认证错误
      if (result.message && result.message.includes('实名认证')) {
        showAuthTipDialog.value = true
        return
      }
      // 检查是否是手机号缺失错误
      if (result.message && result.message.includes('手机号')) {
        infoTipType.value = 'phone'
        infoTipMessage.value = '您尚未绑定手机号，无法结算。'
        showInfoTipDialog.value = true
        return
      }
      // 检查是否是收货地址缺失错误
      if (result.message && result.message.includes('收货地址')) {
        infoTipType.value = 'address'
        infoTipMessage.value = '您尚未添加收货地址，无法结算。'
        showInfoTipDialog.value = true
        return
      }
      ElMessage.error(result.message || '结算失败，请稍后重试')
      return
    }

    // 后端返回 List<PreOrderDetailVO>，过滤掉 null 值（失败的商品）
    const preOrderDetails = result.data.filter(order => order !== null)

    if (preOrderDetails.length === 0) {
      ElMessage.error('所有商品结算失败，请检查商品状态')
      return
    }

    // 检查是否有失败的商品
    const failedCount = result.data.length - preOrderDetails.length
    if (failedCount > 0) {
      ElMessage.warning(`${failedCount} 件商品结算失败，已过滤`)
    }

    // 将预创建订单详情存储到sessionStorage，直接用于结算页面
    sessionStorage.setItem('preOrderDetails', JSON.stringify(preOrderDetails))

    // 询问用户是否删除已结算的商品
    try {
      await ElMessageBox.confirm(
        `是否从购物车中删除已结算的 ${cartIds.length} 件商品？`,
        '删除确认',
        {
          confirmButtonText: '删除',
          cancelButtonText: '保留',
          type: 'info',
          distinguishCancelAndClose: true
        }
      )
      // 用户选择删除，调用批量删除接口
      await deleteSettledItems(cartIds)
    } catch (action) {
      // 用户选择保留或关闭弹窗，不做删除操作
    }

    // 无论是否删除，都跳转到结算页面
    router.push('/checkout')
  } catch (error) {
    console.error('结算异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    settling.value = false
  }
}

// 删除已结算的商品（静默删除，不显示确认框）
const deleteSettledItems = async (cartIds) => {
  try {
    const response = await request(`${CART_API.BATCH_DELETE_CART}?cartIds=${cartIds.join(',')}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()

    if (result.code === 0 && result.data) {
      const successCount = result.data.filter(success => success).length
      if (successCount > 0) {
        // 从列表中移除成功删除的商品
        const successCartIds = cartIds.filter((_, index) => result.data[index])
        cartItems.value = cartItems.value.filter(item => !successCartIds.includes(item.cartId))
        totalCount.value = Math.max(0, totalCount.value - successCount)
        selectAll.value = false
        // 刷新导航栏购物车角标数量
        refreshCartCount()
      }
    }
  } catch (error) {
    console.error('删除已结算商品异常:', error)
    // 静默失败，不影响结算流程
  }
}

// 获取购物车数据（支持搜索keyword，为空时返回全部）
const fetchCartData = async (keyword = '') => {
  if (!props.isLoggedIn || !props.userInfo?.userId) {
    loading.value = false
    return
  }
  loading.value = true
  // 超时保护：3秒后强制重置loading
  const timeoutId = setTimeout(() => {
    loading.value = false
    console.warn('购物车请求超时，强制重置loading状态')
  }, 3000)
  try {
    const response = await request(CART_API.GET_CART_LIST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        keyword: keyword,
        currentPage: currentPage.value,
        pageSize: pageSize.value
      })
    })
    const result = await response.json()
    console.log('获取购物车列表响应:', result)

    if (result.code === 0 && result.data) {
      // 转换后端数据格式到前端格式
      const records = result.data.totalRecords || []
      cartItems.value = records.map(item => ({
        id: item.productId,
        cartId: item.cartId, // 购物车记录ID，用于修改数量
        productId: item.productId,
        name: item.productName,
        image: item.productImageUrl || 'https://via.placeholder.com/200x200/f5f7fa/999?text=No+Image',
        price: parseFloat(item.price),
        quantity: item.totalCount,
        stock: 99, // 暂时设置默认库存，后续可从后端获取
        selected: false,
        type: item.productType,
        status: item.productStatus,
        totalPrice: parseFloat(item.totalPrice),
        specs: item.specs || '', // 规格/属性信息
        createTime: item.createTime // 创建时间（后端默认按此排序）
      }))
      totalCount.value = result.data.totalCount || 0
      totalPages.value = result.data.totalPages || 0
      currentPage.value = result.data.currentPage || 1
    } else {
      console.error('获取购物车失败:', result.message)
      ElMessage.error(result.message || '获取购物车列表失败')
    }
  } catch (error) {
    console.error('获取购物车失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    clearTimeout(timeoutId)
    loading.value = false
  }
}

onMounted(() => {
  fetchCartData()
})

// 搜索购物车商品 - 统一调用fetchCartData
const handleSearch = async () => {
  const keyword = searchKeyword.value.trim()

  if (!props.isLoggedIn || !props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 重置到第一页
  currentPage.value = 1
  selectAll.value = false

  // 调用list接口，传入keyword（为空时返回全部）
  await fetchCartData(keyword)
}

// 清除搜索，返回购物车列表
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  selectAll.value = false
  fetchCartData()
}

// 分页切换
const handlePageChange = (page) => {
  currentPage.value = page
  selectAll.value = false
  const keyword = searchKeyword.value.trim()
  fetchCartData(keyword)
}

// 每页数量切换
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  selectAll.value = false
  const keyword = searchKeyword.value.trim()
  fetchCartData(keyword)
}

// 判断商品是否不可用（售罄或下架）
const isItemUnavailable = (item) => {
  // status: 1=出售中, 2=售罄, 3=已下架
  return item.status === 2 || item.status === 3 || item.stock <= 0
}

// 获取商品不可用原因
const getUnavailableReason = (item) => {
  if (item.status === 3) return '已下架'
  if (item.status === 2 || item.stock <= 0) return '已售罄'
  return ''
}

// 返回上一页（使用统一的来源跟踪系统）
const goBack = () => {
  const sourcePath = getPageSource('Cart')
  if (sourcePath) {
    clearPageSource('Cart')
    markAsBackNavigation() // 标记为返回操作，防止目标页面更新来源记录
    router.push(sourcePath)
  } else {
    router.push('/products')
  }
}

// 格式化创建时间
const formatCreateTime = (createTime) => {
  if (!createTime) return ''
  const date = new Date(createTime)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) {
    return '刚刚'
  } else if (diffMins < 60) {
    return `${diffMins}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 30) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }
}
</script>

<template>
  <div class="cart-page">
    <div class="cart-container">
      <!-- 导航栏：返回按钮 + 面包屑 -->
      <div class="nav-header">
        <div class="back-nav" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <div class="breadcrumb-nav">
          <span class="breadcrumb-item" @click="router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">购物车</span>
        </div>
      </div>

      <!-- 未登录提示 -->
      <div v-if="!isLoggedIn" class="empty-state-wrapper">
        <div class="empty-state">
          <el-icon :size="100" color="#ddd"><ShoppingCart /></el-icon>
          <p class="empty-title">请先登录查看购物车</p>
          <p class="empty-desc">登录后可以查看购物车中的商品</p>
          <el-button type="primary" size="large" class="empty-btn" @click="emit('openLogin')">
            立即登录
          </el-button>
        </div>
      </div>

      <!-- 双栏布局：左侧列表 + 右侧结算栏（登录后始终显示） -->
      <el-row v-else :gutter="24">
        <!-- 左侧：购物车列表区 -->
        <el-col :span="18">
          <div class="cart-list-card">
            <!-- 头部操作栏 -->
            <div class="list-header-card" :class="{ 'is-disabled': loading }">
              <div class="header-left">
                <span class="header-title">
                  购物车
                  <span class="item-count">({{ totalCount }})</span>
                </span>
              </div>
              <div class="header-center">
                <div class="search-box">
                  <input
                    v-model="searchKeyword"
                    type="text"
                    placeholder="搜索购物车内商品..."
                    class="search-input"
                    @keyup.enter="handleSearch"
                  />
                  <button
                    v-if="searchKeyword"
                    class="clear-btn"
                    @click="clearSearch"
                    title="清除搜索"
                  >
                    <el-icon><Close /></el-icon>
                  </button>
                  <button
                    class="search-btn"
                    @click="handleSearch"
                  >
                    <el-icon><Search /></el-icon>
                    <span class="search-btn-text">{{ loading ? '搜索中...' : '搜索' }}</span>
                  </button>
                </div>
              </div>
              <div class="header-right">
                <el-button 
                  type="danger" 
                  text 
                  :disabled="selectedCount === 0 || cartItems.length === 0"
                  @click="handleBatchDelete"
                >
                  <el-icon><Delete /></el-icon>
                  批量删除
                </el-button>
              </div>
            </div>

            <!-- 列标题栏 -->
            <div class="column-header" :class="{ 'is-disabled': loading }">
              <div class="col-checkbox">
                <el-checkbox v-model="selectAll" @change="handleSelectAll" :disabled="cartItems.length === 0" />
              </div>
              <div class="col-product">商品信息</div>
              <div class="col-price">单价</div>
              <div class="col-quantity">数量</div>
              <div class="col-subtotal">小计</div>
              <div class="col-action">操作</div>
            </div>

            <!-- 商品列表 -->
            <div class="cart-items-wrapper">
              <!-- 空购物车提示（仅在无搜索关键词时显示） -->
              <div v-if="cartItems.length === 0 && !searchKeyword && !loading" class="empty-cart-hint">
                <el-icon :size="64" color="#dcdfe6"><ShoppingCart /></el-icon>
                <p class="hint-title">购物车还是空的</p>
                <p class="hint-desc">快去挑选心仪的商品加入购物车吧~</p>
                <el-button type="primary" class="hint-btn" @click="router.push('/products')">
                  去逛逛
                </el-button>
              </div>

              <!-- 加载骨架屏 / 商品列表 -->
              <el-skeleton v-else :loading="loading" animated :rows="6">
              <template #default>
                <div
                  v-for="item in cartItems"
                  :key="item.id"
                  class="cart-item-card"
                  :class="{ 'is-unavailable': isItemUnavailable(item) }"
                >
                  <!-- 不可用商品蒙版 -->
                  <div v-if="isItemUnavailable(item)" class="unavailable-mask">
                    <span class="unavailable-tag">{{ getUnavailableReason(item) }}</span>
                  </div>

                  <!-- 勾选框 -->
                  <div class="item-checkbox">
                    <el-checkbox 
                      v-model="item.selected" 
                      @change="handleItemSelect" 
                    />
                  </div>

                  <!-- 商品图片 -->
                  <div class="item-image" @click="goToDetail(item)">
                    <img :src="item.image" :alt="item.name" />
                  </div>

                  <!-- 商品信息 -->
                  <div class="item-info">
                    <h3 class="item-name" @click="goToDetail(item)">{{ item.name }}</h3>
                    <p v-if="item.specs" class="item-specs">{{ item.specs }}</p>
                    <p v-if="item.createTime" class="item-create-time">
                      <el-icon><Clock /></el-icon>
                      <span>{{ formatCreateTime(item.createTime) }}</span>
                    </p>
                  </div>

                  <!-- 单价 -->
                  <div class="item-price">
                    <span class="price-value">¥{{ item.price.toFixed(2) }}</span>
                  </div>

                  <!-- 数量显示（点击弹出修改对话框） -->
                  <div class="item-quantity">
                    <span 
                      class="quantity-text"
                      :class="{ 'is-disabled': isItemUnavailable(item) }"
                      @click="openQuantityDialog(item)"
                    >
                      {{ item.quantity }}
                    </span>
                  </div>

                  <!-- 小计金额 -->
                  <div class="item-subtotal">
                    <span class="subtotal-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                  </div>

                  <!-- 删除操作 -->
                  <div class="item-action">
                    <el-icon class="delete-icon" @click="handleDelete(item)"><Delete /></el-icon>
                  </div>
                </div>

                <!-- 列表为空（搜索无结果） -->
                <div v-if="cartItems.length === 0 && searchKeyword && !loading" class="no-search-result">
                  <el-icon :size="64" color="#dcdfe6"><Search /></el-icon>
                  <p class="result-title">未找到匹配商品</p>
                  <p class="result-desc">没有找到与"{{ searchKeyword }}"相关的商品</p>
                  <el-button type="primary" class="result-btn" @click="clearSearch">返回购物车</el-button>
                </div>
              </template>
            </el-skeleton>
            </div>

            <!-- 分页组件 -->
            <div v-if="totalCount > 0" class="pagination-wrapper">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="pageSizes"
                :total="totalCount"
                :pager-count="5"
                layout="total, sizes, prev, pager, next, jumper"
                background
                @current-change="handlePageChange"
                @size-change="handleSizeChange"
              />
            </div>
          </div>
        </el-col>

        <!-- 右侧：结算概览卡片 -->
        <el-col :span="6">
          <div class="summary-panel">
            <h3 class="summary-title">结算明细</h3>

            <!-- 已选商品缩略图预览 -->
            <div class="selected-preview">
              <div class="preview-scroll" v-if="selectedItems.length > 0">
                <div 
                  v-for="item in selectedItems" 
                  :key="item.id" 
                  class="preview-thumb"
                >
                  <img :src="item.image" :alt="item.name" />
                </div>
              </div>
              <p v-else class="preview-empty">暂未选择商品</p>
            </div>

            <!-- 分隔线 -->
            <div class="summary-divider"></div>

            <!-- 价格明细 -->
            <div class="price-details">
              <div class="detail-row">
                <span class="detail-label">可结算商品</span>
                <span class="detail-value">{{ checkoutCount }} 件</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">商品总价</span>
                <span class="detail-value">¥{{ totalPrice.toFixed(2) }}</span>
              </div>
            </div>

            <!-- 分隔线 -->
            <div class="summary-divider"></div>

            <!-- 应付总额 -->
            <div class="total-row">
              <span class="total-label">应付总额</span>
              <span class="total-value">¥{{ totalPrice.toFixed(2) }}</span>
            </div>

            <!-- 结算按钮 -->
            <el-button
              type="primary"
              class="checkout-btn"
              :disabled="checkoutCount === 0 || settling"
              :loading="settling"
              @click="handleCheckout"
            >
              {{ settling ? '结算中...' : '立即结算' }}
            </el-button>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
  
  <!-- 修改数量模态框 -->
  <Teleport to="body">
    <Transition name="quantity-modal">
      <div v-if="showQuantityDialog" class="quantity-modal-overlay" @mousedown.self="quantityModalMouseDown = true" @mouseup.self="quantityModalMouseDown && (showQuantityDialog = false); quantityModalMouseDown = false">
        <div class="quantity-modal" @mousedown="quantityModalMouseDown = false">
          <!-- 模态框头部 -->
          <div class="modal-header">
            <h3 class="modal-title">
              <el-icon><ShoppingCart /></el-icon>
              修改数量
            </h3>
            <button class="modal-close" @click="showQuantityDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <!-- 商品信息 -->
          <div class="modal-body" v-if="editingItem">
            <div class="product-preview">
              <img :src="editingItem.image" :alt="editingItem.name" class="preview-image" />
              <div class="preview-info">
                <h4 class="preview-name">{{ editingItem.name }}</h4>
                <div class="preview-price">¥{{ editingItem.price.toFixed(2) }}</div>
              </div>
            </div>
            
            <!-- 数量输入 -->
            <div class="quantity-row">
              <span class="quantity-label">购买数量</span>
              <input 
                type="text" 
                v-model="newQuantity" 
                class="qty-input-simple"
                placeholder="请输入数量"
                @input="handleQuantityInput"
              />
            </div>
            <p v-if="quantityError" class="quantity-error">{{ quantityError }}</p>
            
            <!-- 小计 -->
            <div class="subtotal-row">
              <span class="subtotal-label">小计</span>
              <span class="subtotal-price">¥{{ (editingItem.price * (parseInt(newQuantity) || 0)).toFixed(2) }}</span>
            </div>
          </div>
          
          <!-- 模态框底部 -->
          <div class="modal-footer">
            <button class="btn-cancel" @click="showQuantityDialog = false">取消</button>
            <button class="btn-confirm" :disabled="quantitySaving" @click="confirmQuantityChange">
              <span v-if="quantitySaving" class="loading-spinner"></span>
              {{ quantitySaving ? '保存中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 实名认证提示弹窗 -->
  <Teleport to="body">
    <Transition name="auth-tip-modal">
      <div v-if="showAuthTipDialog" class="auth-tip-modal-overlay">
        <div class="auth-tip-modal">
          <div class="modal-header">
            <h3 class="modal-title">
              <el-icon><Warning /></el-icon>
              需要实名认证
            </h3>
            <button class="modal-close" @click="showAuthTipDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div class="modal-body">
            <div class="auth-tip-content">
              <p>您尚未完成实名认证，无法结算商品。</p>
              <p class="auth-tip-hint">请前往个人资料页面完成实名认证后再进行操作。</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showAuthTipDialog = false">取消</button>
            <button class="btn-confirm" @click="showAuthTipDialog = false; router.push('/profile/profile')">去认证</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 信息缺失提示弹窗（手机号/收货地址） -->
  <Teleport to="body">
    <Transition name="info-tip-modal">
      <div v-if="showInfoTipDialog" class="info-tip-modal-overlay">
        <div class="info-tip-modal">
          <div class="modal-header">
            <h3 class="modal-title">
              <el-icon><Warning /></el-icon>
              {{ infoTipType === 'phone' ? '需要绑定手机号' : '需要添加收货地址' }}
            </h3>
            <button class="modal-close" @click="showInfoTipDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div class="modal-body">
            <div class="info-tip-content">
              <p>{{ infoTipMessage }}</p>
              <p class="info-tip-hint">
                {{ infoTipType === 'phone' 
                  ? '请前往账号管理页面绑定手机号后再进行下单。' 
                  : '请前往账号管理页面添加收货地址后再进行下单。' 
                }}
              </p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showInfoTipDialog = false">取消</button>
            <button class="btn-confirm" @click="showInfoTipDialog = false; router.push('/profile/settings')">
              {{ infoTipType === 'phone' ? '去绑定' : '去添加' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped lang="scss">
// 核心色调变量
$primary-color: #FF8A5B;
$primary-color-dark: #E67A4D; // darken($primary-color, 8%) 的预计算值
$accent-color: #2F8F6A;
$bg-color: #FFF8F4;
$card-bg: #ffffff;
$card-radius: 12px;
$shadow-base: 0 4px 12px rgba(0, 0, 0, 0.05);
$shadow-strong: 0 8px 24px rgba(0, 0, 0, 0.08);

.cart-page {
  min-height: calc(100vh - 80px);
  background: $bg-color;
  padding: 32px 0 60px;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

// 导航栏
.nav-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-base) 0;
  margin-bottom: var(--spacing-lg);
}

.back-nav {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-base);
  transition: all var(--transition-fast);
  flex-shrink: 0;
  
  &:hover {
    color: var(--color-primary);
    background: var(--color-secondary-light);
  }
  
  .el-icon {
    font-size: 14px;
  }
  
  span {
    font-size: var(--font-size-sm);
  }
}

// 面包屑导航
.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  
  .breadcrumb-item {
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  .breadcrumb-separator {
    color: var(--color-text-tertiary);
  }
  
  .breadcrumb-current {
    color: var(--color-text-primary);
    font-weight: 500;
  }
}

// 空状态样式
.empty-state-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 60px 40px;
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $shadow-base;

  .empty-title {
    margin: 24px 0 8px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }

  .empty-desc {
    margin: 0 0 24px;
    font-size: 14px;
    color: #909399;
  }

  .empty-btn {
    background: $primary-color;
    border-color: $primary-color;
    border-radius: 24px;
    padding: 12px 40px;
    font-size: 16px;

    &:hover {
      background: $primary-color-dark;
      border-color: $primary-color-dark;
    }
  }
}

// 整体购物车卡片容器
.cart-list-card {
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $shadow-base;
  overflow: hidden;
}

// 左侧：头部操作栏（融入标题栏风格）
.list-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: $card-bg;
  border-radius: 0;
  box-shadow: none;
  margin-bottom: 0;
  gap: 20px;
  border-bottom: 1px solid #f0f0f0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;

    .item-count {
      color: #909399;
      font-weight: 400;
      font-size: 15px;
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
    max-width: 360px;

    .search-box {
      display: flex;
      align-items: center;
      width: 100%;
      height: 36px;
      background: #f5f7fa;
      border: 1px solid transparent;
      border-radius: 18px;
      overflow: hidden;
      box-shadow: none;
      transition: all 0.2s ease;

      &:hover {
        background: #eef1f6;
      }

      &:focus-within {
        border-color: $primary-color;
        background: #fff;
        box-shadow: 0 0 0 2px rgba($primary-color, 0.1);
      }

      .search-input {
        flex: 1;
        min-width: 0;
        height: 100%;
        padding: 0 14px;
        border: none;
        border-radius: 0;
        outline: none;
        background: transparent;
        font-size: 13px;
        color: #303133;
        box-shadow: none;

        &::placeholder {
          color: #a8abb2;
        }
      }

      .clear-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        margin-right: 4px;
        border: none;
        background: transparent;
        color: #909399;
        border-radius: 50%;
        cursor: pointer;
        transition: all 0.2s ease;
        flex-shrink: 0;

        &:hover {
          background: #e4e7ed;
          color: #606266;
        }

        .el-icon {
          font-size: 14px;
        }
      }

      .search-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 4px;
        height: 28px;
        padding: 0 14px;
        margin: 4px;
        border: none;
        background: $primary-color;
        color: #fff;
        border-radius: 14px;
        cursor: pointer;
        transition: all 0.25s ease;
        flex-shrink: 0;
        font-size: 12px;
        font-weight: 500;
        box-shadow: none;

        &:hover {
          background: $primary-color-dark;
        }

        &:disabled {
          background: #c0c4cc;
          cursor: not-allowed;
        }

        &:active {
          transform: scale(0.96);
        }

        .el-icon {
          font-size: 13px;
          color: #fff;
        }

        .search-btn-text {
          color: #fff;
        }
      }
    }
  }

  .header-right {
    flex-shrink: 0;
    
    :deep(.el-button) {
      font-size: 14px;
      
      &:not(.is-disabled):hover {
        color: #f56c6c;
      }
    }
  }
}

// 列标题栏
.column-header {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  background: #f9fafb;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
  color: #909399;
  font-weight: 500;

  .col-checkbox {
    flex-shrink: 0;
    margin-right: 16px;
  }

  .col-product {
    flex: 1;
    min-width: 0;
    padding-left: 96px; // 图片宽度 + margin
  }

  .col-price {
    flex-shrink: 0;
    width: 90px;
    text-align: center;
  }

  .col-quantity {
    flex-shrink: 0;
    width: 110px;
    text-align: center;
  }

  .col-subtotal {
    flex-shrink: 0;
    width: 100px;
    text-align: center;
  }

  .col-action {
    flex-shrink: 0;
    width: 60px;
    text-align: center;
  }

  // 禁用状态
  &.is-disabled {
    opacity: 0.6;
    pointer-events: none;
    user-select: none;
  }
}

// 头部操作栏禁用状态
.list-header-card.is-disabled {
  .search-box {
    opacity: 0.5;
    pointer-events: none;

    .search-input {
      cursor: not-allowed;
    }

    .search-btn {
      cursor: not-allowed;
      background: #c0c4cc;
    }
  }
}

// 商品列表容器（固定高度，支持滚动）
.cart-items-wrapper {
  display: flex;
  flex-direction: column;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  height: 520px; // 固定高度，约能容纳5个商品项
  overflow-y: auto;
  overflow-x: hidden;

  // 自定义滚动条样式
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f5f5f5;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
    transition: background 0.2s ease;

    &:hover {
      background: #c0c4cc;
    }
  }
}

// 商品卡片
.cart-item-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #fafbfc;
  }

  // 勾选框
  .item-checkbox {
    flex-shrink: 0;
    margin-right: 16px;
    position: relative;
    z-index: 15; // 高于蒙版
  }

  // 商品图片
  .item-image {
    flex-shrink: 0;
    width: 80px;
    height: 80px;
    margin-right: 16px;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
      border: 1px solid #f0f0f0;
      transition: transform 0.2s ease;
    }

    &:hover img {
      transform: scale(1.03);
    }
  }

  // 商品信息
  .item-info {
    flex: 1;
    min-width: 0;
    margin-right: 20px;

    .item-name {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      line-height: 1.4;
      margin: 0 0 6px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      cursor: pointer;
      transition: color 0.2s ease;

      &:hover {
        color: $primary-color;
      }
    }

    .item-specs {
      font-size: 12px;
      color: #909399;
      margin: 0 0 6px;
    }

    .item-create-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #a8abb2;
      margin: 0;

      .el-icon {
        font-size: 12px;
      }
    }
  }

  // 单价
  .item-price {
    flex-shrink: 0;
    width: 90px;
    text-align: center;

    .price-value {
      font-size: 14px;
      color: #606266;
    }
  }

  // 数量显示
  .item-quantity {
    flex-shrink: 0;
    width: 110px;
    display: flex;
    justify-content: center;

    .quantity-text {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 60px;
      height: 32px;
      padding: 0 12px;
      background: #f5f7fa;
      border-radius: 6px;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
      cursor: pointer;
      transition: all 0.2s ease;
      border: 1px solid transparent;

      &:hover:not(.is-disabled) {
        background: #fff;
        border-color: $primary-color;
        color: $primary-color;
      }

      &.is-disabled {
        cursor: not-allowed;
        opacity: 0.5;
      }
    }
  }

  // 小计金额
  .item-subtotal {
    flex-shrink: 0;
    width: 100px;
    text-align: center;

    .subtotal-value {
      font-size: 16px;
      font-weight: 700;
      color: $primary-color;
    }
  }

  // 删除操作
  .item-action {
    flex-shrink: 0;
    width: 60px;
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    z-index: 15; // 高于蒙版

    .delete-icon {
      font-size: 18px;
      color: #c0c4cc;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        color: #f56c6c;
        transform: scale(1.1);
      }
    }
  }

  // 不可用商品状态（售罄/下架）
  &.is-unavailable {
    position: relative;

    .item-image,
    .item-info {
      opacity: 0.5;
    }

    .item-price,
    .item-quantity,
    .item-subtotal {
      opacity: 0.5;
      pointer-events: none; // 禁用交互
      user-select: none;    // 禁止文本选择
    }

    .item-name:hover {
      color: $primary-color; // 保留悬停样式，因为可以跳转
    }
  }

  // 不可用商品蒙版（不遮住勾选框和删除按钮）
  .unavailable-mask {
    position: absolute;
    top: 0;
    left: 50px;  // 避开左侧勾选框
    right: 60px; // 避开右侧删除按钮
    bottom: 0;
    background: rgba(255, 255, 255, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    pointer-events: none;

    .unavailable-tag {
      padding: 8px 20px;
      background: rgba(0, 0, 0, 0.6);
      color: #fff;
      font-size: 14px;
      font-weight: 600;
      border-radius: 4px;
      letter-spacing: 1px;
    }
  }
}

// 搜索无结果
.no-search-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 60px 20px;
  text-align: center;

  .result-title {
    margin: 20px 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #606266;
  }

  .result-desc {
    margin: 0 0 24px;
    font-size: 14px;
    color: #909399;
    line-height: 1.5;
  }

  .result-btn {
    background: $primary-color;
    border-color: $primary-color;
    border-radius: 20px;
    padding: 10px 32px;
    font-size: 14px;
    font-weight: 500;

    &:hover {
      background: $primary-color-dark;
      border-color: $primary-color-dark;
    }
  }
}

// 空购物车提示
.empty-cart-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 60px 20px;
  text-align: center;

  .hint-title {
    margin: 20px 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #606266;
  }

  .hint-desc {
    margin: 0 0 24px;
    font-size: 14px;
    color: #909399;
  }

  .hint-btn {
    background: $primary-color;
    border-color: $primary-color;
    border-radius: 20px;
    padding: 10px 32px;
    font-size: 14px;
    font-weight: 500;

    &:hover {
      background: $primary-color-dark;
      border-color: $primary-color-dark;
    }
  }
}

// 分页组件
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;

  :deep(.el-pagination) {
    --el-pagination-bg-color: #{$card-bg};
    --el-pagination-button-bg-color: #{$card-bg};
    --el-pagination-hover-color: #{$primary-color};

    .el-pager li {
      border-radius: 6px;
      margin: 0 3px;
      font-weight: 500;

      &.is-active {
        background: $primary-color;
        color: #fff;
      }
    }

    .btn-prev,
    .btn-next {
      border-radius: 6px;
    }

    .el-pagination__sizes {
      .el-select {
        .el-input__wrapper {
          border-radius: 6px;
        }
        
        // 选中状态时输入框边框颜色
        &.is-focused .el-input__wrapper {
          border-color: $primary-color !important;
          box-shadow: 0 0 0 1px $primary-color inset !important;
        }
      }
    }

    .el-pagination__jump {
      .el-input__wrapper {
        border-radius: 6px;
        
        &:focus-within {
          border-color: $primary-color !important;
          box-shadow: 0 0 0 1px $primary-color inset !important;
        }
      }
    }
  }
}

// 右侧：结算概览卡片
.summary-panel {
  position: sticky;
  top: 100px;
  padding: 24px;
  background: $card-bg;
  border-radius: $card-radius;
  box-shadow: $shadow-strong;

  .summary-title {
    margin: 0 0 20px;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  // 已选商品缩略图预览
  .selected-preview {
    margin-bottom: 20px;

    .preview-scroll {
      display: flex;
      gap: 8px;
      overflow-x: auto;
      padding-bottom: 8px;

      &::-webkit-scrollbar {
        height: 4px;
      }

      &::-webkit-scrollbar-track {
        background: #f5f5f5;
        border-radius: 2px;
      }

      &::-webkit-scrollbar-thumb {
        background: #ddd;
        border-radius: 2px;
      }
    }

    .preview-thumb {
      flex-shrink: 0;
      width: 36px;
      height: 36px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
        border: 2px solid #f0f0f0;
      }
    }

    .preview-empty {
      font-size: 13px;
      color: #c0c4cc;
      margin: 0;
    }
  }

  // 分隔线
  .summary-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 16px 0;
  }

  // 价格明细
  .price-details {
    .detail-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .detail-label {
        font-size: 14px;
        color: #909399;
      }

      .detail-value {
        font-size: 14px;
        color: #606266;
      }
    }
  }

  // 应付总额
  .total-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .total-label {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }

    .total-value {
      font-size: 26px;
      font-weight: 700;
      color: $accent-color;
    }
  }

  // 结算按钮
  .checkout-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    background: $primary-color;
    border-color: $primary-color;
    border-radius: 24px;
    transition: all 0.2s ease;

    &:not(.is-disabled):hover {
      background: $primary-color-dark;
      border-color: $primary-color-dark;
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba($primary-color, 0.35);
    }

    &.is-disabled {
      background: #e0e0e0;
      border-color: #e0e0e0;
      color: #999;
    }
  }

}

// 响应式适配
@media (max-width: 1024px) {
  .cart-container {
    :deep(.el-row) {
      flex-direction: column;

      .el-col {
        max-width: 100%;
        flex: 0 0 100%;
      }
    }
  }

  .summary-panel {
    position: relative;
    top: 0;
    margin-top: 24px;
  }
}

@media (max-width: 768px) {
  .cart-page {
    padding: 16px 0 40px;
  }

  .cart-item-card {
    flex-wrap: wrap;
    padding: 16px;

    .item-checkbox {
      margin-right: 12px;
    }

    .item-image {
      width: 60px;
      height: 60px;
      margin-right: 12px;
    }

    .item-info {
      flex: 1;
      min-width: calc(100% - 120px);
      margin-right: 0;
      margin-bottom: 12px;
    }

    .item-price,
    .item-quantity,
    .item-subtotal {
      width: auto;
      flex: 1;
    }

    .item-action {
      width: auto;
    }
  }
}

// 数量修改模态框动画
.quantity-modal-enter-active,
.quantity-modal-leave-active {
  transition: all 0.3s ease;
}

.quantity-modal-enter-from,
.quantity-modal-leave-to {
  opacity: 0;
  
  .quantity-modal {
    transform: scale(0.9) translateY(-20px);
  }
}
</style>

<!-- 数量修改模态框样式 - 非 scoped -->
<style lang="scss">
.quantity-modal-overlay {
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

.quantity-modal {
  width: 420px;
  max-width: 90%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
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

.quantity-modal .modal-header {
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
      color: #FF8A5B;
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

.quantity-modal .modal-body {
  padding: 24px;
}

.quantity-modal .product-preview {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
  
  .preview-image {
    width: 80px;
    height: 80px;
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
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .preview-price {
    font-size: 18px;
    font-weight: 700;
    color: #FF8A5B;
    margin-top: auto;
  }
}

.quantity-modal .quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  .quantity-label {
    font-size: 15px;
    color: #333;
    font-weight: 500;
  }
  
  .qty-input-simple {
    width: 120px;
    height: 44px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    background: #fff;
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    outline: none;
    transition: all 0.2s ease;
    
    &::placeholder {
      color: #c0c4cc;
      font-weight: 400;
      font-size: 14px;
    }
    
    &:hover {
      border-color: #c0c4cc;
    }
    
    &:focus {
      border-color: #FF8A5B;
      box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1);
    }
  }
}

.quantity-modal .quantity-error {
  font-size: 12px;
  color: #F56C6C;
  text-align: center;
  margin: -8px 0 16px;
}

.quantity-modal .subtotal-row {
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
    color: #2F8F6A;
  }
}

.quantity-modal .modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
  
  .btn-cancel,
  .btn-confirm {
    flex: 1;
    height: 44px;
    font-size: 15px;
    border-radius: 22px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
  }
  
  .btn-cancel {
    background: #fff;
    border: 1px solid #dcdfe6;
    color: #606266;
    
    &:hover {
      background: #f5f7fa;
      border-color: #c0c4cc;
      color: #333;
    }
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
    border: none;
    color: #fff;
    box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
    }
    
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    
    .loading-spinner {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 分页下拉选择器样式（全局，因为 popper 渲染在 body 下）
.el-select__popper.el-popper {
  .el-select-dropdown__item {
    &.is-selected,
    &.selected {
      color: #FF8A5B !important;
      font-weight: 600;
    }
    
    &:hover {
      background-color: rgba(255, 138, 91, 0.08);
    }
    
    &.is-selected:hover,
    &.selected:hover {
      background-color: rgba(255, 138, 91, 0.12);
    }
  }
}

// 实名认证提示弹窗样式
.auth-tip-modal-overlay {
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
  padding: 20px;
}

.auth-tip-modal {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
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
        color: #e6a23c;
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
  
  .auth-tip-content {
    text-align: center;
    
    p {
      margin: 0 0 12px;
      font-size: 15px;
      color: #333;
      line-height: 1.6;
    }
    
    .auth-tip-hint {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    
    .btn-cancel,
    .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #E67A4D 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
    }
  }
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

.auth-tip-modal-enter-active,
.auth-tip-modal-leave-active {
  transition: all 0.3s ease;
}

.auth-tip-modal-enter-from,
.auth-tip-modal-leave-to {
  opacity: 0;
}

.auth-tip-modal-enter-from .auth-tip-modal,
.auth-tip-modal-leave-to .auth-tip-modal {
  transform: scale(0.9);
  opacity: 0;
}

// 信息缺失提示弹窗样式
.info-tip-modal-overlay {
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
  padding: 20px;
}

.info-tip-modal {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: infoModalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
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
        color: #e6a23c;
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
  
  .info-tip-content {
    text-align: center;
    
    p {
      margin: 0 0 12px;
      font-size: 15px;
      color: #333;
      line-height: 1.6;
    }
    
    .info-tip-hint {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    
    .btn-cancel,
    .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
    }
  }
}

@keyframes infoModalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.info-tip-modal-enter-active,
.info-tip-modal-leave-active {
  transition: all 0.3s ease;
}

.info-tip-modal-enter-from,
.info-tip-modal-leave-to {
  opacity: 0;
}

.info-tip-modal-enter-from .info-tip-modal,
.info-tip-modal-leave-to .info-tip-modal {
  transform: scale(0.9);
  opacity: 0;
}
</style>
