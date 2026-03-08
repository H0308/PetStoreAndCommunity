/**
 * API 配置文件
 * 集中管理后端接口地址
 */

// 后端API基础地址
export const API_BASE_URL = 'http://localhost:8080'

// 商城相关接口 - 统一使用首页接口
export const PRODUCT_API = {
  // 轮播图
  GET_CAROUSELS: `${API_BASE_URL}/api/user/index/getCarousels`,
  // 一级分类
  GET_SUPER_CATEGORIES: `${API_BASE_URL}/api/user/index/getSuperCategories`,
  // 二级分类
  GET_SUB_CATEGORIES: `${API_BASE_URL}/api/user/index/getSubCategories`,
  // 最新商品
  GET_LATEST_PRODUCTS: `${API_BASE_URL}/api/user/index/getLatestProducts`,
  // 获取指定一级分类下的所有商品
  GET_PRODUCTS_UNDER_CATEGORY: `${API_BASE_URL}/api/user/index/getProductsUnderMainCategory`,
  // 获取指定二级分类下的所有商品
  GET_PRODUCTS_UNDER_SUB_CATEGORY: `${API_BASE_URL}/api/user/index/getProductsUnderSubCategory`,
  // 宠物商品详情
  GET_PET_DETAIL: `${API_BASE_URL}/api/user/product/pet/getPetDetail`,
  // 宠物用品详情
  GET_SUPPLY_DETAIL: `${API_BASE_URL}/api/user/product/supply/getSupplyDetail`,
}

// 用户认证相关接口
export const AUTH_API = {
  // 登录
  LOGIN: `${API_BASE_URL}/api/user/auth/login`,
  // 注册
  REGISTER: `${API_BASE_URL}/api/user/auth/register`,
  // Google OAuth 授权 URL
  GOOGLE_AUTH_URL: `${API_BASE_URL}/api/user/auth/google/url`,
  // Google OAuth 回调
  GOOGLE_CALLBACK: `${API_BASE_URL}/api/user/auth/google/callback`,
}

// 验证码相关接口
export const CAPTCHA_API = {
  // 获取验证码图片
  GET_CAPTCHA: `${API_BASE_URL}/api/user/captcha/getCaptcha`,
  // 校验图片验证码
  CHECK_CAPTCHA: `${API_BASE_URL}/api/user/captcha/check`,
  // 发送邮箱验证码
  GET_EMAIL_CAPTCHA: `${API_BASE_URL}/api/user/captcha/getEmailCaptcha`,
  // 校验邮箱验证码
  CHECK_EMAIL_CAPTCHA: `${API_BASE_URL}/api/user/captcha/checkEmailCaptcha`,
}

// 评论相关接口
export const COMMENT_API = {
  // 获取商品评论
  GET_PRODUCT_COMMENTS: `${API_BASE_URL}/api/user/comment/getProductComment`,
  // 发布商品评论
  POST_PRODUCT_COMMENT: `${API_BASE_URL}/api/user/comment/postProductComment`,
  // 上传评论媒体
  POST_COMMENT_MEDIA: `${API_BASE_URL}/api/user/comment/postCommentMedia`,
  // 删除评论
  DELETE_PRODUCT_COMMENT: `${API_BASE_URL}/api/user/comment/deleteProductComment`,
}

// 购物车相关接口
export const CART_API = {
  // 获取购物车商品数量
  GET_CART_COUNT: `${API_BASE_URL}/api/user/cart/getCounts`,
  // 获取购物车列表
  GET_CART_LIST: `${API_BASE_URL}/api/user/cart/list`,
  // 添加到购物车
  ADD_TO_CART: `${API_BASE_URL}/api/user/cart/add`,
  // 修改购物车商品（数量等）
  CHANGE_CART: `${API_BASE_URL}/api/user/cart/change`,
  // 删除购物车商品
  DELETE_CART: `${API_BASE_URL}/api/user/cart/delete`,
  // 批量删除购物车商品
  BATCH_DELETE_CART: `${API_BASE_URL}/api/user/cart/batchDelete`,
  // 结算单个购物车商品
  SETTLE_CART: `${API_BASE_URL}/api/user/cart/settle`,
  // 批量结算购物车商品
  BATCH_SETTLE_CART: `${API_BASE_URL}/api/user/cart/batchSettle`,
}

