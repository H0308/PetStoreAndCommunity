<script setup>
import { ref, computed, watch, nextTick, onMounted, inject } from 'vue'
import { Close, Picture, Promotion, ChatDotRound, Loading, VideoPlay, ChatLineRound, Plus, Delete, Food, FirstAidKit, Guide } from '@element-plus/icons-vue'
import CustomVideoPlayer from './CustomVideoPlayer.vue'
import { marked } from 'marked'
import { API_BASE_URL, request } from '@/api/config'
import {
  fetchMessageList,
  fetchCustomerServiceInfo,
  readMessages,
  uploadMedia,
  fetchAIChatList,
  createAIChat,
  generateChatTitle,
  fetchAIChatHistory,
  deleteAIChat,
  MESSAGE_TYPE_CHAT,
  MESSAGE_TYPE_MEDIA,
  MESSAGE_TYPE_STATUS,
  MESSAGE_TYPE_PRODUCT,
  MESSAGE_TYPE_ORDER,
  ONLINE_STATUS
} from '@/api/chat'

// 配置 marked - 不启用 breaks，避免多余换行
marked.setOptions({
  gfm: true,
  breaks: false
})

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'ai', // 'ai' | 'human'
    validator: (value) => ['ai', 'human'].includes(value)
  },
  // 当前查看的商品信息（可选）
  currentProduct: {
    type: Object,
    default: null
  },
  // 当前查看的订单信息（可选）
  currentOrder: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'new-message'])

// ============ WebSocket ============
// 从 App.vue 注入的 WebSocket 实例
const webSocket = inject('webSocket', null)
const wsStatus = computed(() => webSocket?.status || ref('disconnected'))

// ============ 输入和状态 ============
const inputMessage = ref('')
const isAiTyping = ref(false)
const isHumanTyping = ref(false)
const isUploading = ref(false)

// 消息区域是否准备好显示（避免闪烁）
const isMessagesReady = ref(false)

// ============ 媒体消息 ============
const selectedFile = ref(null)
const filePreviewUrl = ref(null)
const fileInputRef = ref(null)

// 视频播放器相关
const showVideoPlayer = ref(false)
const currentVideoUrl = ref('')
const videoPlayerRef = ref(null)

// 待发送的卡片信息（商品或订单）
const pendingCard = ref(null)

// ============ AI 会话相关 ============
// 会话列表
const aiChatList = ref([])
// 当前选中的会话
const currentChatId = ref(null)
const currentConversationId = ref(null)
const currentChatTitle = ref('新建会话')
// 会话列表分页
const chatListPage = ref(1)
const chatListTotalPages = ref(1)
const isLoadingChatList = ref(false)
// 历史消息分页
const historyPage = ref(1)
const historyTotalPages = ref(1)
const isLoadingHistory = ref(false)
// 是否显示会话列表面板
const showChatListPanel = ref(false)
// 会话标题是否需要更新（新会话发送第一条消息后更新）
const needUpdateTitle = ref(false)
// 标记当前会话是否已持久化到后端（false=本地临时会话，未调用后端接口）
const isSessionPersisted = ref(false)

// ============ 面板拖拽调整宽度 ============
const chatListPanelWidth = ref(220) // 默认宽度
const MIN_PANEL_WIDTH = 180 // 最小宽度
const MAX_PANEL_WIDTH = 400 // 最大宽度

// 整个聊天容器宽度拖拽
const drawerWidth = ref(420) // 默认宽度
const MIN_DRAWER_WIDTH = 375 // 最小宽度 (iPhone标准宽度，确保内容正常显示)
const MAX_DRAWER_WIDTH = 800 // 最大宽度
const isResizingDrawer = ref(false)
const drawerResizeStartX = ref(0)
const drawerResizeStartWidth = ref(0)

const isResizing = ref(false)
const resizeStartX = ref(0)
const resizeStartWidth = ref(0)

/**
 * 开始拖拽调整会话列表宽度
 */
const startResize = (e) => {
  isResizing.value = true
  resizeStartX.value = e.clientX
  resizeStartWidth.value = chatListPanelWidth.value

  // 添加全局事件监听
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  // 禁止选择文本
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
}

/**
 * 拖拽过程中（会话列表）
 */
const handleResize = (e) => {
  if (!isResizing.value) return

  const deltaX = e.clientX - resizeStartX.value
  const newWidth = resizeStartWidth.value + deltaX

  // 限制在最小和最大宽度之间
  if (newWidth >= MIN_PANEL_WIDTH && newWidth <= MAX_PANEL_WIDTH) {
    chatListPanelWidth.value = newWidth
  }
}

/**
 * 结束拖拽（会话列表）
 */
const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  // 恢复文本选择
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
}

/**
 * 开始拖拽调整整个聊天容器宽度
 */
const startResizeDrawer = (e) => {
  isResizingDrawer.value = true
  drawerResizeStartX.value = e.clientX
  drawerResizeStartWidth.value = drawerWidth.value

  // 添加全局事件监听
  document.addEventListener('mousemove', handleResizeDrawer)
  document.addEventListener('mouseup', stopResizeDrawer)
  // 禁止选择文本
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
}

/**
 * 拖拽过程中（整个聊天容器）
 * 由于 drawer 固定在右侧，向左拖拽时宽度增加
 */
const handleResizeDrawer = (e) => {
  if (!isResizingDrawer.value) return

  // drawer 在右侧，向左拖拽（clientX 减小）时宽度增加
  const deltaX = drawerResizeStartX.value - e.clientX
  const newWidth = drawerResizeStartWidth.value + deltaX

  // 限制在最小和最大宽度之间
  if (newWidth >= MIN_DRAWER_WIDTH && newWidth <= MAX_DRAWER_WIDTH) {
    drawerWidth.value = newWidth
  }
}

/**
 * 结束拖拽（整个聊天容器）
 */
const stopResizeDrawer = () => {
  isResizingDrawer.value = false
  document.removeEventListener('mousemove', handleResizeDrawer)
  document.removeEventListener('mouseup', stopResizeDrawer)
  // 恢复文本选择
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
}

/**
 * 设置待发送的卡片
 * @param {Object} card - 卡片数据
 * @param {string} cardType - 卡片类型：'product' | 'order'
 */
const setPendingCard = (card, cardType) => {
  if (!card) {
    pendingCard.value = null
    return
  }
  pendingCard.value = {
    type: cardType,
    data: card
  }
  console.log('[ChatDrawer] 设置待发送卡片:', pendingCard.value)
}

/**
 * 取消发送卡片
 */
const cancelPendingCard = () => {
  pendingCard.value = null
}

/**
 * 确认发送待处理的卡片
 */
const confirmSendPendingCard = async () => {
  if (!pendingCard.value) return

  const { type, data } = pendingCard.value
  let success = false

  if (type === 'product') {
    success = await sendProductCard(data)
  } else if (type === 'order') {
    success = await sendOrderCard(data)
  }

  if (success) {
    pendingCard.value = null
  }
}

/**
 * 打开视频播放器
 */
const openVideoPlayer = (url) => {
  currentVideoUrl.value = url
  showVideoPlayer.value = true
}

/**
 * 关闭视频播放器
 */
const closeVideoPlayer = () => {
  showVideoPlayer.value = false
  currentVideoUrl.value = ''
}

// ============ 消息列表 ============
const aiMessages = ref([])
const humanMessages = ref([])
const currentUserId = ref(null)

// 分页相关状态
const currentPage = ref(1)
const totalPages = ref(1)
const loadingMore = ref(false)
const hasMoreMessages = computed(() => currentPage.value < totalPages.value)

// 当前消息列表
const messages = computed(() => {
  return props.mode === 'ai' ? aiMessages.value : humanMessages.value
})

// 消息容器引用
const messagesRef = ref(null)

// 客服信息
const serviceInfo = ref({
  serverId: null,  // 管理员用户ID，用于发送消息
  name: '',
  id: '',
  avatar: '',
  status: 'offline' // online | offline | busy
})

// 头部配置
const headerConfig = computed(() => {
  if (props.mode === 'ai') {
    return {
      title: '小橘智能助手',
      subtitle: currentChatTitle.value || 'AI客服24小时在线为您服务',
      gradient: 'linear-gradient(135deg, #FF8A5B 0%, #FFB088 100%)'
    }
  }
  const statusText = {
    online: '在线',
    busy: '忙碌中',
    offline: '离线'
  }
  return {
    title: '人工在线客服',
    subtitle: statusText[serviceInfo.value.status] || '离线',
    gradient: 'linear-gradient(135deg, #FF8A5B 0%, #FFB088 100%)'
  }
})

