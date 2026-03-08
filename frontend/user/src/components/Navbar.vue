<script setup>
import { ref, onMounted, onUnmounted, computed, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, ShoppingCart, Bell, CaretBottom, Shop, Search, Close, Goods, Document, Loading, ArrowRight, VideoPlay } from '@element-plus/icons-vue'
import { CART_API, SEARCH_API, request } from '@/api/config'
import { fetchUnreadCount } from '@/api/notify.js'
import { MESSAGE_TYPE_SYSTEM } from '@/api/chat.js'

// Props
const props = defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => ({
      avatar: '',
      nickname: '游客'
    })
  }
})

// Emits
const emit = defineEmits(['openLogin', 'logout', 'navigate'])

// 使用路由
const route = useRoute()
const router = useRouter()

// 导航栏滚动状态
const isScrolled = ref(false)

// 搜索相关状态
const showSearchOverlay = ref(false)
const searchOverlayMouseDown = ref(false)
const searchKeyword = ref('')
const searchInputRef = ref(null)

// 搜索结果相关状态
const searchResults = ref([])
const searchLoading = ref(false)
const searchCurrentPage = ref(1)
const searchPageSize = ref(10)
const searchTotal = ref(0)
const searchTotalPages = ref(0)
const hasSearched = ref(false)
const lastSearchKeyword = ref('') // 记录实际执行搜索时的关键词

// 对象类型映射
const OBJECT_TYPE_MAP = {
  1: { label: '商品', icon: Goods, color: '#FF8A5B' },
  2: { label: '帖子', icon: Document, color: '#409EFF' }
}

// 子类型映射
const SUB_TYPE_MAP = {
  1: '宠物商品',
  2: '用品商品'
}

// 判断是否为首页
const isHomePage = computed(() => route.path === '/')

// 根据当前路由计算活跃菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return 'home'
  if (path === '/products' || path === '/pets' || path === '/supplies' || path.startsWith('/product/') || path === '/category' || path === '/search' || path === '/checkout' || path === '/cart' || path.startsWith('/order/') || path === '/payment-success') return 'products'
  if (path === '/forum' || path === '/community' || path.startsWith('/post/')) return 'community'
  if (path.startsWith('/profile')) return 'profile'
  return 'home'
})

// 监听滚动事件
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  // 页面加载时重置搜索状态（刷新页面后清空之前的搜索记录）
  resetSearchState()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 打开搜索遮罩
const openSearchOverlay = () => {
  if (!props.isLoggedIn) {
    ElMessage.warning('请先登录再操作')
    emit('openLogin')
    return
  }
  showSearchOverlay.value = true
  // 等待DOM更新后聚焦输入框
  setTimeout(() => {
    searchInputRef.value?.focus()
  }, 100)
}

// 关闭搜索遮罩（保留搜索记录）
const closeSearchOverlay = () => {
  showSearchOverlay.value = false
}

// 重置搜索状态
const resetSearchState = () => {
  searchKeyword.value = ''
  searchResults.value = []
  searchCurrentPage.value = 1
  searchTotal.value = 0
  searchTotalPages.value = 0
  hasSearched.value = false
}

// 执行搜索
const handleSearch = async () => {
  // 空白字符校验
  const trimmedKeyword = searchKeyword.value.trim()
  if (!trimmedKeyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  // 防止重复点击 - 如果正在搜索中则直接返回
  if (searchLoading.value) return

  searchCurrentPage.value = 1
  await performSearch()
}

// 执行搜索请求
const performSearch = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return

  searchLoading.value = true
  hasSearched.value = true
  lastSearchKeyword.value = keyword // 记录实际执行搜索时的关键词

  try {
    const response = await request(SEARCH_API.SEARCH, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        keyword: keyword,
        currentPage: searchCurrentPage.value,
        pageSize: searchPageSize.value
      })
    })

    const data = await response.json()

    if (data.code === 0) {
      searchResults.value = data.data?.totalRecords || []
      searchTotal.value = data.data?.totalCount || 0
      searchTotalPages.value = data.data?.totalPages || 0
    } else {
      searchResults.value = []
      searchTotal.value = 0
      searchTotalPages.value = 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
    searchTotal.value = 0
    searchTotalPages.value = 0
  } finally {
    searchLoading.value = false
  }
}