// 订单相关接口
export const ORDER_API = {
  // 获取订单列表
  GET_ORDER_LIST: `${API_BASE_URL}/api/user/order/list`,
  // 获取指定订单详情
  GET_ORDER_DETAIL: `${API_BASE_URL}/api/user/order/getOne`,
  // 预创建订单（第一步：获取订单预览信息，可修改）
  PRE_CREATE_ORDER: `${API_BASE_URL}/api/user/order/preCreate`,
  // 修改预创建订单的用户信息（地址、电话）
  CHANGE_USER_INFO: `${API_BASE_URL}/api/user/order/changeUserInfo`,
  // 修改预创建订单的商品数量
  CHANGE_PRODUCT_COUNT: `${API_BASE_URL}/api/user/order/changeProductCount`,
  // 确认创建订单（第二步：最终提交，扣减库存）
  CONFIRM_ORDER: `${API_BASE_URL}/api/user/order/create`,
  // 批量创建订单
  BATCH_CREATE_ORDER: `${API_BASE_URL}/api/user/order/batchCreate`,
  // 取消订单
  CANCEL_ORDER: `${API_BASE_URL}/api/user/order/cancel`,
  // 批量取消订单
  BATCH_CANCEL_ORDER: `${API_BASE_URL}/api/user/order/batchCancel`,
  // 支付订单
  PAY_ORDER: `${API_BASE_URL}/api/user/order/pay`,
  // 批量支付订单
  BATCH_PAY_ORDER: `${API_BASE_URL}/api/user/order/batchPay`,
  // 同步用户信息到个人资料（地址、电话）
  SYNC_USER_INFO: `${API_BASE_URL}/api/user/order/save`,
  // 申请退款
  REFUND_ORDER: `${API_BASE_URL}/api/user/order/refund`,
  // 签收订单
  RECEIVE_ORDER: `${API_BASE_URL}/api/user/order/receive`,
  // 取消退款
  CANCEL_REFUND: `${API_BASE_URL}/api/user/order/cancelRefund`,
  // 获取物流信息
  GET_LOGISTICS: `${API_BASE_URL}/api/user/order/logistics`,
  // 更新当前位置并检查是否到达
  CHECK_DELIVERED: `${API_BASE_URL}/api/user/order/checkDelivered`,
}

// 地址相关接口
export const ADDRESS_API = {
  // 获取地址列表
  GET_ADDRESS_LIST: `${API_BASE_URL}/api/user/address/list`,
  // 添加地址
  ADD_ADDRESS: `${API_BASE_URL}/api/user/address/add`,
  // 更新地址
  UPDATE_ADDRESS: `${API_BASE_URL}/api/user/address/update`,
  // 删除地址
  DELETE_ADDRESS: `${API_BASE_URL}/api/user/address/delete`,
  // 设置默认地址
  SET_DEFAULT_ADDRESS: `${API_BASE_URL}/api/user/address/setDefault`,
}

