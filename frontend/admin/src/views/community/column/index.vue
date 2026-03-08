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
          <h4 class="m-0">栏目列表</h4>
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
              新增栏目
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
        <!-- 栏目名称列 -->
        <template #columnName="{ row }">
          <div class="flex items-center gap-2">
            <ElTag class="tag-column">
              <ArtSvgIcon icon="ri:file-copy-line" :size="14" />
            </ElTag>
            <span class="font-medium">{{ row.columnName }}</span>
          </div>
        </template>

        <!-- 帖子数量列 -->
        <template #postCount="{ row }">
          <span class="post-count">{{ row.postCount }}</span>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看帖子" @click="handleViewPosts(row)" />
            <ArtButtonTable type="edit" :row="row" tooltip="编辑专栏" @click="handleEdit(row)" />
            <ArtButtonTable type="delete" :row="row" tooltip="删除专栏" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 新增/编辑栏目弹窗 -->
    <ElDialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑栏目' : '新增栏目'"
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
        <ElFormItem label="栏目名称" prop="columnName">
          <ElInput
            v-model.trim="formData.columnName"
            placeholder="请输入栏目名称"
            maxlength="25"
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
  fetchColumnList,
  addColumn,
  changeColumn,
  deleteColumn,
  batchDeleteColumn,
  type ColumnListItem,
  type ColumnListWithFilterDTO
} from '@/api/column'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ColumnOption } from '@/types/component'

defineOptions({ name: 'ColumnManage' })

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<ColumnListItem[]>([])
const selectedRows = ref<ColumnListItem[]>([])
const searchBarRef = ref()
const tableRef = ref()

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const currentColumnId = ref<number | null>(null)
const originalColumnName = ref('') // 记录原始栏目名称，用于检测是否修改

// 表单数据
const formData = reactive({
  columnName: ''
})

// 表单验证规则
const formRules: FormRules = {
  columnName: [
    { required: true, message: '请输入栏目名称', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value || value.trim() === '') {
          callback(new Error('栏目名称不能为空或纯空格'))
        } else if (value !== value.trim()) {
          callback(new Error('栏目名称首尾不能包含空格'))
        } else if (value.trim().length > 25) {
          callback(new Error('栏目名称不能超过25个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 搜索表单状态
const searchFormState = ref({
  columnName: '',
  sortFlag: '1' // 默认降序
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'columnName',
    label: '栏目名称',
    type: 'input',
    props: {
      placeholder: '请输入栏目名称'
    }
  },
  {
    key: 'sortFlag',
    label: '帖子数排序',
    labelWidth: '100px',
    type: 'select',
    props: {
      placeholder: '选择排序方式',
      clearable: false,
      options: [
        { label: '从高到低', value: '1' },
        { label: '从低到高', value: '2' }
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
const columns = ref<ColumnOption<ColumnListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 80, label: '序号', fixed: 'left', visible: true },
  {
    prop: 'columnName',
    label: '栏目名称',
    minWidth: 300,
    useSlot: true,
    visible: true
  },
  {
    prop: 'postCount',
    label: '帖子数量',
    width: 150,
    useSlot: true,
    visible: true
  },
  {
    prop: 'operation',
    label: '操作',
    width: 150,
    useSlot: true,
    fixed: 'right',
    visible: true
  }
])

// 过滤后的可见列
const visibleColumns = computed(() =>
  columns.value.filter(col => col.visible !== false)
)

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) {
    console.warn('用户ID不存在，无法加载栏目列表')
    return
  }

  try {
    loading.value = true

    // 构建请求参数
    const params: ColumnListWithFilterDTO = {
      columnListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      columnListFilterDTO: {
        columnName: searchFormState.value.columnName.trim() || null,
        sortFlag: Number(searchFormState.value.sortFlag)
      }
    }

    const res = await fetchColumnList(params)

    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error) {
    console.error('获取栏目列表失败:', error)
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
  searchFormState.value = {
    columnName: '',
    sortFlag: '1'
  }
  pagination.current = 1
  loadData()
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 选择变化
const handleSelectionChange = (selection: ColumnListItem[]) => {
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

// 新增栏目
const handleAdd = () => {
  isEdit.value = false
  currentColumnId.value = null
  originalColumnName.value = ''
  formData.columnName = ''
  dialogVisible.value = true
}

// 编辑栏目
const handleEdit = (row: ColumnListItem) => {
  isEdit.value = true
  currentColumnId.value = row.columnId
  originalColumnName.value = row.columnName // 记录原始名称
  formData.columnName = row.columnName
  dialogVisible.value = true
}

// 查看栏目下的帖子
const handleViewPosts = (row: ColumnListItem) => {
  router.push({
    path: '/community/post',
    query: { columnId: row.columnId.toString() }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息不存在')
    return
  }

  // 去除首尾空格
  const trimmedName = formData.columnName.trim()

  // 编辑模式下检测是否有修改
  if (isEdit.value && trimmedName === originalColumnName.value) {
    ElMessage.info('栏目名称未发生变化')
    dialogVisible.value = false
    return
  }

  try {
    submitting.value = true

    if (isEdit.value) {
      // 修改栏目
      await changeColumn({
        columnId: currentColumnId.value!,
        userId: Number(userId),
        columnName: trimmedName
      })
      ElMessage.success('修改成功')
    } else {
      // 新增栏目
      await addColumn({
        columnName: trimmedName,
        userId: Number(userId)
      })
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || (isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formData.columnName = ''
  currentColumnId.value = null
  originalColumnName.value = ''
  formRef.value?.resetFields()
}

// 删除栏目
const handleDelete = async (row: ColumnListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息不存在')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除栏目"${row.columnName}"吗？删除后无法恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteColumn(row.columnId, Number(userId))
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息不存在')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个栏目吗？删除后无法恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const columnIds = selectedRows.value.map(row => row.columnId)
    await batchDeleteColumn(columnIds, Number(userId))
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量删除失败')
    }
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped lang="scss">
.tag-column {
  background-color: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
  font-weight: 500;
}

.post-count {
  font-weight: 500;
  color: var(--el-color-primary);
  font-size: 14px;
}

.font-medium {
  font-weight: 500;
}

// 固定表格高度
:deep(.art-table) {
  min-height: 680px;

  .el-table {
    height: 100% !important;
  }
}
</style>
