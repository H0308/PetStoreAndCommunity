<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Edit, Delete, ChatDotRound, VideoCamera, Plus, Search, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { POST_API, request } from '../../api/config.js'
import PostDetailModal from '../../components/PostDetailModal.vue'

const router = useRouter()

// 帖子列表
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选状态
const activeTab = ref('all') // all, published, draft, pending, rejected
const searchKeyword = ref('')

// 帖子详情弹窗
const showDetailModal = ref(false)
const currentPost = ref(null)

// 帖子状态常量（与后端对应）
const POST_STATUS = {
  DRAFT: 1,       // 草稿
  ON_AUDIT: 2,    // 审核中
  AUDIT_PASS: 3,  // 审核通过（已发布）
  AUDIT_FAIL: 4   // 审核失败
}

// 状态映射：后端数字 -> 前端字符串
const statusMap = {
  [POST_STATUS.DRAFT]: 'draft',
  [POST_STATUS.ON_AUDIT]: 'pending',
  [POST_STATUS.AUDIT_PASS]: 'published',
  [POST_STATUS.AUDIT_FAIL]: 'rejected'
}

// 状态显示配置
const statusConfig = {
  draft: { label: '草稿', type: 'info' },
  pending: { label: '审核中', type: 'warning' },
  published: { label: '已发布', type: 'success' },
  rejected: { label: '审核失败', type: 'danger' }
}

// Tab 配置（使用 ref 使其响应式）
const tabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'published', label: '已发布', count: 0 },
  { key: 'pending', label: '审核中', count: 0 },
  { key: 'draft', label: '草稿箱', count: 0 },
  { key: 'rejected', label: '未通过', count: 0 }
])

// 判断媒体是否为视频
const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.includes(ext))
}

// 去除 HTML 标签，提取纯文本，并截取指定长度
const stripHtml = (html, maxLength = 50) => {
  if (!html) return ''
  // 创建临时元素来解析 HTML
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  const text = tmp.textContent || tmp.innerText || ''
  // 截取指定长度，超出部分显示省略号
  if (text.length > maxLength) {
    return text.slice(0, maxLength) + '...'
  }
  return text
}

// 获取当前用户ID
const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.userId || user.id
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  return null
}

// 获取当前用户信息
const currentUserInfo = ref(null)
const loadCurrentUserInfo = () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    try {
      currentUserInfo.value = JSON.parse(userInfo)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
}

// 将前端状态字符串映射为后端数字状态
const statusToBackend = {
  'draft': POST_STATUS.DRAFT,
  'pending': POST_STATUS.ON_AUDIT,
  'published': POST_STATUS.AUDIT_PASS,
  'rejected': POST_STATUS.AUDIT_FAIL
}