// ============ AI 会话方法 ============

/**
 * 加载 AI 会话列表
 */
const loadAIChatList = async (isLoadMore = false) => {
  if (isLoadingChatList.value) return

  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return

  try {
    isLoadingChatList.value = true
    const userInfo = JSON.parse(userInfoStr)
    const userId = Number(userInfo.userId)
    if (!userId) return

    const page = isLoadMore ? chatListPage.value + 1 : 1
    const pageSize = 20

    const res = await fetchAIChatList({
      userId,
      currentPage: page,
      pageSize
    })

    if (res.data?.totalRecords) {
      chatListTotalPages.value = res.data.totalPages || 1

      if (isLoadMore) {
        chatListPage.value = page
        aiChatList.value = [...aiChatList.value, ...res.data.totalRecords]
      } else {
        chatListPage.value = 1
        aiChatList.value = res.data.totalRecords
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    isLoadingChatList.value = false
  }
}

/**
 * 创建新会话（本地临时会话，不调用后端接口）
 * 当用户发送第一条消息时才会真正创建到后端
 */
const handleCreateNewChat = () => {
  // 生成临时会话ID（使用本地时间戳+随机数）
  const tempChatId = `temp-${Date.now()}-${Math.floor(Math.random() * 1000)}`

  currentChatId.value = tempChatId
  currentConversationId.value = tempChatId
  currentChatTitle.value = '新建会话'
  needUpdateTitle.value = true
  isSessionPersisted.value = false // 标记为未持久化
  aiMessages.value = []
  showChatListPanel.value = false
  // 新会话没有历史消息需要加载，直接显示消息区域（显示中心欢迎界面）
  isMessagesReady.value = true
}

/**
 * 持久化会话到后端（发送第一条消息时调用）
 */
const persistSessionToBackend = async (userId) => {
  try {
    const res = await createAIChat(userId)
    if (res.data) {
      currentChatId.value = res.data.chatId
      currentConversationId.value = res.data.conversationId
      isSessionPersisted.value = true // 标记为已持久化
      return true
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败，请重试')
    return false
  }
  return false
}

/**
 * 选择会话
 */
const handleSelectChat = async (chat) => {
  if (currentChatId.value === chat.chatId) {
    showChatListPanel.value = false
    return
  }

  currentChatId.value = chat.chatId
  currentConversationId.value = chat.conversationId
  currentChatTitle.value = chat.title || '新建会话'
  needUpdateTitle.value = false // 已有会话不需要更新标题
  isSessionPersisted.value = true // 从列表中选择的会话已持久化
  showChatListPanel.value = false

  // 加载该会话的历史消息
  await loadAIChatHistory()
}

/**
 * 删除会话
 */
const handleDeleteChat = async (chat, event) => {
  event.stopPropagation()

  // 如果删除的是当前临时会话，只需清理本地状态
  if (currentChatId.value === chat.chatId && !isSessionPersisted.value) {
    currentChatId.value = null
    currentConversationId.value = null
    currentChatTitle.value = '新建会话'
    aiMessages.value = []
    isSessionPersisted.value = false
    handleCreateNewChat()
    // 从列表中移除（临时会话不应该在列表中，这里是保险起见）
    aiChatList.value = aiChatList.value.filter(c => c.chatId !== chat.chatId)
    return
  }

  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return
    const userInfo = JSON.parse(userInfoStr)
    const userId = Number(userInfo.userId)

    const res = await deleteAIChat(chat.chatId, userId)
    if (res.data) {
      // 如果删除的是当前会话，重置当前会话
      if (currentChatId.value === chat.chatId) {
        currentChatId.value = null
        currentConversationId.value = null
        currentChatTitle.value = '新建会话'
        aiMessages.value = []
        isSessionPersisted.value = false
        // 创建新会话
        handleCreateNewChat()
      }
      // 刷新列表
      await loadAIChatList()
      ElMessage.success('删除成功')
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

/**
 * 更新会话标题
 */
const updateChatTitle = async (firstMessage) => {
  if (!currentChatId.value || !firstMessage) return

  try {
    const res = await generateChatTitle(currentChatId.value, firstMessage)
    if (res.data) {
      currentChatTitle.value = res.data
      // 刷新会话列表
      await loadAIChatList()
    }
  } catch (error) {
    console.error('更新会话标题失败:', error)
  }
}

/**
 * 加载 AI 历史消息
 */
const loadAIChatHistory = async (isLoadMore = false) => {
  if (!currentChatId.value || isLoadingHistory.value) return

  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return

  try {
    isLoadingHistory.value = true
    const userInfo = JSON.parse(userInfoStr)
    const userId = Number(userInfo.userId)
    if (!userId) return

    const page = isLoadMore ? historyPage.value + 1 : 1
    const pageSize = 20

    const res = await fetchAIChatHistory({
      chatId: currentChatId.value,
      userId,
      currentPage: page,
      pageSize
    })

    if (res.data?.totalRecords) {
      historyTotalPages.value = res.data.totalPages || 1

      // 转换消息格式（后端返回的是降序，最新在前）
      const newMessages = res.data.totalRecords.map(msg => ({
        id: msg.id || `msg-${Date.now()}-${Math.random()}`,
        type: msg.type === 1 ? 'user' : 'system', // 1=用户, 2=AI
        content: msg.content || '',
        time: msg.createTime ? new Date(msg.createTime).toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit'
        }) : getCurrentTime(),
        createTime: msg.createTime,
        isMarkdown: msg.type !== 1 // AI 消息使用 Markdown
      }))

      // 将消息按原始创建时间正序排序（旧的在前面，新的在后面）
      newMessages.sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())

      if (isLoadMore) {
        // 加载更多（更早的历史消息）：将新消息插入到前面
        historyPage.value = page
        aiMessages.value = [...newMessages, ...aiMessages.value]
      } else {
        // 初始加载
        historyPage.value = 1
        aiMessages.value = newMessages
        // 空会话不添加默认消息，显示中心欢迎界面
        await scrollToBottomOnLoad()
      }
    } else {
      // 没有历史消息，保持空列表，显示中心欢迎界面
      aiMessages.value = []
      isMessagesReady.value = true
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    // 错误时保持空列表，显示中心欢迎界面
    aiMessages.value = []
    isMessagesReady.value = true
  } finally {
    isLoadingHistory.value = false
    loadingMore.value = false
  }
}

/**
 * 处理消息区域滚动，滚动到顶部时加载更多历史消息
 */
const handleMessagesScroll = async () => {
  if (!messagesRef.value) return

  const scrollTop = messagesRef.value.scrollTop

  // 当滚动到顶部附近且有更多消息时加载
  if (scrollTop < 50 && !isLoadingHistory.value) {
    if (props.mode === 'ai' && historyPage.value < historyTotalPages.value) {
      // 记录当前滚动位置（加载前第一条消息）
      const firstMessage = messagesRef.value.querySelector('.message-item')
      if (firstMessage) {
        const firstMessageHeight = firstMessage.offsetHeight

        await loadAIChatHistory(true)

        // 加载完成后恢复滚动位置，防止跳动
        nextTick(() => {
          if (messagesRef.value && firstMessage) {
            messagesRef.value.scrollTop = firstMessageHeight
          }
        })
      } else {
        await loadAIChatHistory(true)
      }
    } else if (props.mode === 'human' && hasMoreMessages.value && !loadingMore.value) {
      // 人工客服模式的历史消息加载
      const firstMessage = messagesRef.value.querySelector('.message-item')
      if (firstMessage) {
        const firstMessageHeight = firstMessage.offsetHeight

        await loadHistoryMessages(true)

        nextTick(() => {
          if (messagesRef.value && firstMessage) {
            messagesRef.value.scrollTop = firstMessageHeight
          }
        })
      } else {
        await loadHistoryMessages(true)
      }
    }
  }
}

// 初始化消息
const initMessages = async () => {
  console.log('[ChatDrawer] initMessages called, mode:', props.mode, 'currentProduct:', props.currentProduct, 'currentOrder:', props.currentOrder)

  if (props.mode === 'ai') {
    // AI 模式：加载会话列表和当前会话
    if (!currentChatId.value) {
      // 如果没有当前会话，创建本地临时会话（不调用后端）
      handleCreateNewChat()
    } else if (isSessionPersisted.value) {
      // 有已持久化的会话，加载历史消息
      await loadAIChatHistory()
    } else {
      // 有临时会话但未持久化，直接显示欢迎界面
      isMessagesReady.value = true
    }
    // 加载会话列表
    await loadAIChatList()
    return
  }

  if (props.mode === 'human') {
    // 先加载客服信息
    await loadCustomerServiceInfo()
    console.log('[ChatDrawer] 客服信息加载完成, serverId:', serviceInfo.value.serverId)
    // 再加载历史消息
    await loadHistoryMessages()
    // 标记消息已读
    await markMessagesAsRead()

    // 设置待发送卡片（如果有商品或订单信息）
    if (props.currentProduct) {
      console.log('[ChatDrawer] 设置待发送商品卡片:', props.currentProduct)
      setPendingCard(props.currentProduct, 'product')
    } else if (props.currentOrder) {
      console.log('[ChatDrawer] 设置待发送订单卡片:', props.currentOrder)
      setPendingCard(props.currentOrder, 'order')
    } else {
      // 如果没有商品或订单，清空待发送卡片
      pendingCard.value = null
    }

    return
  }
}

// 获取当前时间
const getCurrentTime = () => {
  return new Date().toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// ============ WebSocket 方法 ============

// 发送 WebSocket 消息（使用从 App.vue 注入的连接）
const sendWebSocketMessage = (message) => {
  if (!webSocket) {
    console.error('[ChatDrawer] WebSocket 未初始化')
    return false
  }
  return webSocket.sendMessage(message)
}

// 处理接收到的消息
const handleReceivedMessage = async (message) => {
  if (!message) return

  // 状态消息 — 重新请求后端获取最新客服信息（可能切换了客服）
  if (message.type === MESSAGE_TYPE_STATUS) {
    await loadCustomerServiceInfo()
    return
  }

  // 聊天消息
  if (message.type === MESSAGE_TYPE_CHAT) {
    isHumanTyping.value = false
    // 只处理接收到的消息（不是自己发送的）- 使用 == 允许类型转换比较
    if (message.senderId != currentUserId.value) {
      humanMessages.value.push({
        id: `system-${Date.now()}`,
        type: 'system',
        content: message.content,
        time: getCurrentTime()
      })
      nextTick(() => {
        scrollToBottom(true, true)
      })
      emit('new-message')
    }
  }

  // 商品消息
  if (message.type === MESSAGE_TYPE_PRODUCT) {
    isHumanTyping.value = false
    if (message.senderId != currentUserId.value) {
      try {
        const productData = JSON.parse(message.content)
        humanMessages.value.push({
          id: `system-product-${Date.now()}`,
          type: 'system',
          content: '',
          time: getCurrentTime(),
          product: productData
        })
        nextTick(() => {
          scrollToBottom(true, true)
        })
        emit('new-message')
      } catch (e) {
        console.error('解析商品消息失败:', e)
      }
    }
  }

  // 订单消息
  if (message.type === MESSAGE_TYPE_ORDER) {
    isHumanTyping.value = false
    if (message.senderId != currentUserId.value) {
      try {
        const orderData = JSON.parse(message.content)
        humanMessages.value.push({
          id: `system-order-${Date.now()}`,
          type: 'system',
          content: '',
          time: getCurrentTime(),
          order: orderData
        })
        nextTick(() => {
          scrollToBottom(true, true)
        })
        emit('new-message')
      } catch (e) {
        console.error('解析订单消息失败:', e)
      }
    }
  }
}

// 加载客服信息
const loadCustomerServiceInfo = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return
    const userInfo = JSON.parse(userInfoStr)
    const userId = Number(userInfo.userId)
    if (!userId) return
    const res = await fetchCustomerServiceInfo(userId)
    if (res.data) {
      serviceInfo.value.serverId = res.data.serverId || null
      serviceInfo.value.name = res.data.serverName || '客服'
      serviceInfo.value.avatar = res.data.serverAvatarUrl || ''
      // onlineStatus: 1=在线, 2=离线
      serviceInfo.value.status = res.data.onlineStatus === ONLINE_STATUS ? 'online' : 'offline'
    }
  } catch (error) {
    console.error('加载客服信息失败:', error)
    // 使用默认值
    serviceInfo.value.name = '客服'
    serviceInfo.value.status = 'offline'
  }
}

// 加载历史消息
const loadHistoryMessages = async (isLoadMore = false) => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return

  let userInfo = null
  try {
    userInfo = JSON.parse(userInfoStr)
  } catch (e) {
    console.error('解析用户信息失败:', e)
    return
  }

  const token = userInfo.token
  if (!token) return

  // 确保已加载客服信息（获取 serverId）
  if (!serviceInfo.value.serverId) {
    await loadCustomerServiceInfo()
  }

  // 如果是加载更多，设置加载状态
  if (isLoadMore) {
    loadingMore.value = true
  }

  try {
    const userId = Number(userInfo.userId || JSON.parse(atob(token.split('.')[1])).userId)
    const serverId = Number(serviceInfo.value.serverId)
    currentUserId.value = userId

    // 确保参数有效
    if (!userId || !serverId) {
      console.error('加载历史消息失败: 缺少必要参数')
      return
    }

    const page = isLoadMore ? currentPage.value + 1 : 1

    const res = await fetchMessageList({
      userId: userId,
      serverId: serverId,
      currentPage: page,
      pageSize: 20
    })

    if (res.data?.totalRecords) {
      // 更新分页信息
      totalPages.value = res.data.totalPages || 1

      // 转换消息格式（后端返回的是降序，最新在前）
      const newMessages = res.data.totalRecords.map(msg => {
        const baseMsg = {
          id: msg.messageId,
          type: msg.sendUserId === userId ? 'user' : 'system',
          content: msg.content || '',
          time: new Date(msg.createTime).toLocaleTimeString('zh-CN', {
            hour: '2-digit',
            minute: '2-digit'
          }),
          createTime: msg.createTime, // 保存原始时间用于排序
          mediaUrl: msg.mediaUrl,
          isMedia: !!msg.mediaUrl
        }

        // 处理商品消息
        if (msg.type === MESSAGE_TYPE_PRODUCT && msg.content) {
          try {
            baseMsg.product = JSON.parse(msg.content)
            baseMsg.content = ''
          } catch (e) {
            console.error('解析历史商品消息失败:', e)
          }
        }

        // 处理订单消息
        if (msg.type === MESSAGE_TYPE_ORDER && msg.content) {
          try {
            baseMsg.order = JSON.parse(msg.content)
            baseMsg.content = ''
          } catch (e) {
            console.error('解析历史订单消息失败:', e)
          }
        }

        return baseMsg
      })

      // 将消息按原始创建时间正序排序（旧的在前面，新的在后面）
      newMessages.sort((a, b) => new Date(a.createTime).getTime() - new Date(b.createTime).getTime())

      if (isLoadMore) {
        // 加载更多（更早的历史消息）：将新消息插入到前面
        currentPage.value = page
        humanMessages.value = [...newMessages, ...humanMessages.value]
      } else {
        // 初始加载
        currentPage.value = 1
        humanMessages.value = newMessages
        // 滚动到底部（保持隐藏直到滚动完成，避免闪烁）
        await scrollToBottomOnLoad()
      }
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    // 显示默认欢迎消息
    if (humanMessages.value.length === 0) {
      humanMessages.value = [
        {
          id: `system-${Date.now()}`,
          type: 'system',
          content: '您好！欢迎咨询人工客服，请描述您的问题...',
          time: getCurrentTime()
        }
      ]
      // 默认消息显示后也要滚动到底部（不使用动画，避免闪烁）
      nextTick(() => {
        scrollToBottom(false)
      })
    }
  } finally {
    loadingMore.value = false
  }
}

// 标记消息已读
const markMessagesAsRead = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return
    const userInfo = JSON.parse(userInfoStr)
    const userId = Number(userInfo.userId)
    const serverId = Number(serviceInfo.value.serverId)
    if (userId && serverId) {
      await readMessages(userId, serverId)
    }
  } catch (error) {
    // 静默处理错误，不影响用户体验
    console.error('标记已读失败:', error)
  }
}

