import request from '@/utils/http'

/**
 * 订单管理相关API
 */

// 订单列表请求参数
export interface OrderListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 订单筛选参数
export interface OrderListFilterDTO {
  productName?: string | null
  username?: string | null
  orderId?: number | null
  productType?: number | null
  status?: number | null
  refundFlag?: number | null
  startTime?: string | null
  endTime?: string | null
}

// 订单列表请求（包含筛选）
export interface OrderListWithFilterDTO {
  orderListDTO: OrderListDTO
  orderListFilterDTO?: OrderListFilterDTO
}

// 订单列表项
export interface OrderListItem {
  orderId: number
  userId: number
  username: string
  productId: number
  productType: number // 1-宠物，2-宠物用品
  productName: string
  imgUrl: string
  totalCount: number
  totalPrice: number
  phone: string
  receiptName: string
  receiptAddress: string
  createTime: string
  status: number // 1-待支付，2-待发货，3-已发货，4-待签收，5-已收货，6-已取消
  refundFlag: number // 0-未退款，1-退款中
  refundId?: number // 退款记录ID（退款中时存在）
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 订单状态枚举
export const ORDER_STATUS = {
  TO_PAY: 1,        // 待支付
  TO_DELIVER: 2,    // 待发货
  DELIVERED: 3,     // 已发货
  TO_SIGN: 4,       // 待签收
  SIGNED: 5,        // 已收货
  CANCELLED: 6      // 已取消
} as const

// 订单状态文本映射
export const ORDER_STATUS_TEXT: Record<number, string> = {
  [ORDER_STATUS.TO_PAY]: '待支付',
  [ORDER_STATUS.TO_DELIVER]: '待发货',
  [ORDER_STATUS.DELIVERED]: '已发货',
  [ORDER_STATUS.TO_SIGN]: '待签收',
  [ORDER_STATUS.SIGNED]: '已收货',
  [ORDER_STATUS.CANCELLED]: '已取消'
}

// 订单状态样式映射
export const ORDER_STATUS_TYPE: Record<number, string> = {
  [ORDER_STATUS.TO_PAY]: 'warning',
  [ORDER_STATUS.TO_DELIVER]: 'primary',
  [ORDER_STATUS.DELIVERED]: 'info',
  [ORDER_STATUS.TO_SIGN]: 'info',
  [ORDER_STATUS.SIGNED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'info'
}

// 获取订单列表
export function fetchOrderList(params: OrderListWithFilterDTO) {
  return request.post<PageResponse<OrderListItem>>({
    url: '/api/admin/order/list',
    data: params
  })
}

// 修改订单请求参数
export interface AdminOrderChangeDTO {
  userId: number
  orderId: number
  receiptName?: string | null
  receiptAddress?: string | null
  phone?: string | null
}

// 修改订单收货信息
export function changeOrder(params: AdminOrderChangeDTO) {
  return request.post<boolean>({
    url: '/api/admin/order/change',
    data: params
  })
}

// 退款信息VO
export interface RefundInfoVO {
  refundId: number
  orderId: number
  userId: number
  message: string
  createTime: string
}

// 获取退款信息（通过orderId）
export function fetchRefundInfo(orderId: number, userId: number) {
  return request.get<RefundInfoVO>({
    url: '/api/admin/order/getRefund',
    params: { orderId, userId }
  })
}

// 处理退款请求参数
export interface HandleRefundDTO {
  refundId: number       // 退款记录ID
  orderId: number
  userIdNotify: number   // 被通知的用户ID（订单所属用户）
  userId: number         // 操作的管理员ID
  opFlag: number         // 0-不予退款，1-予以退款
}

// 处理退款
export function handleRefund(params: HandleRefundDTO) {
  return request.post<boolean>({
    url: '/api/admin/order/handleRefund',
    data: params
  })
}

// 发货
export function deliverOrder(orderId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/order/deliver?orderId=${orderId}&userId=${userId}`
  })
}

// 导出订单到Excel
export function exportOrdersToExcel(orderIds?: number[]) {
  if (orderIds && orderIds.length > 0) {
    const params = new URLSearchParams()
    orderIds.forEach(id => params.append('orderIds', id.toString()))
    return `/api/admin/order/excel?${params.toString()}`
  }
  // 导出所有订单时不传orderIds
  return '/api/admin/order/excel'
}


// 物流信息VO
export interface TransportVO {
  logisticsId: number
  transportType: number // 1-空运，2-陆运
  orderStatus: number
  originLat: string
  originLng: string
  destLat: string
  destLng: string
  currentLat?: string
  currentLng?: string
}

// 获取物流信息
export function fetchLogistics(orderId: number, userId: number, productType: number) {
  return request.post<TransportVO>({
    url: '/api/user/order/logistics',
    data: { orderId, userId, productType }
  })
}

// 检查是否到达并保存当前位置
export interface CheckDeliveredDTO {
  logisticsId: number
  currentLat: string
  currentLng: string
}

export interface CheckDeliveredVO {
  isArrived: boolean
}

export function checkDelivered(params: CheckDeliveredDTO) {
  return request.post<CheckDeliveredVO>({
    url: '/api/user/order/checkDelivered',
    data: params
  })
}
