<template>
  <div class="flex flex-col gap-4 pb-5">
    <!-- 搜索区域 -->
    <ArtSearchBar
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
          <h4 class="m-0">帖子列表</h4>
          <div class="flex gap-2">
            <ElTag v-if="loading" type="warning">加载中...</ElTag>
            <ElTag v-else type="success">{{ tableData.length }} 条数据</ElTag>
          </div>
        </div>
      </template>

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
        <template #title="{ row }">
          <div class="flex items-center gap-2">
            <ArtSvgIcon icon="ri:sticky-note-line" class="text-primary" :size="16" />
            <span class="font-medium post-title" :title="row.title">{{ row.title }}</span>
          </div>
        </template>

        <template #username="{ row }">
          <span class="text-g-700">{{ row.username }}</span>
        </template>

        <template #columnName="{ row }">
          <div class="flex items-center gap-1">
            <ElTag class="tag-column"><ArtSvgIcon icon="ri:file-copy-line" :size="12" /></ElTag>
            <span>{{ row.columnName }}</span>
          </div>
        </template>

        <template #topicNames="{ row }">
          <div class="flex flex-wrap gap-1">
            <ElTag v-for="(name, id) in row.topicNames" :key="id" class="tag-topic" size="small">#{{ name }}</ElTag>
            <span v-if="!row.topicNames || Object.keys(row.topicNames).length === 0" class="text-g-500">无话题</span>
          </div>
        </template>

        <template #status="{ row }">
          <ElTag :class="getStatusClass(row.status)">{{ getStatusText(row.status) }}</ElTag>
        </template>

        <template #createTime="{ row }">
          <span class="text-g-700">{{ row.createTime }}</span>
        </template>

        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看详情" @click="handleView(row)" />
            <ArtButtonTable type="delete" :row="row" tooltip="删除帖子" @click="handleDelete(row)" />
            <ArtButtonTable v-if="row.status === 2" icon="ri:checkbox-circle-line" iconClass="bg-success/12 text-success" tooltip="审核通过" @click="handleAudit(row, 3)" />
            <ArtButtonTable v-if="row.status === 2 || row.status === 3" icon="ri:close-circle-line" iconClass="bg-danger/12 text-danger" tooltip="拒绝帖子" @click="handleAudit(row, 4)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 帖子详情对话框 -->
    <ElDialog v-model="detailDialogVisible" title="帖子详情" width="900px" :close-on-click-modal="true" destroy-on-close class="post-detail-dialog">
      <div v-if="detailLoading" class="detail-loading">
        <ElSkeleton :rows="8" animated />
      </div>
      <div v-else-if="postDetail" class="post-detail">
        <!-- 基本信息区 -->
        <div class="detail-section">
          <div class="section-title"><ArtSvgIcon icon="ri:file-text-line" class="mr-2" />基本信息</div>
          <ElDescriptions :column="2" border size="small">
            <ElDescriptionsItem label="帖子ID"><ElTag type="info" size="small">{{ postDetail.postId }}</ElTag></ElDescriptionsItem>
            <ElDescriptionsItem label="审核状态"><ElTag :class="getStatusClass(postDetail.status)" size="small">{{ getStatusText(postDetail.status) }}</ElTag></ElDescriptionsItem>
            <ElDescriptionsItem label="所属栏目">{{ postDetail.columnName || '未分类' }}</ElDescriptionsItem>
            <ElDescriptionsItem label="关联话题">
              <div class="topic-tags">
                <ElTag v-for="(name, id) in postDetail.topicNames" :key="id" class="tag-topic" size="small">#{{ name }}</ElTag>
                <span v-if="!postDetail.topicNames || Object.keys(postDetail.topicNames).length === 0" class="text-g-500">无话题</span>
              </div>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="创建时间">{{ postDetail.createTime }}</ElDescriptionsItem>
            <ElDescriptionsItem label="更新时间">{{ postDetail.updateTime }}</ElDescriptionsItem>
          </ElDescriptions>
        </div>

        <!-- 作者信息区 -->
        <div class="detail-section">
          <div class="section-title"><ArtSvgIcon icon="ri:user-line" class="mr-2" />作者信息</div>
          <div class="author-info-card">
            <ElAvatar :size="56" :src="postDetail.avatarUrl">{{ postDetail.username?.charAt(0) || '匿' }}</ElAvatar>
            <div class="author-detail">
              <div class="author-name-row">
                <span class="author-name">{{ postDetail.username || '匿名用户' }}</span>
                <ElTag v-if="postDetail.userStatus === 1" type="info" size="small">已销户</ElTag>
                <ElTag v-if="postDetail.banFlag === 1" type="danger" size="small">已禁言</ElTag>
              </div>
              <div class="author-info-row">
                <span class="info-item"><ArtSvgIcon icon="ri:user-3-line" :size="14" />用户ID: {{ postDetail.userId }}</span>
                <span class="info-item">
                  <ArtSvgIcon icon="ri:shield-user-line" :size="14" />
                  账号状态: 
                  <ElTag :type="postDetail.userStatus === 0 ? 'success' : 'info'" size="small">{{ postDetail.userStatus === 0 ? '正常' : '已销户' }}</ElTag>
                </span>
                <span class="info-item">
                  <ArtSvgIcon icon="ri:chat-off-line" :size="14" />
                  禁言状态: 
                  <ElTag :type="postDetail.banFlag === 0 ? 'success' : 'danger'" size="small">{{ postDetail.banFlag === 0 ? '正常' : '已禁言' }}</ElTag>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据统计区 -->
        <div class="detail-section">
          <div class="section-title"><ArtSvgIcon icon="ri:bar-chart-line" class="mr-2" />数据统计</div>
          <div class="stats-grid">
            <div class="stat-item"><div class="stat-icon like"><ArtSvgIcon icon="ri:heart-line" :size="18" /></div><div class="stat-info"><div class="stat-value">{{ postDetail.likeCount || 0 }}</div><div class="stat-label">点赞</div></div></div>
            <div class="stat-item"><div class="stat-icon dislike"><ArtSvgIcon icon="ri:thumb-down-line" :size="18" /></div><div class="stat-info"><div class="stat-value">{{ postDetail.rejectCount || 0 }}</div><div class="stat-label">点踩</div></div></div>
            <div class="stat-item"><div class="stat-icon favorite"><ArtSvgIcon icon="ri:star-line" :size="18" /></div><div class="stat-info"><div class="stat-value">{{ postDetail.favorCount || 0 }}</div><div class="stat-label">收藏</div></div></div>
          </div>
        </div>

        <!-- 媒体内容区 -->
        <div class="detail-section" v-if="mediaList.length > 0">
          <div class="section-title"><ArtSvgIcon icon="ri:image-line" class="mr-2" />媒体内容</div>
          <div class="media-gallery">
            <div v-for="(media, index) in mediaList" :key="index" class="media-item" @click="previewMedia(media)">
              <video v-if="isVideo(media)" :src="media" class="media-video" muted preload="metadata" />
              <ElImage v-else :src="media" fit="cover" class="media-image" :preview-src-list="imageList" :initial-index="getImageIndex(index)" preview-teleported />
              <div v-if="isVideo(media)" class="video-badge"><ArtSvgIcon icon="ri:play-circle-line" :size="24" /></div>
            </div>
          </div>
        </div>

        <!-- 帖子内容区 -->
        <div class="detail-section">
          <div class="section-title"><ArtSvgIcon icon="ri:article-line" class="mr-2" />帖子内容</div>
          <h3 class="post-title-text">{{ postDetail.title }}</h3>
          <div class="post-content" v-html="renderedContent"></div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="detailDialogVisible = false">关闭</ElButton>
          <ElButton v-if="postDetail?.status === 2" type="primary" plain @click="handleAuditFromDetail(3)"><ArtSvgIcon icon="ri:checkbox-circle-line" class="mr-1" />审核通过</ElButton>
          <ElButton v-if="postDetail?.status === 2 || postDetail?.status === 3" type="warning" plain @click="handleAuditFromDetail(4)"><ArtSvgIcon icon="ri:close-circle-line" class="mr-1" />审核拒绝</ElButton>
          <ElButton type="danger" plain @click="handleDeleteFromDetail"><ArtSvgIcon icon="ri:delete-bin-line" class="mr-1" />删除帖子</ElButton>
        </div>
      </template>
    </ElDialog>

    <!-- 视频预览对话框 -->
    <ElDialog v-model="videoDialogVisible" width="auto" destroy-on-close :close-on-click-modal="true" :show-close="true" class="video-preview-dialog" :modal="true" align-center>
      <video v-if="currentVideoUrl" :src="currentVideoUrl" controls autoplay class="preview-video" />
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { useRoute } from 'vue-router'
import { fetchPostList, fetchPostDetail, deletePost, batchDeletePost, verifyPost, type PostListItem, type PostListWithFilterDTO, type PostDetailVO } from '@/api/post'
import { fetchColumnList, type ColumnListItem } from '@/api/column'
import { fetchTopicList, type TopicListItem } from '@/api/topic'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import { marked } from 'marked'