// 文件选择相关
const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event) => {
  const file = event.target.files[0]
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
 * @param {string} url - 媒体URL
 * @returns {boolean} 是否为视频
 */
const isVideoUrl = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.mov', '.avi', '.wmv', '.flv', '.webm', '.mkv', '.m4v']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.endsWith(ext) || lowerUrl.includes(ext + '?'))
}

// 滚动到底部
// @param animated - 是否使用平滑滚动动画
// @param hideFirst - 是否先隐藏消息区域（用于发送消息时避免闪烁）
const scrollToBottom = async (animated = true, hideFirst = false) => {
  // 只有发送消息时才先隐藏消息区域防止闪烁
  if (hideFirst) {
    isMessagesReady.value = false
    await nextTick()
  }

  if (messagesRef.value) {
    // 禁用平滑滚动，立即跳到底部
    messagesRef.value.style.scrollBehavior = 'auto'
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight

    // 强制浏览器重绘，确保滚动已完成
    messagesRef.value.offsetHeight

    // 重新启用平滑滚动（用于后续消息）
    if (animated) {
      messagesRef.value.style.scrollBehavior = 'smooth'
    }
  }

  // 如果之前隐藏了，现在显示出来
  if (hideFirst) {
    isMessagesReady.value = true
  }
}