// 分页切换
const handlePageChange = (page) => {
  searchCurrentPage.value = page
  performSearch()
}

// 跳转到详情页
const goToDetail = (item) => {
  closeSearchOverlay()
  if (item.objectType === 1) {
    // 商品类型，需要根据subType带上type参数：1=宠物，2=用品
    const productType = item.subType || 1
    router.push({
      path: `/product/${item.objectId}`,
      query: { type: productType }
    })
  } else if (item.objectType === 2) {
    // 帖子类型：关闭搜索框，跳转到论坛页并带上postId参数
    router.push({
      path: '/forum',
      query: { postId: item.objectId }
    })
  }
}

// 键盘事件处理
const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    closeSearchOverlay()
  }
}

// 判断是否为视频（通过URL后缀）
const isVideoUrl = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.includes(ext))
}

// 判断是否为视频类型（优先使用objectMediaType，否则通过URL判断）
const isVideoMedia = (item) => {
  // 如果objectMediaType为2，确定为视频
  if (Number(item.objectMediaType) === 2) return true
  // 如果objectMediaType为null/undefined，通过URL后缀判断
  if (item.objectMediaType === null || item.objectMediaType === undefined) {
    return isVideoUrl(item.objectImageUrl)
  }
  return false
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  // 小于1小时显示"x分钟前"
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  }

  // 小于24小时显示"x小时前"
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }

  // 小于7天显示"x天前"
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }

  // 否则显示具体日期
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 导航菜单点击
const handleMenuClick = (menu) => {
  emit('navigate', menu)
}

// 记录进入个人中心前的页面路径
const saveCurrentPathBeforeProfile = () => {
  const currentPath = route.fullPath
  // 只有当前不在个人中心时才记录
  if (!currentPath.startsWith('/profile')) {
    sessionStorage.setItem('profilePreviousPath', currentPath)
  }
}

// 用户菜单操作
const handleCommand = (command) => {
  if (command === 'logout') {
    emit('logout')
  } else if (command === 'profile') {
    saveCurrentPathBeforeProfile()
    router.push('/profile/profile')
  } else if (command === 'orders') {
    saveCurrentPathBeforeProfile()
    router.push('/profile/orders')
  } else if (command === 'posts') {
    saveCurrentPathBeforeProfile()
    router.push('/profile/posts')
  }
}

// ===== 通知数量 =====
const notifyCount = ref(0)
const hasNewNotify = ref(false)

// 注入 WebSocket
const webSocket = inject('webSocket', null)

// 获取未读通知数量
const fetchNotifyUnreadCount = async () => {
  if (!props.isLoggedIn || !props.userInfo?.userId) {
    notifyCount.value = 0
    hasNewNotify.value = false
    return
  }

  try {
    const count = await fetchUnreadCount()
    notifyCount.value = count
    hasNewNotify.value = count > 0
  } catch (error) {
    console.error('获取未读通知数量失败:', error)
    notifyCount.value = 0
    hasNewNotify.value = false
  }
}

// 监听登录状态变化，及时更新通知数量
watch(() => props.isLoggedIn, (newVal) => {
  if (newVal) {
    fetchNotifyUnreadCount()
  } else {
    notifyCount.value = 0
    hasNewNotify.value = false
  }
}, { immediate: true })

// 监听 userInfo 变化（可能userId在登录后才获取到）
watch(() => props.userInfo?.userId, (newUserId) => {
  if (newUserId && props.isLoggedIn) {
    fetchNotifyUnreadCount()
  }
})

// 监听通知数量变化事件（从通知箱页面触发）
const handleNotifyCountChanged = () => {
  fetchNotifyUnreadCount()
}

onMounted(() => {
  window.addEventListener('notify-count-changed', handleNotifyCountChanged)
})

onUnmounted(() => {
  window.removeEventListener('notify-count-changed', handleNotifyCountChanged)
})

