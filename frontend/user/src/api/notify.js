import { API_BASE_URL } from './config'

// 从 userInfo 中获取 token
const getToken = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.token || ''
  } catch (e) {
    return ''
  }
}

// 从 userInfo 中获取 userId
const getUserId = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.userId || null
  } catch (e) {
    return null
  }
}

// ============ 消息类型常量 ============

/** 订单通知 */
export const NOTIFY_TYPE_ORDER = 1
/** 审核通知 */
export const NOTIFY_TYPE_AUDIT = 2
/** 点赞回复通知 */
export const NOTIFY_TYPE_LIKE_REPLY = 3
/** 禁言通知 */
export const NOTIFY_TYPE_BAN = 4

/** 通知类型映射 */
export const NOTIFY_TYPE_MAP = {
  [NOTIFY_TYPE_ORDER]: { label: '订单通知', color: '#FF8A5B' },
  [NOTIFY_TYPE_AUDIT]: { label: '审核通知', color: '#409EFF' },
  [NOTIFY_TYPE_LIKE_REPLY]: { label: '互动通知', color: '#67C23A' },
  [NOTIFY_TYPE_BAN]: { label: '禁言通知', color: '#F56C6C' }
}

// ============ API接口 ============

/**
 * 获取通知列表
 * @param params { currentPage, pageSize, type }
 * @returns PageVO<NotifyListVO>
 */
export const fetchNotifyList = async (params) => {
  const userId = getUserId()
  if (!userId) {
    throw new Error('用户未登录')
  }

  const response = await fetch(`${API_BASE_URL}/api/user/notify/list`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify({
      userId,
      ...params
    })
  })

  if (!response.ok) {
    throw new Error('获取通知列表失败')
  }

  return response.json()
}

/**
 * 标记单条消息已读
 * @param notifyId 通知ID
 * @returns Boolean
 */
export const readNotify = async (notifyId) => {
  const userId = getUserId()
  if (!userId) {
    throw new Error('用户未登录')
  }

  const response = await fetch(`${API_BASE_URL}/api/user/notify/read?userId=${userId}&notifyId=${notifyId}`, {
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
 * 批量标记消息已读
 * @param notifyIds 通知ID列表
 * @returns Boolean
 */
export const readAllNotify = async (notifyIds) => {
  const userId = getUserId()
  if (!userId) {
    throw new Error('用户未登录')
  }

  // 将数组转换为逗号分隔的字符串
  const idsStr = notifyIds.join(',')

  const response = await fetch(`${API_BASE_URL}/api/user/notify/readAll?userId=${userId}&notifyIds=${idsStr}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('批量标记已读失败')
  }

  return response.json()
}

/**
 * 获取未读消息数量
 * @returns Long 未读数量
 */
export const fetchUnreadCount = async () => {
  const userId = getUserId()
  if (!userId) {
    return 0
  }

  const response = await fetch(`${API_BASE_URL}/api/user/notify/unreadCount?userId=${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('获取未读数量失败')
  }

  const result = await response.json()
  return result.data || 0
}

/**
 * 删除消息
 * @param notifyId 通知ID
 * @returns Boolean
 */
export const deleteNotify = async (notifyId) => {
  const userId = getUserId()
  if (!userId) {
    throw new Error('用户未登录')
  }

  const response = await fetch(`${API_BASE_URL}/api/user/notify/delete?userId=${userId}&notifyId=${notifyId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getToken()}`
    }
  })

  if (!response.ok) {
    throw new Error('删除消息失败')
  }

  return response.json()
}
