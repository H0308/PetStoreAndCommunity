import request from '@/utils/http'

/**
 * 商品管理相关API
 */

// 商品列表请求参数
export interface ProductListDTO {
  userId: number
  currentPage: number
  pageSize: number
}

// 商品筛选参数
export interface ProductFilterDTO {
  keyword?: string | null
  productId?: number | null
  type?: number | null
  status?: number | null
  mainCategoryId?: number | null
  subCategoryId?: number | null
}

// 商品列表请求（包含筛选）
export interface ProductListWithFilterDTO {
  productListDTO: ProductListDTO
  productFilterDTO?: ProductFilterDTO
}

// 商品列表项
export interface ProductListItem {
  productId: number
  productName: string
  identifier: string
  type: number // 1-宠物，2-宠物用品
  mainCategoryId: number
  mainCategoryName: string
  subCategoryId: number
  subCategoryName: string
  productImageUrl: string
  shipAddress: string
  stock: number
  status: number
  price: number
}

// 分页响应
export interface PageResponse<T> {
  currentPage: number
  totalPages: number
  totalCount: number
  totalRecords: T[]
}

// 基础商品详情
export interface BaseProductDetail {
  productId: number
  identifier: string
  productType: number // 1-宠物，2-宠物用品
  deliverAddress: string
  name: string
  description: string
  mainCategoryName: string
  subCategoryName: string
  status: number
  price: number
  stock: number
  productImages: string[]
}

// 宠物商品详情
export interface PetProductDetail extends BaseProductDetail {
  healthStatus: number
  trainNote: string
  raiseNote: string
  vaccineFlag: number
}

// 宠物用品商品详情
export interface SupplyProductDetail extends BaseProductDetail {
  brand: string
  fitAge: string
  figVariety: string
  manufactureDate: string
  guaranteeDate: string
  company: string
}

// 获取商品列表
export function fetchProductList(params: ProductListWithFilterDTO) {
  return request.post<PageResponse<ProductListItem>>({
    url: '/api/admin/product/list',
    data: params
  })
}

// 获取宠物商品详情（调用用户端接口）
export function fetchPetProductDetail(productId: number) {
  return request.get<PetProductDetail>({
    url: `/api/user/product/pet/getPetDetail`,
    params: { productId }
  })
}

// 获取宠物用品商品详情（调用用户端接口）
export function fetchSupplyProductDetail(productId: number) {
  return request.get<SupplyProductDetail>({
    url: `/api/user/product/supply/getSupplyDetail`,
    params: { productId }
  })
}

// ==================== 新增商品相关 ====================

// 新增宠物商品请求参数
export interface PetProductAddDTO {
  userId: number
  productName: string
  description?: string
  shipAddress: string
  mainCategoryId: number
  subCategoryId: number
  price: number
  stock?: number
  healthStatus?: number // 1-健康，2-良好，3-治疗中
  trainNote?: string
  raiseNote?: string
  vaccineFlag?: number // 0-未接种，1-已接种
}

// 新增宠物用品请求参数
export interface SupplyProductAddDTO {
  userId: number
  productName: string
  description?: string
  shipAddress: string
  mainCategoryId: number
  subCategoryId: number
  price: number
  stock?: number
  brand?: string
  fitAge?: string
  fitVariety?: string
  manufactureDate: string // ISO日期字符串
  guaranteeDate: string // ISO日期字符串
  company?: string
}

// 新增商品返回结果
export interface AddProductResult {
  productId: number
}

// 图片条目：file + isMain(0-非主图, 1-主图)
export interface ProductImageItem {
  file: File
  isMain: 0 | 1
}

/**
 * 构建包含图片的 FormData
 * files 逐个 append，mainFlags 作为 JSON 数组一次性 append
 */
function buildImageFormData(images: ProductImageItem[]): FormData {
  const formData = new FormData()
  images.forEach(img => formData.append('files', img.file))
  formData.append('mainFlags', new Blob([JSON.stringify(images.map(img => img.isMain))], { type: 'application/json' }))
  return formData
}