// 监听 WebSocket 消息，收到系统通知时更新红点
watch(
  () => webSocket?.messages?.value,
  (messages) => {
    if (!messages?.length) return
    const latestMessage = messages[messages.length - 1]
    // 收到系统通知消息
    if (latestMessage?.type === MESSAGE_TYPE_SYSTEM) {
      hasNewNotify.value = true
      notifyCount.value = (notifyCount.value || 0) + 1
    }
  },
  { deep: true }
)

// ===== 购物车数量 =====
const cartCount = ref(0)

// 获取购物车数量
const fetchCartCount = async () => {
  console.log('尝试获取购物车数量, isLoggedIn:', props.isLoggedIn, ', userId:', props.userInfo?.userId)
  
  if (!props.isLoggedIn || !props.userInfo?.userId) {
    cartCount.value = 0
    return
  }
  
  try {
    const url = `${CART_API.GET_CART_COUNT}?userId=${props.userInfo.userId}`
    console.log('请求购物车数量:', url)
    const response = await request(url, {
      headers: {}
    })
    const data = await response.json()
    console.log('购物车数量响应:', data)
    if (data.code === 0) {
      cartCount.value = data.data || 0
    } else {
      console.error('获取购物车数量失败:', data.message)
    }
  } catch (error) {
    console.error('获取购物车数量失败:', error)
    cartCount.value = 0
  }
}

// 监听登录状态变化，及时更新购物车数量
watch(() => props.isLoggedIn, (newVal) => {
  if (newVal) {
    fetchCartCount()
  } else {
    cartCount.value = 0
  }
}, { immediate: true })

// 监听 userInfo 变化（可能userId在登录后才获取到）
watch(() => props.userInfo?.userId, (newUserId) => {
  if (newUserId && props.isLoggedIn) {
    fetchCartCount()
  }
})

// 暴露方法供外部调用（如添加购物车后刷新数量）
defineExpose({
  fetchCartCount,
  fetchNotifyUnreadCount
})
</script>

