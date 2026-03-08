import request from '@/utils/http'

/**
 * 栏目管理相关API
 */

// 栏目列表请求参数
export interface ColumnListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 栏目筛选参数
export interface ColumnListFilterDTO {
  columnName?: string | null
  sortFlag?: number // 1-降序(默认), 2-升序
}

// 栏目列表请求（包含筛选）
export interface ColumnListWithFilterDTO {
  columnListDTO: ColumnListDTO
  columnListFilterDTO: ColumnListFilterDTO
}

// 栏目列表项
export interface ColumnListItem {
  columnId: number
  columnName: string
  postCount: number
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 获取栏目列表
export function fetchColumnList(params: ColumnListWithFilterDTO) {
  return request.post<PageResponse<ColumnListItem>>({
    url: '/api/admin/column/list',
    data: params
  })
}

// 新增栏目请求参数
export interface ColumnAddDTO {
  columnName: string
  userId: number
}

// 修改栏目请求参数
export interface ColumnChangeDTO {
  columnId: number
  userId: number
  columnName: string
}

// 新增栏目
export function addColumn(params: ColumnAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/column/add',
    data: params
  })
}

// 修改栏目
export function changeColumn(params: ColumnChangeDTO) {
  return request.post<boolean>({
    url: '/api/admin/column/change',
    data: params
  })
}

// 删除栏目
export function deleteColumn(columnId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/column/delete?columnId=${columnId}&userId=${userId}`
  })
}

// 批量删除栏目
export function batchDeleteColumn(columnIds: number[], userId: number) {
  const params = new URLSearchParams()
  columnIds.forEach(id => params.append('columnIds', id.toString()))
  params.append('userId', userId.toString())

  return request.post<boolean>({
    url: `/api/admin/column/batchDelete?${params.toString()}`
  })
}
