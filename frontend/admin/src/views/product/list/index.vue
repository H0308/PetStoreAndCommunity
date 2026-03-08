<template>
  <div class="flex flex-col gap-4 pb-5">
    <!-- 搜索区域 -->
    <ArtSearchBar
      ref="searchBarRef"
      v-model="searchFormState"
      :items="searchItems"
      :is-expand="false"
      :show-expand="true"
      :show-reset-button="true"
      :show-search-button="true"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 表格区域 -->
    <ElCard class="flex-1 art-table-card" shadow="never" style="margin-top: 0">
      <template #header>
        <div class="flex-cb">
          <h4 class="m-0">商品列表</h4>
          <div class="flex gap-2">
            <ElTag v-if="loading" type="warning">加载中...</ElTag>
            <ElTag v-else type="success">{{ tableData.length }} 条数据</ElTag>
          </div>
        </div>
      </template>

      <!-- 表格工具栏 -->
      <ArtTableHeader
        v-model:columns="columns"
        :loading="loading"
        @refresh="handleRefresh"
        layout="refresh,size,fullscreen,columns"
        fullClass="art-table-card"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="handleAdd" v-ripple>
              <ArtSvgIcon icon="ri:add-line" class="mr-1" />
              新增商品
            </ElButton>
            <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple>
              <ArtSvgIcon icon="ri:delete-bin-line" class="mr-1" />
              批量删除 ({{ selectedRows.length }})
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :pagination="pagination"
        :data="tableData"
        :columns="visibleColumns"
        table-layout="fixed"
        height="680px"
        empty-height="360px"
        style="min-height: 680px;"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <!-- 商品信息列 -->
        <template #productInfo="{ row }">
          <div class="flex gap-3 product-info">
            <div v-if="!row.productImageUrl" class="image-placeholder">
              <ArtSvgIcon icon="ri:image-line" class="text-xl text-g-500" />
            </div>
            <ElImage
              v-else
              :src="row.productImageUrl"
              fit="cover"
              class="product-image"
            >
              <template #error>
                <div class="image-placeholder">
                  <ArtSvgIcon icon="ri:image-line" class="text-xl text-g-500" />
                </div>
              </template>
            </ElImage>
            <div class="flex-1 min-w-0">
              <p class="m-0 overflow-hidden font-medium text-ellipsis whitespace-nowrap">
                {{ row.productName }}
              </p>
              <p class="m-0 mt-1 overflow-hidden text-xs text-g-700 text-ellipsis whitespace-nowrap">
                编号: {{ row.identifier }}
              </p>
            </div>
          </div>
        </template>

        <!-- 类型列 -->
        <template #type="{ row }">
          <ElTag class="tag-supply">
            {{ row.type === 1 ? '宠物' : '宠物用品' }}
          </ElTag>
        </template>

        <!-- 分类列 -->
        <template #category="{ row }">
          <span>{{ row.mainCategoryName }}</span>
          <span v-if="row.subCategoryName" class="text-g-500"> / {{ row.subCategoryName }}</span>
        </template>

        <!-- 价格列 -->
        <template #price="{ row }">
          <span class="font-medium">¥{{ row.price?.toFixed(2) }}</span>
        </template>

        <!-- 库存列 -->
        <template #stock="{ row }">
          <span :class="row.stock <= 0 ? 'text-danger' : ''">{{ row.stock }}</span>
        </template>

        <!-- 状态列 -->
        <template #status="{ row }">
          <ElTag :class="getStatusClass(row.status)">
            {{ getStatusText(row.status) }}
          </ElTag>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看详情" @click="handleView(row)" />
            <ArtButtonTable type="edit" :row="row" tooltip="编辑商品" @click="handleEdit(row)" />
            <ArtButtonTable 
              v-if="row.status === 3" 
              icon="ri:arrow-up-circle-line" 
              iconClass="bg-success/12 text-success"
              tooltip="上架商品"
              @click="handleOnShelf(row)" 
            />
            <ArtButtonTable 
              v-if="row.status !== 3" 
              icon="ri:arrow-down-circle-line" 
              iconClass="bg-warning/12 text-warning"
              tooltip="下架商品"
              @click="handleOffShelf(row)" 
            />
            <ArtButtonTable type="delete" :row="row" tooltip="删除商品" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 商品详情弹窗 -->
    <ProductDetailDialog
      v-model="detailDialogVisible"
      :product-id="currentProductId"
      :product-type="currentProductType"
    />

    <!-- 新增商品弹窗 -->
    <AddProductDialog
      v-model="addDialogVisible"
      @success="handleAddSuccess"
    />

    <!-- 编辑商品弹窗 -->
    <EditProductDialog
      v-model="editDialogVisible"
      :product-id="editProductId"
      :product-type="editProductType"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { useRoute } from 'vue-router'
