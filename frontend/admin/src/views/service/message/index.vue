<template>
  <div class="customer-service-container" v-bind="$attrs">
    <!-- 左侧用户列表（简化版） -->
    <div class="user-list-panel">
      <!-- 连接状态 -->
      <div class="connection-status" :class="connectionStatus">
        <ElTag :type="connectionStatus === 'connected' ? 'success' : connectionStatus === 'connecting' ? 'warning' : 'danger'" size="small">
          <ArtSvgIcon :icon="connectionStatus === 'connected' ? 'ri:wifi-line' : connectionStatus === 'connecting' ? 'ri:loader-4-line' : 'ri:wifi-off-line'" class="mr-1" />
          {{ connectionStatusText }}
        </ElTag>
      </div>

      <!-- 搜索框 -->
      <div class="search-box">
        <ElInput
          v-model="searchKeyword"
          placeholder="搜索用户名"
          clearable
          size="small"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <ArtSvgIcon icon="ri:search-line" class="text-g-400" />
          </template>
        </ElInput>
      </div>

      <!-- 用户列表 -->
      <div class="user-list-content">
        <div
          v-for="user in filteredUserList"
          :key="user.userId"
          :class="['user-item', { active: currentUser?.userId === user.userId }]"
          @click="handleSelectUser(user)"
        >
          <div class="user-avatar-wrapper">
            <ElAvatar :src="user.avatarUrl" :size="44" class="user-avatar">
              <template #default>
                <ArtSvgIcon icon="ri:user-line" class="text-lg" />
              </template>
            </ElAvatar>
            <span v-if="user.online" class="online-status"></span>
          </div>
          <div class="user-info">
            <div class="user-name-row">
              <span class="user-name">{{ user.username }}</span>
              <ElTag v-if="user.roleId === 0" size="small" type="danger" class="role-tag">管理员</ElTag>
              <ElTag v-else size="small" type="info" class="role-tag">用户</ElTag>
              <span v-if="user.lastTime" class="user-time">{{ user.lastTime }}</span>
            </div>
            <div class="user-message-row">
              <span v-if="user.lastMessage" class="user-last-message">{{ user.lastMessage }}</span>
              <ElBadge v-if="user.unreadCount > 0" :value="user.unreadCount > 99 ? '99+' : user.unreadCount" class="unread-badge" />
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredUserList.length === 0" class="empty-state">
          <ArtSvgIcon icon="ri:chat-off-line" class="text-4xl text-g-300 mb-2" />
          <span class="text-g-400">等待用户连接</span>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-panel">
      <template v-if="currentUser">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-user-info">
            <ElAvatar :src="currentUser.avatarUrl" :size="36">
              <template #default>
                <ArtSvgIcon icon="ri:user-line" class="text-base" />
              </template>
            </ElAvatar>
            <div class="chat-user-detail">
              <span class="chat-user-name">{{ currentUser.username }}</span>
              <ElTag v-if="currentUser.online" type="success" size="small" class="online-tag">
                在线
              </ElTag>
              <ElTag v-else type="info" size="small" class="offline-tag">离线</ElTag>
            </div>
          </div>
          <div class="chat-actions">
            <ElButton type="info" text @click="handleViewUserDetail">
              <ArtSvgIcon icon="ri:user-line" class="mr-1" />
              查看资料
            </ElButton>
          </div>
        </div>

        <!-- 消息列表（基于WebSocket的实时通信，不保存历史） -->
        <div ref="messageContainerRef" class="message-list" :class="{ 'messages-hidden': !isMessagesReady }" @scroll="handleMessagesScroll">
          <!-- 加载更多提示 -->
          <div v-if="loadingMore" class="loading-more">
            <ElIcon class="loading-icon"><Loading /></ElIcon>
            <span>加载中...</span>
          </div>
          <div v-else-if="hasMoreMessages && currentUser" class="load-more-hint">
            <span>向上滑动加载更多</span>
          </div>

          <div
            v-for="(msg, index) in messageList"
            :key="msg.id || index"
            :class="['message-item', { self: isSelfMessage(msg) }]"
          >
            <!-- 时间分隔线 -->
            <div v-if="shouldShowTime(index)" class="time-divider">
              <span>{{ formatMessageTime(msg.createTime) }}</span>
            </div>

            <!-- 消息内容 -->
            <div class="message-content">
              <ElAvatar
                v-if="!isSelfMessage(msg)"
                :src="currentUser.avatarUrl"
                :size="36"
                class="message-avatar"
              >
                <template #default>
                  <ArtSvgIcon icon="ri:user-line" class="text-sm" />
                </template>
              </ElAvatar>

              <!-- 商品卡片消息 -->
              <div v-if="msg.type === MESSAGE_TYPE_PRODUCT && msg.product" class="card-message" @click="handleViewProduct(msg.product.productId)">
                <div class="card-header">
                  <ArtSvgIcon icon="ri:shopping-bag-line" class="card-icon" />
                  <span class="card-title">商品信息</span>
                </div>
                <div class="card-content">
                  <img v-if="msg.product.image" :src="msg.product.image" class="card-image" alt="商品图片" />
                  <div class="card-info">
                    <p class="card-name">{{ msg.product.name }}</p>
                    <p class="card-price">¥{{ msg.product.price }}</p>
                  </div>
                </div>
                <div class="card-footer">
                  <span class="card-action">点击查看详情 →</span>
                </div>
              </div>

              <!-- 订单卡片消息 -->
              <div v-else-if="msg.type === MESSAGE_TYPE_ORDER && msg.order" class="card-message order" @click="handleViewOrder(msg.order.orderId)">
                <div class="card-header">
                  <ArtSvgIcon icon="ri:file-list-line" class="card-icon" />
                  <span class="card-title">订单信息</span>
                </div>
                <div class="card-content">
                  <div class="order-info-with-image">
                    <img v-if="msg.order.imgUrl" :src="msg.order.imgUrl" class="order-product-image" alt="商品图片" />
                    <div v-else class="order-product-image placeholder">
                      <ArtSvgIcon icon="ri:image-line" class="placeholder-icon" />
                    </div>
                    <div class="order-details">
                      <p class="order-id">订单号：{{ msg.order.orderId }}</p>
                      <p class="order-product">{{ msg.order.productName || '多件商品' }}</p>
                      <p class="order-quantity">x{{ msg.order.totalCount || 1 }}</p>
                      <p class="order-amount">¥{{ msg.order.totalAmount }}</p>
                    </div>
                  </div>
                </div>
                <div class="card-footer">
                  <span class="order-status" :class="'status-' + msg.order.status">{{ getOrderStatusText(msg.order.status) }}</span>
                  <span class="card-action">点击查看详情 →</span>
                </div>
              </div>

              <!-- 媒体消息：无气泡背景 -->
              <div v-else-if="msg.type === MESSAGE_TYPE_MEDIA || msg.mediaUrl" class="media-message">
                <!-- 视频：显示视频封面+播放按钮 -->
                <div v-if="isVideoUrl(msg.mediaUrl)" class="video-thumbnail-wrapper" @click="openVideoPlayer(msg.mediaUrl)">
                  <video :src="msg.mediaUrl" class="video-thumbnail" preload="metadata" muted />
                  <div class="video-play-overlay">
                    <ArtSvgIcon icon="ri:play-fill" class="play-icon" />
                  </div>
                </div>
                <img v-else :src="msg.mediaUrl" alt="图片" class="media-image" />
              </div>

              <!-- 文字消息：有气泡背景 -->
              <div
                v-else
                :class="['message-bubble', { self: isSelfMessage(msg) }]"
              >
                <div class="message-text">{{ msg.message }}</div>
              </div>

              <ElAvatar
                v-if="isSelfMessage(msg)"
                :src="userStore.info?.avatar"
                :size="36"
                class="message-avatar"
              >
                <template #default>
                  <ArtSvgIcon icon="ri:customer-service-2-line" class="text-sm" />
                </template>
              </ElAvatar>
            </div>
          </div>

          <!-- 空消息状态 -->
          <div v-if="messageList.length === 0 && !messageLoading" class="empty-messages">
            <ArtSvgIcon icon="ri:chat-smile-2-line" class="text-5xl text-g-300 mb-3" />
            <span class="text-g-400">开始与用户对话</span>
          </div>
        </div>

        <!-- 消息输入区 -->
        <div class="message-input-area">
          <!-- 文件预览 -->
          <div v-if="filePreviewUrl" class="file-preview">
            <!-- 视频预览：显示缩略图+播放按钮 -->
            <div v-if="selectedFile && selectedFile.type.startsWith('video/')" class="preview-video-wrapper" @click="openVideoPlayer(filePreviewUrl)">
              <video :src="filePreviewUrl" class="preview-video" preload="metadata" muted />
              <div class="preview-play-overlay">
                <ArtSvgIcon icon="ri:play-fill" class="preview-play-icon" />
              </div>
            </div>
            <img v-else :src="filePreviewUrl" alt="预览" />
            <button class="remove-file-btn" @click="removeSelectedFile">
              <ArtSvgIcon icon="ri:close-line" class="text-xs" />
            </button>
          </div>
          <div class="input-toolbar">
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*,video/*"
              style="display: none"
              @change="handleFileSelect"
            />
            <ElTooltip content="发送图片或视频" placement="top">
              <ElButton text circle @click="triggerFileSelect">
                <ArtSvgIcon icon="ri:image-line" class="text-lg" />
              </ElButton>
            </ElTooltip>
          </div>
          <div class="input-box">
            <ElInput
              v-model="messageText"
              type="textarea"
              :rows="3"
              :maxlength="2000"
              show-word-limit
              :placeholder="isUploading ? '上传中...' : '请输入回复内容...'"
              resize="none"
              class="message-textarea"
              :disabled="isUploading"
              @keydown.enter.prevent="handleSendMessage"
            />
          </div>
          <div class="input-actions">
            <span class="input-tip">按 Enter 发送，Ctrl + Enter 换行（最多2000字）</span>
            <ElButton
              type="primary"
              :disabled="(!messageText.trim() && !selectedFile) || connectionStatus !== 'connected' || isUploading"
              :loading="sending || isUploading"
              @click="handleSendMessage"
              v-ripple
            >
              <ArtSvgIcon icon="ri:send-plane-line" class="mr-1" />
              发送
            </ElButton>
          </div>
        </div>
      </template>

      <!-- 未选择用户时的空状态 -->
      <div v-else class="no-user-selected">
        <div class="empty-content">
          <ArtSvgIcon icon="ri:customer-service-2-line" class="text-6xl text-g-300 mb-4" />
          <h3 class="text-lg font-medium text-g-600 mb-2">客服中心</h3>
          <p class="text-g-400">请从左侧选择用户开始对话</p>
          <ElButton v-if="connectionStatus !== 'connected'" type="primary" class="mt-4" @click="reconnect">
            重新连接
          </ElButton>
        </div>
      </div>
    </div>
  </div>

  <!-- 视频播放器全屏覆盖层 -->
  <div v-if="showVideoPlayer" class="video-player-overlay" @click.self="closeVideoPlayer">
    <div class="video-player-container">
      <button class="video-close-btn" @click="closeVideoPlayer">
        <ArtSvgIcon icon="ri:close-line" />
      </button>
      <video
        v-if="currentVideoUrl"
        ref="adminVideoRef"
        :src="currentVideoUrl"
        class="video-player"
        controls
        autoplay
        @loadedmetadata="handleVideoMetadata"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { ElMessage, ElIcon } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  MESSAGE_TYPE_CHAT,
  MESSAGE_TYPE_MEDIA,
  MESSAGE_TYPE_STATUS,
  MESSAGE_TYPE_PRODUCT,
  MESSAGE_TYPE_ORDER,
  ONLINE_STATUS,
  OFFLINE_STATUS,
  readMessages,
  uploadMedia,
  fetchLatestMessages,
  fetchHistoryMessages,
  fetchUserInfo,
  type CommonMessage,
  type Message,
  type LatestMessage
} from '@/api/chat'
import { inject, computed, ref, watch, nextTick, onActivated, type Ref, type ComputedRef } from 'vue'
import { useRouter } from 'vue-router'

// 简化的用户信息类型
interface SimpleUser {
  userId: number
  username: string
  avatarUrl?: string
  online: boolean
  unreadCount: number
  roleId?: number       // 0=管理员, 1=普通用户
  lastMessage?: string  // 最新消息内容
  lastTime?: string     // 最新消息时间
}

defineOptions({ name: 'MessageReply', inheritAttrs: false })

const userStore = useUserStore()
const router = useRouter()

// ============ WebSocket (从 App.vue 注入) ============
const webSocket = inject<{
  status: Ref<'disconnected' | 'connecting' | 'connected'>
  messages: Ref<CommonMessage[]>
  sendMessage: (message: CommonMessage) => Promise<boolean>
  initWebSocket: () => void
  closeWebSocket: () => void
  currentUserId: ComputedRef<number | undefined>
}>('webSocket', {
  status: ref('disconnected'),
  messages: ref([]),
  sendMessage: async () => false,
  initWebSocket: () => {},
  closeWebSocket: () => {},
  currentUserId: computed(() => undefined)
})

// 连接状态
const connectionStatus = computed(() => webSocket.status.value)
const connectionStatusText = computed(() => {
  const map = {
    disconnected: '未连接',
    connecting: '连接中...',
    connected: '已连接'
  }
  return map[connectionStatus.value]
})

// ============ 状态管理 ============
const sending = ref(false)
const messageLoading = ref(false)

// 当前选中的用户
const currentUser = ref<SimpleUser | null>(null)

// 用户列表（简化版，仅基于WebSocket通信）
const userList = ref<SimpleUser[]>([])

// 每个用户的消息列表 Map<userId, Message[]>
const userMessagesMap = ref<Map<number, Message[]>>(new Map())

// 当前对话的消息列表（根据当前选中用户获取）
const messageList = computed<Message[]>({
  get: () => {
    if (!currentUser.value) return []
    return userMessagesMap.value.get(currentUser.value.userId) || []
  },
  set: (value: Message[]) => {
    if (!currentUser.value) return
    userMessagesMap.value.set(currentUser.value.userId, value)
  }
})

// 分页相关状态
const currentPage = ref(1)
const totalPages = ref(1)
const loadingMore = ref(false)
const hasMoreMessages = computed(() => currentPage.value < totalPages.value)

// 每个用户的分页状态 Map<userId, { currentPage, totalPages }>
const userPaginationMap = ref<Map<number, { currentPage: number; totalPages: number }>>(new Map())

// 消息容器引用
const messageContainerRef = ref<HTMLElement>()

// 消息区域是否准备好显示（避免闪烁）
const isMessagesReady = ref(false)

// 消息输入文本
const messageText = ref('')

// 媒体消息相关
const selectedFile = ref<File | null>(null)
const filePreviewUrl = ref<string | null>(null)
const fileInputRef = ref<HTMLInputElement>()
const isUploading = ref(false)

// 搜索相关
const searchKeyword = ref('')

// 视频播放器相关
const showVideoPlayer = ref(false)
const currentVideoUrl = ref('')
const adminVideoRef = ref<HTMLVideoElement>()
const videoAspectRatio = ref(16 / 9) // 默认16:9比例
const maxVideoWidth = 800 // 最大宽度
const maxVideoHeight = 600 // 最大高度

// 视频播放器样式（根据比例计算）
const videoPlayerStyle = computed(() => {
  let width = maxVideoWidth
  let height = width / videoAspectRatio.value

  // 如果高度超过最大值，则以高度为基准
  if (height > maxVideoHeight) {
    height = maxVideoHeight
    width = height * videoAspectRatio.value
  }

  return {
    width: `${width}px`,
    height: `${height}px`,
    maxWidth: '90vw',
    maxHeight: '80vh'
  }
})

/**
 * 打开视频播放器
 */
