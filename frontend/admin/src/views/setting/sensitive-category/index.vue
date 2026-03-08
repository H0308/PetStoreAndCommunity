<template>
  <div class="flex flex-col gap-4 pb-5">
    <!-- 搜索区域 -->
    <ArtSearchBar
      ref="searchBarRef"
      v-model="searchFormState"
      :items="searchItems"
      :is-expand="false"
      :show-expand="false"
      :show-reset-button="true"
      :show-search-button="true"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 表格区域 -->
    <ElCard class="flex-1 art-table-card" shadow="never" style="margin-top: 0">
      <template #header>
        <div class="flex-cb">
          <h4 class="m-0">敏感词分类列表</h4>
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
              新增分类
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
        <!-- 分类名称列 -->
        <template #categoryName="{ row }">
          <span class="category-name">{{ row.categoryName }}</span>
        </template>

        <!-- 敏感词数量列 -->
        <template #wordCount="{ row }">
          <ElTag type="info">{{ row.wordCount }} 个</ElTag>
        </template>

        <!-- 创建时间列 -->
        <template #createTime="{ row }">
          <span class="text-g-700">{{ row.createTime }}</span>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看分类商品" @click="handleViewWords(row)" />
            <ArtButtonTable type="edit" :row="row" tooltip="编辑" @click="handleEdit(row)" />
            <ArtButtonTable type="delete" :row="row" tooltip="删除" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 新增/编辑分类弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑敏感词分类' : '新增敏感词分类'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
      @close="handleDialogClose"
    >
      <ElForm
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
        label-position="left"
      >
        <ElFormItem label="分类名称" prop="categoryName">
          <ElInput
            v-model.trim="formData.categoryName"
            placeholder="请输入分类名称"
            maxlength="30"
            show-word-limit
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { useRouter } from 'vue-router'
import {
  fetchSensitiveCategoryPageList,
  addSensitiveCategory,
  updateSensitiveCategory,
  deleteSensitiveCategory,
  batchDeleteSensitiveCategory,
  type SensitiveCategoryListItem,
  type SensitiveCategoryListWithFilterDTO
} from '@/api/sensitiveWord'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ColumnOption } from '@/types/component'

defineOptions({ name: 'SensitiveCategory' })

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<SensitiveCategoryListItem[]>([])
const selectedRows = ref<SensitiveCategoryListItem[]>([])
const searchBarRef = ref()
const tableRef = ref()

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  categoryName: ''
})

// 表单验证规则
const formRules: FormRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 30, message: '分类名称不能超过30个字符', trigger: 'blur' }
  ]
}

// 搜索表单状态
const searchFormState = ref({
  keyword: ''
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'keyword',
    label: '分类名称',
    type: 'input',
    props: { placeholder: '请输入分类名称关键字' }
  }
])

// 分页
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 表格列配置
const columns = ref<ColumnOption<SensitiveCategoryListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 80, label: '序号', fixed: 'left', visible: true },
  { prop: 'categoryName', label: '分类名称', minWidth: 200, useSlot: true, visible: true },
  { prop: 'wordCount', label: '敏感词数量', width: 150, useSlot: true, visible: true },
  { prop: 'createTime', label: '创建时间', width: 180, useSlot: true, visible: true },
  { prop: 'operation', label: '操作', width: 150, useSlot: true, fixed: 'right', visible: true }
])

// 过滤后的可见列
const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    loading.value = true
    const params: SensitiveCategoryListWithFilterDTO = {
      sensitiveCategoryListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      sensitiveCategoryListFilterDTO: {
        keyword: searchFormState.value.keyword.trim() || null
      }
    }
    const res = await fetchSensitiveCategoryPageList(params)
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取敏感词分类列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchFormState.value = { keyword: '' }; pagination.current = 1; loadData() }
const handleRefresh = () => loadData()
const handleSelectionChange = (selection: SensitiveCategoryListItem[]) => { selectedRows.value = selection }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.current = 1; loadData() }
const handleCurrentChange = (page: number) => { pagination.current = page; loadData() }

// 新增分类
const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  formData.categoryName = ''
  dialogVisible.value = true
}

// 查看该分类下的敏感词
const handleViewWords = (row: SensitiveCategoryListItem) => {
  router.push({
    path: '/setting/sensitive',
    query: { categoryId: String(row.categoryId) }
  })
}

// 编辑分类
const handleEdit = (row: SensitiveCategoryListItem) => {
  isEdit.value = true
  editingId.value = row.categoryId
  formData.categoryName = row.categoryName
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }

  try {
    submitting.value = true
    if (isEdit.value && editingId.value) {
      await updateSensitiveCategory({
        userId: Number(userId),
        categoryId: editingId.value,
        name: formData.categoryName.trim()
      })
      ElMessage.success('修改成功')
    } else {
      await addSensitiveCategory({
        userId: Number(userId),
        name: formData.categoryName.trim()
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || (isEdit.value ? '修改失败' : '新增失败'))
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formData.categoryName = ''
  editingId.value = null
  isEdit.value = false
  formRef.value?.resetFields()
}

// 删除分类
const handleDelete = async (row: SensitiveCategoryListItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm(`确定要删除分类"${row.categoryName}"吗？删除后该分类下的敏感词将无法归类。`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteSensitiveCategory(row.categoryId, Number(userId))
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个分类吗？删除后这些分类下的敏感词将无法归类。`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const categoryIds = selectedRows.value.map(row => row.categoryId)
    await batchDeleteSensitiveCategory(categoryIds, Number(userId))
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '批量删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.category-name {
  font-weight: 500;
  color: var(--el-color-primary);
}

:deep(.art-table) {
  min-height: 680px;
  .el-table { height: 100% !important; }
}
</style>