import { fetchProductList, onSellingProduct, offShelfProduct, deleteProduct, batchDeleteProduct, type ProductListItem, type ProductListWithFilterDTO } from '@/api/product'
import { fetchAllMainCategories, fetchAllSubCategories, type SuperCategoryVO, type SubCategoryVO } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import ProductDetailDialog from '../detail/index.vue'
import AddProductDialog from './AddProductDialog.vue'
import EditProductDialog from './EditProductDialog.vue'

defineOptions({ name: 'ProductList' })

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<ProductListItem[]>([])
const selectedRows = ref<ProductListItem[]>([])
const searchBarRef = ref()
const tableRef = ref()

// 商品详情弹窗
const detailDialogVisible = ref(false)
const currentProductId = ref<number | null>(null)
const currentProductType = ref<number | null>(null)

// 新增商品弹窗
const addDialogVisible = ref(false)

// 编辑商品弹窗
const editDialogVisible = ref(false)
const editProductId = ref<number | null>(null)
const editProductType = ref<number | null>(null)

// 分类数据
const mainCategoryOptions = ref<{ label: string; value: string }[]>([])
const subCategoryOptions = ref<{ label: string; value: string }[]>([])
const allSubCategories = ref<SubCategoryVO[]>([])

// 根据路由参数初始化状态筛选
const getInitialStatus = () => {
  const statusQuery = route.query.status
  if (statusQuery === 'out_of_stock') {
    return '2' // 缺货状态
  }
  return ''
}

// 根据路由参数初始化分类筛选
const getInitialMainCategoryId = () => {
  return route.query.mainCategoryId ? String(route.query.mainCategoryId) : ''
}

const getInitialSubCategoryId = () => {
  return route.query.subCategoryId ? String(route.query.subCategoryId) : ''
}

// 根据路由参数初始化商品ID筛选（从聊天页面跳转）
const getInitialProductId = () => {
  return route.query.productId ? String(route.query.productId) : ''
}

// 搜索表单状态
const searchFormState = ref({
  productName: '',
  productId: getInitialProductId(),
  type: '',
  status: getInitialStatus(),
  mainCategoryId: getInitialMainCategoryId(),
  subCategoryId: getInitialSubCategoryId()
})

// 标记是否来自路由变化，避免重复加载
const isFromRouteChange = ref(false)

// 监听路由参数变化（用于从其他页面跳转过来的情况）
watch(
  () => route.query,
  async (newQuery, oldQuery) => {
    // 如果是同一个页面内的查询变化，才处理
    if (JSON.stringify(newQuery) === JSON.stringify(oldQuery)) return
    
    isFromRouteChange.value = true
    
    // 处理状态筛选
    if (newQuery.status === 'out_of_stock') {
      searchFormState.value.status = '2'
    } else if (!newQuery.status) {
      searchFormState.value.status = ''
    }
    
    // 处理商品ID筛选（从聊天页面跳转）
    searchFormState.value.productId = newQuery.productId ? String(newQuery.productId) : ''

    // 处理分类筛选
    const newMainCategoryId = newQuery.mainCategoryId ? String(newQuery.mainCategoryId) : ''
    const newSubCategoryId = newQuery.subCategoryId ? String(newQuery.subCategoryId) : ''

    // 更新一级分类
    searchFormState.value.mainCategoryId = newMainCategoryId
    
    // 如果有一级分类，加载对应的二级分类
    if (newMainCategoryId) {
      try {
        const res = await fetchAllSubCategories(Number(newMainCategoryId))
        allSubCategories.value = res
        subCategoryOptions.value = res.map(item => ({
          label: item.name,
          value: String(item.categoryId)
        }))
        // 设置二级分类
        searchFormState.value.subCategoryId = newSubCategoryId
      } catch (error) {
        console.error('获取二级分类失败:', error)
      }
    } else {
      subCategoryOptions.value = []
      searchFormState.value.subCategoryId = ''
    }
    
    pagination.current = 1
    await loadData()
    isFromRouteChange.value = false
  }
)