<template>
  <header 
    class="navbar" 
    :class="{ 'navbar-scrolled': isScrolled }"
  >
    <div class="container-base">
      <div class="navbar-content">
        <!-- 左侧 Logo -->
        <div class="navbar-logo" @click="handleMenuClick('home')">
          <div class="logo-icon">
            <svg class="logo-svg" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
              <path d="M514.56 424.8832c-146.688 0-266.6752 192.6144-266.6752 266.6752 0 146.688 119.9872 148.1472 266.6752 148.1472 146.6624 0 266.6496-1.4848 266.6496-148.1472 0-74.0608-119.9872-266.6752-266.6496-266.6752z m0 355.584c-114.0736 0-207.4112 0-207.4112-88.9088 0-44.4416 93.3376-207.4112 207.4112-207.4112s207.3856 162.9696 207.3856 207.4112c0 103.7056-93.312 88.8832-207.3856 88.8832zM207.872 365.6192a86.5024 86.5024 0 0 0-86.656 86.656 86.5024 86.5024 0 0 0 86.656 86.656 86.5024 86.5024 0 0 0 86.6816-86.656 86.5024 86.5024 0 0 0-86.6816-86.656z m0 119.9872a33.024 33.024 0 0 1-33.3312-33.3312c0-18.688 14.6688-33.3312 33.3312-33.3312 18.688 0 33.3568 14.6688 33.3568 33.3312A33.024 33.024 0 0 1 207.872 485.632z m606.6688-106.6752a86.5024 86.5024 0 0 0-86.656 86.6816 86.5024 86.5024 0 0 0 86.656 86.656 86.5024 86.5024 0 0 0 86.6816-86.656 86.5024 86.5024 0 0 0-86.6816-86.6816z m0 120.0128a33.024 33.024 0 0 1-33.3312-33.3312c0-18.688 14.6688-33.3312 33.3312-33.3312 18.688 0 33.3312 14.6688 33.3312 33.3312a33.024 33.024 0 0 1-33.3312 33.3312z m-160-280.0128a86.5024 86.5024 0 0 0-86.656 86.6816 86.5024 86.5024 0 0 0 86.656 86.656 86.5024 86.5024 0 0 0 86.6816-86.656 86.5024 86.5024 0 0 0-86.6816-86.6816z m0 120.0128a33.024 33.024 0 0 1-33.3312-33.3312c0-18.688 14.6688-33.3312 33.3312-33.3312 18.688 0 33.3312 14.6688 33.3312 33.3312a33.024 33.024 0 0 1-33.3312 33.3312z m-253.312-133.3248c-54.6816 0-100.0192 45.312-100.0192 99.9936 0 54.656 45.3376 99.9936 100.0192 99.9936 54.656 0 99.9936-45.312 99.9936-99.9936s-45.3376-99.9936-99.9936-99.9936z m0 146.6624c-25.344 0-46.6688-21.3504-46.6688-46.6688 0-25.344 21.3248-46.6688 46.6688-46.6688s46.6432 21.3248 46.6432 46.6688-21.3248 46.6688-46.6432 46.6688z" fill="#FF8A5B" />
            </svg>
          </div>
          <div class="logo-text">小橘岛</div>
        </div>

        <!-- 中间导航菜单 -->
        <nav class="navbar-menu">
          <div 
            class="menu-item"
            :class="{ active: activeMenu === 'home' }"
            @click="handleMenuClick('home')"
          >
            首页
          </div>
          <div 
            class="menu-item"
            :class="{ active: activeMenu === 'products' }"
            @click="handleMenuClick('products')"
          >
            宠物商城
          </div>
          <div 
            class="menu-item"
            :class="{ active: activeMenu === 'community' }"
            @click="handleMenuClick('community')"
          >
            宠物贴吧
          </div>
        </nav>

        <!-- 右侧用户区域 -->
        <div class="navbar-actions">
          <!-- 搜索按钮 - 首页不显示 -->
          <div 
            v-if="!isHomePage" 
            class="action-item search-trigger" 
            @click="openSearchOverlay"
            title="搜索"
          >
            <el-icon :size="22"><Search /></el-icon>
          </div>

          <!-- 购物车 -->
          <div class="action-item" @click="router.push('/cart')">
            <el-badge :value="cartCount" :max="99" :hidden="cartCount === 0" class="cart-badge">
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </el-badge>
          </div>

                    <!-- 消息通知 -->
          <div class="action-item" @click="props.isLoggedIn ? (saveCurrentPathBeforeProfile(), router.push('/profile/notifications')) : (ElMessage.warning('请先登录再操作'), emit('openLogin'))">
            <el-badge :is-dot="hasNewNotify" :hidden="!hasNewNotify">
              <el-icon :size="24"><Bell /></el-icon>
            </el-badge>
          </div>

          <!-- 用户信息 -->
          <div class="user-section">
            <!-- 未登录状态 -->
            <div 
              v-if="!isLoggedIn" 
              class="login-trigger"
              @click="emit('openLogin')"
            >
              <el-icon :size="24" class="default-avatar"><User /></el-icon>
              <span class="login-text">您好，请登录</span>
            </div>

            <!-- 已登录状态 -->
            <el-dropdown v-else @command="handleCommand" trigger="click">
              <div class="user-info">
                <img 
                  v-if="userInfo.avatar"
                  :src="userInfo.avatar" 
                  class="user-avatar"
                  alt="用户头像"
                />
                <el-icon v-else :size="24" class="default-avatar"><User /></el-icon>
                <span class="user-nickname">{{ userInfo.nickname }}</span>
                <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item command="posts">我的帖子</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

  </header>

  <!-- 搜索遮罩层 - 使用 Teleport 传送到 body -->
  <Teleport to="body">
    <Transition name="search-overlay">
      <div 
        v-if="showSearchOverlay" 
        class="search-overlay"
        @mousedown.self="searchOverlayMouseDown = true"
        @mouseup.self="searchOverlayMouseDown && closeSearchOverlay(); searchOverlayMouseDown = false"
        @keydown="handleKeydown"
      >
        <div class="search-overlay-content" @mousedown="searchOverlayMouseDown = false">
          <!-- 搜索框容器 -->
          <div class="search-box-wrapper">
            <div class="search-row">
              <div class="search-box">
                <input
                  ref="searchInputRef"
                  v-model="searchKeyword"
                  type="text"
                  class="search-input"
                  placeholder="搜索宠物、用品、帖子..."
                  :disabled="searchLoading"
                  @keyup.enter="handleSearch"
                />
                <button
                  class="search-btn"
                  :class="{ 'is-loading': searchLoading }"
                  :disabled="searchLoading"
                  @click="handleSearch"
                >
                  <el-icon v-if="!searchLoading" :size="20"><Search /></el-icon>
                  <el-icon v-else :size="20" class="loading-icon"><Loading /></el-icon>
                  <span>{{ searchLoading ? '搜索中...' : '搜索' }}</span>
                </button>
              </div>
              <!-- 关闭按钮 -->
              <button class="search-close-btn" @click="closeSearchOverlay">
                <el-icon :size="24"><Close /></el-icon>
              </button>
            </div>
            <!-- 搜索结果区域 -->
            <div class="search-results-wrapper" v-if="hasSearched">
              <!-- 加载状态 -->
              <div v-if="searchLoading" class="search-loading">
                <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
                <span>搜索中...</span>
              </div>

              <!-- 搜索结果列表 -->
              <template v-else>
                <!-- 有结果 -->
                <div v-if="searchResults.length > 0" class="search-results-container">
                  <div class="search-results-header">
                    <span class="results-count">找到 {{ searchTotal }} 个结果</span>
                  </div>

                  <div class="search-results-list">
                    <div
                      v-for="item in searchResults"
                      :key="`${item.objectType}-${item.objectId}`"
                      class="search-result-item"
                      @click="goToDetail(item)"
                    >
                      <!-- 图片/视频缩略图 -->
                      <div class="result-image-wrapper">
                        <!-- 图片类型 -->
                        <template v-if="!isVideoMedia(item)">
                          <img
                            v-if="item.objectImageUrl"
                            :src="item.objectImageUrl"
                            :alt="item.objectName"
                            class="result-image"
                          />
                          <div v-else class="result-image-placeholder">
                            <el-icon :size="24">
                              <component :is="OBJECT_TYPE_MAP[item.objectType]?.icon || Goods" />
                            </el-icon>
                          </div>
                        </template>
                        <!-- 视频类型 - 使用视频第一帧作为缩略图 -->
                        <video
                          v-else
                          :src="`${item.objectImageUrl}#t=0.1`"
                          muted
                          preload="metadata"
                          class="result-image video-thumbnail"
                          @loadeddata="$event.target.currentTime = 0.1"
                        />
                        <!-- 视频标识 -->
                        <div v-if="isVideoMedia(item)" class="video-badge">
                          <el-icon :size="16"><VideoPlay /></el-icon>
                        </div>
                      </div>

                      <!-- 内容 -->
                      <div class="result-content">
                        <div class="result-title">{{ item.objectName }}</div>
                        <div class="result-meta">
                          <span
                            class="result-type-tag"
                            :style="{ backgroundColor: OBJECT_TYPE_MAP[item.objectType]?.color + '20', color: OBJECT_TYPE_MAP[item.objectType]?.color }"
                          >
                            {{ OBJECT_TYPE_MAP[item.objectType]?.label }}
                          </span>
                          <span v-if="item.objectType === 1 && item.subType" class="result-sub-type">
                            {{ SUB_TYPE_MAP[item.subType] }}
                          </span>
                          <span class="result-time">{{ formatTime(item.createTime) }}</span>
                        </div>
                      </div>

                      <!-- 箭头 -->
                      <div class="result-arrow">
                        <el-icon :size="16"><ArrowRight /></el-icon>
                      </div>
                    </div>
                  </div>

                  <!-- 分页 -->
                  <div class="search-pagination" v-if="searchTotalPages > 1">
                    <el-pagination
                      v-model:current-page="searchCurrentPage"
                      :page-size="searchPageSize"
                      :total="searchTotal"
                      layout="prev, pager, next"
                      @current-change="handlePageChange"
                    />
                  </div>
                </div>

                <!-- 无结果 -->
                <div v-else class="search-no-results">
                  <el-icon :size="48"><Search /></el-icon>
                  <p>未找到与 "{{ lastSearchKeyword }}" 相关的结果</p>
                  <span class="no-results-tip">请尝试更换关键词或检查拼写</span>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped lang="scss">
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-index-sticky);
  background: rgba(255, 248, 244, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid transparent;
  transition: background var(--transition-base) var(--ease-out),
              border-bottom-color var(--transition-base) var(--ease-out),
              box-shadow var(--transition-base) var(--ease-out),
              padding var(--transition-base) var(--ease-out);
  padding: var(--spacing-base) 0;

  &.navbar-scrolled {
    background: var(--color-bg-surface);
    border-bottom-color: var(--color-border-base);
    box-shadow: var(--shadow-sm);
    padding: var(--spacing-sm) 0;
  }
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-3xl);
}

