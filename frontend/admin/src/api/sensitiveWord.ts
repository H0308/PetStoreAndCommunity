import request from '@/utils/http'

/**
 * 敏感词管理相关API
 */

// 敏感词列表请求参数
export interface SensitiveWordListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 敏感词筛选参数
export interface SensitiveWordListFilterDTO {
  keyword?: string | null
  categoryId?: number | null
}

// 敏感词列表请求（包含筛选）
export interface SensitiveWordListWithFilterDTO {
  sensitiveWordListDTO: SensitiveWordListDTO
  sensitiveWordListFilterDTO: SensitiveWordListFilterDTO
}

// 敏感词列表项
export interface SensitiveWordListItem {
  sensitiveId: number
  word: string
  categoryId: number
  categoryName: string
  createTime: string
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 敏感词分类
export interface SensitiveCategoryVO {
  categoryId: number
  categoryName: string
}

// 新增敏感词请求参数
export interface SensitiveWordAddDTO {
  userId: number
  word: string
  categoryId: number
}

// 获取敏感词列表
export function fetchSensitiveWordList(params: SensitiveWordListWithFilterDTO) {
  return request.post<PageResponse<SensitiveWordListItem>>({
    url: '/api/admin/sensitiveWord/list',
    data: params
  })
}

// 获取所有敏感词分类
export function fetchSensitiveCategoryList(userId: number) {
  return request.get<SensitiveCategoryVO[]>({
    url: '/api/admin/sensitiveCategory/getAll',
    params: { userId }
  })
}

// 新增敏感词
export function addSensitiveWord(params: SensitiveWordAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/sensitiveWord/add',
    data: params
  })
}

// 通过CSV文件导入敏感词
export function importSensitiveWordCSV(file: File, userId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId.toString())
  return request.post<number>({
    url: '/api/admin/sensitiveWord/addWithCSV',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除敏感词
export function deleteSensitiveWord(wordId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/sensitiveWord/delete?wordId=${wordId}&userId=${userId}`
  })
}

// 批量删除敏感词
export function batchDeleteSensitiveWord(wordIds: number[], userId: number) {
  const params = new URLSearchParams()
  wordIds.forEach(id => params.append('wordIds', id.toString()))
  params.append('userId', userId.toString())
  return request.post<boolean>({
    url: `/api/admin/sensitiveWord/batchDelete?${params.toString()}`
  })
}

// ==================== 敏感词分类管理 ====================

// 敏感词分类列表请求参数
export interface SensitiveCategoryListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 敏感词分类筛选参数
export interface SensitiveCategoryListFilterDTO {
  keyword?: string | null
}

// 敏感词分类列表请求（包含筛选）
export interface SensitiveCategoryListWithFilterDTO {
  sensitiveCategoryListDTO: SensitiveCategoryListDTO
  sensitiveCategoryListFilterDTO: SensitiveCategoryListFilterDTO
}

// 敏感词分类列表项
export interface SensitiveCategoryListItem {
  categoryId: number
  categoryName: string
  wordCount: number
  createTime: string
}

// 新增敏感词分类请求参数
export interface SensitiveCategoryAddDTO {
  userId: number
  name: string
}

// 修改敏感词分类请求参数
export interface SensitiveCategoryUpdateDTO {
  userId: number
  categoryId: number
  name: string
}

// 获取敏感词分类列表（分页）
export function fetchSensitiveCategoryPageList(params: SensitiveCategoryListWithFilterDTO) {
  return request.post<PageResponse<SensitiveCategoryListItem>>({
    url: '/api/admin/sensitiveCategory/list',
    data: params
  })
}

// 新增敏感词分类
export function addSensitiveCategory(params: SensitiveCategoryAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/sensitiveCategory/add',
    data: params
  })
}

// 修改敏感词分类
export function updateSensitiveCategory(params: SensitiveCategoryUpdateDTO) {
  return request.post<boolean>({
    url: '/api/admin/sensitiveCategory/change',
    data: params
  })
}

// 删除敏感词分类
export function deleteSensitiveCategory(categoryId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/sensitiveCategory/delete?categoryId=${categoryId}&userId=${userId}`
  })
}

// 批量删除敏感词分类
export function batchDeleteSensitiveCategory(categoryIds: number[], userId: number) {
  const params = new URLSearchParams()
  categoryIds.forEach(id => params.append('categoryIds', id.toString()))
  params.append('userId', userId.toString())
  return request.post<boolean>({
    url: `/api/admin/sensitiveCategory/batchDelete?${params.toString()}`
  })
}
