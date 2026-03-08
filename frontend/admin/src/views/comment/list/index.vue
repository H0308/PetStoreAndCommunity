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
          <h4 class="m-0">评论列表</h4>
          <div class="flex gap-2">
            <ElTag v-if="filterCommentId" type="warning" closable @close="clearCommentIdFilter">
              正在查看评论 #{{ filterCommentId }}
            </ElTag>
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
        <!-- 用户信息列 -->
        <template #userInfo="{ row }">
          <div class="flex items-center gap-3 user-info">
            <ElAvatar :src="row.avatarUrl" :size="36" class="user-avatar">
              <template #default>
                <ArtSvgIcon icon="ri:user-line" class="text-base" />
              </template>
            </ElAvatar>
            <span class="font-medium">{{ row.username }}</span>
          </div>
        </template>

        <!-- 评论类型列 -->
        <template #commentType="{ row }">
          <ElTag :class="row.commentType === 1 ? 'tag-product' : 'tag-post'">
            {{ row.commentType === 1 ? '商品评论' : '帖子评论' }}
          </ElTag>
        </template>

        <!-- 评分列（仅商品评论） -->
        <template #stars="{ row }">
          <ElRate 
            v-if="row.commentType === 1 && row.stars" 
            v-model="row.stars" 
            disabled 
            :max="5"
            size="small"
          />
          <span v-else class="text-g-500">-</span>
        </template>

        <!-- 评论对象列 -->
        <template #objectName="{ row }">
          <div class="object-info">
            <span class="text-xs text-g-500">{{ row.commentType === 1 ? '商品' : '帖子' }}：</span>
            <span class="font-medium overflow-hidden text-ellipsis whitespace-nowrap" :title="row.objectName">
              {{ row.objectName }}
            </span>
          </div>
        </template>

        <!-- 评论内容列 -->
        <template #content="{ row }">
          <div class="comment-content">
            <!-- 父评论引用 -->
            <div v-if="row.parentId" class="parent-comment">
              <span class="parent-label">回复：</span>
              <span class="parent-content">{{ row.parentContent || '原评论已删除' }}</span>
            </div>
            <div v-else class="top-comment-tag">
              <ElTag size="small" type="success">顶级评论</ElTag>
            </div>
            <p class="m-0 overflow-hidden text-ellipsis content-text" :title="row.content">
              {{ row.content }}
            </p>
            <div v-if="row.mediaCount > 0" class="media-preview mt-1">
              <ElTag size="small" type="info">
                <ArtSvgIcon icon="ri:image-line" class="mr-1" />
                {{ row.mediaCount }} 个附件
              </ElTag>
            </div>
          </div>
        </template>

        <!-- 回复数量列 -->
        <template #replyCount="{ row }">
          <ElButton 
            v-if="row.replyCount > 0" 
            type="primary" 
            link 
            @click="handleViewReplyChain(row)"
          >
            {{ row.replyCount }} 条回复
          </ElButton>
          <span v-else class="text-g-500">无回复</span>
        </template>

        <!-- 状态列 -->
        <template #status="{ row }">
          <div class="flex flex-col gap-1">
            <ElTag :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </ElTag>
            <ElTag v-if="row.deleteFlag === 1" type="danger" size="small">
              已删除
            </ElTag>
          </div>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看详情" @click="handleViewDetail(row)" />
            <ArtButtonTable 
              v-if="row.deleteFlag !== 1 && (row.status === CommentStatus.PENDING || row.status === CommentStatus.REJECTED)"
              icon="ri:check-line" 
              iconClass="bg-success/12 text-success"
              tooltip="审核通过"
              @click="handleApprove(row)" 
            />
            <ArtButtonTable 
              v-if="row.deleteFlag !== 1 && (row.status === CommentStatus.PENDING || row.status === CommentStatus.APPROVED)"
              icon="ri:close-line" 
              iconClass="bg-danger/12 text-danger"
              tooltip="拒绝评论"
              @click="handleReject(row)" 
            />
            <ArtButtonTable v-if="row.deleteFlag !== 1" type="delete" :row="row" tooltip="删除评论" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 评论详情弹窗 -->
    <CommentDetailDialog
      v-model="detailDialogVisible"
      :comment="currentComment"
    />

    <!-- 评论回复链弹窗 -->
    <CommentReplyChainDialog
      v-model="replyChainDialogVisible"
      :comment="currentComment"
      :reply-list="replyChainList"
      :loading="replyChainLoading"
      :pagination="replyChainPagination"
      @page-change="handleReplyPageChange"
      @go-to-comment="handleGoToComment"
    />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import CommentDetailDialog from './modules/CommentDetailDialog.vue'