const openVideoPlayer = (url: string) => {
  currentVideoUrl.value = url
  showVideoPlayer.value = true
  // 重置比例，等待视频加载后获取实际比例
  videoAspectRatio.value = 16 / 9
}

/**
 * 关闭视频播放器
 */
const closeVideoPlayer = () => {
  showVideoPlayer.value = false
  currentVideoUrl.value = ''
}

/**
 * 处理视频元数据加载完成
 */
const handleVideoMetadata = () => {
  const video = adminVideoRef.value
  if (video && video.videoWidth && video.videoHeight) {
    videoAspectRatio.value = video.videoWidth / video.videoHeight
  }
}

/**
 * 格式化时间显示
 * 今天显示 HH:mm，昨天显示"昨天"，其他显示 MM-DD
 */
const formatTime = (timeStr: string): string => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate())

  if (messageDate.getTime() === today.getTime()) {
    // 今天，显示时分
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (messageDate.getTime() === yesterday.getTime()) {
    // 昨天
    return '昨天'
  } else {
    // 其他日期，显示月-日
    return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  }
}

// ============ 计算属性 ============

// 用户列表（搜索由后端处理，前端直接使用返回结果）
const filteredUserList = computed(() => userList.value)

// 当前用户ID
const currentUserId = computed(() => webSocket.currentUserId?.value || userStore.info?.userId)