// 用户相关接口
export const USER_API = {
  // 获取用户信息
  GET_USER_INFO: `${API_BASE_URL}/api/user/profile/getInfo`,
  // 修改用户信息
  CHANGE_USER_INFO: `${API_BASE_URL}/api/user/profile/changeInfo`,
  // 上传头像
  UPLOAD_AVATAR: `${API_BASE_URL}/api/user/profile/uploadAvatar`,
  // 实名认证
  REAL_NAME_AUTH: `${API_BASE_URL}/api/user/profile/realNameAuth`,
  // 获取快递地址
  GET_RECEIPT_ADDRESS: `${API_BASE_URL}/api/user/profile/getReceiptAddress`,
  // 修改快递地址
  CHANGE_RECEIPT_ADDRESS: `${API_BASE_URL}/api/user/profile/changeReceiptAddress`,
  // 修改密码
  CHANGE_PASSWORD: `${API_BASE_URL}/api/user/profile/changePassword`,
  // 获取手机号
  GET_PHONE: `${API_BASE_URL}/api/user/profile/getPhone`,
  // 修改手机号
  CHANGE_PHONE: `${API_BASE_URL}/api/user/profile/changePhone`,
  // 获取邮箱
  GET_EMAIL: `${API_BASE_URL}/api/user/profile/getEmail`,
  // 修改邮箱
  CHANGE_EMAIL: `${API_BASE_URL}/api/user/profile/changeEmail`,
  // 注销账号
  DEACTIVATE_ACCOUNT: `${API_BASE_URL}/api/user/profile/deactivateAccount`,
}

// 推荐相关接口
export const RECOMMEND_API = {
  // 猜你喜欢（协同过滤）
  GET_RECOMMENDATIONS: `${API_BASE_URL}/api/user/index/getRecommendations`,
  // 帖子推荐（协同过滤）
  GET_POST_RECOMMENDATIONS: `${API_BASE_URL}/api/user/post/getRecommendations`,
}

// 消息通知相关接口
export const MESSAGE_API = {
  // 获取消息列表
  GET_MESSAGE_LIST: `${API_BASE_URL}/api/user/notify/list`,
  // 标记已读
  MARK_READ: `${API_BASE_URL}/api/user/notify/markRead`,
  // 标记全部已读
  MARK_ALL_READ: `${API_BASE_URL}/api/user/notify/markAllRead`,
}

// 全站搜索相关接口
export const SEARCH_API = {
  // 搜索商品和帖子
  SEARCH: `${API_BASE_URL}/api/user/search`,
}

// 论坛帖子相关接口
export const POST_API = {
  // 获取帖子列表
  GET_POST_LIST: `${API_BASE_URL}/api/user/post/list`,
  // 获取所有栏目（话题）
  GET_COLUMNS: `${API_BASE_URL}/api/user/post/column`,
  // 获取指定用户的帖子
  GET_USER_POSTS: `${API_BASE_URL}/api/user/post/getFromUser`,
  // 获取指定帖子的详情
  GET_POST_DETAIL: `${API_BASE_URL}/api/user/post/detail`,
  // 获取指定栏目的帖子
  GET_POSTS_UNDER_COLUMN: `${API_BASE_URL}/api/user/post/getUnderColumn`,
  // 获取指定话题下的帖子
  GET_POSTS_UNDER_TOPIC: `${API_BASE_URL}/api/user/post/getUnderTopic`,
  // 获取指定帖子的评论
  GET_POST_COMMENTS: `${API_BASE_URL}/api/user/comment/getPostComment`,
  // 发布帖子评论
  POST_POST_COMMENT: `${API_BASE_URL}/api/user/comment/postPostComment`,
  // 上传评论媒体（与商品评论共用）
  POST_COMMENT_MEDIA: `${API_BASE_URL}/api/user/comment/postCommentMedia`,
  // 删除帖子评论
  DELETE_POST_COMMENT: `${API_BASE_URL}/api/user/comment/deletePostComment`,
  // 发布帖子
  CREATE_POST: `${API_BASE_URL}/api/user/post/toPost`,
  // 上传帖子媒体
  UPLOAD_POST_MEDIA: `${API_BASE_URL}/api/user/post/toPostMedia`,
  // 编辑帖子（修改帖子内容）
  EDIT_POST: `${API_BASE_URL}/api/user/post/edit`,
  // 删除帖子
  DELETE_POST: `${API_BASE_URL}/api/user/post/delete`,
  // 上传帖子媒体
  UPLOAD_MEDIA: `${API_BASE_URL}/api/user/post/uploadMedia`,
  // 保存草稿
  SAVE_DRAFT: `${API_BASE_URL}/api/user/post/saveDraft`,
  // 获取草稿列表
  GET_DRAFTS: `${API_BASE_URL}/api/user/post/getDrafts`,
  // 获取话题列表
  GET_TOPICS: `${API_BASE_URL}/api/user/post/topic`,
  // 搜索话题
  SEARCH_TOPICS: `${API_BASE_URL}/api/user/post/searchTopics`,
  // 获取收藏列表（分页）
  GET_FAVORITES: `${API_BASE_URL}/api/user/post/favorite`,
  // 获取用户对帖子的收藏状态
  GET_FAVOR_STATUS: `${API_BASE_URL}/api/user/post/getFavor`,
  // 收藏/取消收藏帖子（opFlag: 0-收藏, 1-取消收藏）
  FAVOR_POST: `${API_BASE_URL}/api/user/post/favor`,
  // 点赞/取消点赞帖子
  LIKE_POST: `${API_BASE_URL}/api/user/post/like`,
  // 点踩/取消点踩帖子
  DISLIKE_POST: `${API_BASE_URL}/api/user/post/dislike`,
  // 获取用户对帖子的点赞状态
  GET_LIKE_STATUS: `${API_BASE_URL}/api/user/post/getLike`,
  // 获取用户对帖子的点踩状态
  GET_DISLIKE_STATUS: `${API_BASE_URL}/api/user/post/getDislike`,
}

