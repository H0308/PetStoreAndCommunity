<script setup>
import { ref, computed, onMounted, provide, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'
import LoginModal from './components/LoginModal.vue'
import Footer from './components/Footer.vue'
import FloatingSideBar from './components/FloatingSideBar.vue'
import BackToTop from './components/BackToTop.vue'
import {
  getWebSocketUrl,
  MESSAGE_TYPE_CHAT,
  MESSAGE_TYPE_STATUS,
  ONLINE_STATUS
} from './api/chat.js'

const router = useRouter()
const route = useRoute()

// Navbar 引用
const navbarRef = ref(null)

// 判断是否为首页（首页有自己的页脚）
const isLandingPage = computed(() => route.path === '/')

// 登录状态
const isLoggedIn = ref(false)
const userInfo = ref({
  userId: null,
  avatar: '',
  nickname: '游客',
  roleId: null,
  token: null
})

// 登录弹窗
const showLoginModal = ref(false)

// 计算并设置滚动条宽度 CSS 变量，用于修复模态框打开时导航栏偏移问题
const setScrollbarWidth = () => {
  const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth
  document.documentElement.style.setProperty('--scrollbar-width', `${scrollbarWidth}px`)
}

// 页面加载时检查登录状态
onMounted(() => {
  // 设置滚动条宽度
  setScrollbarWidth()
  // 监听窗口大小变化，重新计算滚动条宽度
  window.addEventListener('resize', setScrollbarWidth)
  // 监听 request.js 触发的 403 全局登录事件
  window.addEventListener('open-login', openLogin)

  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      if (user && user.userId) {
        isLoggedIn.value = true
        userInfo.value = {
          userId: user.userId,
          avatar: user.avatar || '',
          nickname: user.username,
          roleId: user.roleId,
          token: user.token
        }
        // 恢复登录后立即建立 WebSocket 连接
        initWebSocket()
      }
    } catch (e) {
      console.error('解析用户信息失败:', e)
      localStorage.removeItem('userInfo')
    }
  }
})

// 打开登录弹窗
const openLogin = () => {
  showLoginModal.value = true
}

// 登录成功
const handleLoginSuccess = (user) => {
  isLoggedIn.value = true
  userInfo.value = {
    userId: user.userId,
    avatar: user.avatar || '',
    nickname: user.nickname,
    roleId: user.roleId,
    token: user.token
  }
  // 登录成功后建立 WebSocket 连接
  initWebSocket()
}

// 退出登录
const handleLogout = async () => {
  // 调用后端登出接口，将 token 加入黑名单
  try {
    const token = userInfo.value?.token
    if (token) {
      await fetch('http://localhost:8080/api/user/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      })
    }
  } catch {}
  // 先关闭 WebSocket 连接
  closeWebSocket()
  isLoggedIn.value = false
  userInfo.value = { userId: null, avatar: '', nickname: '游客', roleId: null, token: null }
  localStorage.removeItem('userInfo')
  localStorage.removeItem('token')
  router.push('/')
}

// 导航处理
const handleNavigate = (menu) => {
  const routeMap = {
    'home': '/',
    'products': '/products',
    'community': '/forum',
    'profile': '/profile',
    'cart': '/cart',
    'messages': '/profile/notifications'
  }
  if (routeMap[menu]) {
    router.push(routeMap[menu])
  }
}

// 刷新购物车数量（供子组件调用）
const refreshCartCount = () => {
  navbarRef.value?.fetchCartCount()
}

// 更新用户信息（供子组件调用，如头像、用户名等）
const updateUserInfo = (updates) => {
  if (updates.avatar !== undefined) userInfo.value.avatar = updates.avatar
  if (updates.nickname !== undefined) userInfo.value.nickname = updates.nickname
  // 同步更新 localStorage
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      if (updates.avatar !== undefined) user.avatar = updates.avatar
      if (updates.nickname !== undefined) user.username = updates.nickname
      localStorage.setItem('userInfo', JSON.stringify(user))
    } catch (e) {
      console.error('更新本地用户信息失败:', e)
    }
  }
}

// 通过 provide 向所有子组件提供刷新购物车数量的方法
provide('refreshCartCount', refreshCartCount)
provide('updateUserInfo', updateUserInfo)
// 提供全局打开登录弹窗的方法，供 request.js 403 拦截使用
provide('openLogin', openLogin)

// ============ 聊天商品/订单状态管理 ============
const currentChatProduct = ref(null)
const currentChatOrder = ref(null)

const setChatProduct = (product) => {
  console.log('[App] setChatProduct called:', product)
  currentChatProduct.value = product
  // 设置商品时清空订单
  if (product) {
    currentChatOrder.value = null
  }
}

