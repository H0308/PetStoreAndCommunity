import request from '@/utils/http'

/**
 * 话题管理相关API
 */

// 话题列表请求参数
export interface TopicListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 话题筛选参数
export interface TopicListFilterDTO {
  topicName?: string | null
  sortFlag?: number // 1-降序(默认), 2-升序
}

// 话题列表请求（包含筛选）
export interface TopicListWithFilterDTO {
  topicListDTO: TopicListDTO
  topicListFilterDTO: TopicListFilterDTO
}

// 话题列表项
export interface TopicListItem {
  topicId: number
  topicName: string
  postCount: number
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 获取话题列表
export function fetchTopicList(params: TopicListWithFilterDTO) {
  return request.post<PageResponse<TopicListItem>>({
    url: '/api/admin/topic/list',
    data: params
  })
}

// 新增话题请求参数
export interface TopicAddDTO {
  topicName: string
  userId: number
}

// 修改话题请求参数
export interface TopicChangeDTO {
  topicId: number
  userId: number
  topicName: string
}

// 新增话题
export function addTopic(params: TopicAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/topic/add',
    data: params
  })
}

// 修改话题
export function changeTopic(params: TopicChangeDTO) {
  return request.post<boolean>({
    url: '/api/admin/topic/change',
    data: params
  })
}

// 删除话题
export function deleteTopic(topicId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/topic/delete?topicId=${topicId}&userId=${userId}`
  })
}

// 批量删除话题
export function batchDeleteTopic(topicIds: number[], userId: number) {
  const params = new URLSearchParams()
  topicIds.forEach(id => params.append('topicIds', id.toString()))
  params.append('userId', userId.toString())
  
  return request.post<boolean>({
    url: `/api/admin/topic/batchDelete?${params.toString()}`
  })
}