// 加载用户帖子
const loadUserPosts = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    // 构建请求体，适配后端新的 SelfPostsWithFilterDTO 结构
    const requestBody = {
      selfPostDTO: {
        userId: userId,
        currentPage: currentPage.value,
        pageSize: pageSize.value
      },
      selfPostFilterDTO: {}
    }

    // 添加状态筛选："全部"传null，其他传对应状态码
    requestBody.selfPostFilterDTO.status = activeTab.value === 'all' ? null : statusToBackend[activeTab.value]

    // 如果有搜索关键词，添加到筛选条件
    if (searchKeyword.value.trim()) {
      requestBody.selfPostFilterDTO.keyword = searchKeyword.value.trim()
    }

    const response = await request(POST_API.GET_USER_POSTS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    const result = await response.json()

    if (result.code === 0 && result.data) {
      const pageData = result.data
      posts.value = (pageData.totalRecords || []).map(post => ({
        id: post.postId,
        title: post.title,
        content: post.content,
        images: post.mediaUrl ? [post.mediaUrl] : [],
        views: post.viewCount || 0,
        comments: post.commentCount || 0,
        likes: post.likeCount || 0,
        time: post.createTime,
        status: statusMap[post.status] || 'published', // 将后端数字状态映射为字符串
        columnName: post.columnName || ''
      }))
      total.value = pageData.totalCount || 0
    } else {
      ElMessage.error(result.message || '加载帖子失败')
    }
  } catch (error) {
    console.error('加载用户帖子失败:', error)
    ElMessage.error('加载帖子失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 帖子列表直接使用后端返回的数据（搜索和筛选已由后端处理）
const filteredPosts = computed(() => posts.value)

// Tab 到后端状态码的映射
const tabToStatusMap = {
  'all': null,
  'published': POST_STATUS.AUDIT_PASS,  // 3
  'pending': POST_STATUS.ON_AUDIT,      // 2
  'draft': POST_STATUS.DRAFT,           // 1
  'rejected': POST_STATUS.AUDIT_FAIL    // 4
}


// 打开帖子详情
const openPostDetail = (post) => {
  currentPost.value = post
  showDetailModal.value = true
}

// 从详情弹窗同步点赞状态到列表
const handleDetailLike = (post) => {
  const listPost = posts.value.find(p => p.id === post.id)
  if (listPost) {
    listPost.isLiked = post.isLiked
    listPost.likeCount = post.likeCount
    listPost.isRejected = post.isRejected
    listPost.rejectCount = post.rejectCount
  }
}

// 从详情弹窗同步点踩状态到列表
const handleDetailReject = (post) => {
  const listPost = posts.value.find(p => p.id === post.id)
  if (listPost) {
    listPost.isRejected = post.isRejected
    listPost.rejectCount = post.rejectCount
    listPost.isLiked = post.isLiked
    listPost.likeCount = post.likeCount
  }
}

// 发布新帖
const createPost = () => {
  router.push('/post/create')
}

// 编辑帖子
const editPost = (post, event) => {
  event?.stopPropagation()
  router.push(`/post/create?postId=${post.id}`)
}

// 删除帖子
const deletePost = async (id, event) => {
  event?.stopPropagation()
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后无法恢复', '删除确认', { 
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    const response = await request(`${POST_API.DELETE_POST}?userId=${currentUserInfo.value.userId}&postId=${id}`, {
      method: 'POST'
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      posts.value = posts.value.filter(p => p.id !== id)
      total.value--
      ElMessage.success('删除成功')
      // 如果删除后当前页没有数据了且不是第一页，回到上一页
      if (posts.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadUserPosts()
      }
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除帖子失败:', error)
    }
  }
}

// 切换 Tab（重新请求后端数据）
const handleTabChange = (key) => {
  activeTab.value = key
  currentPage.value = 1
  loadUserPosts()
}

// 搜索帖子
const handleSearch = () => {
  currentPage.value = 1
  loadUserPosts()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadUserPosts()
}

// 页码改变
const handlePageChange = (page) => {
  currentPage.value = page
  loadUserPosts()
}

// 每页条数改变
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUserPosts()
}

// 格式化数字
const formatCount = (count) => {
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count || 0
}

// 组件挂载时加载数据
onMounted(() => {
  loadCurrentUserInfo()
  loadUserPosts()
})
</script>

<template>
  <div class="my-posts">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="section-title">我的帖子</h2>
      <el-button type="primary" class="create-btn" @click="createPost">
        <el-icon><Plus /></el-icon>
        发布新帖
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="tabs">
        <div 
          v-for="tab in tabs" 
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="handleTabChange(tab.key)"
        >
          {{ tab.label }}
        </div>
      </div>
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索帖子..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button
          v-if="searchKeyword"
          class="clear-btn"
          @click="clearSearch"
          title="清除搜索"
        >
          <el-icon><Close /></el-icon>
        </button>
        <button class="search-btn" @click="handleSearch">
          <el-icon><Search /></el-icon>
          <span class="search-btn-text">{{ loading ? '搜索中...' : '搜索' }}</span>
        </button>
      </div>
    </div>
    
    <!-- 帖子列表 -->
    <div v-loading="loading" class="post-list">
      <div v-if="!loading && filteredPosts.length === 0" class="empty-state">
        <el-icon :size="64" class="empty-icon"><ChatDotRound /></el-icon>
        <p class="empty-text">{{ searchKeyword ? '没有找到相关帖子' : '暂无帖子' }}</p>
        <el-button v-if="!searchKeyword && activeTab === 'all'" type="primary" @click="createPost">
          发布第一篇帖子
        </el-button>
      </div>
      
      <div 
        v-for="post in filteredPosts" 
        :key="post.id" 
        class="post-card"
        @click="openPostDetail(post)"
      >
        <!-- 帖子封面 -->
        <div class="post-cover" v-if="post.images.length > 0">
          <template v-if="isVideo(post.images[0])">
            <video 
              :src="`${post.images[0]}#t=1`"
              muted
              preload="metadata"
              class="cover-media"
              @loadeddata="$event.target.currentTime = 1"
            />
            <div class="video-badge">
              <el-icon><VideoCamera /></el-icon>
            </div>
          </template>
          <img v-else :src="post.images[0]" class="cover-media" />
        </div>
        <div v-else class="post-cover placeholder">
          <el-icon :size="32"><ChatDotRound /></el-icon>
        </div>
        
        <!-- 帖子内容 -->
        <div class="post-content">
          <div class="post-header">
            <h3 class="post-title">{{ post.title }}</h3>
            <el-tag 
              v-if="post.status && statusConfig[post.status]" 
              size="small" 
              :type="statusConfig[post.status].type"
              effect="plain"
            >
              {{ statusConfig[post.status].label }}
            </el-tag>
          </div>
          <p class="post-excerpt">{{ stripHtml(post.content) }}</p>
          <div class="post-meta">
            <div class="meta-left">
              <span class="meta-item" title="点赞">
                <svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                {{ formatCount(post.likes) }}
              </span>
              <span class="meta-item" title="评论">
                <el-icon><ChatDotRound /></el-icon>
                {{ formatCount(post.comments) }}
              </span>
            </div>
            <span class="meta-item time">{{ post.time }}</span>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="post-actions">
          <button class="action-btn edit-btn" @click="editPost(post, $event)">
            <el-icon><Edit /></el-icon>
            <span>编辑</span>
          </button>
          <button class="action-btn delete-btn" @click="deletePost(post.id, $event)">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 30]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>

    <!-- 帖子详情弹窗 -->
    <PostDetailModal 
      v-model:visible="showDetailModal"
      :post="currentPost"
      :is-logged-in="true"
      :user-info="currentUserInfo"
      @like="handleDetailLike"
      @reject="handleDetailReject"
    />
  </div>
