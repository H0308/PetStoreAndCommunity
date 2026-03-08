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
          <h4 class="m-0">敏感词列表</h4>
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
              新增敏感词
            </ElButton>
            <ElButton @click="handleImport" v-ripple>
              <ArtSvgIcon icon="ri:download-2-line" class="mr-1" />
              导入CSV
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
        <!-- 敏感词列 -->
        <template #word="{ row }">
          <span class="sensitive-word">{{ row.word }}</span>
        </template>

        <!-- 分类列 -->
        <template #categoryName="{ row }">
          <ElTag class="tag-category">{{ row.categoryName }}</ElTag>
        </template>

        <!-- 创建时间列 -->
        <template #createTime="{ row }">
          <span class="text-g-700">{{ row.createTime }}</span>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="delete" :row="row" tooltip="删除" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 新增敏感词弹窗 -->
    <ElDialog
      v-model="addDialogVisible"
      title="新增敏感词"
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
        <ElFormItem label="敏感词" prop="word">
          <ElInput
            v-model.trim="formData.word"
            placeholder="请输入敏感词"
            maxlength="50"
            show-word-limit
          />
        </ElFormItem>
        <ElFormItem label="分类" prop="categoryId">
          <ElSelect v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <ElOption
              v-for="item in categoryOptions"
              :key="item.categoryId"
              :label="item.categoryName"
              :value="item.categoryId"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="addDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit" :loading="submitting">创建</ElButton>
      </template>
    </ElDialog>

    <!-- 隐藏的文件上传 -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".csv"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { useRoute } from 'vue-router'
import {
  fetchSensitiveWordList,
  fetchSensitiveCategoryList,
  addSensitiveWord,
  importSensitiveWordCSV,
  deleteSensitiveWord,
  batchDeleteSensitiveWord,
  type SensitiveWordListItem,
  type SensitiveWordListWithFilterDTO,
  type SensitiveCategoryVO
} from '@/api/sensitiveWord'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { ColumnOption } from '@/types/component'

defineOptions({ name: 'SensitiveWord' })

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<SensitiveWordListItem[]>([])
const selectedRows = ref<SensitiveWordListItem[]>([])
const searchBarRef = ref()
const tableRef = ref()
const fileInputRef = ref<HTMLInputElement>()

// 分类选项
const categoryOptions = ref<SensitiveCategoryVO[]>([])

// 弹窗相关
const addDialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive({
  word: '',
  categoryId: null as number | null
})

// 表单验证规则
const formRules: FormRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { max: 50, message: '敏感词不能超过50个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ]
}

// 搜索表单状态 - 从路由参数初始化
const searchFormState = ref({
  keyword: '',
  categoryId: (route.query.categoryId as string) || ''
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'keyword',
    label: '敏感词',
    type: 'input',
    props: { placeholder: '请输入敏感词关键字' }
  },
  {
    key: 'categoryId',
    label: '分类',
    type: 'select',
    props: {
      placeholder: '全部分类',
      clearable: true,
      options: categoryOptions.value.map(item => ({
        label: item.categoryName,
        value: String(item.categoryId)
      }))
    }
  }
])

// 分页
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 表格列配置
const columns = ref<ColumnOption<SensitiveWordListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 80, label: '序号', fixed: 'left', visible: true },
  { prop: 'word', label: '敏感词', minWidth: 200, useSlot: true, visible: true },
  { prop: 'categoryName', label: '分类', width: 150, useSlot: true, visible: true },
  { prop: 'createTime', label: '创建时间', width: 180, useSlot: true, visible: true },
  { prop: 'operation', label: '操作', width: 100, useSlot: true, fixed: 'right', visible: true }
])

// 过滤后的可见列
const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

// 加载分类选项
const loadCategoryOptions = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    categoryOptions.value = await fetchSensitiveCategoryList(Number(userId))
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    loading.value = true
    const params: SensitiveWordListWithFilterDTO = {
      sensitiveWordListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      sensitiveWordListFilterDTO: {
        keyword: searchFormState.value.keyword.trim() || null,
        categoryId: searchFormState.value.categoryId ? Number(searchFormState.value.categoryId) : null
      }
    }
    const res = await fetchSensitiveWordList(params)
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取敏感词列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchFormState.value = { keyword: '', categoryId: '' }; pagination.current = 1; loadData() }
const handleRefresh = () => loadData()
const handleSelectionChange = (selection: SensitiveWordListItem[]) => { selectedRows.value = selection }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.current = 1; loadData() }
const handleCurrentChange = (page: number) => { pagination.current = page; loadData() }

// 新增敏感词
const handleAdd = () => {
  formData.word = ''
  formData.categoryId = null
  addDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }

  try {
    submitting.value = true
    await addSensitiveWord({ userId: Number(userId), word: formData.word.trim(), categoryId: formData.categoryId! })
    ElMessage.success('新增成功')
    addDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '新增失败')
  } finally {
    submitting.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  formData.word = ''
  formData.categoryId = null
  formRef.value?.resetFields()
}

// 删除敏感词
const handleDelete = async (row: SensitiveWordListItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm(`确定要删除敏感词"${row.word}"吗？`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteSensitiveWord(row.sensitiveId, Number(userId))
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个敏感词吗？`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const wordIds = selectedRows.value.map(row => row.sensitiveId)
    await batchDeleteSensitiveWord(wordIds, Number(userId))
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '批量删除失败')
  }
}

// 导入CSV
const handleImport = () => {
  const categoryRows = categoryOptions.value.map(item => 
    `<tr><td style="padding: 6px 12px; border-bottom: 1px solid #ebeef5;">${item.categoryId}</td><td style="padding: 6px 12px; border-bottom: 1px solid #ebeef5;">${item.categoryName}</td></tr>`
  ).join('')
  
  ElMessageBox.confirm(
    `<div style="text-align: left;">
      <p style="margin-bottom: 12px; color: #606266;">第一列：<strong>敏感词</strong>，第二列：<strong>分类ID</strong>，<span style="color: #e6a23c;">不需要标题行</span></p>
      <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
        <thead>
          <tr style="background: #f5f7fa;">
            <th style="padding: 8px 12px; text-align: left; border-bottom: 1px solid #ebeef5;">分类ID</th>
            <th style="padding: 8px 12px; text-align: left; border-bottom: 1px solid #ebeef5;">分类名称</th>
          </tr>
        </thead>
        <tbody>${categoryRows || '<tr><td colspan="2" style="padding: 12px; text-align: center; color: #909399;">暂无分类</td></tr>'}</tbody>
      </table>
    </div>`,
    '导入CSV文件',
    { confirmButtonText: '选择文件', cancelButtonText: '取消', dangerouslyUseHTMLString: true }
  ).then(() => { fileInputRef.value?.click() }).catch(() => {})
}

// 文件选择变化
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }

  try {
    loading.value = true
    const count = await importSensitiveWordCSV(file, Number(userId))
    ElMessage.success(`成功导入 ${count} 条敏感词`)
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    loading.value = false
    target.value = ''
  }
}

onMounted(async () => {
  await loadCategoryOptions()
  await loadData()
})
</script>

<style scoped lang="scss">
.sensitive-word {
  font-weight: 500;
  color: var(--el-color-danger);
}

.tag-category {
  background-color: #f0f9eb;
  color: #67c23a;
  border-color: #c2e7b0;
}

:deep(.art-table) {
  min-height: 680px;
  .el-table { height: 100% !important; }
}
</style>