// Logo
.navbar-logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  transition: transform var(--transition-base) var(--ease-out);

  &:hover {
    transform: scale(1.05);
  }

  .logo-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    
    .logo-svg {
      width: 32px;
      height: 32px;
    }
    
    .logo-image {
      width: 32px;
      height: 32px;
      object-fit: contain;
    }
    
    .el-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 1em;
        height: 1em;
        fill: currentColor;
      }
    }
  }

  .logo-text {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-primary);
    white-space: nowrap;
  }
}

// 导航菜单
.navbar-menu {
  display: flex;
  align-items: center;
  gap: var(--spacing-3xl);
  flex: 1;
  justify-content: center;

  .menu-item {
    position: relative;
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
    cursor: pointer;
    padding: var(--spacing-sm) var(--spacing-md);
    transition: all var(--transition-base) var(--ease-out);
    white-space: nowrap;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%) scaleX(0);
      width: 80%;
      height: 3px;
      background: var(--color-primary);
      border-radius: var(--radius-circle);
      transition: transform var(--transition-base) var(--ease-spring);
    }

    &:hover,
    &.active {
      color: var(--color-primary);

      &::after {
        transform: translateX(-50%) scaleX(1);
      }
    }
  }
}

// 右侧操作区
.navbar-actions {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-xl);

  .action-item {
    cursor: pointer;
    color: #333;
    transition: all var(--transition-base) var(--ease-out);
    padding: var(--spacing-sm);
    border-radius: var(--radius-base);
    display: flex;
    align-items: flex-end;
    justify-content: center;

    &:hover {
      color: var(--color-primary);
      background: var(--color-secondary-light);
      transform: translateY(-2px);
    }

    .el-icon {
      color: #333;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 1em;
        height: 1em;
        fill: currentColor;
      }
    }
  }

  // 搜索触发按钮
  .search-trigger {
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      right: -10px;
      top: 50%;
      transform: translateY(-50%);
      width: 1px;
      height: 20px;
      background: var(--color-border-base);
    }
  }

  .navbar-scrolled & .action-item {
    .el-icon {
      color: #333;
    }
  }
}