defineOptions({ name: 'PostManage' })

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<PostListItem[]>([])
const selectedRows = ref<PostListItem[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const postDetail = ref<PostDetailVO | null>(null)
const videoDialogVisible = ref(false)
const currentVideoUrl = ref('')

// 栏目和话题选项
const columnOptions = ref<{ label: string; value: string }[]>([])
const topicOptions = ref<{ label: string; value: string }[]>([])

const getInitialColumnId = () => route.query.columnId ? String(route.query.columnId) : ''
const getInitialTopicId = () => route.query.topicId ? String(route.query.topicId) : ''

const searchFormState = ref({
  title: '',
  columnId: getInitialColumnId(),
  topicId: getInitialTopicId(),
  status: '',
  startTime: '',
  endTime: ''
})

watch(() => route.query, (newQuery, oldQuery) => {
  if (JSON.stringify(newQuery) === JSON.stringify(oldQuery)) return
  searchFormState.value.columnId = newQuery.columnId ? String(newQuery.columnId) : ''
  searchFormState.value.topicId = newQuery.topicId ? String(newQuery.topicId) : ''
  pagination.current = 1
  loadData()
})

const searchItems = computed(() => [
  { key: 'title', label: '帖子标题', type: 'input', props: { placeholder: '请输入帖子标题' } },
  { key: 'columnId', label: '栏目', type: 'select', props: { placeholder: '全部栏目', clearable: true, options: columnOptions.value } },
  { key: 'topicId', label: '话题', type: 'select', props: { placeholder: '全部话题', clearable: true, options: topicOptions.value } },
  { key: 'status', label: '审核状态', type: 'select', props: { placeholder: '全部状态', clearable: true, options: [{ label: '草稿', value: '1' }, { label: '审核中', value: '2' }, { label: '审核成功', value: '3' }, { label: '审核失败', value: '4' }] } },
  { key: 'startTime', label: '开始时间', type: 'datetime', props: { type: 'datetime', placeholder: '选择开始时间', valueFormat: 'YYYY-MM-DDTHH:mm:ss', clearable: true } },
  { key: 'endTime', label: '结束时间', type: 'datetime', props: { type: 'datetime', placeholder: '选择结束时间', valueFormat: 'YYYY-MM-DDTHH:mm:ss', clearable: true } }
])

const pagination = reactive({ current: 1, size: 10, total: 0 })

const columns = ref<ColumnOption<PostListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 60, label: '序号', fixed: 'left', visible: true },
  { prop: 'title', label: '帖子标题', minWidth: 200, useSlot: true, visible: true },
  { prop: 'username', label: '作者', width: 120, useSlot: true, visible: true },
  { prop: 'columnName', label: '栏目', width: 140, useSlot: true, visible: true },
  { prop: 'topicNames', label: '话题', minWidth: 180, useSlot: true, visible: true },
  { prop: 'status', label: '状态', width: 100, useSlot: true, visible: true },
  { prop: 'createTime', label: '创建时间', width: 170, useSlot: true, visible: true },
  { prop: 'operation', label: '操作', width: 180, useSlot: true, fixed: 'right', visible: true }
])