/**
 * 处理接收到的消息
 */
const handleReceivedMessage = (message: CommonMessage) => {
  console.log('[Message] 收到消息:', message)

  if (!message || !message.type) {
    console.warn('[Message] 收到无效消息:', message)
    return
  }

  // 状态消息处理
  if (message.type === MESSAGE_TYPE_STATUS) {
    updateUserOnlineStatus(message.senderId!, message.onlineStatus === ONLINE_STATUS)
    return
  }

  // 聊天消息和媒体消息处理
  if (message.type === MESSAGE_TYPE_CHAT || message.type === MESSAGE_TYPE_MEDIA) {
    // 检查是否是用户发送的消息（不是管理员自己发的）
    const isFromUser = message.senderId !== currentUserId.value

    // 如果用户不在列表中，自动添加到列表
    if (isFromUser) {
      const existingUserIndex = userList.value.findIndex((u) => u.userId === message.senderId)
      if (existingUserIndex === -1) {
        // 新用户，添加到列表顶部并自动选中
        const newUser: SimpleUser = {
          userId: message.senderId!,
          username: message.senderName || `用户${message.senderId}`,
          avatarUrl: message.senderAvatar,
          online: true,
          unreadCount: 0,
          lastMessage: message.type === MESSAGE_TYPE_MEDIA ? '[媒体文件]' : (message.content || ''),
          lastTime: formatTime(new Date().toISOString())
        }
        userList.value.unshift(newUser)
        // 自动选中新用户并显示聊天窗口
        handleSelectUser(newUser)
        // 刷新用户列表以获取完整的用户信息（包括头像）
        loadLatestMessages()
      } else {
        // 用户已存在，将其置顶（像微信那样）
        const existingUser = userList.value[existingUserIndex]
        // 从当前位置移除
        userList.value.splice(existingUserIndex, 1)
        // 添加到顶部
        userList.value.unshift(existingUser)

        // 更新最新消息内容和时间
        existingUser.lastMessage = message.type === MESSAGE_TYPE_MEDIA ? '[媒体文件]' : (message.content || '')
        existingUser.lastTime = formatTime(new Date().toISOString())

        if (!currentUser.value || currentUser.value.userId !== message.senderId) {
          // 不是当前聊天对象，增加未读数
          existingUser.unreadCount++
        }
      }
    } else {
      // 管理员自己发送的消息，也需要更新左侧列表中该用户的最新消息
      const targetUserId = message.receiverId
      if (targetUserId) {
        const existingUserIndex = userList.value.findIndex((u) => u.userId === targetUserId)
        if (existingUserIndex !== -1) {
          // 用户存在，将其置顶
          const existingUser = userList.value[existingUserIndex]
          userList.value.splice(existingUserIndex, 1)
          userList.value.unshift(existingUser)

          // 更新最新消息内容和时间
          existingUser.lastMessage = message.type === MESSAGE_TYPE_MEDIA ? '[媒体文件]' : (message.content || '')
          existingUser.lastTime = formatTime(new Date().toISOString())
        }
      }
    }

    // 将消息添加到对应用户的消息列表
    const targetUserId = isFromUser ? message.senderId! : message.receiverId!
    if (!targetUserId) {
      console.warn('[Message] 无法确定目标用户ID:', message)
      return
    }

    if (!userMessagesMap.value.has(targetUserId)) {
      userMessagesMap.value.set(targetUserId, [])
    }
    const userMessages = userMessagesMap.value.get(targetUserId)!
    userMessages.push({
      id: Date.now(),
      sendUserId: message.senderId!,
      receiveUserId: message.receiverId!,
      message: message.content || '',
      type: message.type,
      createTime: new Date().toISOString(),
      mediaUrl: message.mediaUrl
    })

    console.log(`[Message] 消息已添加到用户 ${targetUserId} 的列表，当前消息数:`, userMessages.length)

    // 如果是当前聊天用户的消息，滚动到底部并标记已读
    if (currentUser.value && targetUserId === currentUser.value.userId) {
      nextTick(() => scrollToBottom())
      // 自动调用已读接口清除未读消息数
      if (currentUserId.value) {
        readMessages(Number(currentUserId.value), targetUserId).catch((error) => {
          console.error('[Message] 自动标记已读失败:', error)
        })
      }
    }
  }

  // 商品消息处理
  if (message.type === MESSAGE_TYPE_PRODUCT) {
    handleCardMessage(message, 'product')
  }

  // 订单消息处理
  if (message.type === MESSAGE_TYPE_ORDER) {
    handleCardMessage(message, 'order')
  }
}