// 用户区域
.user-section {
  display: flex;
  align-items: flex-end;
  
  .login-trigger {
    display: flex;
    align-items: flex-end;
    gap: var(--spacing-sm);
    cursor: pointer;
    padding: var(--spacing-sm);
    border-radius: var(--radius-base);
    transition: all var(--transition-base) var(--ease-out);

    .default-avatar {
      color: #333;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      svg {
        width: 1em;
        height: 1em;
        fill: currentColor;
      }
    }

    .login-text {
      font-size: var(--font-size-sm);
      color: #333;
      font-weight: 500;
      white-space: nowrap;
      line-height: 24px;
    }

    &:hover {
      background: var(--color-secondary-light);

      .default-avatar {
        color: var(--color-primary);
      }

      .login-text {
        color: var(--color-primary);
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    cursor: pointer;
    padding: var(--spacing-xs) var(--spacing-base);
    border-radius: var(--radius-lg);
    transition: all var(--transition-base) var(--ease-out);

    &:hover {
      background: var(--color-secondary-light);
    }

    .user-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      object-fit: cover;
      border: 2px solid var(--color-border-light);
    }

    .user-nickname {
      font-size: var(--font-size-sm);
      font-weight: var(--font-weight-medium);
      color: var(--color-text-primary);
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dropdown-icon {
      color: var(--color-text-tertiary);
      font-size: 12px;
    }
  }
}



// 徽章样式
.action-item {
  :deep(.el-badge) {
    display: flex !important;
    align-items: flex-end;
  }
  
  .el-icon {
    position: relative;
  }
}

.cart-badge {
  :deep(.el-badge__content) {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
    position: absolute;
    top: 0;
    right: 0;
    transform: translate(50%, -50%);
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    padding: 0 4px;
    border-radius: 8px;
    text-align: center;
    font-size: 11px !important;
  }
}

// 响应式
@media (max-width: 992px) {
  .navbar-menu {
    gap: var(--spacing-lg);
  }

  .navbar-logo .logo-text {
    display: none;
  }
}

@media (max-width: 768px) {
  .navbar-menu {
    display: none;
  }

  .navbar-actions {
    gap: var(--spacing-md);
  }
}

:deep(.el-badge__content) {
  font-size: 12px !important;
  color: white !important;
  font-weight: 600 !important;
}
</style>

<!-- 下拉菜单全局样式 -->
<style lang="scss">
.el-dropdown__popper {
  border-radius: 12px !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1) !important;
  overflow: hidden;
  
  .el-dropdown-menu {
    padding: 8px 0 !important;
    border: none !important;
    
    .el-dropdown-menu__item {
      padding: 10px 20px !important;
      font-size: 14px !important;
      color: var(--color-text-primary) !important;
      transition: all 0.2s ease !important;
      
      &:hover {
        background: var(--color-secondary-light) !important;
        color: var(--color-primary) !important;
      }
      
      &.is-divided {
        border-top: 1px solid var(--color-border-light) !important;
        margin-top: 8px !important;
        padding-top: 10px !important;
        color: var(--color-primary) !important;
        
        &::before {
          display: none !important;
        }
        
        &:hover {
          background: rgba(255, 138, 91, 0.1) !important;
        }
      }
    }
  }
  
  // 隐藏箭头
  .el-popper__arrow {
    display: none !important;
  }
}
</style>