const visibleColumns = computed(() => columns.value.filter(col => col.visible !== false))

// 媒体列表
const mediaList = computed(() => {
  if (!postDetail.value?.mediaUrls) return []
  return postDetail.value.mediaUrls
})

const imageList = computed(() => mediaList.value.filter(url => !isVideo(url)))

const renderedContent = computed(() => {
  if (!postDetail.value?.content) return '<p class="empty-content">暂无内容</p>'
  return marked(postDetail.value.content) as string
})

const isVideo = (url: string) => {
  if (!url) return false
  return ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv'].some(ext => url.toLowerCase().includes(ext))
}

const getImageIndex = (mediaIndex: number) => {
  const url = mediaList.value[mediaIndex]
  return imageList.value.indexOf(url)
}

const getStatusClass = (status: number) => ({ 1: 'tag-draft', 2: 'tag-pending', 3: 'tag-approved', 4: 'tag-rejected' }[status] || 'tag-draft')
const getStatusText = (status: number) => ({ 1: '草稿', 2: '审核中', 3: '审核成功', 4: '审核失败' }[status] || '未知')

const loadColumnOptions = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    const res = await fetchColumnList({ columnListDTO: { userId: Number(userId), currentPage: 1, pageSize: 100 }, columnListFilterDTO: {} })
    columnOptions.value = (res.totalRecords || []).map((item: ColumnListItem) => ({ label: item.columnName, value: String(item.columnId) }))
  } catch (error) { console.error('获取栏目列表失败:', error) }
}

