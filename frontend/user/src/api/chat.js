import { API_BASE_URL } from './config'

// WebSocket连接URL
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

// 从 userInfo 中获取 token
const getToken = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.token || ''
  } catch (e) {
    return ''
  }
}

/**
 * 获取WebSocket连接URL
 * @param token JWT令牌
 * @returns WebSocket连接URL
 */
export const getWebSocketUrl = (token) => {
  const tokenToUse = token || getToken()
  return `${WS_BASE_URL}/api/customer-chat?Authorization=${encodeURIComponent('Bearer ' + tokenToUse)}`
}

// ============ 消息类型常量 ============

/** 系统消息 */
export const MESSAGE_TYPE_SYSTEM = 1
/** 聊天消息 */
export const MESSAGE_TYPE_CHAT = 2
/** 商品消息 */
export const MESSAGE_TYPE_PRODUCT = 3
/** 订单消息 */
export const MESSAGE_TYPE_ORDER = 4
/** 媒体消息 */
export const MESSAGE_TYPE_MEDIA = 5
/** 状态消息 */
export const MESSAGE_TYPE_STATUS = 6

/** 在线状态 */
export const ONLINE_STATUS = 1
/** 离线状态 */
export const OFFLINE_STATUS = 2

// ============ API接口 ============

export const fetchMessageList = async (params) => {
  const response = await fetch(`${API_BASE_URL}/api/user/customer-chat/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    throw new Error('获取消息列表失败')
  }

  return response.json()
}

/**
 * 获取客服信息
 * @returns 客服信息
 */
export const fetchCustomerServiceInfo = async (userId) => {
  const response = await fetch(`${API_BASE_URL}/api/user/customer-chat/getCustomerServerInfo?userId=${userId}`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('获取客服信息失败')
  }

  return response.json()
}

/**
 * 标记消息已读
 * @param userId 用户ID
 * @param sendUserId 发送者ID（客服ID）
 * @returns 是否成功
 */
export const readMessages = async (userId, sendUserId) => {
  const response = await fetch(`${API_BASE_URL}/api/user/customer-chat/read?userId=${userId}&sendUserId=${sendUserId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('标记已读失败')
  }

  return response.json()
}

/**
 * 上传媒体文件
 * @param mediaType 媒体类型
 * @param file 文件
 * @returns { mediaUrl, mediaType } 媒体URL和类型
 */
export const uploadMedia = async (mediaType, file) => {
  const formData = new FormData()
  formData.append('mediaType', mediaType)
  formData.append('file', file)

  const response = await fetch(`${API_BASE_URL}/api/user/customer-chat/uploadMedia`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    },
    body: formData
  })

  if (!response.ok) {
    throw new Error('上传媒体文件失败')
  }

  return response.json()
}

/**
 * 检查是否有未读消息
 * @param userId 用户ID
 * @returns {Boolean} 是否有未读消息
 */
export const checkUnreadMessages = async (userId) => {
  const response = await fetch(`${API_BASE_URL}/api/user/customer-chat/hasUnread?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('检查未读消息失败')
  }

  const result = await response.json()
  return result.data === true
}

// ============ AI 聊天会话相关接口 ============

/**
 * 获取AI会话列表
 * @param {Object} params - 查询参数
 * @param {number} params.userId - 用户ID
 * @param {number} params.currentPage - 当前页
 * @param {number} params.pageSize - 每页大小
 * @returns {Promise<PageResult>} 会话列表分页结果
 */
export const fetchAIChatList = async (params) => {
  const response = await fetch(`${API_BASE_URL}/api/user/ai/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    throw new Error('获取AI会话列表失败')
  }

  return response.json()
}

/**
 * 创建新会话
 * @param {number} userId - 用户ID
 * @returns {Promise<{chatId: number, conversationId: string}>} 会话信息
 */
export const createAIChat = async (userId) => {
  const response = await fetch(`${API_BASE_URL}/api/user/ai/create?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('创建会话失败')
  }

  return response.json()
}

/**
 * 获取会话标题（根据第一条消息生成）
 * @param {number} chatId - 会话ID
 * @param {string} firstMessage - 第一条消息内容
 * @returns {Promise<string>} 生成的标题
 */
export const generateChatTitle = async (chatId, firstMessage) => {
  const response = await fetch(`${API_BASE_URL}/api/user/ai/title?chatId=${chatId}&firstMessage=${encodeURIComponent(firstMessage)}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('生成会话标题失败')
  }

  return response.json()
}

/**
 * 获取AI会话历史记录
 * @param {Object} params - 查询参数
 * @param {number} params.chatId - 会话ID
 * @param {number} params.userId - 用户ID
 * @param {number} params.currentPage - 当前页
 * @param {number} params.pageSize - 每页大小
 * @returns {Promise<PageResult>} 历史记录分页结果
 */
export const fetchAIChatHistory = async (params) => {
  const response = await fetch(`${API_BASE_URL}/api/user/ai/history`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(params)
  })

  if (!response.ok) {
    throw new Error('获取历史记录失败')
  }

  return response.json()
}

/**
 * 删除会话
 * @param {number} chatId - 会话ID
 * @param {number} userId - 用户ID
 * @returns {Promise<boolean>} 是否成功
 */
export const deleteAIChat = async (chatId, userId) => {
  const response = await fetch(`${API_BASE_URL}/api/user/ai/delete?chatId=${chatId}&userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('删除会话失败')
  }

  return response.json()
}