// 加载历史消息时滚动到底部（无闪烁版本）
const scrollToBottomOnLoad = async () => {
  // 先让消息区域可见
  isMessagesReady.value = true
  await nextTick()

  if (messagesRef.value) {
    // 禁用平滑滚动，立即跳到底部
    messagesRef.value.style.scrollBehavior = 'auto'
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight

    // 强制浏览器重绘
    messagesRef.value.offsetHeight
  }
}

/**
 * 发送商品卡片
 * @param {Object} product - 商品信息
 * @returns {Promise<boolean>} 是否发送成功
 */
const sendProductCard = async (product) => {
  if (!serviceInfo.value.serverId || !currentUserId.value) {
    console.error('[ChatDrawer] 发送商品卡片失败: 缺少serverId或userId')
    return false
  }

  const productData = {
    productId: product.id || product.productId,
    name: product.name || product.productName,
    price: product.price,
    image: product.image || product.mainImage || product.coverImage,
    status: product.status
  }

  console.log('[ChatDrawer] 发送商品卡片:', productData)

  const message = {
    senderId: currentUserId.value,
    receiverId: serviceInfo.value.serverId,
    type: MESSAGE_TYPE_PRODUCT,
    content: JSON.stringify(productData)
  }

  const success = sendWebSocketMessage(message)
  if (success) {
    humanMessages.value.push({
      id: `user-product-${Date.now()}`,
      type: 'user',
      content: '',
      time: getCurrentTime(),
      product: productData
    })
    nextTick(() => {
      scrollToBottom(true, true)
    })
    console.log('[ChatDrawer] 商品卡片发送成功')
  } else {
    console.error('[ChatDrawer] 商品卡片发送失败')
    ElMessage.error('发送商品卡片失败，请重试')
  }
  return success
}

/**
 * 发送订单卡片
 * @param {Object} order - 订单信息
 * @returns {Promise<boolean>} 是否发送成功
 */
const sendOrderCard = async (order) => {
  if (!serviceInfo.value.serverId || !currentUserId.value) {
    console.error('[ChatDrawer] 发送订单卡片失败: 缺少serverId或userId')
    return false
  }

  const orderData = {
    orderId: order.orderId || order.id,
    productName: order.productName || (order.items && order.items[0]?.productName),
    totalAmount: order.totalAmount || order.totalPrice,
    status: order.status,
    createTime: order.createTime,
    imgUrl: order.imgUrl,
    totalCount: order.totalCount
  }

  console.log('[ChatDrawer] 发送订单卡片:', orderData)

  const message = {
    senderId: currentUserId.value,
    receiverId: serviceInfo.value.serverId,
    type: MESSAGE_TYPE_ORDER,
    content: JSON.stringify(orderData)
  }

  const success = sendWebSocketMessage(message)
  if (success) {
    humanMessages.value.push({
      id: `user-order-${Date.now()}`,
      type: 'user',
      content: '',
      time: getCurrentTime(),
      order: orderData
    })
    nextTick(() => {
      scrollToBottom(true, true)
    })
    console.log('[ChatDrawer] 订单卡片发送成功')
  } else {
    console.error('[ChatDrawer] 订单卡片发送失败')
    ElMessage.error('发送订单卡片失败，请重试')
  }
  return success
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  const hasContent = content.length > 0
  const hasFile = selectedFile.value !== null

  // 检查是否有内容或文件
  if ((!hasContent && !hasFile) || isAiTyping.value || isUploading.value) return

  // AI 模式只处理文字
  if (props.mode === 'ai') {
    if (!hasContent) return

    // 如果没有当前会话，先创建本地临时会话
    if (!currentChatId.value || !currentConversationId.value) {
      handleCreateNewChat()
    }

    // 如果会话未持久化，先调用后端创建会话
    if (!isSessionPersisted.value) {
      const userInfoStr = localStorage.getItem('userInfo')
      if (!userInfoStr) {
        ElMessage.error('请先登录')
        return
      }
      const userInfo = JSON.parse(userInfoStr)
      const userId = Number(userInfo.userId)
      if (!userId) {
        ElMessage.error('用户信息无效')
        return
      }

      const persisted = await persistSessionToBackend(userId)
      if (!persisted) {
        return // 持久化失败，不继续发送消息
      }
      // 刷新会话列表以显示新创建的会话
      await loadAIChatList()
    }

    const userMsgId = `user-${Date.now()}`
    aiMessages.value.push({
      id: userMsgId,
      type: 'user',
      content,
      time: getCurrentTime()
    })
    inputMessage.value = ''
    scrollToBottom(true, true)

    // 如果是新会话的第一条消息，更新会话标题
    if (needUpdateTitle.value && aiMessages.value.filter(m => m.type === 'user').length === 1) {
      await updateChatTitle(content)
      needUpdateTitle.value = false
    }

    await sendToAI(content)
    return
  }

  // 人工客服模式
  if (!serviceInfo.value.serverId) {
    humanMessages.value.push({
      id: `system-${Date.now()}`,
      type: 'system',
      content: '客服信息加载失败，请刷新页面重试',
      time: getCurrentTime()
    })
    scrollToBottom(true, true)
    return
  }

  // 客服离线标记（发送完消息后再提示）
  const isOffline = serviceInfo.value.status === 'offline'


  // 如果有文件，先上传文件，然后发送媒体消息
  if (hasFile) {
    isUploading.value = true
    try {
      // 确定媒体类型（图片或视频）
      const isVideo = selectedFile.value.type.startsWith('video/')
      const mediaType = isVideo ? 2 : 1 // 1=图片, 2=视频

      // 上传文件获取 mediaUrl
      const uploadRes = await uploadMedia(mediaType, selectedFile.value)
      if (!uploadRes.data) {
        throw new Error('上传文件失败')
      }

      const { mediaUrl } = uploadRes.data

      // 构建媒体消息
      const mediaMessage = {
        senderId: currentUserId.value,
        receiverId: serviceInfo.value.serverId,
        type: MESSAGE_TYPE_MEDIA,
        content: '',
        mediaUrl: mediaUrl,
        mediaType: mediaType
      }

      // 发送 WebSocket 消息
      const mediaSuccess = sendWebSocketMessage(mediaMessage)
      if (!mediaSuccess) {
        throw new Error('发送媒体消息失败')
      }

      // 显示媒体消息到列表
      const mediaMsgId = `user-media-${Date.now()}`
      humanMessages.value.push({
        id: mediaMsgId,
        type: 'user',
        content: '',
        time: getCurrentTime(),
        mediaUrl: mediaUrl,
        isMedia: true
      })

      // 清除文件选择
      removeSelectedFile()
      scrollToBottom(true, true)
    } catch (error) {
      console.error('发送媒体消息失败:', error)
      ElMessage.error('发送失败，请重试')
      isUploading.value = false
      return
    }
    isUploading.value = false
  }

  // 如果有待发送的卡片，先发送卡片
  if (pendingCard.value) {
    await confirmSendPendingCard()
  }

  // 如果有文字内容，发送文字消息
  if (hasContent) {
    const userMsgId = `user-${Date.now()}`
    humanMessages.value.push({
      id: userMsgId,
      type: 'user',
      content,
      time: getCurrentTime()
    })

    inputMessage.value = ''
    scrollToBottom(true, true)

    const message = {
      senderId: currentUserId.value,
      receiverId: serviceInfo.value.serverId,
      type: MESSAGE_TYPE_CHAT,
      content: content
    }

    const success = sendWebSocketMessage(message)
    if (!success) {
      humanMessages.value.push({
        id: `system-${Date.now()}`,
        type: 'system',
        content: '消息发送失败，请检查网络连接后重试',
        time: getCurrentTime()
      })
      scrollToBottom(true, true)
    }
  }

  // 消息发送完毕后，如果客服离线则追加本地提示
  if (isOffline) {
    humanMessages.value.push({
      id: `system-${Date.now()}`,
      type: 'system',
      content: '当前客服不在线，建议您在客服上线后再发送消息，届时客服会第一时间回复您。',
      time: getCurrentTime()
    })
    scrollToBottom(true, true)
  }
}

