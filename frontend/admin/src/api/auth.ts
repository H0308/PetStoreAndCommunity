import request from '@/utils/http'

/**
 * 管理员登录参数
 */
export interface AdminLoginParams {
  email: string
  password: string
}

/**
 * 管理员登录响应
 */
export interface AdminLoginResponse {
  userId: number
  username: string
  avatarUrl: string
  roleId: number
  token: string
}

/**
 * 管理员登录
 * @param params 登录参数
 * @returns 登录响应
 */
export function fetchLogin(params: AdminLoginParams) {
  return request.post<AdminLoginResponse>({
    url: '/api/admin/auth/login',
    params
  })
}

/**
 * 管理员登出
 */
export function fetchLogout() {
  return request.post<void>({
    url: '/api/admin/auth/logout'
  })
}

/**
 * 获取用户信息
 * @returns 用户信息
 */
export function fetchGetUserInfo() {
  return request.get<Api.Auth.UserInfo>({
    url: '/api/user/info'
  })
}
