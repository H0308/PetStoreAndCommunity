import request from '@/utils/http'

// WebSocket连接URL（根据环境配置）
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

/**
 * 获取WebSocket连接URL
 * @param token JWT令牌
 * @returns WebSocket连接URL
 */
export const getWebSocketUrl = (token: string): string => {
  return `${WS_BASE_URL}/api/customer-chat?Authorization=${encodeURIComponent('Bearer ' + token)}`
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

// ============ 接口类型定义 ============

/**
 * WebSocket通用消息
 */
export interface CommonMessage {
  /** 发送者ID */
  senderId?: number
  /** 发送者名称 */
  senderName?: string
  /** 发送者头像 */
  senderAvatar?: string
  /** 接收者ID */
  receiverId?: number
  /** 消息类型：1=系统，2=聊天，3=商品，4=订单，5=媒体，6=状态 */
  type: number
  /** 消息内容 */
  content?: string
  /** 媒体文件URL */
  mediaUrl?: string
  /** 媒体类型 */
  mediaType?: string
  /** 在线状态：1=在线，2=离线（用于状态消息） */
  onlineStatus?: number
  /** 创建时间 */
  createTime?: string
}

/**
 * 商品卡片数据
 */
export interface ProductCard {
  productId: number
  name: string
  price: number
  image: string
  status?: number
}

/**
 * 订单卡片数据
 */
export interface OrderCard {
  orderId: string
  productName: string
  totalAmount: number
  status: number
  createTime?: string
}

/**
 * 消息实体
 */
export interface Message {
  /** 消息ID */
  id: number
  /** 接收用户ID */
  receiveUserId: number
  /** 发送用户ID */
  sendUserId: number
  /** 消息内容 */
  message: string
  /** 消息类型 */
  type: number
  /** 创建时间 */
  createTime: string
  /** 媒体URL */
  mediaUrl?: string
  /** 商品卡片数据 */
  product?: ProductCard
  /** 订单卡片数据 */
  order?: OrderCard
}

/**
 * 最近消息
 */
export interface LatestMessage {
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 用户头像 */
  avatarUrl: string
  /** 最新消息ID */
  messageId: number
  /** 最新消息内容 */
  message: string
  /** 未读消息数 */
  unreadCount: number
  /** 创建时间 */
  createTime: string
  /** 在线状态 */
  online?: boolean
}

/**
 * 历史消息查询DTO
 */
export interface HistoryMessageDTO {
  /** 发送者ID（要查询的用户） */
  sendUserId: number
  /** 当前用户ID（管理员） */
  userId: number
  /** 当前页 */
  currentPage?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 最近消息查询DTO
 */
export interface LatestMessageDTO {
  /** 当前用户ID（管理员） */
  userId: number
  /** 搜索关键词（用户名） */
  keyword?: string
  /** 当前页 */
  currentPage?: number
  /** 每页大小 */
  pageSize?: number
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  /** 当前页 */
  currentPage: number
  /** 每页大小 */
  pageSize: number
  /** 总记录数 */
  totalCount: number
  /** 总页数 */
  totalPage: number
  /** 数据列表 */
  totalRecords: T[]
}

// ============ API接口 ============

/**
 * 获取最近消息列表
 * @param data 查询参数
 * @returns 最近消息列表
 */
export const fetchLatestMessages = (data: LatestMessageDTO): Promise<LatestMessage[]> => {
  return request.post({ url: '/api/admin/chat/latest', data })
}

/**
 * 获取历史消息
 * @param data 查询参数
 * @returns 历史消息分页结果
 */
export const fetchHistoryMessages = (data: HistoryMessageDTO): Promise<PageResult<Message>> => {
  return request.post({ url: '/api/admin/chat/history', data })
}

/**
 * 标记消息已读（管理员端）
 * @param userId 管理员ID
 * @param sendUserId 发送者ID（用户）
 * @returns 是否成功
 */
export const readMessages = (userId: number, sendUserId: number) => {
  return request.post({ url: `/api/admin/chat/read?userId=${userId}&sendUserId=${sendUserId}` })
}

/**
 * 上传媒体文件（管理员端使用用户接口）
 * @param mediaType 媒体类型
 * @param file 文件
 * @returns { mediaUrl, mediaType } 媒体URL和类型
 */
export const uploadMedia = (mediaType: number, file: File) => {
  const formData = new FormData()
  formData.append('mediaType', String(mediaType))
  formData.append('file', file)

  return request.post<{
    mediaUrl: string
    mediaType: number
  }>({
    url: '/api/user/customer-chat/uploadMedia',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 用户信息（聊天用）
 */
export interface ChatUserInfo {
  userId: number
  username: string
  avatar: string
  onlineStatus: number // 1=在线, 0=离线
}

/**
 * 获取用户信息（包含在线状态）
 * @param userId 管理员ID
 * @param sendUserId 用户ID
 * @returns 用户信息
 */
export const fetchUserInfo = (userId: number, sendUserId: number): Promise<ChatUserInfo> => {
  return request.get({ url: `/api/admin/chat/getUserInfo?userId=${userId}&sendUserId=${sendUserId}` })
}