const setChatOrder = (order) => {
  console.log('[App] setChatOrder called:', order)
  currentChatOrder.value = order
  // 设置订单时清空商品
  if (order) {
    currentChatProduct.value = null
  }
}

// 提供给子组件使用
provide('setChatProduct', setChatProduct)
provide('setChatOrder', setChatOrder)
provide('currentChatProduct', currentChatProduct)
provide('currentChatOrder', currentChatOrder)

// ============ WebSocket 连接管理 ============
let ws = null
const wsStatus = ref('disconnected') // disconnected, connecting, connected
const wsMessages = ref([]) // 存储接收到的消息
const RECONNECT_INTERVAL = 5000
let reconnectTimer = null

// 初始化 WebSocket 连接
const initWebSocket = () => {
  // 只有在登录状态下才连接
  if (!isLoggedIn.value || !userInfo.value.token) {
    return
  }

  // 避免重复连接
  if (ws?.readyState === WebSocket.OPEN || ws?.readyState === WebSocket.CONNECTING) {
    return
  }

  wsStatus.value = 'connecting'

  try {
    ws = new WebSocket(getWebSocketUrl(userInfo.value.token))

    ws.onopen = () => {
      wsStatus.value = 'connected'
      if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
      }
    }

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        handleWebSocketMessage(message)
      } catch (error) {
        console.error('[App] 解析消息失败:', error)
      }
    }

    ws.onerror = (error) => {
      console.error('[App] WebSocket 错误:', error)
      wsStatus.value = 'disconnected'
    }

    ws.onclose = () => {
      wsStatus.value = 'disconnected'
      // 如果仍在登录状态，自动重连
      if (isLoggedIn.value) {
        scheduleReconnect()
      }
    }
  } catch (error) {
    console.error('[App] WebSocket 初始化失败:', error)
    wsStatus.value = 'disconnected'
    if (isLoggedIn.value) {
      scheduleReconnect()
    }
  }
}

// 处理收到的消息
const handleWebSocketMessage = (message) => {
  // 将消息添加到消息列表（供子组件监听）
  wsMessages.value.push(message)

  // 限制消息列表长度
  if (wsMessages.value.length > 1000) {
    wsMessages.value = wsMessages.value.slice(-500)
  }
}

// 发送消息
const sendWebSocketMessage = (message) => {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    console.error('[App] WebSocket 未连接')
    return false
  }
  try {
    ws.send(JSON.stringify(message))
    return true
  } catch (error) {
    console.error('[App] 发送消息失败:', error)
    return false
  }
}

// 计划重连
const scheduleReconnect = () => {
  if (reconnectTimer) return
  reconnectTimer = setTimeout(() => {
    initWebSocket()
  }, RECONNECT_INTERVAL)
}

// 关闭 WebSocket
const closeWebSocket = () => {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
  wsStatus.value = 'disconnected'
}

// 提供给子组件使用
provide('webSocket', {
  status: wsStatus,
  messages: wsMessages,
  sendMessage: sendWebSocketMessage,
  initWebSocket,
  closeWebSocket
})

// 组件卸载时清理
onUnmounted(() => {
  closeWebSocket()
  window.removeEventListener('open-login', openLogin)
})
</script>

<template>
  <div class="app">
    <!-- 全局导航栏 -->
    <Navbar 
      ref="navbarRef"
      :is-logged-in="isLoggedIn"
      :user-info="userInfo"
      @open-login="openLogin"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />

    <!-- 主要内容 - 使用路由视图 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <keep-alive :include="['ProductsPage', 'LandingPage', 'MyOrders', 'Favorites']">
          <component 
            :is="Component" 
            :is-logged-in="isLoggedIn"
            :user-info="userInfo"
            @start-shopping="() => router.push('/products')"
            @navigate="handleNavigate"
            @open-login="openLogin"
          />
        </keep-alive>
      </router-view>
    </main>

    <!-- 全局页脚（首页除外，首页有自己的页脚） -->
    <Footer v-if="!isLandingPage" />

    <!-- 登录/注册模态框 -->
    <LoginModal 
      v-model:visible="showLoginModal"
      @login-success="handleLoginSuccess"
    />

    <!-- 全局悬浮侧边栏（首页不显示） -->
    <FloatingSideBar v-if="!isLandingPage" :is-logged-in="isLoggedIn" />
    
    <!-- 返回顶部按钮（首页单独显示） -->
    <BackToTop v-if="isLandingPage" />
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  width: 100%;
}

.main-content {
  width: 100%;
  /* 导航栏高度的padding，避免内容被遮挡 */
  padding-top: var(--header-height);
}
</style>