// 调用 AI 流式接口
const sendToAI = async (message) => {
  isAiTyping.value = true

  // 创建 AI 回复消息占位（使用 'ai-' 前缀确保唯一）
  const aiMsgId = `ai-${Date.now()}`
  aiMessages.value.push({
    id: aiMsgId,
    type: 'system',
    content: '',
    time: getCurrentTime(),
    isTyping: true,
    isMarkdown: true  // AI 回复使用 Markdown 渲染
  })
  scrollToBottom(true, true)

  try {
    const response = await request(
      `${API_BASE_URL}/api/user/ai/chat?message=${encodeURIComponent(message)}&conversationId=${encodeURIComponent(currentConversationId.value)}`,
      {
        method: 'GET',
        headers: {
          'Accept': 'text/html;charset=utf-8'
        }
      }
    )

    if (!response.ok) {
      throw new Error('AI 服务暂时不可用')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let aiContent = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      aiContent += chunk

      // 更新消息内容
      const msgIndex = aiMessages.value.findIndex(m => m.id === aiMsgId)
      if (msgIndex !== -1) {
        aiMessages.value[msgIndex].content = aiContent
      }

      scrollToBottom(true, true)
    }

    // 完成后移除 typing 状态
    const msgIndex = aiMessages.value.findIndex(m => m.id === aiMsgId)
    if (msgIndex !== -1) {
      aiMessages.value[msgIndex].isTyping = false
    }

  } catch (error) {
    console.error('AI 请求失败:', error)
    // 更新为错误消息
    const msgIndex = aiMessages.value.findIndex(m => m.id === aiMsgId)
    if (msgIndex !== -1) {
      aiMessages.value[msgIndex].content = '抱歉，我暂时无法回复，请稍后再试或联系人工客服~'
      aiMessages.value[msgIndex].isTyping = false
    }
  } finally {
    isAiTyping.value = false
    scrollToBottom(true, true)
  }
}

// 渲染 Markdown 内容
const renderMarkdown = (content) => {
  if (!content) return ''
  // 直接渲染，不做预处理，让 marked 按标准 Markdown 规则处理
  return marked.parse(content)
}

/**
 * 获取订单状态文本
 * @param {number} status - 订单状态
 * @returns {string} 状态文本
 */
const getOrderStatusText = (status) => {
  const statusMap = {
    1: '待付款',
    2: '待发货',
    3: '已发货',
    4: '待签收',
    5: '已完成',
    6: '已取消'
  }
  return statusMap[status] || '未知状态'
}

// 关闭抽屉
const handleClose = () => {
  emit('close')
}

// 监听显示状态
watch(() => props.visible, async (val) => {
  if (val) {
    // 重置消息准备状态，避免闪烁
    isMessagesReady.value = false
    // 等待消息加载完成后再滚动（initMessages 内部会调用 scrollToBottomOnLoad）
    await initMessages()
  } else {
    // 关闭时重置状态
    isMessagesReady.value = false
    showChatListPanel.value = false
  }
})

// 监听模式切换
watch(() => props.mode, async () => {
  // 重置消息准备状态，避免闪烁
  isMessagesReady.value = false
  // 等待消息加载完成后再滚动（initMessages 内部会调用 scrollToBottomOnLoad）
  await initMessages()
})

// 监听注入的消息（从 App.vue 接收到的消息）
// 使用 immediate 确保初始化时也能监听到已有消息
const messagesWatcher = watch(
  () => webSocket?.messages?.value?.slice(), // 返回数组副本触发变化检测
  (messages) => {
    if (!messages?.length || props.mode !== 'human') return

    // 获取最新的消息
    const latestMessage = messages[messages.length - 1]
    handleReceivedMessage(latestMessage)
  },
  { deep: true, immediate: true }
)

// 监听 WebSocket 连接状态，连接成功后显示提示
watch(() => wsStatus.value, (status) => {
  if (status === 'connected' && props.visible && props.mode === 'human') {
    // 只在首次连接时显示连接成功消息
    const hasConnectedMsg = humanMessages.value.some(
      msg => msg.content === '已连接到客服系统，请描述您的问题...'
    )
    if (!hasConnectedMsg) {
      humanMessages.value.push({
        id: `system-${Date.now()}`,
        type: 'system',
        content: '已连接到客服系统，请描述您的问题...',
        time: getCurrentTime()
      })
    }
  }
})
</script>