import CommentReplyChainDialog from './modules/CommentReplyChainDialog.vue'
import {
  fetchCommentList,
  fetchCommentReplyList,
  verifyComment,
  deleteComment,
  batchDeleteComments,
  type CommentItem,
  type CommentReplyItem,
  type CommentListWithFilterDTO,
  type ReplyListDTO,
  CommentStatus
} from '@/api/comment'

defineOptions({ name: 'CommentList' })

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<CommentItem[]>([])
const selectedRows = ref<CommentItem[]>([])
const searchBarRef = ref()
const tableRef = ref()

// 评论详情弹窗
const detailDialogVisible = ref(false)
const currentComment = ref<CommentItem | null>(null)

// 评论回复链弹窗
const replyChainDialogVisible = ref(false)
const replyChainList = ref<CommentReplyItem[]>([])
const replyChainLoading = ref(false)
const replyChainPagination = reactive({ current: 1, size: 10, total: 0 })

// 内部筛选状态（不显示在搜索栏）
const filterCommentId = ref<number | null>(null)

// 搜索表单状态
const searchFormState = ref({
  username: '',
  objectType: '',
  objectName: '',
  content: '',
  status: '',
  deleteFlag: '',
  startTime: '',
  endTime: ''
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'username',
    label: '用户名',
    type: 'input',
    props: { placeholder: '请输入用户名' }
  },
  {
    key: 'objectType',
    label: '评论类型',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '商品评论', value: '1' },
        { label: '帖子评论', value: '2' }
      ]
    }
  },
  {
    key: 'objectName',
    label: '评论对象',
    type: 'input',
    props: { placeholder: '商品/帖子名称' }
  },
  {
    key: 'content',
    label: '评论内容',
    type: 'input',
    props: { placeholder: '请输入评论内容关键字' }
  },
  {
    key: 'status',
    label: '状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '审核中', value: '1' },
        { label: '审核成功', value: '2' },
        { label: '审核失败', value: '3' }
      ]
    }
  },
  {
    key: 'deleteFlag',
    label: '删除状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '正常', value: '0' },
        { label: '已删除', value: '1' }
      ]
    }
  },
  {
    key: 'startTime',
    label: '开始时间',
    type: 'datetime',
    props: {
      type: 'datetime',
      placeholder: '选择开始时间',
      valueFormat: 'YYYY-MM-DDTHH:mm:ss'
    }
  },
  {
    key: 'endTime',
    label: '结束时间',
    type: 'datetime',
    props: {
      type: 'datetime',
      placeholder: '选择结束时间',
      valueFormat: 'YYYY-MM-DDTHH:mm:ss'
    }
  }
])

// 分页
const pagination = reactive({ current: 1, size: 10, total: 0 })

// 表格列配置
const columns = ref<ColumnOption<CommentItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 60, label: '序号', fixed: 'left', visible: true },
  { prop: 'userInfo', label: '评论用户', minWidth: 150, useSlot: true, visible: true },
  { prop: 'commentType', label: '评论类型', width: 110, useSlot: true, visible: true },
  { prop: 'stars', label: '评分', width: 140, useSlot: true, visible: true },
  { prop: 'objectName', label: '评论对象', minWidth: 180, useSlot: true, visible: true },
  { prop: 'content', label: '评论内容', minWidth: 250, useSlot: true, visible: true },
  { prop: 'replyCount', label: '回复', width: 100, useSlot: true, visible: true },
  { prop: 'status', label: '状态', width: 100, useSlot: true, visible: true },
  { prop: 'createTime', label: '发布时间', width: 170, showOverflowTooltip: true, visible: true },
  { prop: 'operation', label: '操作', width: 180, useSlot: true, fixed: 'right', visible: true }
])

// 过滤后的可见列
const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    [CommentStatus.PENDING]: '审核中',
    [CommentStatus.APPROVED]: '审核成功',
    [CommentStatus.REJECTED]: '审核失败'
  }
  return map[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    [CommentStatus.PENDING]: 'tag-pending',
    [CommentStatus.APPROVED]: 'tag-approved',
    [CommentStatus.REJECTED]: 'tag-rejected'
  }
  return map[status] || ''
}

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  
  try {
    loading.value = true
    const params: CommentListWithFilterDTO = {
      commentListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      commentListFilterDTO: {
        commentId: filterCommentId.value || null,
        username: searchFormState.value.username.trim() || null,
        objectType: searchFormState.value.objectType ? Number(searchFormState.value.objectType) : null,
        objectName: searchFormState.value.objectName.trim() || null,
        content: searchFormState.value.content.trim() || null,
        status: searchFormState.value.status !== '' ? Number(searchFormState.value.status) : null,
        deleteFlag: searchFormState.value.deleteFlag !== '' ? Number(searchFormState.value.deleteFlag) : null,
        startTime: searchFormState.value.startTime || null,
        endTime: searchFormState.value.endTime || null
      }
    }
    const res = await fetchCommentList(params)
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { filterCommentId.value = null; pagination.current = 1; loadData() }
const handleReset = () => {
  filterCommentId.value = null
  searchFormState.value = { username: '', objectType: '', objectName: '', content: '', status: '', deleteFlag: '', startTime: '', endTime: '' }
  pagination.current = 1
  loadData()
}
const handleRefresh = () => loadData()
const handleSelectionChange = (selection: CommentItem[]) => { selectedRows.value = selection }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.current = 1; loadData() }
const handleCurrentChange = (page: number) => { pagination.current = page; loadData() }

