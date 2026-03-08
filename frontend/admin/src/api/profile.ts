import request from '@/utils/http'

/**
 * 个人中心相关API
 */

// 用户信息响应
export interface UserProfileInfo {
  avatarUrl: string
  username: string
  gender: number // 1-男, 2-女, 3-保密
  profile: string
  roleId: number // 0-管理员, 1-普通用户
  banFlag: number // 0-正常, 1-禁言
  realNameAuthFlag: number // 0-未认证, 1-已认证
}

// 修改用户信息请求参数
export interface ChangeUserInfoDTO {
  userId: number
  username?: string
  gender?: number
  profile?: string
}


// 修改密码请求参数
export interface ChangePasswordDTO {
  userId: number
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 获取用户信息
export function getUserInfo(userId: number) {
  return request.get<UserProfileInfo>({
    url: '/api/user/profile/getInfo',
    params: { userId }
  })
}

// 修改用户信息
export function changeUserInfo(data: ChangeUserInfoDTO) {
  return request.post<boolean>({
    url: '/api/user/profile/changeInfo',
    data
  })
}

// 上传头像
export function uploadAvatar(userId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post<string>({
    url: '/api/user/profile/uploadAvatar',
    params: { userId },
    data: formData
  })
}

// 修改密码
export function changePassword(data: ChangePasswordDTO) {
  return request.post<boolean>({
    url: '/api/user/profile/changePassword',
    data
  })
}

// 实名认证请求参数
export interface RealNameAuthDTO {
  userId: number
  realName: string
  idCard: string
}

// 实名认证
export function realNameAuth(data: RealNameAuthDTO) {
  return request.post<boolean>({
    url: '/api/user/profile/realNameAuth',
    data
  })
}

// 获取手机号
export function getPhone(userId: number) {
  return request.get<string>({
    url: '/api/user/profile/getPhone',
    params: { userId }
  })
}

// 修改手机号请求参数
export interface ChangePhoneDTO {
  userId: number
  phone: string
}

// 修改手机号
export function changePhone(data: ChangePhoneDTO) {
  return request.post<boolean>({
    url: '/api/user/profile/changePhone',
    params: { userId: data.userId, phone: data.phone },
    data: {}
  })
}

// 获取邮箱
export function getEmail(userId: number) {
  return request.get<string>({
    url: '/api/user/profile/getEmail',
    params: { userId }
  })
}

// 修改邮箱请求参数
export interface ChangeEmailDTO {
  userId: number
  email: string
}

// 修改邮箱
export function changeEmail(data: ChangeEmailDTO) {
  return request.post<boolean>({
    url: '/api/user/profile/changeEmail',
    params: { userId: data.userId, email: data.email },
    data: {}
  })
}
