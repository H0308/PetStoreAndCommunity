<template>
  <div class="category-page">
    <!-- 顶部工具栏 -->
    <ElCard class="toolbar-card" shadow="never">
      <div class="toolbar">
        <h3 class="toolbar-title">
          <el-icon class="title-icon"><FolderOpened /></el-icon>
          商品分类库
        </h3>
        <div class="toolbar-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索分类名称"
            clearable
            class="search-input"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">
            搜索
          </el-button>
          <ElButton type="primary" @click="handleAddParent" v-ripple>
            <el-icon class="mr-1"><Plus /></el-icon>
            新增一级分类
          </ElButton>
          <ElButton @click="handleBatchDeleteMain" :disabled="selectedMainCategories.length === 0" v-ripple>
            <el-icon class="mr-1"><Delete /></el-icon>
            批量删除 ({{ selectedMainCategories.length }})
          </ElButton>
        </div>
      </div>
    </ElCard>

    <!-- 主体表格 -->
    <ElCard class="table-card art-table-card" shadow="never">
      <div class="table-wrapper">
        <el-table
          ref="mainTableRef"
          v-loading="parentLoading"
          :data="parentCategoryList"
          row-key="id"
          :expand-row-keys="expandedRowKeys"
          @expand-change="handleExpandChange"
          @selection-change="handleMainSelectionChange"
          class="category-table"
          height="100%"
        >
        <!-- 复选框列 -->
        <el-table-column type="selection" width="50" />
        
        <!-- 展开列（隐藏箭头） -->
        <el-table-column type="expand" width="1">
          <template #default="{ row }">
            <div class="expand-container">
              <!-- 左侧引导线 -->
              <div class="guide-line"></div>
              
              <!-- 二级分类内容区 -->
              <div class="child-content" v-loading="childLoadingMap[row.id]">
                <!-- 有子分类数据 -->
                <template v-if="childDataMap[row.id]?.list?.length">
                  <!-- 二级分类批量操作工具栏 -->
                  <div class="child-batch-toolbar" v-if="selectedSubCategories[row.id]?.length > 0">
                    <span class="selected-info">已选择 {{ selectedSubCategories[row.id].length }} 个二级分类</span>
                    <el-button type="danger" size="small" @click="handleBatchDeleteSub(row.id)">
                      <el-icon class="mr-1"><Delete /></el-icon>
                      批量删除
                    </el-button>
                    <el-button size="small" @click="clearSubSelection(row.id)">取消选择</el-button>
                  </div>
                  
                  <el-table
                    :ref="(el: any) => setChildTableRef(row.id, el)"
                    :data="childDataMap[row.id].list"
                    class="child-table"
                    :show-header="false"
                    @selection-change="(selection: ChildCategoryItem[]) => handleSubSelectionChange(row.id, selection)"
                  >
                    <!-- 复选框列 -->
                    <el-table-column type="selection" width="40" />
                    
                    <el-table-column min-width="280">
                      <template #default="{ row: childRow }">
                        <div class="child-name-cell">
                          <span class="child-dot"></span>
                          <span class="child-name">{{ childRow.name }}</span>
                        </div>
                      </template>
                    </el-table-column>
                    
                    <el-table-column min-width="260">
                      <template #default="{ row: childRow }">
                        <el-tag class="tag-products-light">
                          商品数量 {{ childRow.productCount || 0 }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    
                    <el-table-column min-width="140">
                      <template #default="{ row: childRow }">
                        <div class="action-buttons child-actions">
                          <ArtButtonTable type="view" tooltip="查看商品" @click="handleViewSubCategoryProducts(childRow)" />
                          <ArtButtonTable type="edit" tooltip="编辑分类" @click="handleEdit(childRow)" />
                          <ArtButtonTable type="delete" tooltip="删除分类" @click="handleDelete(childRow)" />
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                  
                  <!-- 二级分类微型分页器 -->
                  <div class="child-pagination-wrapper" v-if="childDataMap[row.id]?.total > childPageSize">
                    <el-pagination
                      small
                      layout="prev, pager, next"
                      :total="childDataMap[row.id].total"
                      :page-size="childPageSize"
                      :current-page="childDataMap[row.id].currentPage"
                      @current-change="(page: number) => handleChildPageChange(row.id, page)"
                    />
                  </div>
                </template>
                
                <!-- 空状态 -->
                <template v-else-if="!childLoadingMap[row.id]">
                  <div class="empty-child">
                    <el-empty description="暂无子分类" :image-size="60">
                      <el-button type="primary" size="small" @click="handleAddChild(row)">
                        <el-icon class="mr-1"><Plus /></el-icon>
                        点击新增
                      </el-button>
                    </el-empty>
                  </div>
                </template>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 分类名称列 -->
        <el-table-column label="分类名称" min-width="320">
          <template #default="{ row }">
            <div class="category-name" @click="toggleExpand(row)">
              <el-icon class="folder-icon" :class="{ 'is-open': expandedRowKeys.includes(String(row.id)) }">
                <FolderOpened v-if="expandedRowKeys.includes(String(row.id))" />
                <Folder v-else />
              </el-icon>
              <span class="parent-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 数据概览列 -->
        <el-table-column label="数据概览" min-width="260">
          <template #default="{ row }">
            <div class="data-overview">
              <el-tag class="tag-children">
                二级分类 {{ row.childrenCount || 0 }}
              </el-tag>
              <el-tag class="tag-products">
                商品数量 {{ row.totalProducts || 0 }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons parent-actions">
              <ArtButtonTable type="view" tooltip="查看商品" @click="handleViewMainCategoryProducts(row)" />
              <ArtButtonTable type="add" tooltip="新增子分类" @click="handleAddChild(row)" />
              <ArtButtonTable type="edit" tooltip="编辑分类" @click="handleEdit(row)" />
              <ArtButtonTable type="delete" tooltip="删除分类" @click="handleDelete(row)" />
            </div>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <!-- 一级分类分页器 -->
      <div class="parent-pagination-wrapper">
        <el-pagination
          v-model:current-page="parentCurrentPage"
          v-model:page-size="parentPageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="parentTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleParentSizeChange"
          @current-change="handleParentPageChange"
        />
      </div>
    </ElCard>

    <!-- 新增/编辑分类弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
      class="category-dialog"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="left"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item v-if="dialogType === 'addParent' || (dialogType === 'edit' && !formData.parentId)" label="分类类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :value="1">宠物分类</el-radio>
            <el-radio :value="2">用品分类</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dialogType === 'addChild'" label="所属一级分类">
          <el-input :value="parentCategoryName" disabled />
        </el-form-item>
        <el-form-item v-if="dialogType === 'edit' && formData.parentId !== null" label="所属一级分类" prop="parentId">
          <el-select v-model="formData.parentId" placeholder="请选择所属一级分类" style="width: 100%">
            <el-option
              v-for="item in allMainCategories"
              :key="item.categoryId"
              :label="item.name"
              :value="item.categoryId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { FolderOpened, Folder, Plus, Search, Delete } from '@element-plus/icons-vue'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { 
  fetchMainCategoryList, 
  fetchSubCategoryList,
  searchCategory,
  addMainCategory,
  addSubCategory,
  changeMainCategory,
  changeSubCategory,
  deleteMainCategory,
  deleteSubCategory,
  batchDeleteMainCategory,
  batchDeleteSubCategory,
  fetchAllMainCategories,
  type MainCategoryListVO,
  type SubCategoryListVO,
  type SuperCategoryVO
} from '@/api/category'

defineOptions({ name: 'ProductCategory' })

const router = useRouter()
const userStore = useUserStore()

// 获取当前用户ID
const userId = computed(() => userStore.info.userId || 0)

// ==================== 表格引用 ====================
const mainTableRef = ref()

// ==================== 批量选择相关 ====================
const selectedMainCategories = ref<ParentCategoryItem[]>([])
const selectedSubCategories = ref<Record<number, ChildCategoryItem[]>>({})
const childTableRefs = ref<Record<number, any>>({})

// 设置二级分类表格引用
const setChildTableRef = (parentId: number, el: any) => {
  if (el) {
    childTableRefs.value[parentId] = el
  }
}

// ==================== 一级分类分页相关 ====================
const parentLoading = ref(false)
const parentCurrentPage = ref(1)
const parentPageSize = ref(10)
const parentTotal = ref(0)
const parentCategoryList = ref<ParentCategoryItem[]>([])

// ==================== 二级分类分页相关 ====================
const childPageSize = 5 // 二级分类每页显示数量
const expandedRowKeys = ref<string[]>([]) // 当前展开的行
const childLoadingMap = ref<Record<number, boolean>>({}) // 各一级分类下二级分类的加载状态
const childDataMap = ref<Record<number, ChildPaginationData>>({}) // 各一级分类下二级分类的数据

// 搜索关键词
const searchKeyword = ref('')
// 是否处于搜索模式
const isSearchMode = ref(false)

// ==================== 数据接口定义 ====================
interface ParentCategoryItem {
  id: number
  name: string
  type: number // 1: 宠物分类, 2: 用品分类
  childrenCount: number
  totalProducts: number
}

interface ChildCategoryItem {
  id: number
  name: string
  parentId: number
  productCount: number
}

interface ChildPaginationData {
  list: ChildCategoryItem[]
  total: number
  currentPage: number
}

// ==================== 一级分类数据获取 ====================
const fetchParentCategories = async () => {
  parentLoading.value = true
  // 收起所有展开行
  expandedRowKeys.value = []
  // 清空二级分类缓存
  childDataMap.value = {}
  isSearchMode.value = false
  
  try {
    const res = await fetchMainCategoryList({
      userId: userId.value,
      currentPage: parentCurrentPage.value,
      pageSize: parentPageSize.value
    })
    
    // 转换数据格式
    parentCategoryList.value = res.totalRecords.map((item: MainCategoryListVO) => ({
      id: item.categoryId,
      name: item.categoryName,
      type: item.type,
      childrenCount: item.subCategoryCount,
      totalProducts: item.productCount
    }))
    parentTotal.value = res.totalCount
  } catch (error) {
    console.error('获取一级分类失败:', error)
    ElMessage.error('获取分类数据失败')
  } finally {
    parentLoading.value = false
  }
}

// ==================== 二级分类数据获取 ====================
const fetchChildCategories = async (parentId: number, page: number = 1) => {
  childLoadingMap.value[parentId] = true
  
  try {
    const res = await fetchSubCategoryList({
      mainCategoryId: parentId,
      userId: userId.value,
      currentPage: page,
      pageSize: childPageSize
    })
    
    // 转换数据格式
    const list = res.totalRecords.map((item: SubCategoryListVO) => ({
      id: item.categoryId,
      name: item.categoryName,
      parentId: item.mainCategoryId,
      productCount: item.productCount
    }))
    
    childDataMap.value[parentId] = {
      list,
      total: res.totalCount,
      currentPage: page
    }
  } catch (error) {
    console.error('获取二级分类失败:', error)
    ElMessage.error('获取子分类数据失败')
  } finally {
    childLoadingMap.value[parentId] = false
  }
}

// ==================== 搜索分类 ====================
const searchCategories = async () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    // 关键词为空，恢复正常列表
    fetchParentCategories()
    return
  }
  
  parentLoading.value = true
  isSearchMode.value = true
  // 清空之前的数据
  expandedRowKeys.value = []
  childDataMap.value = {}
  
  try {
    const res = await searchCategory({
      userId: userId.value,
      keyword: keyword,
      currentPage: parentCurrentPage.value,
      pageSize: parentPageSize.value
    })
    
    const mainCategoryResult = res.mainCategoryListVOPageVO
    const subCategoryResult = res.subCategoryListVOPageVO
    
    // 转换一级分类数据
    parentCategoryList.value = mainCategoryResult.totalRecords.map((item: MainCategoryListVO) => ({
      id: item.categoryId,
      name: item.categoryName,
      type: item.type,
      childrenCount: item.subCategoryCount,
      totalProducts: item.productCount
    }))
    parentTotal.value = mainCategoryResult.totalCount
    
    // 处理二级分类搜索结果
    if (subCategoryResult.totalRecords && subCategoryResult.totalRecords.length > 0) {
      // 按一级分类ID分组二级分类
      const groupedSubCategories: Record<number, ChildCategoryItem[]> = {}
      const parentIdsToExpand: Set<number> = new Set()
      
      subCategoryResult.totalRecords.forEach((item: SubCategoryListVO) => {
        const parentId = item.mainCategoryId
        parentIdsToExpand.add(parentId)
        
        if (!groupedSubCategories[parentId]) {
          groupedSubCategories[parentId] = []
        }
        groupedSubCategories[parentId].push({
          id: item.categoryId,
          name: item.categoryName,
          parentId: parentId,
          productCount: item.productCount
        })
      })
      
      // 将分组后的二级分类存入childDataMap
      Object.keys(groupedSubCategories).forEach(parentIdStr => {
        const parentId = Number(parentIdStr)
        const list = groupedSubCategories[parentId]
        childDataMap.value[parentId] = {
          list,
          total: list.length,
          currentPage: 1
        }
      })
      
      // 自动展开包含搜索结果的一级分类
      // 只展开当前页面存在的一级分类
      const currentPageParentIds = parentCategoryList.value.map(item => item.id)
      const expandIds = Array.from(parentIdsToExpand)
        .filter(id => currentPageParentIds.includes(id))
        .map(id => String(id))
      expandedRowKeys.value = expandIds
    }
  } catch (error) {
    console.error('搜索分类失败:', error)
    ElMessage.error('搜索分类失败')
  } finally {
    parentLoading.value = false
  }
}