<!-- 搜索遮罩层样式 - 非 scoped，因为使用了 Teleport -->
<style lang="scss">
.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 15vh;
}

.search-overlay-content {
  position: relative;
  width: 100%;
  max-width: 800px;
  padding: 0 24px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-close-btn {
  flex-shrink: 0;
  width: 50px;
  height: 50px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: rotate(90deg);
  }
}

.search-box-wrapper {
  animation: searchSlideDown 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes searchSlideDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 50px;
  overflow: hidden;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.3);
}

.search-input {
  flex: 1;
  height: 60px;
  padding: 0 30px;
  border: none;
  outline: none;
  font-size: 18px;
  color: #333;
  background: transparent;

  &::placeholder {
    color: #999;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }
}

.search-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 48px;
  padding: 0 32px;
  margin: 6px;
  border: none;
  background: var(--color-primary);
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover:not(:disabled) {
    background: var(--color-primary-dark, #e65c00);
    transform: scale(1.02);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &.is-loading {
    background: var(--color-primary-dark, #e65c00);
  }

  .el-icon {
    color: #fff;
  }

  .loading-icon {
    animation: spin 1s linear infinite;
  }
}

.search-overlay-enter-active {
  animation: overlayFadeIn 0.3s ease;
}

.search-overlay-leave-active {
  animation: overlayFadeOut 0.25s ease;
}

@keyframes overlayFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes overlayFadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

@media (max-width: 768px) {
  .search-overlay {
    padding-top: 10vh;
  }

  .search-input {
    height: 50px;
    padding: 0 20px;
    font-size: 16px;
  }

  .search-btn {
    height: 40px;
    padding: 0 20px;
    font-size: 14px;

    span {
      display: none;
    }
  }
}

// 搜索结果样式
.search-results-wrapper {
  margin-top: 24px;
  margin-right: 66px; // 与搜索框对齐，减去关闭按钮宽度(50px)和间距(16px)
  background: #fff;
  border-radius: 16px;
  max-height: 60vh;
  overflow-y: auto;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);

  // 自定义滚动条样式 - Webkit 浏览器
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 16px;
    margin: 8px 0;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.15);
    border-radius: 3px;
    transition: background 0.3s ease;

    &:hover {
      background: rgba(0, 0, 0, 0.25);
    }
  }

  // Firefox 滚动条样式
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.15) transparent;
}