<template>
  <!-- 遮罩层 -->
  <transition name="fade">
    <div
      v-if="visible"
      class="drawer-overlay"
      @click="handleClose"
    ></div>
  </transition>

  <!-- 抽屉主体 -->
  <transition name="slide">
    <div
      v-if="visible"
      class="chat-drawer"
      :class="[mode, { resizing: isResizingDrawer }]"
      :style="{ width: drawerWidth + 'px' }"
    >
      <!-- 左侧拖拽调整宽度手柄 -->
      <div
        class="drawer-resize-handle"
        :class="{ resizing: isResizingDrawer }"
        @mousedown.prevent="startResizeDrawer"
        title="拖拽调整宽度"
      >
        <div class="drawer-resize-indicator"></div>
      </div>
      <!-- 头部 -->
      <div class="drawer-header" :style="{ background: headerConfig.gradient }">
        <div class="header-content">
          <div class="header-avatar">
            <template v-if="mode === 'ai'">
              <div class="ai-avatar">
                <el-icon :size="28"><ChatDotRound /></el-icon>
              </div>
            </template>
            <template v-else>
              <div class="human-avatar">
                <img v-if="serviceInfo.avatar" :src="serviceInfo.avatar" class="avatar-img" />
                <span v-else>{{ serviceInfo.name?.[0] || '客' }}</span>
              </div>
            </template>
          </div>
          <div class="header-info">
            <h3 class="header-title">
              {{ mode === 'human' ? serviceInfo.name || '人工客服' : headerConfig.title }}
            </h3>
            <div class="header-status">
              <span
                v-if="mode === 'human'"
                class="status-dot"
                :class="serviceInfo.status"
              ></span>
              <span class="status-text">{{ headerConfig.subtitle }}</span>
              <span v-if="mode === 'human' && wsStatus === 'connecting'" class="connecting-text">(连接中...)</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <!-- AI 模式显示会话列表按钮 -->
          <button v-if="mode === 'ai'" class="action-btn" @click="showChatListPanel = !showChatListPanel" :class="{ active: showChatListPanel }">
            <el-icon :size="20"><ChatLineRound /></el-icon>
          </button>
          <button class="close-btn" @click="handleClose">
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>
      </div>

      <!-- 主体区域 -->
      <div class="drawer-body">
        <!-- AI 会话列表面板 -->
        <div
          v-show="mode === 'ai'"
          class="chat-list-panel"
          :class="{ collapsed: !showChatListPanel, resizing: isResizing }"
          :style="{ width: chatListPanelWidth + 'px' }"
        >
            <div class="chat-list-inner">
              <div class="chat-list-header">
                <span class="panel-title">会话列表</span>
                <button class="new-chat-btn" @click="handleCreateNewChat">
                  <el-icon :size="14"><Plus /></el-icon>
                  <span>新建会话</span>
                </button>
              </div>
              <div class="chat-list-content">
                <div
                  v-for="chat in aiChatList"
                  :key="chat.chatId"
                  class="chat-list-item"
                  :class="{ active: currentChatId === chat.chatId }"
                  @click="handleSelectChat(chat)"
                >
                  <div class="chat-item-icon">
                    <el-icon :size="16"><ChatDotRound /></el-icon>
                  </div>
                  <span class="chat-item-title">{{ chat.title || '新建会话' }}</span>
                  <button class="delete-chat-btn" @click="handleDeleteChat(chat, $event)">
                    <el-icon :size="14"><Delete /></el-icon>
                  </button>
                </div>
                <div v-if="isLoadingChatList" class="chat-list-loading">
                  <el-icon class="loading-icon"><Loading /></el-icon>
                  <span>加载中...</span>
                </div>
                <div v-else-if="aiChatList.length === 0" class="chat-list-empty">
                  暂无会话，点击新建会话开始对话
                </div>
              </div>
            </div>
            <!-- 拖拽调整宽度手柄 -->
            <div
              class="resize-handle"
              :class="{ resizing: isResizing }"
              @mousedown.prevent="startResize"
              title="拖拽调整宽度"
            ></div>
          </div>

        <!-- 消息区域 -->
        <div ref="messagesRef" class="drawer-messages" :class="{ 'messages-hidden': !isMessagesReady, resizing: isResizing }" :style="mode === 'ai' && showChatListPanel ? { marginLeft: chatListPanelWidth + 'px' } : {}" @scroll="handleMessagesScroll">
          <!-- AI 模式中心欢迎界面 -->
          <div v-if="mode === 'ai' && aiMessages.length === 0 && !isLoadingHistory" class="ai-welcome-center">
            <div class="welcome-icon">
              <el-icon :size="48"><ChatDotRound /></el-icon>
            </div>
            <h2 class="welcome-title">小橘智能助手</h2>
            <p class="welcome-subtitle">AI客服24小时在线为您服务</p>
            <div class="welcome-tips">
              <div class="tip-item" @click="inputMessage = '推荐一些宠物食品'; sendMessage()">
                <el-icon class="tip-icon" :size="18"><Food /></el-icon>
                <span class="tip-text">推荐一些宠物食品</span>
              </div>
              <div class="tip-item" @click="inputMessage = '狗狗生病了怎么办'; sendMessage()">
                <el-icon class="tip-icon" :size="18"><FirstAidKit /></el-icon>
                <span class="tip-text">狗狗生病了怎么办</span>
              </div>
              <div class="tip-item" @click="inputMessage = '如何训练猫咪'; sendMessage()">
                <el-icon class="tip-icon" :size="18"><Guide /></el-icon>
                <span class="tip-text">如何训练猫咪</span>
              </div>
            </div>
          </div>
          <!-- 加载更多提示 -->
          <div v-if="loadingMore || isLoadingHistory" class="loading-more">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div v-else-if="(hasMoreMessages && mode === 'human') || (historyPage < historyTotalPages && mode === 'ai')" class="load-more-hint">
            <span>向上滑动加载更多</span>
          </div>
          <!-- 消息列表 -->
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-item"
            :class="msg.type"
          >
            <!-- 纯媒体消息不需要气泡背景 -->
            <div v-if="msg.isMedia && !msg.content" class="media-message-wrapper">
              <!-- 视频：显示视频封面+播放按钮 -->
              <div v-if="isVideoUrl(msg.mediaUrl)" class="video-thumbnail-wrapper" @click="openVideoPlayer(msg.mediaUrl)">
                <video :src="msg.mediaUrl" class="video-thumbnail" preload="metadata" muted />
                <div class="video-play-overlay">
                  <el-icon class="play-icon"><VideoPlay /></el-icon>
                </div>
              </div>
              <img v-else :src="msg.mediaUrl" alt="图片" class="media-image" />
            </div>
            <!-- 商品卡片 - 无气泡包裹 -->
            <div v-else-if="msg.product" class="card-message-wrapper">
              <div class="product-card">
                <img :src="msg.product.image" :alt="msg.product.name" class="product-image">
                <div class="product-info">
                  <p class="product-name">{{ msg.product.name }}</p>
                  <p class="product-price">¥{{ msg.product.price }}</p>
                </div>
              </div>
            </div>
            <!-- 订单卡片 - 无气泡包裹 -->
            <div v-else-if="msg.order" class="card-message-wrapper">
              <div class="order-card">
                <div class="order-header">
                  <span class="order-icon">📦</span>
                  <span class="order-id">订单号：{{ msg.order.orderId }}</span>
                </div>
                <div class="order-content">
                  <img v-if="msg.order.imgUrl" :src="msg.order.imgUrl" class="order-product-image" alt="商品图片">
                  <div v-else class="order-product-image placeholder">
                    <el-icon :size="24"><Picture /></el-icon>
                  </div>
                  <div class="order-details">
                    <p class="order-product">{{ msg.order.productName || '多件商品' }}</p>
                    <p class="order-quantity">x{{ msg.order.totalCount || 1 }}</p>
                  </div>
                  <p class="order-amount">¥{{ msg.order.totalAmount }}</p>
                </div>
                <div class="order-status" :class="'status-' + msg.order.status">
                  {{ getOrderStatusText(msg.order.status) }}
                </div>
              </div>
            </div>
            <!-- 普通消息使用气泡 -->
            <div v-else class="message-bubble">
              <!-- AI 消息使用 Markdown 渲染（仅 isMarkdown 为 true 时） -->
              <div
                v-if="msg.type === 'system' && msg.content && msg.isMarkdown"
                class="message-content markdown-body"
                v-html="renderMarkdown(msg.content)"
              ></div>
              <!-- 普通系统消息纯文本显示 -->
              <p v-else-if="msg.type === 'system' && msg.content" class="message-content">{{ msg.content }}</p>
              <!-- 用户消息纯文本显示 -->
              <p v-else-if="msg.type === 'user'" class="message-content">{{ msg.content }}</p>
              <!-- AI 正在输入指示器 -->
              <span v-if="msg.isTyping && !msg.content" class="typing-indicator">
                <span></span><span></span><span></span>
              </span>
            </div>
            <span class="message-time">{{ msg.time }}</span>
          </div>
        </div>
      </div>

      <!-- 底部输入区 -->
      <div class="drawer-footer">
        <!-- 待发送卡片预览 -->
        <div v-if="pendingCard && mode === 'human'" class="pending-card-preview">
          <div class="pending-card-header">
            <span class="pending-card-title">咨询内容</span>
            <button class="cancel-card-btn" @click="cancelPendingCard">
              <el-icon :size="14"><Close /></el-icon>
            </button>
          </div>
          <!-- 商品卡片预览 -->
          <div v-if="pendingCard.type === 'product'" class="pending-product-card">
            <img :src="pendingCard.data.image || pendingCard.data.mainImage || pendingCard.data.coverImage" :alt="pendingCard.data.name || pendingCard.data.productName" class="pending-card-image">
            <div class="pending-card-info">
              <p class="pending-card-name">{{ pendingCard.data.name || pendingCard.data.productName }}</p>
              <p class="pending-card-price">¥{{ pendingCard.data.price }}</p>
            </div>
          </div>
          <!-- 订单卡片预览 -->
          <div v-else-if="pendingCard.type === 'order'" class="pending-order-card">
            <div class="pending-order-header">
              <span class="order-icon">📦</span>
              <span class="pending-order-id">订单号：{{ pendingCard.data.orderId || pendingCard.data.id }}</span>
            </div>
            <div class="pending-order-content">
              <img v-if="pendingCard.data.imgUrl" :src="pendingCard.data.imgUrl" class="pending-order-image" alt="商品图片">
              <div v-else class="pending-order-image placeholder">
                <el-icon :size="20"><Picture /></el-icon>
              </div>
              <div class="pending-order-details">
                <p class="pending-order-product">{{ pendingCard.data.productName || (pendingCard.data.items && pendingCard.data.items[0]?.productName) || '多件商品' }}</p>
                <p class="pending-order-quantity">x{{ pendingCard.data.totalCount || 1 }}</p>
              </div>
              <p class="pending-order-amount">¥{{ pendingCard.data.totalAmount || pendingCard.data.totalPrice }}</p>
            </div>
          </div>
          <button class="send-card-btn" @click="confirmSendPendingCard">
            <el-icon :size="14"><Promotion /></el-icon>
            <span>发送卡片</span>
          </button>
        </div>

        <!-- 文件预览 -->
        <div v-if="filePreviewUrl" class="file-preview">
          <!-- 视频预览：显示缩略图+播放按钮 -->
          <div v-if="selectedFile && selectedFile.type.startsWith('video/')" class="preview-video-wrapper" @click="openVideoPlayer(filePreviewUrl)">
            <video :src="filePreviewUrl" class="preview-video" preload="metadata" muted />
            <div class="preview-play-overlay">
              <el-icon class="preview-play-icon"><VideoPlay /></el-icon>
            </div>
          </div>
          <img v-else :src="filePreviewUrl" alt="预览" />
          <button class="remove-file-btn" @click="removeSelectedFile">
            <el-icon :size="14"><Close /></el-icon>
          </button>
        </div>
        <div class="input-wrapper">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*,video/*"
            style="display: none"
            @change="handleFileSelect"
          />
          <button v-if="mode === 'human'" class="upload-btn" @click="triggerFileSelect">
            <el-icon :size="20"><Picture /></el-icon>
          </button>
          <input
            v-model="inputMessage"
            type="text"
            class="message-input"
            maxlength="2000"
            :placeholder="isAiTyping ? 'AI 正在回复中...' : (isUploading ? '上传中...' : '输入您的问题（最多2000字）')"
            :disabled="isAiTyping || isUploading"
            @keyup.enter="sendMessage"
          >
          <span class="char-count" :class="{ 'near-limit': inputMessage.length > 1800 }">{{ inputMessage.length }}/2000</span>
          <button
            class="send-btn"
            :class="{ active: (inputMessage.trim() || selectedFile) && !isAiTyping && !isUploading }"
            :disabled="isAiTyping || isUploading"
            @click="sendMessage"
          >
            <el-icon :size="18"><Promotion /></el-icon>
          </button>
        </div>
      </div>
    </div>
  </transition>

  <!-- 视频播放器全屏覆盖层 -->
  <div v-if="showVideoPlayer" class="video-player-overlay" @click.self="closeVideoPlayer">
    <div class="video-player-container">
      <button class="video-close-btn" @click="closeVideoPlayer">
        <el-icon><Close /></el-icon>
      </button>
      <CustomVideoPlayer
        v-if="currentVideoUrl"
        ref="videoPlayerRef"
        :src="currentVideoUrl"
        autoplay
        class="custom-video-player-instance"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: calc(var(--z-index-modal) - 1);
}

.chat-drawer {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  background: var(--color-bg-surface);
  z-index: var(--z-index-modal);
  display: flex;
  flex-direction: column;
  box-shadow: -4px 0 24px rgba(0, 0, 0, 0.12);

  &.resizing {
    user-select: none;
  }
}

// 左侧拖拽调整宽度手柄（整个聊天容器）
.drawer-resize-handle {
  position: absolute;
  top: 0;
  left: 0;
  width: 6px;
  height: 100%;
  cursor: col-resize;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 2px;
    transform: translateY(-50%);
    width: 2px;
    height: 40px;
    background: var(--color-border-base);
    border-radius: 2px;
    opacity: 0;
    transition: all 0.2s;
  }

  &:hover::before,
  &.resizing::before {
    background: var(--color-primary);
    opacity: 0.8;
    height: 60px;
  }
}

// 头部
.drawer-header {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
}

.human-avatar {
  width: 44px;
  height: 44px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 18px;
  font-weight: 600;
  overflow: hidden;

  .avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.header-info {
  color: #FFFFFF;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 2px;
}

.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  opacity: 0.9;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.online {
    background: #4ADE80;
  }

  &.busy {
    background: #FBBF24;
  }

  &.offline {
    background: #9CA3AF;
  }
}

.connecting-text {
  font-size: 12px;
  opacity: 0.8;
  font-style: italic;
  margin-left: 4px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #FFFFFF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;

  &:hover, &.active {
    background: rgba(255, 255, 255, 0.35);
  }
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: #FFFFFF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.35);
  }
}

// 主体区域
.drawer-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

// 会话列表面板
.chat-list-panel {
  background: var(--color-bg-secondary);
  border-right: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  min-width: 180px;
  max-width: 400px;
  overflow: hidden;
  will-change: transform;
  transform: translateX(-100%);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;

  &:not(.collapsed) {
    transform: translateX(0);
  }

  // 拖拽时禁用过渡动画，避免视觉延迟
  &.resizing {
    transition: none !important;
    will-change: width;
  }
}

.chat-list-inner {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

// 拖拽调整宽度手柄
.resize-handle {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 100%;
  cursor: col-resize;
  z-index: 100;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    right: 2px;
    transform: translateY(-50%);
    width: 3px;
    height: 40px;
    background: var(--color-border-base);
    border-radius: 2px;
    opacity: 0.4;
    transition: all 0.2s;
  }

  &:hover::before,
  &.resizing::before {
    background: var(--color-text-secondary);
    opacity: 0.8;
    height: 60px;
  }
}

.chat-list-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  white-space: nowrap;
  flex-shrink: 0;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.new-chat-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  background: var(--color-primary);
  color: #FFFFFF;
  border-radius: var(--radius-base);
  font-size: 12px;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.9;
  }
}

.chat-list-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-list-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
  margin-bottom: 4px;
  white-space: nowrap;

  &:hover {
    background: var(--color-bg-tertiary);

    .delete-chat-btn {
      opacity: 1;
    }
  }

  &.active {
    background: rgba(255, 138, 91, 0.1);

    .chat-item-title {
      color: var(--color-primary);
      font-weight: 500;
    }
  }
}

.chat-item-icon {
  width: 28px;
  height: 28px;
  background: var(--color-bg-tertiary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.chat-item-title {
  flex: 1;
  font-size: 13px;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delete-chat-btn {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0;
  transition: all 0.2s;

  &:hover {
    background: rgba(239, 68, 68, 0.1);
    color: #EF4444;
  }
}

.chat-list-loading,
.chat-list-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  color: var(--color-text-tertiary);
  font-size: 13px;
  text-align: center;
}

// 消息区域
.drawer-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: margin-left 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  // 隐藏状态：使用 visibility 保留布局空间，防止输入框跑到上面
  &.messages-hidden {
    visibility: hidden;
    opacity: 0;
  }

  // 拖拽时禁用过渡动画，避免与会话列表动画不同步
  &.resizing {
    transition: none !important;
  }
}

// AI 中心欢迎界面
.ai-welcome-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 40px 20px;
  text-align: center;
  min-height: 400px;

  .welcome-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, #FF8A5B 0%, #FFB088 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #FFFFFF;
    margin-bottom: 20px;
    box-shadow: 0 4px 16px rgba(255, 138, 91, 0.3);
  }

  .welcome-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0 0 8px;
  }

  .welcome-subtitle {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0 0 32px;
  }

  .welcome-tips {
    display: flex;
    flex-direction: column;
    gap: 12px;
    width: 100%;
    max-width: 280px;

    .tip-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      background: var(--color-bg-secondary);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid var(--color-border-light);

      &:hover {
        background: var(--color-bg-tertiary);
        transform: translateX(4px);
        border-color: var(--color-primary-light);
      }

      .tip-icon {
        flex-shrink: 0;
        color: var(--color-primary);
      }

      .tip-text {
        font-size: 14px;
        color: var(--color-text-primary);
      }
    }
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
  color: var(--color-text-secondary);

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

// 消息项
.message-item {
  display: flex;
  flex-direction: column;
  max-width: 85%;

  &.system {
    align-self: flex-start;

    .message-bubble {
      background: #F5F5F5;
      color: var(--color-text-primary);
      border-radius: 4px 16px 16px 16px;
    }

    .message-time {
      text-align: left;
    }
  }

  &.user {
    align-self: flex-end;

    .message-bubble {
      background: var(--color-primary);
      color: #FFFFFF;
      border-radius: 16px 4px 16px 16px;
    }

    .message-time {
      text-align: right;
    }
  }
}

.message-bubble {
  padding: 12px 16px;
}

.message-content {
  margin: 0;
  line-height: 1.6;
  word-break: break-word;
}

// Markdown 渲染样式
.markdown-body {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;

  :deep(p) {
    margin: 0 0 0.6em;
    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: 0.8em 0 0.4em;
    font-weight: 600;
    &:first-child {
      margin-top: 0;
    }
  }

  :deep(h1) { font-size: 1.1em; }
  :deep(h2) { font-size: 1.05em; }
  :deep(h3), :deep(h4), :deep(h5), :deep(h6) { font-size: 1em; }

  :deep(ul), :deep(ol) {
    margin: 0 0 0.6em;
    padding-left: 1.5em;
    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(li) {
    margin: 0.2em 0;
  }

  :deep(h1) { font-size: 1.1em; font-weight: 600; }
  :deep(h2) { font-size: 1.05em; font-weight: 600; }
  :deep(h3), :deep(h4), :deep(h5), :deep(h6) { font-size: 1em; font-weight: 600; }

  :deep(code) {
    background: rgba(0, 0, 0, 0.06);
    padding: 1px 4px;
    border-radius: 3px;
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 0.85em;
  }

  :deep(pre) {
    background: #1e1e1e;
    color: #d4d4d4;
    padding: 8px 10px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 4px 0 !important;
    font-size: 12px;

    code {
      background: none;
      padding: 0;
      color: inherit;
    }
  }

  :deep(blockquote) {
    padding: 4px 8px !important;
    border-left: 2px solid var(--color-primary);
    background: rgba(255, 138, 91, 0.06);
    border-radius: 0 4px 4px 0;
  }

  :deep(a) {
    color: var(--color-primary);
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }

  :deep(strong) {
    font-weight: 600;
  }

  :deep(hr) {
    border: none;
    border-top: 1px solid var(--color-border-base);
    margin: 4px 0 !important;
  }

  :deep(table) {
    border-collapse: collapse;
    font-size: 12px;
  }

  :deep(th), :deep(td) {
    border: 1px solid var(--color-border-base);
    padding: 2px 6px !important;
  }

  :deep(th) {
    background: rgba(0, 0, 0, 0.04);
    font-weight: 600;
  }
}

// AI 正在输入指示器
.typing-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;

  span {
    width: 6px;
    height: 6px;
    background: var(--color-text-tertiary);
    border-radius: 50%;
    animation: typing-bounce 1.4s infinite ease-in-out both;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
    &:nth-child(3) {
      animation-delay: 0s;
    }
  }
}

@keyframes typing-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-time {
  font-size: 11px;
  color: var(--color-text-tertiary);
  margin-top: 4px;
  padding: 0 4px;
}

// 商品卡片
.product-card {
  display: flex;
  gap: 10px;
  width: 260px;
  margin-top: 10px;
  padding: 10px;
  background: #FFFFFF;
  border-radius: 8px;
  cursor: pointer;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-name {
  font-size: 13px;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  font-size: 14px;
  color: var(--color-primary);
  font-weight: 600;
  margin: 0;
}

// 订单卡片
.order-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 280px;
  margin-top: 10px;
  padding: 12px;
  background: #FFFFFF;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid var(--color-border-light);

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  .order-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 8px;
    border-bottom: 1px dashed var(--color-border-light);

    .order-icon {
      font-size: 16px;
    }

    .order-id {
      font-size: 12px;
      color: var(--color-text-secondary);
    }
  }

  .order-content {
    display: flex;
    align-items: center;
    gap: 10px;

    .order-product-image {
      width: 50px;
      height: 50px;
      object-fit: cover;
      border-radius: 4px;

      &.placeholder {
        background: var(--color-bg-tertiary);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-text-tertiary);
      }
    }

    .order-details {
      flex: 1;

      .order-product {
        font-size: 13px;
        color: var(--color-text-primary);
        margin: 0 0 4px;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .order-quantity {
        font-size: 12px;
        color: var(--color-text-tertiary);
        margin: 0;
      }
    }

    .order-amount {
      font-size: 14px;
      color: var(--color-primary);
      font-weight: 600;
      margin: 0;
    }
  }

  .order-status {
    font-size: 12px;
    padding: 4px 8px;
    border-radius: 4px;
    text-align: center;
    background: var(--color-bg-tertiary);
    color: var(--color-text-secondary);

    &.status-1 { background: #FEF3C7; color: #D97706; }
    &.status-2 { background: #DBEAFE; color: #2563EB; }
    &.status-3 { background: #DBEAFE; color: #2563EB; }
    &.status-4 { background: #F3E8FF; color: #9333EA; }
    &.status-5 { background: #D1FAE5; color: #059669; }
    &.status-6 { background: #FEE2E2; color: #DC2626; }
  }
}

// 媒体消息
.media-message-wrapper {
  max-width: 240px;
  border-radius: 12px;
  overflow: hidden;

  .media-image {
    width: 100%;
    height: auto;
    max-height: 200px;
    object-fit: cover;
    border-radius: 12px;
    cursor: pointer;
  }

  .video-thumbnail-wrapper {
    position: relative;
    cursor: pointer;
    border-radius: 12px;
    overflow: hidden;

    .video-thumbnail {
      width: 100%;
      height: 150px;
      object-fit: cover;
    }

    .video-play-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: background 0.2s;

      &:hover {
        background: rgba(0, 0, 0, 0.3);
      }

      .play-icon {
        font-size: 40px;
        color: #FFFFFF;
      }
    }
  }
}

// 卡片消息
.card-message-wrapper {
  align-self: flex-start;
}

// 底部输入区
.drawer-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-bg-surface);
  flex-shrink: 0;
}

// 待发送卡片预览
.pending-card-preview {
  background: var(--color-bg-secondary);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
}

.pending-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;

  .pending-card-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--color-text-primary);
  }

  .cancel-card-btn {
    width: 24px;
    height: 24px;
    border: none;
    background: var(--color-bg-tertiary);
    border-radius: 50%;
    color: var(--color-text-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: var(--color-border-light);
    }
  }
}

.pending-product-card {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: var(--color-bg-surface);
  border-radius: 8px;
  margin-bottom: 10px;

  .pending-card-image {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 4px;
  }

  .pending-card-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;

    .pending-card-name {
      font-size: 13px;
      color: var(--color-text-primary);
      margin: 0 0 4px;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .pending-card-price {
      font-size: 14px;
      color: var(--color-primary);
      font-weight: 600;
      margin: 0;
    }
  }
}

.pending-order-card {
  background: var(--color-bg-surface);
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;

  .pending-order-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 8px;
    padding-bottom: 8px;
    border-bottom: 1px dashed var(--color-border-light);

    .order-icon {
      font-size: 14px;
    }

    .pending-order-id {
      font-size: 12px;
      color: var(--color-text-secondary);
    }
  }

  .pending-order-content {
    display: flex;
    align-items: center;
    gap: 10px;

    .pending-order-image {
      width: 40px;
      height: 40px;
      object-fit: cover;
      border-radius: 4px;

      &.placeholder {
        background: var(--color-bg-tertiary);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-text-tertiary);
      }
    }

    .pending-order-details {
      flex: 1;

      .pending-order-product {
        font-size: 12px;
        color: var(--color-text-primary);
        margin: 0 0 2px;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .pending-order-quantity {
        font-size: 11px;
        color: var(--color-text-tertiary);
        margin: 0;
      }
    }

    .pending-order-amount {
      font-size: 13px;
      color: var(--color-primary);
      font-weight: 600;
      margin: 0;
    }
  }
}

.send-card-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px;
  border: none;
  background: var(--color-primary);
  color: #FFFFFF;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.9;
  }
}

// 文件预览
.file-preview {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;

  img, .preview-video-wrapper {
    max-width: 120px;
    max-height: 120px;
    border-radius: 8px;
  }

  .preview-video-wrapper {
    position: relative;
    cursor: pointer;

    .preview-video {
      width: 120px;
      height: 90px;
      object-fit: cover;
    }

    .preview-play-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;

      .preview-play-icon {
        font-size: 24px;
        color: #FFFFFF;
      }
    }
  }

  .remove-file-btn {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 20px;
    height: 20px;
    border: none;
    background: var(--color-error);
    color: #FFFFFF;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  }
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--color-bg-secondary);
  border-radius: 24px;
  padding: 6px 6px 6px 12px;
}

.upload-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    background: var(--color-bg-tertiary);
    color: var(--color-primary);
  }
}

.message-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 14px;
  color: var(--color-text-primary);
  outline: none;
  padding: 4px 0;

  &::placeholder {
    color: var(--color-text-tertiary);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.char-count {
  font-size: 11px;
  color: var(--color-text-tertiary);
  white-space: nowrap;

  &.near-limit {
    color: var(--color-warning);
  }
}

.send-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: var(--color-bg-tertiary);
  color: var(--color-text-tertiary);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    background: var(--color-primary);
    color: #FFFFFF;
  }

  &.active {
    background: var(--color-primary);
    color: #FFFFFF;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 视频播放器覆盖层
.video-player-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: calc(var(--z-index-modal) + 10);
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player-container {
  position: relative;
  width: 90%;
  max-width: 900px;
  aspect-ratio: 16 / 9;
}

.video-close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #FFFFFF;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.custom-video-player-instance {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

// 响应式
@media (max-width: 768px) {
  .chat-drawer {
    width: 100% !important;
    min-width: 100%;
    max-width: 100%;
  }

  .drawer-resize-handle {
    display: none; // 移动端隐藏拖拽手柄
  }

  .chat-list-panel {
    min-width: 160px;
    max-width: 300px;
  }

  .resize-handle {
    width: 16px; // 移动端增大拖拽区域，方便触摸

    &::before {
      width: 4px;
      height: 50px;
    }

    &:hover::before,
    &.resizing::before {
      height: 80px;
    }
  }
}
</style>