// ============ 用户列表操作 ============

/**
 * 处理卡片消息（商品或订单）
 * @param message - WebSocket 消息
 * @param cardType - 卡片类型：'product' 或 'order'
 */
const handleCardMessage = (message: CommonMessage, cardType: 'product' | 'order') => {
  const isFromUser = message.senderId !== currentUserId.value
  const cardContent = message.content || ''

  // 更新左侧用户列表
  if (isFromUser) {
    const existingUserIndex = userList.value.findIndex((u) => u.userId === message.senderId)
    const displayText = cardType === 'product' ? '[商品信息]' : '[订单信息]'

    if (existingUserIndex === -1) {
      // 新用户
      const newUser: SimpleUser = {
        userId: message.senderId!,
        username: message.senderName || `用户${message.senderId}`,
        avatarUrl: message.senderAvatar,
        online: true,
        unreadCount: 0,
        lastMessage: displayText,
        lastTime: formatTime(new Date().toISOString())
      }
      userList.value.unshift(newUser)
      handleSelectUser(newUser)
      loadLatestMessages()
    } else {
      const existingUser = userList.value[existingUserIndex]
      userList.value.splice(existingUserIndex, 1)
      userList.value.unshift(existingUser)
      existingUser.lastMessage = displayText
      existingUser.lastTime = formatTime(new Date().toISOString())

      if (!currentUser.value || currentUser.value.userId !== message.senderId) {
        existingUser.unreadCount++
      }
    }
  }

  // 将卡片消息添加到对应用户的消息列表
  const targetUserId = isFromUser ? message.senderId! : message.receiverId!
  if (!targetUserId) return

  if (!userMessagesMap.value.has(targetUserId)) {
    userMessagesMap.value.set(targetUserId, [])
  }
  const userMessages = userMessagesMap.value.get(targetUserId)!

  // 解析 JSON 内容
  let cardData = null
  try {
    cardData = JSON.parse(cardContent)
  } catch (e) {
    console.error(`[Message] 解析${cardType}消息失败:`, e)
    return
  }

  userMessages.push({
    id: Date.now(),
    sendUserId: message.senderId!,
    receiveUserId: message.receiverId!,
    message: '',
    type: message.type,
    createTime: new Date().toISOString(),
    [cardType]: cardData
  } as Message)

  // 如果是当前聊天用户的消息，滚动到底部并标记已读
  if (currentUser.value && targetUserId === currentUser.value.userId) {
    nextTick(() => scrollToBottom())
    if (currentUserId.value) {
      readMessages(Number(currentUserId.value), targetUserId).catch((error) => {
        console.error('[Message] 自动标记已读失败:', error)
      })
    }
  }
}

/**
 * 更新用户在线状态
 */
const updateUserOnlineStatus = (userId: number, online: boolean) => {
  const user = userList.value.find((u) => u.userId === userId)
  if (user) {
    user.online = online
  }
  if (currentUser.value?.userId === userId) {
    currentUser.value.online = online
  }
}

/**
 * 更新用户最新消息（用于发送消息后实时更新左侧列表）
 */
const updateUserLastMessage = (userId: number, lastMessage: string, lastTime: string) => {
  const existingUserIndex = userList.value.findIndex((u) => u.userId === userId)
  if (existingUserIndex !== -1) {
    const existingUser = userList.value[existingUserIndex]
    // 从当前位置移除并置顶
    userList.value.splice(existingUserIndex, 1)
    userList.value.unshift(existingUser)
    // 更新最新消息
    existingUser.lastMessage = lastMessage
    existingUser.lastTime = formatTime(lastTime)
  }
}

/**
 * 加载最近消息列表
 */
const loadLatestMessages = async () => {
  if (!currentUserId.value) {
    console.warn('[Message] 无法加载最近消息列表: 当前用户ID为空')
    return
  }

  try {
    console.log('[Message] 加载最近消息列表...')
    // 如果是搜索，先清空列表
    if (searchKeyword.value.trim()) {
      userList.value = []
    }
    const res = await fetchLatestMessages({
      userId: Number(currentUserId.value),
      keyword: searchKeyword.value.trim(),
      currentPage: 1,
      pageSize: 50
    })

    console.log('[Message] 最近消息列表响应:', res)

    if (res && Array.isArray(res)) {
      // 转换为 SimpleUser 格式
      // 注意：后端返回 avatar，前端需要 avatarUrl；后端返回 onlineStatus，前端需要 online
      const users: SimpleUser[] = res.map((item: any) => ({
        userId: item.userId,
        username: item.username,
        avatarUrl: item.avatar || item.avatarUrl,
        online: item.onlineStatus === ONLINE_STATUS,
        unreadCount: item.unreadCount || 0,
        roleId: item.roleId,
        lastMessage: item.content || '',
        lastTime: item.latestTime ? formatTime(item.latestTime) : ''
      }))

      // 搜索时直接替换列表，非搜索时合并列表
      if (searchKeyword.value.trim()) {
        userList.value = users
      } else {
        // 合并现有列表，保留在线状态（WebSocket状态消息优先）
        users.forEach((newUser) => {
          const existingIndex = userList.value.findIndex((u) => u.userId === newUser.userId)
          if (existingIndex === -1) {
            userList.value.push(newUser)
          } else {
            // 更新未读数和头像
            userList.value[existingIndex].unreadCount = newUser.unreadCount
            userList.value[existingIndex].username = newUser.username
            userList.value[existingIndex].avatarUrl = newUser.avatarUrl
            userList.value[existingIndex].roleId = newUser.roleId
            // 更新在线状态（如果之前没有通过WebSocket设置过）
            userList.value[existingIndex].online = newUser.online
            // 更新最新消息
            userList.value[existingIndex].lastMessage = newUser.lastMessage
            userList.value[existingIndex].lastTime = newUser.lastTime
          }
        })
      }
      console.log(`[Message] 已加载 ${users.length} 个用户到列表`)
    } else {
      console.log('[Message] 最近消息列表为空或格式不正确')
    }
  } catch (error) {
    console.error('[Message] 加载最近消息列表失败:', error)
  }
}