const loadTopicOptions = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    const res = await fetchTopicList({ topicListDTO: { userId: Number(userId), currentPage: 1, pageSize: 100 }, topicListFilterDTO: {} })
    topicOptions.value = (res.totalRecords || []).map((item: TopicListItem) => ({ label: '#' + item.topicName, value: String(item.topicId) }))
  } catch (error) { console.error('获取话题列表失败:', error) }
}

const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) return
  try {
    loading.value = true
    const params: PostListWithFilterDTO = {
      postListDTO: { userId: Number(userId), currentPage: pagination.current, pageSize: pagination.size },
      postListFilterDTO: {
        title: searchFormState.value.title.trim() || null,
        columnId: searchFormState.value.columnId ? Number(searchFormState.value.columnId) : null,
        topicId: searchFormState.value.topicId ? Number(searchFormState.value.topicId) : null,
        status: searchFormState.value.status !== '' ? Number(searchFormState.value.status) : null,
        startTime: searchFormState.value.startTime || null,
        endTime: searchFormState.value.endTime || null
      }
    }
    const res = await fetchPostList(params)
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error) { console.error('获取帖子列表失败:', error) }
  finally { loading.value = false }
}

const loadPostDetail = async (postId: number, rowUserId: number) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    detailLoading.value = true
    postDetail.value = await fetchPostDetail(postId, Number(userId))
  } catch (error: any) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error(error.message || '获取帖子详情失败')
  } finally { detailLoading.value = false }
}

const handleSearch = () => { pagination.current = 1; loadData() }
const handleReset = () => { searchFormState.value = { title: '', columnId: '', topicId: '', status: '', startTime: '', endTime: '' }; pagination.current = 1; loadData() }
const handleRefresh = () => loadData()
const handleSelectionChange = (selection: PostListItem[]) => { selectedRows.value = selection }
const handleSizeChange = (size: number) => { pagination.size = size; pagination.current = 1; loadData() }
const handleCurrentChange = (page: number) => { pagination.current = page; loadData() }

const handleView = (row: PostListItem) => {
  detailDialogVisible.value = true
  loadPostDetail(row.postId, row.userId)
}