.search-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #666;
  gap: 16px;

  .loading-icon {
    animation: spin 1s linear infinite;
    color: var(--color-primary);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.search-results-container {
  padding: 20px;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;

  .results-count {
    font-size: 14px;
    color: #666;
  }
}

.search-results-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;

  &:hover {
    background: var(--color-secondary-light);
    transform: translateX(4px);
  }
}

.result-image-wrapper {
  position: relative;
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  background: #f0f0f0;

  .video-badge {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    color: #fff;

    .el-icon {
      color: #fff;
    }
  }
}

.result-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.result-image.video-thumbnail {
  object-fit: cover;
}

.result-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.result-type-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.result-sub-type {
  font-size: 12px;
  color: #666;
}

.result-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.result-arrow {
  flex-shrink: 0;
  color: #ccc;
  transition: color 0.3s ease;

  .search-result-item:hover & {
    color: var(--color-primary);
  }
}

.search-pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;

  // 覆盖 Element Plus 分页样式，匹配系统主题色
  :deep(.el-pagination) {
    --el-color-primary: #FF8A5B;
    --el-pagination-bg-color: #f5f5f5;
    --el-pagination-button-width: 36px;
    --el-pagination-button-height: 36px;
    --el-pagination-button-disabled-bg-color: #f5f5f5;
    --el-pagination-hover-color: #FF8A5B;
    --el-pagination-text-color: #666;
    --el-pagination-button-color: #666;

    .btn-prev,
    .btn-next {
      border-radius: 8px;
      background-color: #f5f5f5;
      border: none;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        color: #FF8A5B;
      }

      &:disabled {
        opacity: 0.5;
      }
    }

    .el-pager {
      li {
        border-radius: 8px;
        background-color: #f5f5f5;
        border: none;
        margin: 0 4px;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;
        color: #666;

        &:hover:not(.is-active) {
          color: #FF8A5B;
        }

        &.is-active {
          background-color: #FF8A5B;
          color: #fff;
          box-shadow: 0 2px 8px rgba(255, 138, 91, 0.4);
        }
      }
    }
  }
}

.search-no-results {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;

  p {
    font-size: 15px;
    color: #666;
    margin: 0;
  }

  .no-results-tip {
    font-size: 13px;
    color: #999;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .search-results-wrapper {
    max-height: 65vh;
    margin-top: 16px;
    margin-right: 66px; // 保持与搜索框对齐

    // 移动端隐藏滚动条但保留滚动功能
    &::-webkit-scrollbar {
      display: none;
    }
    scrollbar-width: none;
    -ms-overflow-style: none;
  }

  .search-results-container {
    padding: 16px;
  }

  .result-image-wrapper {
    width: 50px;
    height: 50px;
  }

  .result-title {
    font-size: 14px;
  }

  .result-time {
    display: none;
  }
}
</style>

<!-- 全局样式覆盖 Element Plus 分页组件 -->
<style lang="scss">
.search-pagination {
  .el-pagination {
    .el-pager {
      li {
        &.is-active {
          background-color: #FF8A5B !important;
          color: #fff !important;
          box-shadow: 0 2px 8px rgba(255, 138, 91, 0.4);
        }

        &:hover:not(.is-active) {
          color: #FF8A5B !important;
        }
      }
    }

    .btn-prev,
    .btn-next {
      &:hover:not(:disabled) {
        color: #FF8A5B !important;
      }
    }
  }
}
</style>