</template>

<style scoped lang="scss">
.my-posts {
  display: flex;
  flex-direction: column;
  min-height: 500px;
  height: 100%;
}

// 页面头部
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-base);
  border-bottom: 1px solid var(--color-border-light);
}

.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

.create-btn {
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  
  &:hover {
    background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary-darker) 100%);
  }
}

// 筛选栏
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-lg);
}

.tabs {
  display: flex;
  gap: var(--spacing-xs);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  
  &:hover {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.08);
  }
  
  &.active {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.12);
    font-weight: var(--font-weight-medium);
  }
  
}

.search-box {
  display: flex;
  align-items: center;
  width: 240px;
  height: 36px;
  background: var(--color-bg-page);
  border: 1px solid var(--color-border-light);
  border-radius: 18px;
  overflow: hidden;
  box-shadow: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--color-border-lighter);
  }

  &:focus-within {
    border-color: var(--color-primary);
    background: var(--color-bg-base);
  }

  .search-input {
    flex: 1;
    min-width: 0;
    height: 100%;
    padding: 0 14px;
    border: none;
    outline: none;
    background: transparent;
    font-size: var(--font-size-sm);
    color: var(--color-text-primary);

    &::placeholder {
      color: var(--color-text-placeholder);
    }
  }

  .clear-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    margin-right: 4px;
    border: none;
    background: transparent;
    color: var(--color-text-tertiary);
    border-radius: 50%;
    cursor: pointer;
    transition: all var(--transition-fast);
    flex-shrink: 0;

    &:hover {
      background: var(--color-border-light);
      color: var(--color-text-secondary);
    }

    .el-icon {
      font-size: 14px;
    }
  }

  .search-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    height: 28px;
    padding: 0 14px;
    margin: 4px;
    border: none;
    background: var(--color-primary);
    color: #fff;
    border-radius: 14px;
    cursor: pointer;
    transition: all var(--transition-fast);
    flex-shrink: 0;
    font-size: var(--font-size-xs);

    &:hover {
      background: var(--color-primary-dark);
    }

    .el-icon {
      font-size: 14px;
    }

    .search-btn-text {
      font-weight: var(--font-weight-medium);
    }
  }
}

// 帖子列表
.post-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  overflow-y: auto;
  min-height: 300px;
}