// 监听一级分类变化，加载对应的二级分类（用于用户手动选择的情况）
watch(
  () => searchFormState.value.mainCategoryId,
  async (newMainCategoryId, oldMainCategoryId) => {
    // 如果是路由变化触发的，跳过（避免重复加载）
    if (isFromRouteChange.value) return
    // 如果值没变，跳过
    if (newMainCategoryId === oldMainCategoryId) return
    
    // 清空二级分类选择
    searchFormState.value.subCategoryId = ''
    subCategoryOptions.value = []
    
    if (newMainCategoryId) {
      try {
        const res = await fetchAllSubCategories(Number(newMainCategoryId))
        allSubCategories.value = res
        subCategoryOptions.value = res.map(item => ({
          label: item.name,
          value: String(item.categoryId)
        }))
      } catch (error) {
        console.error('获取二级分类失败:', error)
      }
    }
  }
)

// 加载一级分类数据
const loadMainCategories = async () => {
  try {
    const res = await fetchAllMainCategories()
    mainCategoryOptions.value = res.map(item => ({
      label: item.name,
      value: String(item.categoryId)
    }))
  } catch (error) {
    console.error('获取一级分类失败:', error)
  }
}

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'productName',
    label: '商品名称',
    type: 'input',
    props: {
      placeholder: '请输入商品名称'
    }
  },
  {
    key: 'productId',
    label: '商品ID',
    type: 'input',
    props: {
      placeholder: '请输入商品ID'
    }
  },
  {
    key: 'type',
    label: '商品类型',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '宠物', value: '1' },
        { label: '宠物用品', value: '2' }
      ]
    }
  },
  {
    key: 'mainCategoryId',
    label: '一级分类',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: mainCategoryOptions.value
    }
  },
  {
    key: 'subCategoryId',
    label: '二级分类',
    type: 'select',
    props: {
      placeholder: searchFormState.value.mainCategoryId ? '全部' : '请先选择一级分类',
      clearable: true,
      disabled: !searchFormState.value.mainCategoryId,
      options: subCategoryOptions.value
    }
  },
  {
    key: 'status',
    label: '商品状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '在售', value: '1' },
        { label: '缺货', value: '2' },
        { label: '下架', value: '3' }
      ]
    }
  }
])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格列配置
const columns = ref<ColumnOption<ProductListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 60, label: '序号', fixed: 'left', visible: true },
  {
    prop: 'productInfo',
    label: '商品信息',
    minWidth: 280,
    useSlot: true,
    visible: true
  },
  {
    prop: 'type',
    label: '类型',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'category',
    label: '分类',
    minWidth: 180,
    useSlot: true,
    visible: true
  },
  {
    prop: 'price',
    label: '价格',
    width: 120,
    useSlot: true,
    visible: true
  },
  {
    prop: 'stock',
    label: '库存',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'shipAddress',
    label: '发货地',
    width: 150,
    showOverflowTooltip: true,
    visible: true
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'operation',
    label: '操作',
    width: 200,
    useSlot: true,
    fixed: 'right',
    visible: true
  }
])

// 过滤后的可见列
const visibleColumns = computed(() => 
  columns.value.filter(col => col.visible !== false)
)

// 获取状态样式类
const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    1: 'tag-online',
    2: 'tag-outstock',
    3: 'tag-offline'
  }
  return map[status] || 'tag-offline'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    1: '在售',
    2: '缺货',
    3: '下架'
  }
  return map[status] || '未知'
}

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) {
    console.warn('用户ID不存在，无法加载商品列表')
    return
  }

  try {
    loading.value = true
    
    // 构建请求参数
    const params: ProductListWithFilterDTO = {
      productListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      productFilterDTO: {
        keyword: searchFormState.value.productName || null,
        productId: searchFormState.value.productId ? Number(searchFormState.value.productId) : null,
        type: searchFormState.value.type ? Number(searchFormState.value.type) : null,
        status: searchFormState.value.status !== '' ? Number(searchFormState.value.status) : null,
        mainCategoryId: searchFormState.value.mainCategoryId ? Number(searchFormState.value.mainCategoryId) : null,
        subCategoryId: searchFormState.value.subCategoryId ? Number(searchFormState.value.subCategoryId) : null
      }
    }
    
    const res = await fetchProductList(params)
    
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchFormState.value.productId = ''
  searchFormState.value.mainCategoryId = ''
  searchFormState.value.subCategoryId = ''
  subCategoryOptions.value = []
  pagination.current = 1
  loadData()
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 选择变化
const handleSelectionChange = (selection: ProductListItem[]) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadData()
}

