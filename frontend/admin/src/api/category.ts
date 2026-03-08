import request from '@/utils/http'

/**
 * 商品分类管理相关API
 */

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 一级分类列表请求参数
export interface MainCategoryListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 二级分类列表请求参数
export interface SubCategoryListDTO {
  mainCategoryId: number
  userId: number
  currentPage: number
  pageSize: number
}

// 分类搜索请求参数
export interface CategorySearchListDTO {
  userId: number
  keyword: string
  currentPage: number
  pageSize: number
}

// 一级分类列表项
export interface MainCategoryListVO {
  categoryId: number
  type: number // 1: 宠物分类, 2: 用品分类
  categoryName: string
  subCategoryCount: number
  productCount: number
}

// 二级分类列表项
export interface SubCategoryListVO {
  categoryId: number
  categoryName: string
  mainCategoryId: number
  productCount: number
}

// 分类搜索结果
export interface CategorySearchListVO {
  mainCategoryListVOPageVO: PageResponse<MainCategoryListVO>
  subCategoryListVOPageVO: PageResponse<SubCategoryListVO>
}

// 一级分类（用户端，用于筛选）
export interface SuperCategoryVO {
  categoryId: number
  name: string
}

// 二级分类（用户端，用于筛选）
export interface SubCategoryVO {
  categoryId: number
  mainCategoryId: number
  name: string
}

// 获取一级分类列表
export function fetchMainCategoryList(params: MainCategoryListDTO) {
  return request.post<PageResponse<MainCategoryListVO>>({
    url: '/api/admin/category/mainCategory',
    data: params
  })
}

// 获取二级分类列表
export function fetchSubCategoryList(params: SubCategoryListDTO) {
  return request.post<PageResponse<SubCategoryListVO>>({
    url: '/api/admin/category/subCategory',
    data: params
  })
}

// 搜索分类
export function searchCategory(params: CategorySearchListDTO) {
  return request.post<CategorySearchListVO>({
    url: '/api/admin/category/search',
    data: params
  })
}

// 获取所有一级分类（用户端接口，用于筛选下拉框）
export function fetchAllMainCategories() {
  return request.get<SuperCategoryVO[]>({
    url: '/api/user/index/getSuperCategories'
  })
}

// 获取指定一级分类下的所有二级分类（用户端接口，用于筛选下拉框）
export function fetchAllSubCategories(mainCategoryId: number) {
  return request.get<SubCategoryVO[]>({
    url: '/api/user/index/getSubCategories',
    params: { mainCategoryId }
  })
}

// 新增一级分类请求参数
export interface MainCategoryAddDTO {
  userId: number
  categoryName: string
  type: number // 1: 宠物分类, 2: 用品分类
}

// 新增二级分类请求参数
export interface SubCategoryAddDTO {
  userId: number
  mainCategoryId: number
  categoryName: string
}

// 新增一级分类
export function addMainCategory(params: MainCategoryAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/category/addMainCategory',
    data: params
  })
}

// 新增二级分类
export function addSubCategory(params: SubCategoryAddDTO) {
  return request.post<boolean>({
    url: '/api/admin/category/addSubCategory',
    data: params
  })
}

// 修改一级分类请求参数
export interface MainCategoryChangeDTO {
  userId: number
  categoryId: number
  type?: number | null // 未修改传null
  categoryName?: string | null // 未修改传null
}

// 修改二级分类请求参数
export interface SubCategoryChangeDTO {
  userId: number
  categoryId: number
  mainCategoryId?: number | null // 未修改传null
  categoryName?: string | null // 未修改传null
}

// 修改一级分类
export function changeMainCategory(params: MainCategoryChangeDTO) {
  return request.post<boolean>({
    url: '/api/admin/category/changeMainCategory',
    data: params
  })
}

// 修改二级分类
export function changeSubCategory(params: SubCategoryChangeDTO) {
  return request.post<boolean>({
    url: '/api/admin/category/changeSubCategory',
    data: params
  })
}

// 删除一级分类
export function deleteMainCategory(mainCategoryId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/category/deleteMainCategory?mainCategoryId=${mainCategoryId}&userId=${userId}`
  })
}

// 删除二级分类
export function deleteSubCategory(subCategoryId: number, userId: number) {
  return request.post<boolean>({
    url: `/api/admin/category/deleteSubCategory?subCategoryId=${subCategoryId}&userId=${userId}`
  })
}


// 批量删除一级分类
export function batchDeleteMainCategory(mainCategoryIds: number[], userId: number) {
  const idsParam = mainCategoryIds.join(',')
  return request.post<boolean>({
    url: `/api/admin/category/batchDeleteMainCategory?mainCategoryIds=${idsParam}&userId=${userId}`
  })
}

// 批量删除二级分类
export function batchDeleteSubCategory(subCategoryIds: number[], userId: number) {
  const idsParam = subCategoryIds.join(',')
  return request.post<boolean>({
    url: `/api/admin/category/batchDeleteSubCategory?subCategoryIds=${idsParam}&userId=${userId}`
  })
}