// 空状态
.empty-state {
  text-align: center;
  padding: var(--spacing-5xl) var(--spacing-xl);
  
  .empty-icon {
    color: var(--color-border-base);
    margin-bottom: var(--spacing-lg);
  }
  
  .empty-text {
    color: var(--color-text-tertiary);
    margin-bottom: var(--spacing-lg);
  }
}

// 帖子卡片
.post-card {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--color-bg-base);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  
  &:hover {
    border-color: var(--color-primary-lighter);
    box-shadow: var(--shadow-md);
    
    .post-actions {
      opacity: 1;
    }
  }
}

.post-cover {
  position: relative;
  width: 140px;
  height: 105px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-bg-base);
  
  .cover-media {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  &.placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--color-border-light);
    color: var(--color-text-placeholder);
  }
  
  .video-badge {
    position: absolute;
    top: var(--spacing-xs);
    right: var(--spacing-xs);
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.6);
    border-radius: var(--radius-round);
    color: #fff;
    font-size: 12px;
  }
}

.post-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.post-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xs);
}

.post-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-excerpt {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin: 0 0 var(--spacing-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  max-width: 100%;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: auto;

  .meta-left {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 14px;
    }

    &.time {
      flex-shrink: 0;
    }
  }

  .like-icon {
    flex-shrink: 0;
  }
}

.post-actions {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition-fast);
  
  .action-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--spacing-xs);
    padding: var(--spacing-sm) var(--spacing-md);
    border: none;
    border-radius: var(--radius-md);
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    cursor: pointer;
    transition: all var(--transition-fast);
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  .edit-btn {
    background: rgba(255, 138, 91, 0.1);
    color: var(--color-primary);
    
    &:hover {
      background: rgba(255, 138, 91, 0.2);
    }
  }
  
  .delete-btn {
    background: rgba(245, 108, 108, 0.1);
    color: var(--color-danger);
    
    &:hover {
      background: rgba(245, 108, 108, 0.2);
    }
  }
}

// 分页
.pagination-wrapper {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  margin-top: auto;
  padding: var(--spacing-lg) 0;
  border-top: 1px solid var(--color-border-light);
  background: var(--color-bg-surface);
  
  :deep(.el-pagination) {
    --el-pagination-hover-color: var(--color-primary);
    gap: var(--spacing-sm);
    
    // 页码按钮
    .el-pager li {
      border-radius: var(--radius-sm);
      min-width: 32px;
      height: 32px;
      line-height: 32px;
      font-weight: var(--font-weight-medium);
      
      &.is-active {
        background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
      }
      
      &:hover:not(.is-active) {
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
    }
    
    // 上一页/下一页按钮
    .btn-prev,
    .btn-next {
      border-radius: var(--radius-sm);
      min-width: 32px;
      height: 32px;
      
      &:hover:not(:disabled) {
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
      
      &:disabled {
        color: var(--color-text-placeholder);
      }
    }
    
    // 总数
    .el-pagination__total {
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
    }
    
    // 每页条数选择器
    .el-pagination__sizes {
      .el-select {
        width: 110px;
        
        .el-select__wrapper {
          border-radius: 8px !important;
          background: #FFFFFF !important;
          box-shadow: 0 0 0 1px var(--color-border-base) inset !important;
          padding: 4px 11px !important;
          height: 32px !important;
          transition: all 0.2s ease !important;
          
          &:hover,
          &.is-focused,
          &.is-hovering {
            box-shadow: 0 0 0 1px var(--color-primary) inset !important;
            
            .el-select__suffix,
            .el-select__caret {
              color: var(--color-primary) !important;
            }
          }
        }
      }
    }
    
    // 跳转输入框
    .el-pagination__jump {
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
      
      .el-input {
        width: 50px;
        
        .el-input__wrapper {
          border-radius: var(--radius-sm);
          box-shadow: 0 0 0 1px var(--color-border-base) inset;
          height: 32px;
          
          &:hover,
          &.is-focus {
            box-shadow: 0 0 0 1px var(--color-primary) inset;
          }
        }
      }
    }
  }
}

// 响应式
@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-box {
    width: 100%;
  }
  
  .post-card {
    flex-direction: column;
  }
  
  .post-cover {
    width: 100%;
    height: 180px;
  }
  
  .post-actions {
    flex-direction: row;
    opacity: 1;
    
    .action-btn {
      flex: 1;
    }
  }
}
</style>