const previewMedia = (url: string) => {
  if (isVideo(url)) {
    currentVideoUrl.value = url
    videoDialogVisible.value = true
  }
}

const handleDelete = async (row: PostListItem) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm(`确定要删除帖子"${row.title}"吗？删除后无法恢复！`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deletePost(row.postId, row.userId, Number(userId))
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) { if (error !== 'cancel') ElMessage.error(error.message || '删除失败') }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个帖子吗？删除后无法恢复！`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const postIds = selectedRows.value.map(row => row.postId)
    await batchDeletePost({ userId: Number(userId), postIds })
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error: any) { if (error !== 'cancel') ElMessage.error(error.message || '批量删除失败') }
}

const handleAudit = async (row: PostListItem, status: number) => {
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  const actionText = status === 3 ? '通过' : '拒绝'
  const opFlag = status === 3 ? 1 : 0 // 1-审核通过，0-不予通过
  try {
    await ElMessageBox.confirm(`确定要${actionText}帖子"${row.title}"吗？`, '审核确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: status === 3 ? 'success' : 'warning' })
    await verifyPost({ postId: row.postId, userId: Number(userId), opFlag })
    ElMessage.success(`审核${actionText}成功`)
    loadData()
  } catch (error: any) { if (error !== 'cancel') ElMessage.error(error.message || `审核${actionText}失败`) }
}

const handleAuditFromDetail = async (status: number) => {
  if (!postDetail.value) return
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  const actionText = status === 3 ? '通过' : '拒绝'
  const opFlag = status === 3 ? 1 : 0 // 1-审核通过，0-不予通过
  try {
    await ElMessageBox.confirm(`确定要${actionText}该帖子吗？`, '审核确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: status === 3 ? 'success' : 'warning' })
    await verifyPost({ postId: postDetail.value.postId, userId: Number(userId), opFlag })
    ElMessage.success(`审核${actionText}成功`)
    detailDialogVisible.value = false
    loadData()
  } catch (error: any) { if (error !== 'cancel') ElMessage.error(error.message || `审核${actionText}失败`) }
}

const handleDeleteFromDetail = async () => {
  if (!postDetail.value) return
  const userId = userStore.info?.userId
  if (!userId) { ElMessage.error('用户信息不存在'); return }
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？删除后无法恢复！', '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deletePost(postDetail.value.postId, postDetail.value.userId, Number(userId))
    ElMessage.success('删除成功')
    detailDialogVisible.value = false
    loadData()
  } catch (error: any) { if (error !== 'cancel') ElMessage.error(error.message || '删除失败') }
}

onMounted(async () => { await Promise.all([loadColumnOptions(), loadTopicOptions()]); await loadData() })

onActivated(() => {
  const queryColumnId = route.query.columnId ? String(route.query.columnId) : ''
  const queryTopicId = route.query.topicId ? String(route.query.topicId) : ''
  if (searchFormState.value.columnId !== queryColumnId || searchFormState.value.topicId !== queryTopicId) {
    searchFormState.value.columnId = queryColumnId
    searchFormState.value.topicId = queryTopicId
    pagination.current = 1
    loadData()
  }
})
</script>

<style scoped lang="scss">
.tag-topic { background-color: #e6f7ff; color: #1890ff; border-color: #91d5ff; font-weight: 500; }
.tag-column { background-color: #e6f7ff; color: #1890ff; border-color: #91d5ff; font-weight: 500; }
.tag-draft { background-color: #e2e3e5; color: #383d41; border-color: #d6d8db; }
.tag-pending { background-color: #fff7e6; color: #d46b08; border-color: #ffd591; }
.tag-approved { background-color: #f6ffed; color: #389e0d; border-color: #b7eb8f; }
.tag-rejected { background-color: #fff1f0; color: #cf1322; border-color: #ffa39e; }
.post-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 180px; display: inline-block; }
.font-medium { font-weight: 500; }

:deep(.art-table) { min-height: 680px; .el-table { height: 100% !important; } }

// 详情对话框样式
.detail-loading { padding: 20px; }

.post-detail {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 8px;

  .detail-section {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child { border-bottom: none; margin-bottom: 0; }
  }

  .section-title {
    display: flex;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 12px;
  }

  .topic-tags { display: flex; flex-wrap: wrap; gap: 4px; }

  .author-info-card {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 12px;
    background: var(--el-fill-color-light);
    border-radius: 8px;

    .author-detail {
      flex: 1;

      .author-name-row {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;

        .author-name {
          font-size: 16px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
      }

      .author-info-row {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;

        .info-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 12px;
      background: var(--el-fill-color-light);
      border-radius: 8px;

      .stat-icon {
        width: 36px;
        height: 36px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 8px;
        color: #fff;

        &.like { background: linear-gradient(135deg, #ff6b6b, #ee5a5a); }
        &.dislike { background: linear-gradient(135deg, #868e96, #6c757d); }
        &.favorite { background: linear-gradient(135deg, #ffd43b, #fab005); }
        &.comment { background: linear-gradient(135deg, #4dabf7, #339af0); }
      }

      .stat-info {
        .stat-value { font-size: 18px; font-weight: 600; color: var(--el-text-color-primary); }
        .stat-label { font-size: 12px; color: var(--el-text-color-secondary); }
      }
    }
  }

  .media-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 10px;

    .media-item {
      position: relative;
      aspect-ratio: 1;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s;

      &:hover { transform: scale(1.02); }

      .media-image, .media-video { width: 100%; height: 100%; object-fit: cover; }

      .video-badge {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 40px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        color: #fff;
      }
    }
  }

  .post-title-text {
    font-size: 18px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin: 0 0 12px;
    line-height: 1.4;
  }

  .post-content {
    font-size: 14px;
    line-height: 1.8;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    padding: 12px 16px;
    border-radius: 8px;
    max-height: 300px;
    overflow-y: auto;

    :deep(p) { margin: 0 0 8px; &:last-child { margin-bottom: 0; } }
    :deep(img) { max-width: 100%; border-radius: 4px; }
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) { margin: 16px 0 8px; font-weight: 600; }
    :deep(h1) { font-size: 1.5em; }
    :deep(h2) { font-size: 1.3em; }
    :deep(h3) { font-size: 1.1em; }
    :deep(ul), :deep(ol) { padding-left: 20px; margin: 8px 0; }
    :deep(li) { margin: 4px 0; }
    :deep(blockquote) { margin: 8px 0; padding: 8px 12px; border-left: 4px solid var(--el-border-color); background: var(--el-fill-color); }
    :deep(code) { background: var(--el-fill-color-darker); padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 0.9em; }
    :deep(pre) { background: var(--el-fill-color-darker); padding: 12px; border-radius: 6px; overflow-x: auto; margin: 8px 0; }
    :deep(pre code) { background: none; padding: 0; }
    :deep(a) { color: var(--el-color-primary); text-decoration: none; &:hover { text-decoration: underline; } }
    :deep(table) { width: 100%; border-collapse: collapse; margin: 8px 0; }
    :deep(th), :deep(td) { border: 1px solid var(--el-border-color); padding: 8px; text-align: left; }
    :deep(th) { background: var(--el-fill-color); }
    :deep(hr) { border: none; border-top: 1px solid var(--el-border-color); margin: 16px 0; }
    .empty-content { color: var(--el-text-color-placeholder); font-style: italic; }
  }
}

.dialog-footer { display: flex; justify-content: flex-end; gap: 8px; }

// 视频预览对话框样式
:deep(.video-preview-dialog) {
  .el-dialog {
    background: transparent;
    box-shadow: none;
    margin: 0;
    padding: 0;
  }

  .el-dialog__header {
    display: none;
  }

  .el-dialog__body {
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.preview-video {
  max-width: 90vw;
  max-height: 85vh;
  border-radius: 8px;
  background: #000;
}
</style>