// 新增商品
const handleAdd = () => {
  addDialogVisible.value = true
}

// 新增成功回调
const handleAddSuccess = () => {
  loadData()
}

// 查看商品
const handleView = (row: ProductListItem) => {
  currentProductId.value = row.productId
  currentProductType.value = row.type
  detailDialogVisible.value = true
}

// 编辑商品
const handleEdit = (row: ProductListItem) => {
  // 从 tableData 中查找最新的行数据，避免使用可能过时的 row 引用
  const currentRow = tableData.value.find(item => item.productId === row.productId)
  const productType = currentRow?.type ?? row.type
  
  editProductId.value = row.productId
  editProductType.value = productType
  editDialogVisible.value = true
}

// 编辑成功回调
const handleEditSuccess = async () => {
  // 先刷新列表数据，确保类型等信息是最新的
  await loadData()
  // 清空编辑状态
  editProductId.value = null
  editProductType.value = null
  // 关闭对话框
  editDialogVisible.value = false
}

// 删除商品
const handleDelete = async (row: ProductListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除商品"${row.productName}"吗？删除后无法恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProduct(row.productId, Number(userId))
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 上架商品
const handleOnShelf = async (row: ProductListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要上架商品"${row.productName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await onSellingProduct(row.productId, Number(userId))
    ElMessage.success('上架成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '上架失败')
    }
  }
}

// 下架商品
const handleOffShelf = async (row: ProductListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要下架商品"${row.productName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await offShelfProduct(row.productId, Number(userId))
    ElMessage.success('下架成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '下架失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }

  try {
    // 先提示用户商品会先自动下架然后删除
    await ElMessageBox.confirm(
      `选中的 ${selectedRows.value.length} 个商品将会先自动下架，然后被删除。删除后无法恢复！\n\n请核对好需要删除的商品，确认继续吗？`,
      '批量删除确认',
      {
        confirmButtonText: '继续删除',
        cancelButtonText: '取消删除',
        confirmButtonClass: 'el-button--danger',
        type: 'warning'
      }
    )

    // 用户点击继续删除，调用批量删除接口
    const productIds = selectedRows.value.map(item => item.productId)
    await batchDeleteProduct(productIds, Number(userId))
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error: any) {
    // 点击取消删除或关闭弹窗时，保留用户选中的商品（不做任何操作）
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  }
}

onMounted(async () => {
  // 从路由参数初始化商品ID（确保路由已准备好）
  if (route.query.productId) {
    searchFormState.value.productId = String(route.query.productId)
  }

  // 先加载一级分类选项
  await loadMainCategories()

  // 如果路由参数中有一级分类，加载对应的二级分类选项
  const initialMainCategoryId = searchFormState.value.mainCategoryId
  if (initialMainCategoryId) {
    try {
      const res = await fetchAllSubCategories(Number(initialMainCategoryId))
      allSubCategories.value = res
      subCategoryOptions.value = res.map(item => ({
        label: item.name,
        value: String(item.categoryId)
      }))
    } catch (error) {
      console.error('获取二级分类失败:', error)
    }
  }

  // 加载商品数据
  await loadData()
})
</script>

<style scoped lang="scss">
.product-info {
  .product-image {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--el-fill-color-light);
    border-radius: 8px;
  }
}

.text-danger {
  color: var(--el-color-danger);
}

.font-medium {
  font-weight: 500;
}

// 自定义标签样式 - 更深的颜色
.tag-pet {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-supply {
  background-color: #cce5ff;
  color: #004085;
  border-color: #b8daff;
}

.tag-online {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-offline {
  background-color: #e2e3e5;
  color: #383d41;
  border-color: #d6d8db;
}

.tag-outstock {
  background-color: #f8d7da;
  color: #721c24;
  border-color: #f5c6cb;
}

// 固定表格高度
:deep(.art-table) {
  min-height: 680px;
  
  .el-table {
    height: 100% !important;
  }
}
</style>