/**
 * 搜索用户
 */
const handleSearch = () => {
  // 搜索时重新加载列表
  loadLatestMessages()
}

/**
 * 加载用户历史消息
 * @param userId 用户ID
 * @param isLoadMore 是否为加载更多
 */
const loadUserHistoryMessages = async (userId: number, isLoadMore: boolean = false) => {
  if (!currentUserId.value) {
    console.warn('[Message] 无法加载历史消息: 当前用户ID为空')
    return
  }

  // 如果是加载更多，设置加载状态
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    messageLoading.value = true
  }

  try {
    console.log(`[Message] 加载用户 ${userId} 的历史消息...`)
    console.log(`[Message] 当前管理员ID: ${currentUserId.value}, 目标用户ID: ${userId}`)

    // 获取当前分页状态
    const pagination = userPaginationMap.value.get(userId) || { currentPage: 1, totalPages: 1 }
    const page = isLoadMore ? pagination.currentPage + 1 : 1

    const res = await fetchHistoryMessages({
      userId: Number(currentUserId.value),
      sendUserId: userId,
      currentPage: page,
      pageSize: 20
    })

    console.log('[Message] 历史消息响应:', res)

    if (res && res.totalRecords && res.totalRecords.length > 0) {
      // 更新分页信息
      userPaginationMap.value.set(userId, {
        currentPage: page,
        totalPages: res.totalPages || 1
      })
      currentPage.value = page
      totalPages.value = res.totalPages || 1

      // 转换消息格式并存储到 Map
      // 注意：后端返回 messageId 和 content，需要映射到前端的 id 和 message
      const newMessages: Message[] = res.totalRecords.map((msg: any) => {
        const baseMsg: any = {
          id: msg.messageId,
          sendUserId: msg.sendUserId,
          receiveUserId: msg.receiveUserId,
          message: msg.content || '',
          type: msg.type,
          createTime: msg.createTime,
          mediaUrl: msg.mediaUrl
        }

        // 处理商品消息
        if (msg.type === MESSAGE_TYPE_PRODUCT && msg.content) {
          try {
            baseMsg.product = JSON.parse(msg.content)
            baseMsg.message = ''
          } catch (e) {
            console.error('解析历史商品消息失败:', e)
          }
        }

        // 处理订单消息
        if (msg.type === MESSAGE_TYPE_ORDER && msg.content) {
          try {
            baseMsg.order = JSON.parse(msg.content)
            baseMsg.message = ''
          } catch (e) {
            console.error('解析历史订单消息失败:', e)
          }
        }

        return baseMsg
      })

      // 将消息按时间正序排序（旧的在前面，新的在后面）
      newMessages.sort((a: Message, b: Message) => {
        return new Date(a.createTime).getTime() - new Date(b.createTime).getTime()
      })

      if (isLoadMore) {
        // 加载更多（更早的历史消息）：将新消息插入到前面
        const existingMessages = userMessagesMap.value.get(userId) || []
        userMessagesMap.value.set(userId, [...newMessages, ...existingMessages])
        console.log(`[Message] 已加载更多 ${newMessages.length} 条历史消息`)
      } else {
        // 初始加载
        userMessagesMap.value.set(userId, newMessages)
        console.log(`[Message] 已加载 ${newMessages.length} 条历史消息`)
      }
    } else {
      if (!isLoadMore) {
        // 如果没有历史消息，初始化空数组
        userMessagesMap.value.set(userId, [])
        console.log('[Message] 该用户暂无历史消息')
      }
    }
  } catch (error) {
    console.error('[Message] 加载历史消息失败:', error)
  } finally {
    messageLoading.value = false
    loadingMore.value = false
  }
}

/**
 * 处理消息区域滚动，滚动到顶部时加载更多
 */
const handleMessagesScroll = async () => {
  if (!messageContainerRef.value || !currentUser.value) return

  const scrollTop = messageContainerRef.value.scrollTop

  // 获取当前用户的分页状态
  const pagination = userPaginationMap.value.get(currentUser.value.userId)
  const hasMore = pagination ? pagination.currentPage < pagination.totalPages : false

  // 当滚动到顶部附近且有更多消息时加载
  if (scrollTop < 50 && hasMore && !loadingMore.value) {
    // 记录当前滚动位置（加载前第一条消息的高度）
    const firstMessage = messageContainerRef.value.querySelector('.message-item') as HTMLElement
    if (firstMessage) {
      const firstMessageHeight = firstMessage.offsetHeight

      await loadUserHistoryMessages(currentUser.value.userId, true)

      // 加载完成后恢复滚动位置，防止跳动
      nextTick(() => {
        if (messageContainerRef.value && firstMessage) {
          messageContainerRef.value.scrollTop = firstMessageHeight
        }
      })
    } else {
      await loadUserHistoryMessages(currentUser.value.userId, true)
    }
  }
}

/**
 * 选择用户（简化版，仅切换当前对话用户）
 */
const handleSelectUser = async (user: SimpleUser) => {
  console.log('[Message] 选择用户:', user)
  // 重置消息准备状态，避免闪烁
  isMessagesReady.value = false
  currentUser.value = user
  // 清除未读消息数
  user.unreadCount = 0

  // 获取用户在线状态
  if (currentUserId.value && user.userId) {
    try {
      const userInfo = await fetchUserInfo(Number(currentUserId.value), Number(user.userId))
      if (userInfo) {
        user.online = userInfo.onlineStatus === ONLINE_STATUS
        // 同时更新头像和用户名（如果有更新）
        if (userInfo.avatar) user.avatarUrl = userInfo.avatar
        if (userInfo.username) user.username = userInfo.username
        console.log('[Message] 用户在线状态:', user.online ? '在线' : '离线')
      }
    } catch (error) {
      console.error('[Message] 获取用户信息失败:', error)
    }
  }

  // 加载该用户的历史消息
  await loadUserHistoryMessages(user.userId)

  // 调用已读接口标记消息已读
  if (currentUserId.value && user.userId) {
    try {
      await readMessages(Number(currentUserId.value), Number(user.userId))
    } catch (error) {
      // 静默处理错误
      console.error('[Message] 标记已读失败:', error)
    }
  }

  // 滚动到底部（保持隐藏直到滚动完成，避免闪烁）
  await scrollToBottomOnLoad()
}