/**
 * @typedef {Object} ApiResponse
 * @template T
 * @property {number} code - 响应状态码
 * @property {string} message - 响应消息
 * @property {T} data - 响应数据
 */

/**
 * @typedef {Object} CarouselVO
 * @property {number} productId - 商品ID
 * @property {string} imageUrl - 图片URL
 */

/**
 * @typedef {Object} SuperCategoryVO
 * @property {number} categoryId - 分类ID
 * @property {string} name - 分类名称
 */

/**
 * @typedef {Object} SubCategoryVO
 * @property {number} categoryId - 分类ID
 * @property {number} mainCategoryId - 主分类ID
 * @property {string} name - 分类名称
 */

/**
 * @typedef {Object} LatestPetsVO
 * @property {number} productId - 商品ID
 * @property {string} name - 商品名称
 * @property {string} description - 商品描述
 * @property {number} price - 商品价格
 * @property {string} imageUrl - 商品图片URL
 */


// ==================== 统一请求工具 ====================
/**
 * 统一请求工具
 * 自动从 localStorage 读取 token 并添加 Bearer 前缀
 * 401/403 时触发全局登录弹窗
 */
import { ElMessage } from 'element-plus'

const getToken = () => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.token || ''
  } catch {
    return ''
  }
}

const getAuthHeaders = (extra = {}) => ({
  'Authorization': `Bearer ${getToken()}`,
  ...extra
})

/**
 * 发起请求，自动附加 Authorization 头
 * 遇到 401/403 时提示用户并触发登录弹窗
 * @param {string} url - 完整 URL 或路径（路径会自动拼接 API_BASE_URL）
 * @param {RequestInit} options - fetch 选项
 */
export const request = async (url, options = {}) => {
  const fullUrl = url.startsWith('http') ? url : `${API_BASE_URL}${url}`

  const headers = {
    ...getAuthHeaders(),
    ...(options.headers || {})
  }

  // 如果 body 是对象且没有指定 Content-Type，自动设置 JSON
  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(options.body)
  }

  const response = await fetch(fullUrl, { ...options, headers })

  if (response.status === 401) {
    ElMessage.error('登录凭证已过期，请重新登录')
    localStorage.removeItem('userInfo')
    window.dispatchEvent(new CustomEvent('open-login'))
  }

  if (response.status === 403) {
    const token = getToken()
    if (token) {
      ElMessage.error('权限不足，无法访问')
    } else {
      ElMessage.warning('请先登录再操作')
      window.dispatchEvent(new CustomEvent('open-login'))
    }
  }

  return response
}

export { getToken }