// ==================== 事件处理 ====================
// 搜索
const handleSearch = () => {
  parentCurrentPage.value = 1
  if (searchKeyword.value.trim()) {
    searchCategories()
  } else {
    fetchParentCategories()
  }
}

// 一级分类分页变化
const handleParentPageChange = () => {
  if (isSearchMode.value && searchKeyword.value.trim()) {
    searchCategories()
  } else {
    fetchParentCategories()
  }
}

// 一级分类每页数量变化
const handleParentSizeChange = () => {
  parentCurrentPage.value = 1
  if (isSearchMode.value && searchKeyword.value.trim()) {
    searchCategories()
  } else {
    fetchParentCategories()
  }
}

// 展开行变化
const handleExpandChange = (row: ParentCategoryItem, expandedRows: ParentCategoryItem[]) => {
  expandedRowKeys.value = expandedRows.map(r => String(r.id))
  
  // 如果是展开操作且该分类下还没有加载过数据（非搜索模式下才需要加载）
  if (expandedRowKeys.value.includes(String(row.id)) && !childDataMap.value[row.id]) {
    fetchChildCategories(row.id, 1)
  }
}

// 点击文件夹图标切换展开/折叠
const toggleExpand = (row: ParentCategoryItem) => {
  const rowId = String(row.id)
  const isExpanded = expandedRowKeys.value.includes(rowId)
  
  if (isExpanded) {
    // 折叠
    expandedRowKeys.value = expandedRowKeys.value.filter(id => id !== rowId)
  } else {
    // 展开
    expandedRowKeys.value = [...expandedRowKeys.value, rowId]
    // 如果还没有加载过数据，则加载
    if (!childDataMap.value[row.id]) {
      fetchChildCategories(row.id, 1)
    }
  }
}