/**
 * 发送消息
 */
const handleSendMessage = async () => {
  const content = messageText.value.trim()
  const hasContent = content.length > 0
  const hasFile = selectedFile.value !== null

  // 检查是否有内容或文件
  if ((!hasContent && !hasFile) || !currentUser.value || !currentUserId.value) return

  sending.value = true

  // 如果有文件，先上传文件，然后发送媒体消息
  if (hasFile) {
    isUploading.value = true
    try {
      // 确定媒体类型（图片或视频）
      const isVideo = selectedFile.value.type.startsWith('video/')
      const mediaType = isVideo ? 2 : 1 // 1=图片, 2=视频

      // 上传文件获取 mediaUrl
      const uploadRes = await uploadMedia(mediaType, selectedFile.value)
      if (!uploadRes || !uploadRes.mediaUrl) {
        throw new Error('上传文件失败')
      }

      const { mediaUrl } = uploadRes

      // 构建媒体消息
      const mediaMessage: CommonMessage = {
        senderId: currentUserId.value,
        receiverId: currentUser.value.userId,
        type: MESSAGE_TYPE_MEDIA,
        content: '',
        mediaUrl: mediaUrl,
        mediaType: mediaType
      }

      // 发送 WebSocket 消息
      const mediaSuccess = await webSocket.sendMessage(mediaMessage)
      if (!mediaSuccess) {
        throw new Error('发送媒体消息失败')
      }

      // 添加到本地消息列表
      const mediaMsg: Message = {
        id: Date.now(),
        sendUserId: currentUserId.value,
        receiveUserId: currentUser.value.userId,
        message: '',
        type: MESSAGE_TYPE_MEDIA,
        createTime: new Date().toISOString(),
        mediaUrl: mediaUrl
      }
      if (!userMessagesMap.value.has(currentUser.value.userId)) {
        userMessagesMap.value.set(currentUser.value.userId, [])
      }
      userMessagesMap.value.get(currentUser.value.userId)!.push(mediaMsg)

      // 更新左侧聊天列表的最新消息
      updateUserLastMessage(currentUser.value.userId, '[媒体文件]', mediaMsg.createTime)

      // 清除文件选择
      removeSelectedFile()
      scrollToBottom(true, true)
    } catch (error) {
      console.error('发送媒体消息失败:', error)
      ElMessage.error('发送失败，请重试')
      isUploading.value = false
      sending.value = false
      return
    }
    isUploading.value = false
  }

  // 如果有文字内容，发送文字消息
  if (hasContent) {
    try {
      const message: CommonMessage = {
        senderId: currentUserId.value,
        receiverId: currentUser.value.userId,
        type: MESSAGE_TYPE_CHAT,
        content: content
      }

      // 通过WebSocket发送
      const success = await webSocket.sendMessage(message)
      if (!success) {
        sending.value = false
        return
      }

      // 添加到本地消息列表
      const newMessage: Message = {
        id: Date.now(),
        sendUserId: currentUserId.value,
        receiveUserId: currentUser.value.userId,
        message: content,
        type: MESSAGE_TYPE_CHAT,
        createTime: new Date().toISOString(),
        mediaUrl: undefined
      }
      if (!userMessagesMap.value.has(currentUser.value.userId)) {
        userMessagesMap.value.set(currentUser.value.userId, [])
      }
      userMessagesMap.value.get(currentUser.value.userId)!.push(newMessage)

      // 更新左侧聊天列表的最新消息
      updateUserLastMessage(currentUser.value.userId, content, newMessage.createTime)

      messageText.value = ''
    } catch (error) {
      ElMessage.error('发送失败')
    }

    // 滚动到底部（发送消息时先隐藏防止闪烁）
    nextTick(() => {
      scrollToBottom(true, true)
    })
  }

  sending.value = false
}

/**
 * 滚动到底部
 * @param animated - 是否使用平滑滚动动画
 * @param hideFirst - 是否先隐藏消息区域（用于发送消息时避免闪烁）
 */
const scrollToBottom = async (animated = true, hideFirst = false) => {
  // 只有发送消息时才先隐藏消息区域防止闪烁
  if (hideFirst) {
    isMessagesReady.value = false
    await nextTick()
  }

  if (messageContainerRef.value) {
    // 禁用平滑滚动，立即跳到底部
    messageContainerRef.value.style.scrollBehavior = 'auto'
    messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight

    // 强制浏览器重绘，确保滚动已完成
    messageContainerRef.value.offsetHeight

    // 重新启用平滑滚动（用于后续消息）
    if (animated) {
      messageContainerRef.value.style.scrollBehavior = 'smooth'
    }
  }

  // 如果之前隐藏了，现在显示出来
  if (hideFirst) {
    isMessagesReady.value = true
  }
}

/**
 * 加载历史消息时滚动到底部（无闪烁版本）
 * 使用 requestAnimationFrame 确保在渲染完成后立即滚动
 */
const scrollToBottomOnLoad = async () => {
  // 先让消息区域可见
  isMessagesReady.value = true
  await nextTick()

  if (messageContainerRef.value) {
    // 禁用平滑滚动，立即跳到底部
    messageContainerRef.value.style.scrollBehavior = 'auto'
    messageContainerRef.value.scrollTop = messageContainerRef.value.scrollHeight

    // 强制浏览器重绘
    messageContainerRef.value.offsetHeight
  }
}

// ============ 工具函数 ============

/**
 * 判断是否为自己发送的消息
 */
const isSelfMessage = (msg: Message): boolean => {
  return msg.sendUserId === currentUserId.value
}

/**
 * 判断是否显示时间分隔线
 */
const shouldShowTime = (index: number): boolean => {
  if (index === 0) return true
  if (!currentUser.value) return false
  const userMessages = userMessagesMap.value.get(currentUser.value.userId) || []
  const current = new Date(userMessages[index].createTime).getTime()
  const prev = new Date(userMessages[index - 1].createTime).getTime()
  // 间隔超过5分钟显示时间
  return current - prev > 5 * 60 * 1000
}

/**
 * 格式化消息时间
 */
const formatMessageTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const isYesterday = new Date(now.getTime() - 86400000).toDateString() === date.toDateString()

  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (isYesterday) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// ============ 其他操作 ============

/**
 * 查看用户详情
 * 跳转到用户管理页面并自动筛选该用户
 */
const handleViewUserDetail = () => {
  if (!currentUser.value) return

  // 跳转到用户管理页面，传递用户名作为查询参数
  router.push({
    path: '/user/list',
    query: { username: currentUser.value.username }
  })
}

