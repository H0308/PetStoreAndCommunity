<script setup>
import { ref, computed, onMounted, onUnmounted, inject, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowUp, Headset } from '@element-plus/icons-vue'
import ChatDrawer from './ChatDrawer.vue'
import { MESSAGE_TYPE_CHAT, MESSAGE_TYPE_MEDIA, MESSAGE_TYPE_PRODUCT, MESSAGE_TYPE_ORDER, checkUnreadMessages } from '../api/chat.js'

const props = defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false
  }
})

// 当前激活的抽屉
const activeDrawer = ref(null)

// 是否显示回到顶部按钮
const showBackTop = ref(false)

// 滚动进度百分比 (0-100)
const scrollProgress = ref(0)

// 是否有新消息（客服）
const hasNewMessage = ref(false)

// 获取路由
const route = useRoute()

// 注入 WebSocket
const webSocket = inject('webSocket', null)

// 从 App.vue 注入当前商品和订单信息
const currentProduct = inject('currentChatProduct', ref(null))
const currentOrder = inject('currentChatOrder', ref(null))

// SVG 圆环参数
const circleRadius = 21
const circleCircumference = 2 * Math.PI * circleRadius

// 计算圆环的 stroke-dashoffset
const progressOffset = computed(() => {
  return circleCircumference * (1 - scrollProgress.value / 100)
})

// 滚动监听
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  
  // 显示/隐藏按钮
  showBackTop.value = scrollTop > 500
  
  // 计算滚动进度
  if (docHeight > 0) {
    scrollProgress.value = Math.min(100, (scrollTop / docHeight) * 100)
  }
}

// 回到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 打开抽屉
const openDrawer = (mode) => {
  activeDrawer.value = mode
  if (mode === 'human') {
    hasNewMessage.value = false
  }
}

// 关闭抽屉
const closeDrawer = () => {
  activeDrawer.value = null
}