// 查看评论详情
const handleViewDetail = (row: CommentItem) => {
  currentComment.value = row
  detailDialogVisible.value = true
}

// 查看回复链
const handleViewReplyChain = async (row: CommentItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  
  currentComment.value = row
  replyChainPagination.current = 1
  replyChainDialogVisible.value = true
  await loadReplyList(row.commentId)
}

// 加载回复列表
const loadReplyList = async (parentId: number) => {
  const userId = userStore.info?.userId
  if (!userId) return
  
  replyChainLoading.value = true
  try {
    const params: ReplyListDTO = {
      userId: Number(userId),
      parentId,
      currentPage: replyChainPagination.current,
      pageSize: replyChainPagination.size
    }
    const res = await fetchCommentReplyList(params)
    replyChainList.value = res.totalRecords || []
    replyChainPagination.total = res.totalCount || 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取回复列表失败')
    replyChainList.value = []
  } finally {
    replyChainLoading.value = false
  }
}

// 回复列表分页变化
const handleReplyPageChange = (page: number) => {
  if (!currentComment.value) return
  replyChainPagination.current = page
  loadReplyList(currentComment.value.commentId)
}

// 跳转到指定评论
const handleGoToComment = (commentId: number) => {
  replyChainDialogVisible.value = false
  filterCommentId.value = commentId
  pagination.current = 1
  loadData()
}

// 清除评论ID筛选
const clearCommentIdFilter = () => {
  filterCommentId.value = null
  pagination.current = 1
  loadData()
}

// 审核通过
const handleApprove = async (row: CommentItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm('确定要审核通过该评论吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' })
    await verifyComment({
      userId: Number(userId),
      commentId: row.commentId,
      opFlag: 1  // 1-审核通过
    })
    ElMessage.success('审核通过')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

// 审核拒绝
const handleReject = async (row: CommentItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm('确定要拒绝该评论吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await verifyComment({
      userId: Number(userId),
      commentId: row.commentId,
      opFlag: 0  // 0-不予通过
    })
    ElMessage.success('已拒绝')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

// 删除评论
const handleDelete = async (row: CommentItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm('确定要删除该评论吗？此操作不可恢复！', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' })
    await deleteComment({
      userId: Number(userId),
      userIdComment: row.userId,
      commentId: row.commentId,
      commentType: row.commentType
    })
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
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条评论吗？此操作不可恢复！`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' })
    // 构建 Map<评论ID, 评论类型>
    const commentIdsMap: Record<number, number> = {}
    selectedRows.value.forEach(row => {
      commentIdsMap[row.commentId] = row.commentType
    })
    await batchDeleteComments(commentIdsMap, Number(userId))
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
.user-info {
  .user-avatar { flex-shrink: 0; }
}

.object-info {
  display: flex;
  flex-direction: column;
  max-width: 180px;
}

.comment-content {
  max-width: 250px;
  
  .parent-comment {
    background-color: #f5f5f5;
    padding: 4px 8px;
    border-radius: 4px;
    margin-bottom: 6px;
    font-size: 12px;
    color: #666;
    border-left: 2px solid #ddd;
    
    .parent-label {
      color: #999;
    }
    
    .parent-content {
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
  
  .top-comment-tag {
    margin-bottom: 6px;
  }
  
  .content-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.media-preview {
  display: flex;
  align-items: center;
}

.tag-product { background-color: #e6f7ff; color: #1890ff; border-color: #91d5ff; }
.tag-post { background-color: #f6ffed; color: #52c41a; border-color: #b7eb8f; }
.tag-pending { background-color: #fff7e6; color: #fa8c16; border-color: #ffd591; }
.tag-approved { background-color: #f6ffed; color: #52c41a; border-color: #b7eb8f; }
.tag-rejected { background-color: #fff1f0; color: #f5222d; border-color: #ffa39e; }

:deep(.art-table) {
  min-height: 680px;
  .el-table { height: 100% !important; }
}
</style>