/**
 * 获取订单状态文本
 * @param status - 订单状态码
 * @returns 状态文本
 */
const getOrderStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    1: '待付款',
    2: '待发货',
    3: '已发货',
    4: '待签收',
    5: '已完成',
    6: '已取消'
  }
  return statusMap[status] || '未知状态'
}

/**
 * 查看商品详情
 * 跳转到商品管理页面并自动筛选该商品
 */
const handleViewProduct = (productId: number | string) => {
  if (!productId) return

  // 跳转到商品管理页面，传递商品ID作为查询参数
  router.push({
    path: '/product/list',
    query: { productId: String(productId) }
  })
}

/**
 * 查看订单详情
 * 跳转到订单管理页面并自动筛选该订单
 */
const handleViewOrder = (orderId: number | string) => {
  if (!orderId) return

  // 跳转到订单管理页面，传递订单ID作为查询参数
  router.push({
    path: '/order/list',
    query: { orderId: String(orderId) }
  })
}

/**
 * 触发文件选择
 */
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

/**
 * 处理文件选择
 */
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 检查文件类型（支持图片和视频）
  if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
    ElMessage.warning('请选择图片或视频文件')
    return
  }

  // 检查文件大小（最大 50MB）
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 50MB')
    return
  }

  selectedFile.value = file
  filePreviewUrl.value = URL.createObjectURL(file)
}

/**
 * 移除选中的文件
 */
const removeSelectedFile = () => {
  selectedFile.value = null
  if (filePreviewUrl.value) {
    URL.revokeObjectURL(filePreviewUrl.value)
    filePreviewUrl.value = null
  }
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

/**
 * 判断是否为视频URL
 * @param url - 媒体URL
 * @returns 是否为视频
 */
const isVideoUrl = (url: string | undefined): boolean => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.mov', '.avi', '.wmv', '.flv', '.webm', '.mkv', '.m4v']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.endsWith(ext) || lowerUrl.includes(ext + '?'))
}

/**
 * 显示表情
 */
const handleShowEmoji = () => {
  ElMessage.info('表情功能开发中...')
}

/**
 * 快捷回复
 */
const handleQuickReply = () => {
  ElMessage.info('快捷回复功能开发中...')
}

/**
 * 重新连接
 */
const reconnect = () => {
  webSocket.closeWebSocket()
  setTimeout(() => {
    webSocket.initWebSocket()
  }, 100)
}

// ============ 生命周期 ============

// 监听 WebSocket 消息（通过监听数组长度变化来检测新消息）
watch(
  () => webSocket.messages?.value?.length ?? 0,
  (newLength, oldLength) => {
    // 只处理新增的消息
    if (newLength > (oldLength ?? 0) && webSocket.messages?.value) {
      const lastMessage = webSocket.messages.value[newLength - 1]
      console.log('[Message] WebSocket 收到新消息，数组长度:', oldLength, '->', newLength)
      handleReceivedMessage(lastMessage)
    }
  }
)

// 监听连接状态变化
watch(connectionStatus, (newStatus) => {
  // WebSocket 状态变化处理（如有需要可在此添加逻辑）
})

// 监听 currentUserId，当获取到用户ID后加载最近消息列表
watch(currentUserId, (newUserId) => {
  if (newUserId) {
    loadLatestMessages()
  }
}, { immediate: true })

// 页面从 keep-alive 缓存中重新激活时，滚动到最新消息底部
onActivated(() => {
  if (currentUser.value && messageList.value.length > 0) {
    nextTick(() => {
      scrollToBottom(false)
    })
  }
})
</script>

<style scoped lang="scss">
.customer-service-container {
  display: flex;
  height: calc(100vh - 120px);
  min-height: 600px;
  background: var(--default-box-color);
  border: 1px solid var(--art-card-border);
  border-radius: calc(var(--custom-radius) / 2 + 2px);
  overflow: hidden;
}

// 左侧用户列表
.user-list-panel {
  width: 320px;
  flex-shrink: 0;
  border-right: 1px solid var(--art-card-border);
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.connection-status {
  padding: 8px 16px;
  border-bottom: 1px solid var(--art-card-border);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-box {
  margin: 12px 16px;
  padding: 0;
  background: var(--art-gray-100);
  border-radius: calc(var(--custom-radius) / 2);
  transition: all 0.2s ease;

  &:hover {
    background: var(--art-gray-200);
  }

  &:focus-within {
    box-shadow: 0 0 0 1px var(--theme-color);
    background: #ffffff;
  }

  :deep(.el-input__wrapper) {
    background: transparent;
    box-shadow: none;
    border-radius: calc(var(--custom-radius) / 2);
    padding: 8px 12px;

    &.is-focus {
      box-shadow: none;
    }
  }

  :deep(.el-input__inner) {
    height: 36px;
    line-height: 36px;
  }
}

.user-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: calc(var(--custom-radius) / 2);
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;

  &:hover {
    background: var(--art-gray-100);
  }

  &.active {
    background: color-mix(in srgb, var(--theme-color) 10%, transparent);

    .user-name {
      color: var(--theme-color);
    }
  }
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;

  .online-status {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 10px;
    height: 10px;
    background: #13deb9;
    border: 2px solid var(--default-box-color);
    border-radius: 50%;
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  margin-left: 12px;
}

.user-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;

  .user-name {
    font-weight: 500;
    font-size: 14px;
    color: var(--art-text-primary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  .role-tag {
    flex-shrink: 0;
    margin-left: 6px;
    transform: scale(0.85);
  }

  .user-time {
    font-size: 12px;
    color: var(--art-text-secondary);
    margin-left: 8px;
    flex-shrink: 0;
  }
}

.user-message-row {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .user-last-message {
    font-size: 12px;
    color: var(--art-text-secondary);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
    max-width: 150px;
  }
}

.unread-badge {
  margin-left: 8px;
  flex-shrink: 0;
  :deep(.el-badge__content) {
    background: var(--theme-color);
    border: none;
    font-size: 11px;
    height: 18px;
    line-height: 18px;
    padding: 0 6px;
    border-radius: 9px;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
}

// 右侧聊天窗口
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--default-bg-color);
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--art-card-border);
  background: var(--default-box-color);
}

.chat-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-user-detail {
  display: flex;
  align-items: center;
  gap: 8px;

  .chat-user-name {
    font-weight: 500;
    font-size: 15px;
    color: var(--art-text-primary);
  }

  .online-tag {
    background: #d4edda;
    color: #155724;
    border-color: #c3e6cb;
  }

  .offline-tag {
    background: #e2e3e5;
    color: #383d41;
    border-color: #d6d8db;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: var(--art-gray-50);

  // 隐藏状态：使用 visibility 保留布局空间，防止输入框跑到上面
  &.messages-hidden {
    visibility: hidden;
    opacity: 0;
  }
}

// 加载更多提示
.loading-more,
.load-more-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 12px;
  color: var(--art-text-secondary);

  .loading-icon {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

.message-item {
  margin-bottom: 16px;

  &.self {
    .message-content {
      justify-content: flex-end;
    }
  }
}

.time-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0;

  span {
    font-size: 12px;
    color: var(--art-text-secondary);
    background: var(--art-gray-200);
    padding: 4px 12px;
    border-radius: 10px;
  }
}

.message-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--default-box-color);
  border: 1px solid var(--art-card-border);
  word-break: break-word;

  &.self {
    background: color-mix(in srgb, var(--theme-color) 90%, white);
    color: white;
    border-color: transparent;

    .message-text {
      color: white;
    }
  }
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--art-text-primary);
}

