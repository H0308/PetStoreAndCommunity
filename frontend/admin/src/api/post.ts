import request from '@/utils/http'

/**
 * 帖子管理相关API
 */

// 帖子列表请求参数
export interface PostListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 帖子筛选参数
export interface PostListFilterDTO {
  columnId?: number | null
  topicId?: number | null
  title?: string | null
  userId?: number | null
  status?: number | null
  startTime?: string | null
  endTime?: string | null
}

// 帖子列表请求（包含筛选）
export interface PostListWithFilterDTO {
  postListDTO: PostListDTO
  postListFilterDTO: PostListFilterDTO
}

// 帖子列表项
export interface PostListItem {
  postId: number
  userId: number
  username: string
  title: string
  content: string
  columnId: number
  columnName: string
  topicNames: Record<number, string>
  status: number
  createTime: string
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 获取帖子列表
export function fetchPostList(params: PostListWithFilterDTO) {
  return request.post<PageResponse<PostListItem>>({
    url: '/api/admin/post/list',
    data: params
  })
}

// 删除帖子
export function deletePost(postId: number, postUserId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/post/delete?postId=${postId}&postUserId=${postUserId}&userId=${userId}`
  })
}

// 批量删除帖子请求参数
export interface PostBatchDeleteDTO {
  userId: number
  postIds: number[]
}

// 批量删除帖子
export function batchDeletePost(params: PostBatchDeleteDTO) {
  return request.post<boolean>({
    url: '/api/admin/post/batchDelete',
    data: params
  })
}

// 审核帖子请求参数
export interface PostVerifyDTO {
  postId: number
  userId: number
  opFlag: number // 0-不予通过，1-审核通过
}

// 审核帖子
export function verifyPost(params: PostVerifyDTO) {
  return request.post<boolean>({
    url: '/api/admin/post/verify',
    data: params
  })
}

// 帖子详情响应
export interface PostDetailVO {
  postId: number
  userId: number
  username: string
  avatarUrl: string
  userStatus: number
  banFlag: number
  columnId: number
  columnName: string
  topicNames: Record<number, string>
  title: string
  content: string
  favorCount: number
  rejectCount: number
  likeCount: number
  status: number
  mediaUrls: string[]
  createTime: string
  updateTime: string
}

// 获取帖子详情
export function fetchPostDetail(postId: number, userId: number) {
  return request.post<PostDetailVO>({
    url: `/api/admin/post/detail?postId=${postId}&userId=${userId}`
  })
}