// 二级分类分页变化
const handleChildPageChange = (parentId: number, page: number) => {
  // 搜索模式下不支持二级分类分页，直接获取完整数据
  if (!isSearchMode.value) {
    fetchChildCategories(parentId, page)
  }
}

// ==================== 弹窗相关 ====================
const dialogVisible = ref(false)
const dialogType = ref<'addParent' | 'addChild' | 'edit'>('addParent')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: null as number | null,
  name: '',
  parentId: null as number | null,
  type: 1 as number // 1: 宠物分类, 2: 用品分类
})

// 编辑时保存原始数据，用于比较是否修改
const originalData = ref<{
  name: string
  type: number
  parentId: number | null
} | null>(null)

// 所有一级分类列表（用于编辑二级分类时选择）
const allMainCategories = ref<SuperCategoryVO[]>([])

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度为2-50个字符', trigger: 'blur' },
    { 
      validator: (rule: any, value: string, callback: any) => {
        if (!value || value.trim() === '') {
          callback(new Error('分类名称不能为空或纯空格'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  type: [
    { required: true, message: '请选择分类类型', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  const titles = {
    addParent: '新增一级分类',
    addChild: '新增子分类',
    edit: '编辑分类'
  }
  return titles[dialogType.value]
})

const parentCategoryName = computed(() => {
  if (!formData.parentId) return ''
  const parent = parentCategoryList.value.find(item => item.id === formData.parentId)
  return parent?.name || ''
})

// 新增一级分类
const handleAddParent = () => {
  dialogType.value = 'addParent'
  formData.id = null
  formData.name = ''
  formData.parentId = null
  formData.type = 1
  dialogVisible.value = true
}

// 新增子分类
const handleAddChild = (row: ParentCategoryItem) => {
  dialogType.value = 'addChild'
  formData.id = null
  formData.name = ''
  formData.parentId = row.id
  formData.type = 1
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = async (row: ParentCategoryItem | ChildCategoryItem) => {
  dialogType.value = 'edit'
  formData.id = row.id
  formData.name = row.name
  formData.parentId = 'parentId' in row ? row.parentId : null
  // 一级分类有type字段，二级分类没有
  formData.type = 'type' in row ? row.type : 1
  // 保存原始数据用于比较
  originalData.value = {
    name: row.name,
    type: 'type' in row ? row.type : 1,
    parentId: 'parentId' in row ? row.parentId : null
  }
  
  // 如果是编辑二级分类，加载所有一级分类用于下拉选择
  if ('parentId' in row) {
    try {
      allMainCategories.value = await fetchAllMainCategories()
    } catch (error) {
      console.error('获取一级分类列表失败:', error)
    }
  }
  
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row: ParentCategoryItem | ChildCategoryItem) => {
  const isParent = !('parentId' in row)
  const message = isParent
    ? `确定要删除一级分类"${row.name}"及其所有子分类吗？`
    : `确定要删除分类"${row.name}"吗？`

  ElMessageBox.confirm(message, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      if (isParent) {
        // 删除一级分类
        await deleteMainCategory(row.id, userId.value)
        ElMessage.success('删除一级分类成功')
        fetchParentCategories()
      } else {
        // 删除二级分类
        const parentId = (row as ChildCategoryItem).parentId
        await deleteSubCategory(row.id, userId.value)
        ElMessage.success('删除二级分类成功')
        // 刷新该一级分类下的二级分类数据
        const currentPage = childDataMap.value[parentId]?.currentPage || 1
        fetchChildCategories(parentId, currentPage)
        // 刷新一级分类列表以更新子分类数量
        fetchParentCategories()
      }
    } catch (error: any) {
      console.error('删除失败:', error)
      ElMessage.error(error?.message || '删除失败，请重试')
    }
  }).catch(() => {})
}

// 一级分类选择变化
const handleMainSelectionChange = (selection: ParentCategoryItem[]) => {
  selectedMainCategories.value = selection
}

// 清空一级分类选择
const clearMainSelection = () => {
  selectedMainCategories.value = []
  mainTableRef.value?.clearSelection()
}

// 批量删除一级分类
const handleBatchDeleteMain = async () => {
  if (selectedMainCategories.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMainCategories.value.length} 个一级分类及其所有子分类吗？此操作不可恢复！`, 
      '批量删除确认', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const mainCategoryIds = selectedMainCategories.value.map(item => item.id)
    await batchDeleteMainCategory(mainCategoryIds, userId.value)
    ElMessage.success('批量删除成功')
    clearMainSelection()
    fetchParentCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error?.message || '批量删除失败，请重试')
    }
  }
}

// 二级分类选择变化
const handleSubSelectionChange = (parentId: number, selection: ChildCategoryItem[]) => {
  selectedSubCategories.value[parentId] = selection
}

// 清空二级分类选择
const clearSubSelection = (parentId: number) => {
  selectedSubCategories.value[parentId] = []
  childTableRefs.value[parentId]?.clearSelection()
}

// 批量删除二级分类
const handleBatchDeleteSub = async (parentId: number) => {
  const selected = selectedSubCategories.value[parentId]
  if (!selected || selected.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selected.length} 个二级分类吗？此操作不可恢复！`, 
      '批量删除确认', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const subCategoryIds = selected.map(item => item.id)
    await batchDeleteSubCategory(subCategoryIds, userId.value)
    ElMessage.success('批量删除成功')
    clearSubSelection(parentId)
    // 刷新该一级分类下的二级分类数据
    const currentPage = childDataMap.value[parentId]?.currentPage || 1
    fetchChildCategories(parentId, currentPage)
    // 刷新一级分类列表以更新子分类数量
    fetchParentCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error(error?.message || '批量删除失败，请重试')
    }
  }
}

// 查看一级分类下的商品
const handleViewMainCategoryProducts = (row: ParentCategoryItem) => {
  router.push({
    path: '/product/list',
    query: { mainCategoryId: row.id.toString() }
  })
}

// 查看二级分类下的商品
const handleViewSubCategoryProducts = (row: ChildCategoryItem) => {
  router.push({
    path: '/product/list',
    query: { 
      mainCategoryId: row.parentId.toString(),
      subCategoryId: row.id.toString()
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitLoading.value = true
  
  try {
    if (dialogType.value === 'addParent') {
      // 新增一级分类
      await addMainCategory({
        userId: userId.value,
        categoryName: formData.name.trim(),
        type: formData.type
      })
      ElMessage.success('新增一级分类成功')
      dialogVisible.value = false
      fetchParentCategories()
    } else if (dialogType.value === 'addChild' && formData.parentId) {
      // 新增二级分类
      await addSubCategory({
        userId: userId.value,
        mainCategoryId: formData.parentId,
        categoryName: formData.name.trim()
      })
      ElMessage.success('新增子分类成功')
      dialogVisible.value = false
      // 刷新该一级分类下的二级分类数据
      const currentPage = childDataMap.value[formData.parentId]?.currentPage || 1
      fetchChildCategories(formData.parentId, currentPage)
      // 同时刷新一级分类列表以更新子分类数量
      fetchParentCategories()
    } else if (dialogType.value === 'edit' && formData.id && originalData.value) {
      // 编辑分类
      const isParent = formData.parentId === null
      const trimmedName = formData.name.trim()
      
      // 检查是否有修改
      const nameChanged = trimmedName !== originalData.value.name
      const typeChanged = formData.type !== originalData.value.type
      
      if (isParent) {
        // 编辑一级分类 - 可修改名称和类型
        if (!nameChanged && !typeChanged) {
          ElMessage.info('未检测到修改')
          dialogVisible.value = false
          return
        }
        
        await changeMainCategory({
          userId: userId.value,
          categoryId: formData.id,
          categoryName: nameChanged ? trimmedName : null,
          type: typeChanged ? formData.type : null
        })
        ElMessage.success('修改一级分类成功')
        dialogVisible.value = false
        fetchParentCategories()
      } else {
        // 编辑二级分类 - 可修改名称和所属一级分类
        const parentIdChanged = formData.parentId !== originalData.value.parentId
        
        if (!nameChanged && !parentIdChanged) {
          ElMessage.info('未检测到修改')
          dialogVisible.value = false
          return
        }
        
        await changeSubCategory({
          userId: userId.value,
          categoryId: formData.id,
          categoryName: nameChanged ? trimmedName : null,
          mainCategoryId: parentIdChanged ? formData.parentId : null
        })
        ElMessage.success('修改二级分类成功')
        dialogVisible.value = false
        
        // 刷新原一级分类下的二级分类数据
        const oldParentId = originalData.value.parentId!
        const oldCurrentPage = childDataMap.value[oldParentId]?.currentPage || 1
        fetchChildCategories(oldParentId, oldCurrentPage)
        
        // 如果修改了所属一级分类，也刷新新一级分类下的数据
        if (parentIdChanged && formData.parentId) {
          const newCurrentPage = childDataMap.value[formData.parentId]?.currentPage || 1
          fetchChildCategories(formData.parentId, newCurrentPage)
        }
        
        // 刷新一级分类列表以更新子分类数量
        fetchParentCategories()
      }
    }
  } catch (error: any) {
    console.error('操作失败:', error)
    ElMessage.error(error?.message || '操作失败，请重试')
  } finally {
    submitLoading.value = false
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchParentCategories()
})
</script>

<style scoped lang="scss">
.category-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 20px;
  height: calc(100vh - 120px);
}

// 工具栏卡片
.toolbar-card {
  border-radius: calc(var(--custom-radius, 8px) / 2 + 2px) !important;
  
  :deep(.el-card__body) {
    padding: 16px 20px;
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1D2129;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  
  .title-icon {
    color: var(--el-color-primary);
    font-size: 22px;
  }
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 200px;
}

// 表格卡片
.table-card {
  flex: 1;
  border-radius: calc(var(--custom-radius, 8px) / 2 + 2px) !important;
  display: flex;
  flex-direction: column;
  min-height: 0;
  
  :deep(.el-card__body) {
    padding: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }
}

// 表格包装器
.table-wrapper {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

// 二级分类批量操作工具栏
.child-batch-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin-bottom: 8px;
  background-color: #fef0f0;
  border-radius: 6px;
  
  .selected-info {
    color: #f56c6c;
    font-size: 13px;
    font-weight: 500;
  }
}

// 表格样式
.category-table {
  :deep(.el-table__inner-wrapper) {
    &::before {
      display: none;
    }
  }
  
  // 展开行样式
  :deep(.el-table__expanded-cell) {
    padding: 0 !important;
  }
  
  // 隐藏展开列
  :deep(.el-table__expand-column) {
    width: 0 !important;
    padding: 0 !important;
    
    .cell {
      display: none;
    }
  }
  
  // 隐藏展开图标
  :deep(.el-table__expand-icon) {
    display: none;
  }
}

// 分类名称样式
.category-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  
  &:hover .folder-icon {
    transform: scale(1.1);
  }
}

.folder-icon {
  font-size: 20px;
  color: var(--el-color-primary);
  transition: all 0.3s ease;
  
  &.is-open {
    color: var(--el-color-primary);
  }
}

.parent-name {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}

// 数据概览样式
.data-overview {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 自定义标签样式
.tag-children {
  background-color: #e2e3e5;
  color: #383d41;
  border-color: #d6d8db;
}

.tag-products {
  background-color: #cce5ff;
  color: #004085;
  border-color: #b8daff;
}

.tag-products-light {
  background-color: #f0f9ff;
  color: #0369a1;
  border-color: #e0f2fe;
}

// 操作按钮样式
.action-buttons {
  display: flex;
  align-items: center;
  
  &.parent-actions {
    padding-right: 16px;
  }
  
  &.child-actions {
    justify-content: flex-end;
  }
}

// ==================== 展开区域样式 ====================
.expand-container {
  display: flex;
  position: relative;
  min-height: 60px;
}

// 左侧引导线（在文件夹图标下方）
.guide-line {
  position: absolute;
  left: 25px;
  top: 0;
  bottom: 0;
  width: 1px;
  background-color: #E5E6EB;
}

// 二级分类内容区
.child-content {
  flex: 1;
  padding: 12px 20px 12px 56px;
  min-height: 60px;
}

// 二级分类表格
.child-table {
  background-color: transparent !important;
  
  :deep(.el-table__inner-wrapper) {
    &::before {
      display: none;
    }
  }
  
  :deep(.el-table__body-wrapper) {
    tr {
      background-color: transparent !important;
      
      &:hover > td {
        background-color: rgba(255, 138, 91, 0.08) !important;
      }
    }
    
    td {
      border-bottom: 1px solid #EBEEF5 !important;
      padding: 8px 0;
    }
    
    // 最后一行去掉底部边框
    tr:last-child td {
      border-bottom: none !important;
    }
  }
  
  // 移除表格边框
  :deep(.el-table__border-left-patch),
  :deep(.el-table__border-bottom-patch) {
    display: none;
  }
}

// 二级分类名称单元格
.child-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.child-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #FF8A5B;
  flex-shrink: 0;
}

.child-name {
  font-size: 14px;
  color: #4E5969;
}

// 二级分类分页器
.child-pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #E5E6EB;
  
  :deep(.el-pagination) {
    --el-pagination-bg-color: transparent;
    
    .el-pager li {
      background-color: #fff;
      border: 1px solid #E5E6EB;
      border-radius: 4px;
      min-width: 28px;
      height: 28px;
      line-height: 28px;
      
      &.is-active {
        background-color: var(--el-color-primary);
        border-color: var(--el-color-primary);
        color: #fff;
      }
    }
    
    .btn-prev,
    .btn-next {
      background-color: #fff;
      border: 1px solid #E5E6EB;
      border-radius: 4px;
      min-width: 28px;
      height: 28px;
    }
  }
}

// 空状态
.empty-child {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100px;
  
  :deep(.el-empty) {
    padding: 12px 0;
    
    .el-empty__description {
      margin-top: 8px;
    }
    
    .el-empty__bottom {
      margin-top: 12px;
    }
  }
}

// ==================== 一级分类分页器 ====================
.parent-pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #F2F3F5;
  flex-shrink: 0;
}

// ==================== 弹窗样式 ====================
.category-dialog {
  :deep(.el-dialog__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #F2F3F5;
    margin-right: 0;
  }
  
  :deep(.el-dialog__body) {
    padding: 24px 20px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 12px 20px;
    border-top: 1px solid #F2F3F5;
  }
}

// ==================== 暗黑模式适配 ====================
:root[data-theme='dark'] {
  .toolbar-title {
    color: #E5E6EB;
  }
  
  .parent-name {
    color: #E5E6EB;
  }
  
  .child-name {
    color: #A9AEB8;
  }
  
  .guide-line {
    background-color: #3D3D3F;
  }
  
  .child-table {
    :deep(.el-table__body-wrapper) {
      tr:hover > td {
        background-color: rgba(255, 138, 91, 0.12) !important;
      }
      
      td {
        border-color: #2A2A2B !important;
      }
    }
  }
  
  .child-pagination-wrapper {
    border-color: #3D3D3F;
    
    :deep(.el-pagination) {
      .el-pager li {
        background-color: #2A2A2B;
        border-color: #3D3D3F;
        color: #A9AEB8;
        
        &.is-active {
          background-color: var(--el-color-primary);
          border-color: var(--el-color-primary);
          color: #fff;
        }
      }
      
      .btn-prev,
      .btn-next {
        background-color: #2A2A2B;
        border-color: #3D3D3F;
      }
    }
  }
  
  .parent-pagination-wrapper {
    background-color: #1D1D1F;
    border-color: #2A2A2B;
  }
}
</style>