.message-image {
  margin-top: 8px;

  .chat-image {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    cursor: pointer;
  }
}

// 媒体消息（无气泡背景）
.media-message {
  margin: 4px 0;

  .media-image {
    max-width: 200px;
    max-height: 200px;
    border-radius: 8px;
    object-fit: cover;
    cursor: pointer;
  }

  .media-video {
    background: #000;
  }
}

// 卡片消息（商品/订单）
.card-message {
  width: 260px;
  background: var(--default-box-color);
  border: 1px solid var(--art-card-border);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    background: color-mix(in srgb, var(--theme-color) 8%, transparent);
    border-bottom: 1px solid var(--art-card-border);

    .card-icon {
      font-size: 18px;
      color: var(--theme-color);
    }

    .card-title {
      font-size: 13px;
      font-weight: 500;
      color: var(--art-text-primary);
    }
  }

  .card-content {
    display: flex;
    gap: 12px;
    padding: 12px;

    .card-image {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 6px;
      flex-shrink: 0;
    }

    .card-info {
      flex: 1;
      min-width: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;

      .card-name {
        font-size: 13px;
        color: var(--art-text-primary);
        margin: 0 0 4px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .card-price {
        font-size: 14px;
        font-weight: 600;
        color: var(--theme-color);
        margin: 0;
      }
    }

    .order-info-with-image {
      display: flex;
      gap: 12px;
      align-items: flex-start;

      .order-product-image {
        width: 60px;
        height: 60px;
        object-fit: cover;
        border-radius: 6px;
        flex-shrink: 0;

        &.placeholder {
          background: var(--art-gray-100);
          display: flex;
          align-items: center;
          justify-content: center;

          .placeholder-icon {
            width: 24px;
            height: 24px;
            color: var(--art-text-tertiary);
          }
        }
      }

      .order-details {
        flex: 1;
        min-width: 0;

        .order-id {
          font-size: 12px;
          color: var(--art-text-secondary);
          margin: 0 0 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .order-product {
          font-size: 13px;
          color: var(--art-text-primary);
          margin: 0 0 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .order-quantity {
          font-size: 12px;
          color: var(--art-text-secondary);
          margin: 0 0 4px;
        }

        .order-amount {
          font-size: 14px;
          font-weight: 600;
          color: var(--theme-color);
          margin: 0;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    border-top: 1px solid var(--art-card-border);
    background: var(--art-gray-50);

    .card-action {
      font-size: 11px;
      color: var(--theme-color);
      font-weight: 500;
    }

    .order-status {
      font-size: 11px;
      padding: 2px 6px;
      border-radius: 4px;
      font-weight: 500;

      &.status-1 { // 待付款
        background: #FFF3E0;
        color: #F57C00;
      }

      &.status-2 { // 待发货
        background: #E3F2FD;
        color: #1976D2;
      }

      &.status-3 { // 已发货
        background: #E8F5E9;
        color: #388E3C;
      }

      &.status-4 { // 待签收
        background: #F3E5F5;
        color: #7B1FA2;
      }

      &.status-5 { // 已完成
        background: #E0F2F1;
        color: #00796B;
      }

      &.status-6 { // 已取消
        background: #F5F5F5;
        color: #757575;
      }
    }
  }
}

// 视频缩略图（显示封面+播放按钮）
.video-thumbnail-wrapper {
  position: relative;
  display: inline-block;
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;

  .video-thumbnail {
    max-width: 200px;
    max-height: 200px;
    object-fit: cover;
    display: block;
  }

  .video-play-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.3);
    transition: background 0.2s ease;

    .play-icon {
      font-size: 48px;
      color: rgba(255, 255, 255, 0.9);
      filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.5));
      transition: transform 0.2s ease;
    }
  }

  &:hover {
    .video-play-overlay {
      background: rgba(0, 0, 0, 0.4);
    }
    .play-icon {
      transform: scale(1.1);
    }
  }
}

// 视频播放器全屏覆盖层
.video-player-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.video-player-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-close-btn {
  position: absolute;
  top: -50px;
  right: 0;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10000;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.1);
  }

  svg {
    font-size: 20px;
  }
}

.video-player {
  max-width: 80vw;
  max-height: 80vh;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  outline: none;
}

.empty-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--art-text-secondary);
}

// 文件预览
.file-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding: 8px;
  background: var(--art-gray-50);
  border-radius: 8px;

  img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
  }

  // 视频预览缩略图（与聊天窗口样式一致）
  .preview-video-wrapper {
    position: relative;
    display: inline-block;
    width: 60px;
    height: 60px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;

    .preview-video {
      width: 60px;
      height: 60px;
      object-fit: cover;
      display: block;
    }

    .preview-play-overlay {
      position: absolute;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.3);
      transition: background 0.2s ease;

      .preview-play-icon {
        font-size: 24px;
        color: rgba(255, 255, 255, 0.9);
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
        transition: transform 0.2s ease;
      }
    }

    &:hover {
      .preview-play-overlay {
        background: rgba(0, 0, 0, 0.4);
      }
      .preview-play-icon {
        transform: scale(1.1);
      }
    }
  }

  .remove-file-btn {
    width: 24px;
    height: 24px;
    border: none;
    background: rgba(0, 0, 0, 0.5);
    color: #FFFFFF;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: rgba(0, 0, 0, 0.7);
    }
  }
}

// 消息输入区
.message-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--art-card-border);
  background: var(--default-box-color);
}

.input-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;

  .el-button {
    color: var(--art-text-secondary);

    &:hover {
      color: var(--theme-color);
    }
  }
}

.input-box {
  margin-bottom: 12px;
}

.message-textarea {
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    background: var(--art-gray-50);
    border-color: var(--art-card-border);
    resize: none;

    &:focus {
      border-color: var(--theme-color);
    }
  }
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-tip {
  font-size: 12px;
  color: var(--art-text-secondary);
}

// 未选择用户状态
.no-user-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--art-gray-50);
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
</style>