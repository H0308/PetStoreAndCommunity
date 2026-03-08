import request from '@/utils/http'

// 评论类型枚举
export enum CommentType {
  PRODUCT = 1, // 商品评论
  POST = 2     // 帖子评论
}

// 评论状态枚举
export enum CommentStatus {
  PENDING = 1,    // 审核中
  APPROVED = 2,   // 审核成功
  REJECTED = 3    // 审核失败
}

// 媒体类型枚举
export enum MediaType {
  IMAGE = 1,  // 图片
  VIDEO = 2   // 视频
}

// 评论媒体
export interface CommentMedia {
  id: number
  commentId: number
  mediaUrl: string
  mediaType: MediaType
}

// 评论列表项
export interface CommentItem {
  commentId: number
  userId: number
  username: string
  avatarUrl: string
  commentType: number
  stars: number | null
  objectId: number
  objectName: string
  content: string
  mediaCount: number
  mediaUrls: string[]
  replyCount: number
  status: number
  deleteFlag: number
  createTime: string
  // 父评论字段
  parentId: number | null
  parentContent: string | null
}

// 评论回复链项
export interface CommentReplyItem {
  commentId: number
  userId: number
  username: string
  avatarUrl: string
  content: string
  mediaUrls: string[]
  replyCount: number
  status: number
  deleteFlag: number
  createTime: string
}

// 回复列表请求参数
export interface ReplyListDTO {
  userId: number
  parentId: number
  currentPage: number
  pageSize: number
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 评论列表请求参数
export interface CommentListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 评论筛选参数
export interface CommentFilterDTO {
  commentId?: number | null
  username?: string | null
  objectType?: number | null
  objectName?: string | null
  content?: string | null
  status?: number | null
  deleteFlag?: number | null
  startTime?: string | null
  endTime?: string | null
}

// 评论列表请求（带筛选）
export interface CommentListWithFilterDTO {
  commentListDTO: CommentListDTO
  commentListFilterDTO: CommentFilterDTO
}

// 获取评论列表
export function fetchCommentList(params: CommentListWithFilterDTO) {
  return request.post<PageResponse<CommentItem>>({
    url: '/api/admin/comment/list',
    data: params
  })
}

// 审核评论（隐藏/显示）
export function updateCommentStatus(commentId: number, status: number, userId: number) {
  return request.post({
    url: `/api/admin/comment/status?commentId=${commentId}&status=${status}&userId=${userId}`
  })
}

// 审核评论请求参数
export interface CommentVerifyDTO {
  userId: number     // 操作用户ID
  commentId: number  // 评论ID
  opFlag: number     // 0-不予通过，1-审核通过
}

// 审核评论
export function verifyComment(params: CommentVerifyDTO) {
  return request.post<boolean>({
    url: '/api/admin/comment/verify',
    data: params
  })
}

// 删除评论请求参数
export interface CommentDeleteDTO {
  userId: number        // 操作用户ID
  userIdComment: number // 评论所属用户ID
  commentId: number     // 评论ID
  commentType: number   // 评论类型（1=商品评论，2=帖子评论）
}

// 删除评论
export function deleteComment(params: CommentDeleteDTO) {
  return request.post<boolean>({
    url: '/api/admin/comment/delete',
    data: params
  })
}

// 批量删除评论（commentIds: Map<评论ID, 评论类型>）
export function batchDeleteComments(commentIds: Record<number, number>, userId: number) {
  return request.post<boolean>({
    url: '/api/admin/comment/batchDelete',
    data: { commentIds, userId }
  })
}

// 批量隐藏评论
export function batchHideComments(commentIds: number[], userId: number) {
  return request.post({
    url: '/api/admin/comment/batch-hide',
    data: { commentIds, userId }
  })
}

// 批量显示评论
export function batchShowComments(commentIds: number[], userId: number) {
  return request.post({
    url: '/api/admin/comment/batch-show',
    data: { commentIds, userId }
  })
}


// 获取评论回复列表
export function fetchCommentReplyList(params: ReplyListDTO) {
  return request.post<PageResponse<CommentReplyItem>>({
    url: '/api/admin/comment/getReply',
    data: params
  })
}

// 获取评论详情
export function fetchCommentDetail(commentId: number, userId: number) {
  return request.get<CommentItem>({
    url: `/api/admin/comment/detail`,
    params: { commentId, userId }
  })
}
