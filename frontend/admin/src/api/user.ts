import request from '@/utils/http'

/**
 * 用户管理相关API
 */

// 用户注册请求参数
export interface UserRegisterDTO {
  username: string
  email: string
  password: string
  confirmPassword: string
}

// 用户列表请求参数
export interface UserDetailListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 用户筛选参数
export interface UserDetailFilterDTO {
  userId: number
  username?: string | null
  phone?: string | null
  email?: string | null
  roleId?: number | null
  status?: number | null
  banFlag?: number | null
  realNameAuthFlag?: number | null // 实名认证状态，0-未认证，1-已认证
}

// 用户列表请求（包含筛选）
export interface UserDetailListWithFilterDTO {
  userDetailListDTO: UserDetailListDTO
  userDetailFilterDTO?: UserDetailFilterDTO
}

// 用户列表项
export interface UserDetailListVO {
  userId: number
  avatarUrl: string
  username: string
  email: string
  phone: string
  gender: number // 1-男，2-女，3-保密
  receiptName: string
  receiptAddress: string
  realNameAuthFlag: number // 实名认证状态，0-未认证，1-已认证
  status: number // 销户状态，0-未销户，1-已销户
  banFlag: number // 禁言状态，0-未禁言，1-已禁言
  roleId: number // 角色，0-管理员，1-普通用户
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 修改用户信息请求参数（只传递发生修改的字段，未修改的传null）
export interface UserUpdateDTO {
  userId: number // 管理员ID
  userIdChange: number // 被操作的用户ID
  roleId?: number | null
  phone?: string | null
  email?: string | null
  receiptName?: string | null
  receiptAddress?: string | null
}

// 重置用户密码请求参数
export interface UserResetPasswordDTO {
  userId: number // 管理员ID
  userIdChange: number // 被操作的用户ID
}

// 销户请求参数
export interface UserCloseAccountDTO {
  userId: number // 管理员ID
  userIdChange: number // 被操作的用户ID
}

// 删除用户请求参数
export interface UserDeleteDTO {
  userId: number // 管理员ID
  userIdChange: number // 被操作的用户ID
}

// 批量删除用户请求参数
export interface UserBatchDeleteDTO {
  userId: number // 管理员ID
  userIdChangeList: number[] // 被操作的用户ID列表
}

// 禁言用户请求参数
export interface UserBanDTO {
  userId: number // 管理员ID
  userIdChange: number // 被操作的用户ID
}

// 批量禁言用户请求参数
export interface UserBatchBanDTO {
  userId: number // 管理员ID
  userIdChangeList: number[] // 被操作的用户ID列表
}

// 获取用户列表
export function fetchUserList(params: UserDetailListWithFilterDTO) {
  return request.post<PageResponse<UserDetailListVO>>({
    url: '/api/admin/user/list',
    data: params
  })
}

// 修改用户信息
export function updateUser(params: UserUpdateDTO) {
  return request.post({
    url: '/api/admin/user/change',
    data: params
  })
}

// 重置用户密码
export function resetUserPassword(userIdChange: number, userId: number) {
  return request.post({
    url: `/api/admin/user/reset?userIdChange=${userIdChange}&userId=${userId}`
  })
}

// 销户
export function closeUserAccount(userIdChange: number, userId: number) {
  return request.post({
    url: `/api/admin/user/deactivate?userIdChange=${userIdChange}&userId=${userId}`
  })
}

// 删除用户
export function deleteUser(userIdChange: number, userId: number) {
  return request.post({
    url: `/api/admin/user/delete?userIdChange=${userIdChange}&userId=${userId}`
  })
}

// 批量删除用户
export function batchDeleteUsers(userIdChanges: number[], userId: number) {
  const idsParam = userIdChanges.join(',')
  return request.post({
    url: `/api/admin/user/batchDelete?userIdChanges=${idsParam}&userId=${userId}`
  })
}

// 禁言用户
export function banUser(userIdChange: number, userId: number) {
  return request.post({
    url: `/api/admin/user/ban?userIdChange=${userIdChange}&userId=${userId}`
  })
}

// 取消禁言用户
export function unBanUser(userIdChange: number, userId: number) {
  return request.post({
    url: `/api/admin/user/unBan?userIdChange=${userIdChange}&userId=${userId}`
  })
}

// 批量禁言用户
export function batchBanUsers(userIdChanges: number[], userId: number) {
  const idsParam = userIdChanges.join(',')
  return request.post({
    url: `/api/admin/user/batchBan?userIdChanges=${idsParam}&userId=${userId}`
  })
}

// 批量取消禁言用户
export function batchUnBanUsers(userIdChanges: number[], userId: number) {
  const idsParam = userIdChanges.join(',')
  return request.post({
    url: `/api/admin/user/batchUnBan?userIdChanges=${idsParam}&userId=${userId}`
  })
}

// 注册新用户（管理员新增用户）
export function registerUser(params: UserRegisterDTO) {
  return request.post<boolean>({
    url: '/api/user/auth/register',
    data: params
  })
}