// 检查未读消息（客服消息）
const checkUnreadStatus = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return

    const userInfo = JSON.parse(userInfoStr)
    const userId = userInfo.userId
    if (!userId) return

    // 检查客服消息
    const chatUnread = await checkUnreadMessages(userId)
    hasNewMessage.value = chatUnread
  } catch (error) {
    // 静默处理错误，不影响用户体验
    console.error('检查未读消息失败:', error)
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll() // 初始化状态
  checkUnreadStatus() // 检查是否有未读消息
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听 WebSocket 消息，显示新消息红点
watch(
  () => webSocket?.messages?.value,
  (messages) => {
    if (!messages?.length) return
    const latestMessage = messages[messages.length - 1]
    // 处理聊天消息和媒体消息，且不是当前打开人工客服窗口时
    const isChatMessage = latestMessage?.type === MESSAGE_TYPE_CHAT ||
                          latestMessage?.type === MESSAGE_TYPE_MEDIA ||
                          latestMessage?.type === MESSAGE_TYPE_PRODUCT ||
                          latestMessage?.type === MESSAGE_TYPE_ORDER
    if (isChatMessage && activeDrawer.value !== 'human') {
      hasNewMessage.value = true
    }
  },
  { deep: true }
)

// 注入设置方法用于清除状态
const setChatProduct = inject('setChatProduct', null)
const setChatOrder = inject('setChatOrder', null)

// 监听路由变化，自动清除商品和订单信息
watch(() => route.path, () => {
  // 只有在非商品详情页和非订单详情页时才清除
  const isProductPage = route.path.startsWith('/products/') || route.path.startsWith('/product/')
  const isOrderPage = route.path.startsWith('/orders') || route.path.startsWith('/order/')

  if (!isProductPage && currentProduct.value && setChatProduct) {
    setChatProduct(null)
  }
  if (!isOrderPage && currentOrder.value && setChatOrder) {
    setChatOrder(null)
  }
})
</script>

<template>
  <div class="floating-sidebar">
    <!-- AI 宠物助手 -->
    <div 
      v-if="isLoggedIn"
      class="sidebar-item"
      :class="{ active: activeDrawer === 'ai' }"
      @click="openDrawer('ai')"
    >
      <div class="item-icon ai-icon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C9.5 2 7.5 4 7.5 6.5c0 .5.1 1 .2 1.5C5.5 8.5 4 10.5 4 13c0 3.3 2.7 6 6 6h.5c.3 1.2 1.4 2 2.5 2h2c1.1 0 2.2-.8 2.5-2h.5c3.3 0 6-2.7 6-6 0-2.5-1.5-4.5-3.7-5-.1-.5-.2-1-.2-1.5C20 4 18 2 15.5 2c-1.2 0-2.3.5-3.1 1.2C11.6 2.5 10.8 2 10 2h2zm-2 8c.8 0 1.5.7 1.5 1.5S10.8 13 10 13s-1.5-.7-1.5-1.5S9.2 10 10 10zm4 0c.8 0 1.5.7 1.5 1.5S14.8 13 14 13s-1.5-.7-1.5-1.5.7-1.5 1.5-1.5zm-2 4c1.7 0 3 .9 3 2h-6c0-1.1 1.3-2 3-2z"/>
        </svg>
      </div>
      <span class="item-tooltip">小橘助手</span>
    </div>

    <!-- 在线客服 -->
    <div
      v-if="isLoggedIn"
      class="sidebar-item"
      :class="{ active: activeDrawer === 'human' }"
      @click="openDrawer('human')"
    >
      <div class="item-icon">
        <el-icon :size="22"><Headset /></el-icon>
      </div>
      <span class="item-tooltip">联系客服</span>
      <span v-if="hasNewMessage" class="message-badge"></span>
    </div>

    <!-- 回到顶部（带进度环） -->
    <div 
      class="sidebar-item back-top-btn"
      :class="{ visible: showBackTop }"
      @click="scrollToTop"
    >
      <!-- SVG 进度环 -->
      <svg class="progress-ring" viewBox="0 0 48 48">
        <!-- 背景环 -->
        <circle
          class="progress-ring-bg"
          cx="24"
          cy="24"
          :r="circleRadius"
          fill="none"
          stroke="#EEEEEE"
          stroke-width="3"
        />
        <!-- 进度环 -->
        <circle
          class="progress-ring-circle"
          cx="24"
          cy="24"
          :r="circleRadius"
          fill="none"
          stroke="#FF8A5B"
          stroke-width="3"
          stroke-linecap="round"
          :stroke-dasharray="circleCircumference"
          :stroke-dashoffset="progressOffset"
        />
      </svg>
      <!-- 箭头图标 -->
      <div class="item-icon back-top-icon">
        <el-icon :size="20"><ArrowUp /></el-icon>
      </div>
      <span class="item-tooltip">回到顶部</span>
    </div>
  </div>

  <!-- 聊天抽屉 -->
  <ChatDrawer
    :visible="activeDrawer !== null"
    :mode="activeDrawer || 'ai'"
    :current-product="currentProduct"
    :current-order="currentOrder"
    @close="closeDrawer"
  />
</template>

<style scoped lang="scss">
.floating-sidebar {
  position: fixed;
  right: 20px;
  bottom: 100px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: var(--z-index-fixed);
}

.sidebar-item {
  position: relative;
  width: 48px;
  height: 48px;
  background: #FFFFFF;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    background: var(--color-primary);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 138, 91, 0.3);

    .item-icon {
      color: #FFFFFF;
    }

    .item-tooltip {
      opacity: 1;
      visibility: visible;
      transform: translateY(-50%) translateX(-4px);
    }
  }

  &.active {
    background: var(--color-primary);
    
    .item-icon {
      color: #FFFFFF;
    }
  }
}

.item-icon {
  color: #8F8F8F;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;

  &.ai-icon svg {
    width: 24px;
    height: 24px;
  }
}

.item-tooltip {
  position: absolute;
  right: calc(100% + 12px);
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.75);
  color: #FFFFFF;
  font-size: 12px;
  border-radius: 6px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  pointer-events: none;

  &::after {
    content: '';
    position: absolute;
    left: 100%;
    top: 50%;
    transform: translateY(-50%);
    border: 6px solid transparent;
    border-left-color: rgba(0, 0, 0, 0.75);
  }
}

.message-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

// 回到顶部按钮（带进度环）
.back-top-btn {
  border-radius: 50%;
  opacity: 0;
  pointer-events: none;
  transform: translateY(20px);
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);

  &.visible {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }

  &:hover {
    background: var(--color-primary);

    .back-top-icon {
      color: #FFFFFF;
    }

    .progress-ring-circle {
      stroke: rgba(255, 255, 255, 0.6);
    }

    .progress-ring-bg {
      stroke: rgba(255, 255, 255, 0.2);
    }
  }
}

.progress-ring {
  position: absolute;
  width: 48px;
  height: 48px;
  transform: rotate(-90deg);
}

.progress-ring-bg {
  transition: stroke 0.3s ease;
}

.progress-ring-circle {
  transition: stroke-dashoffset 0.1s ease, stroke 0.3s ease;
}

.back-top-icon {
  position: relative;
  z-index: 1;
  color: var(--color-primary);
  transition: color 0.3s ease;
}

// 响应式
@media (max-width: 768px) {
  .floating-sidebar {
    right: 12px;
    bottom: 80px;
  }

  .sidebar-item {
    width: 44px;
    height: 44px;
  }

  .progress-ring {
    width: 44px;
    height: 44px;
  }
}
</style>