// 新增宠物商品（含图片，multipart/form-data）
export function addPetProduct(data: PetProductAddDTO, images: ProductImageItem[]) {
  const formData = buildImageFormData(images)
  formData.append('content', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  return request.post<AddProductResult>({
    url: '/api/admin/product/addPet',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 新增宠物用品（含图片，multipart/form-data）
export function addSupplyProduct(data: SupplyProductAddDTO, images: ProductImageItem[]) {
  const formData = buildImageFormData(images)
  formData.append('content', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  return request.post<AddProductResult>({
    url: '/api/admin/product/addSupply',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 校验图片是否符合主图比例要求（16:9左右）
export function validateMainImageRatio(file: File): Promise<boolean> {
  return new Promise((resolve) => {
    const img = new Image()
    img.onload = () => {
      const ratio = img.width / img.height
      // 16:9 ≈ 1.78，允许一定误差范围（1.5 ~ 2.0）
      const isValid = ratio >= 1.5 && ratio <= 2.0
      URL.revokeObjectURL(img.src)
      resolve(isValid)
    }
    img.onerror = () => {
      URL.revokeObjectURL(img.src)
      resolve(false)
    }
    img.src = URL.createObjectURL(file)
  })
}

// ==================== 商品上架/下架 ====================

// 上架商品
export function onSellingProduct(productId: number, userId: number) {
  return request.post<boolean>({
    url: '/api/admin/product/onSelling',
    params: { productId, userId },
    data: {}
  })
}

// 下架商品
export function offShelfProduct(productId: number, userId: number) {
  return request.post<boolean>({
    url: '/api/admin/product/offShelf',
    params: { productId, userId },
    data: {}
  })
}

// 删除商品
export function deleteProduct(productId: number, userId: number) {
  return request.post<boolean>({
    url: '/api/admin/product/delete',
    params: { productId, userId },
    data: {}
  })
}

// ==================== 修改商品相关 ====================

// 管理员获取商品详情的基础信息（用于修改）
export interface AdminBaseProductDetailVO {
  identifier: string // 只读
  productName: string
  description: string
  type: number // 1-宠物，2-宠物用品
  shipAddress: string
  mainCategoryId: number
  subCategoryId: number
  price: number
  stock: number
  status: number // 只读
  productImageUrls: Record<number, string> // key: 图片ID, value: 图片URL
}

// 管理员获取宠物商品详情（用于修改）
export interface AdminPetDetailVO extends AdminBaseProductDetailVO {
  healthStatus: number // 1-健康，2-良好，3-治疗中
  trainNote: string
  raiseNote: string
  vaccineFlag: number // 0-未接种，1-已接种
}

// 管理员获取宠物用品详情（用于修改）
export interface AdminSupplyDetailVO extends AdminBaseProductDetailVO {
  brand: string
  fitAge: string
  fitVariety: string
  manufactureDate: string
  guaranteeDate: string
  company: string
}

// 获取宠物商品详情（用于修改）
export function fetchAdminPetDetail(productId: number, userId: number) {
  return request.post<AdminPetDetailVO>({
    url: '/api/admin/product/getPet',
    params: { productId, userId },
    data: {}
  })
}

// 获取宠物用品详情（用于修改）
export function fetchAdminSupplyDetail(productId: number, userId: number) {
  return request.post<AdminSupplyDetailVO>({
    url: '/api/admin/product/getSupply',
    params: { productId, userId },
    data: {}
  })
}

// 修改商品基础信息DTO
export interface BaseProductChangeDTO {
  type?: number
  productName?: string
  description?: string
  shipAddress?: string
  mainCategoryId?: number
  subCategoryId?: number
  price?: number
  stock?: number
  productImageUrls?: Record<number, number> // key: 图片ID, value: 删除标记（0-保留，1-删除）
}

// 修改宠物商品DTO
export interface PetProductChangeDTO {
  healthStatus?: number
  trainNote?: string
  raiseNote?: string
  vaccineFlag?: number
}

// 修改宠物用品DTO
export interface SupplyProductChangeDTO {
  brand?: string
  fitAge?: string
  fitVariety?: string
  manufactureDate?: string
  guaranteeDate?: string
  company?: string
}

// 修改商品主DTO
export interface MainProductChangeDTO {
  userId: number
  productId: number
  baseProductChangeDTO: BaseProductChangeDTO | null
  petProductChangeDTO?: PetProductChangeDTO | null
  supplyProductChangeDTO?: SupplyProductChangeDTO | null
}

// 修改商品返回结果
export interface ProductChangeVO {
  successFlag: boolean
  productId: number
}

// 修改商品（含新图片，multipart/form-data）
export function changeProduct(data: MainProductChangeDTO, newImages?: ProductImageItem[]) {
  const formData = new FormData()
  formData.append('content', new Blob([JSON.stringify(data)], { type: 'application/json' }))
  if (newImages && newImages.length > 0) {
    newImages.forEach(img => formData.append('files', img.file))
    formData.append('mainFlags', new Blob([JSON.stringify(newImages.map(img => img.isMain))], { type: 'application/json' }))
  }
  return request.post<ProductChangeVO>({
    url: '/api/admin/product/change',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 批量删除商品
export function batchDeleteProduct(productIds: number[], userId: number) {
  const idsParam = productIds.join(',')
  return request.post<boolean>({
    url: `/api/admin/product/batchDelete?productIds=${idsParam}&userId=${userId}`,
    data: {}
  })
}
