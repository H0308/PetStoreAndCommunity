import request from '@/utils/http'

/**
 * 管理员主页相关API
 */

// 获取当前系统用户数量
export function fetchUserCount(userId: number) {
  return request.get<number>({
    url: `/api/admin/index/userCount/${userId}`
  })
}

// 获取系统中的商品数量
export function fetchProductCount(userId: number) {
  return request.get<number>({
    url: `/api/admin/index/productCount/${userId}`
  })
}

// 成功交易的订单数量
export function fetchOrderCount(userId: number) {
  return request.get<number>({
    url: `/api/admin/index/orderCount/${userId}`
  })
}

// 获取总交易金额
export function fetchOrderPrice(userId: number) {
  return request.get<number>({
    url: `/api/admin/index/orderPrice/${userId}`
  })
}

// 总销售金额占比最高的商品（前10，饼图）
export interface TopTotalPriceProduct {
  name: string
  totalPrice: number
}

export function fetchTopTotalPrice(userId: number) {
  return request.get<TopTotalPriceProduct[]>({
    url: `/api/admin/index/topTotalPrice/${userId}`
  })
}

// 用户增长量（日统计，统计最近七天，折线图）
export interface NewUserCount {
  createTime: string
  count: number
}

export function fetchNewUserCount(userId: number) {
  return request.get<NewUserCount[]>({
    url: `/api/admin/index/newUserCount/${userId}`
  })
}

// 热门话题排行（前10，横向柱状图）
export interface HotTopic {
  name: string
  count: number
}

export function fetchHotTopic(userId: number) {
  return request.get<HotTopic[]>({
    url: `/api/admin/index/hotTopic/${userId}`
  })
}

// 获取当前缺货的商品（表格）
export interface OutOfStockProduct {
  productName: string
  mainCategoryName: string
  subCategoryName: string
}

export function fetchOutOfStock(userId: number) {
  return request.get<OutOfStockProduct[]>({
    url: `/api/admin/index/outOfStock/${userId}`
  })
}
