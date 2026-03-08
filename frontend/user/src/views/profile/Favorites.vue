<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ChatDotRound, VideoCamera, Search, Close, Star, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { POST_API, request } from '../../api/config.js'
import PostDetailModal from '../../components/PostDetailModal.vue'

const router = useRouter()

const favorites = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchKeyword = ref('')

const showDetailModal = ref(false)
const currentPost = ref(null)

const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv']
  return videoExtensions.some(ext => url.toLowerCase().includes(ext))
}

const stripHtml = (html, maxLength = 50) => {
  if (!html) return ''
  const tmp = document.createElement('div')
  tmp.innerHTML = html
  const text = tmp.textContent || tmp.innerText || ''
  // 截取指定长度，超出部分显示省略号
  if (text.length > maxLength) {
    return text.slice(0, maxLength) + '...'
  }
  return text
}

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

const loadFavorites = async () => {
  if (!currentUserInfo.value?.userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const requestBody = {
      userId: currentUserInfo.value.userId,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    }

    // 如果有搜索关键词，添加到请求体
    if (searchKeyword.value.trim()) {
      requestBody.keyword = searchKeyword.value.trim()
    }

    const response = await request(POST_API.GET_FAVORITES, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      const data = result.data || {}
      favorites.value = (data.totalRecords || [])
        .map(item => ({
          id: item.postId,
          title: item.title,
          content: item.content,
          author: {
            id: item.userId,
            nickname: item.username,
            avatar: item.avatar
          },
          images: item.mediaUrl ? [item.mediaUrl] : [],
          likes: item.likeCount || 0,
          comments: item.commentCount || 0,
          collectTime: item.createTime,
          isDeleted: !item.mediaUrl || item.mediaUrl === '' // 媒体文件URL为空字符串表示帖子已被删除
        }))
      total.value = data.totalCount || 0
    } else {
      ElMessage.error(result.message || '加载收藏失败')
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

// 收藏列表直接使用后端返回的数据（搜索已由后端处理）
const filteredFavorites = computed(() => favorites.value)

const openPostDetail = (item) => {
  // 如果帖子已被删除，不打开详情
  if (item.isDeleted) {
    ElMessage.warning('该帖子已被删除')
    return
  }
  currentPost.value = {
    id: item.id,
    title: item.title,
    content: item.content,
    cover: item.images[0],
    author: item.author,
    likeCount: item.likes,
    rejectCount: 0,
    collectCount: item.collectCount,
    isLiked: false,
    isRejected: false,
    isCollected: true
  }
  showDetailModal.value = true
}

const handleDetailLike = (post) => {
  const listItem = favorites.value.find(f => f.id === post.id)
  if (listItem) {
    listItem.isLiked = post.isLiked
    listItem.likes = post.likeCount
  }
}

const handleDetailReject = (post) => {
  const listItem = favorites.value.find(f => f.id === post.id)
  if (listItem) {
    listItem.isRejected = post.isRejected
  }
}

const handleDetailCollect = (post) => {
  if (!post.isCollected) {
    favorites.value = favorites.value.filter(f => f.id !== post.id)
    total.value--
  }
}

const removeFavorite = async (item, event) => {
  event.stopPropagation()
  try {
    await ElMessageBox.confirm(`确定要取消收藏「${item.title}」吗？`, '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request(POST_API.FAVOR_POST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: currentUserInfo.value.userId,
        postId: item.id,
        opFlag: 1  // 1-取消收藏
      })
    })
    
    const result = await response.json()
    
    if (result.code === 0) {
      favorites.value = favorites.value.filter(f => f.id !== item.id)
      total.value--
      ElMessage.success('已取消收藏')
      // 如果删除后当前页没有数据了且不是第一页，回到上一页
      if (favorites.value.length === 0 && currentPage.value > 1) {
        currentPage.value--
        loadFavorites()
      }
    } else {
      ElMessage.error(result.message || '取消收藏失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败:', error)
    }
  }
}

const formatCount = (count) => {
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count || 0
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadFavorites()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadFavorites()
}

const goToForum = () => {
  router.push('/forum')
}

// 搜索收藏
const handleSearch = () => {
  currentPage.value = 1
  loadFavorites()
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadFavorites()
}

onMounted(() => {
  loadCurrentUserInfo()
  loadFavorites()
})
</script>

<template>
  <div class="favorites">
    <div class="page-header">
      <h2 class="section-title">
        收藏帖子
        <span class="title-count" v-if="total > 0">{{ total }}</span>
      </h2>
    </div>

    <div class="filter-bar">
      <div class="filter-hint">
        <span class="hint-text">按收藏时间排序</span>
      </div>
      <div class="search-box">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索收藏..."
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

    <div v-loading="loading" class="post-list">
      <div v-if="!loading && filteredFavorites.length === 0" class="empty-state">
        <el-icon :size="64" class="empty-icon"><Star /></el-icon>
        <p class="empty-text">{{ searchKeyword ? '没有找到相关收藏' : '暂无收藏' }}</p>
        <el-button v-if="!searchKeyword" type="primary" class="empty-btn" @click="goToForum">去发现好内容</el-button>
        <el-button v-else type="primary" class="empty-btn" @click="clearSearch">返回全部收藏</el-button>
      </div>
      
      <div v-for="item in filteredFavorites" :key="item.id" class="post-card" @click="openPostDetail(item)">
        <div class="post-cover" v-if="item.images.length > 0">
          <template v-if="isVideo(item.images[0])">
            <video :src="`${item.images[0]}#t=1`" muted preload="metadata" class="cover-media" />
            <div class="video-badge"><el-icon><VideoCamera /></el-icon></div>
          </template>
          <img v-else :src="item.images[0]" class="cover-media" />
        </div>
        <div v-else class="post-cover placeholder">
          <el-icon :size="32"><ChatDotRound /></el-icon>
        </div>

        <div class="post-content">
          <h3 class="post-title">{{ item.title }}</h3>
          <p class="post-excerpt">{{ stripHtml(item.content) }}</p>
          <div class="post-author">
            <img :src="item.author.avatar" class="author-avatar" />
            <span class="author-name">{{ item.author.nickname }}</span>
          </div>
          <div class="post-meta">
            <span class="meta-item"><svg class="like-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>{{ formatCount(item.likes) }}</span>
            <span class="meta-item"><el-icon><ChatDotRound /></el-icon>{{ formatCount(item.comments) }}</span>
            <span class="meta-item time">收藏于 {{ item.collectTime }}</span>
          </div>
        </div>

        <!-- 帖子已删除遮罩 - 覆盖封面和内容区域，但不覆盖操作按钮 -->
        <div v-if="item.isDeleted" class="deleted-mask" @click.stop>
          <div class="deleted-content">
            <el-icon :size="24"><Warning /></el-icon>
            <span class="deleted-text">帖子已被删除</span>
          </div>
        </div>

        <div class="post-actions">
          <button class="action-btn uncollect-btn" @click="removeFavorite(item, $event)">
            <el-icon><Star /></el-icon><span>取消收藏</span>
          </button>
        </div>
      </div>
    </div>

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

    <PostDetailModal 
      v-model:visible="showDetailModal"
      :post="currentPost"
      :is-logged-in="true"
      :user-info="currentUserInfo"
      @like="handleDetailLike"
      @reject="handleDetailReject"
      @collect="handleDetailCollect"
    />
  </div>
</template>


<style scoped lang="scss">
.favorites {
  display: flex;
  flex-direction: column;
  min-height: 500px;
  height: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-base);
  border-bottom: 1px solid var(--color-border-light);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
  
  .title-count {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-normal);
    color: var(--color-text-tertiary);
    background: var(--color-border-light);
    padding: 2px 8px;
    border-radius: 10px;
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-lg);
  
  .filter-hint {
    display: flex;
    align-items: center;
    flex: 1;
    
    .hint-text {
      font-size: var(--font-size-sm);
      color: var(--color-text-tertiary);
    }
  }
  
  .search-box {
    display: flex;
    align-items: center;
    flex: 0 0 440px;
    width: 440px;
    height: 36px;
    background: var(--color-bg-page);
    border: 1px solid var(--color-border-light);
    border-radius: 18px;
    overflow: hidden;
    box-shadow: none;
    transition: all var(--transition-fast);

    &:hover { background: var(--color-border-lighter); }
    &:focus-within { border-color: var(--color-primary); background: var(--color-bg-base); }

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
      &::placeholder { color: var(--color-text-placeholder); }
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
      &:hover { background: var(--color-border-light); color: var(--color-text-secondary); }
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
      font-size: var(--font-size-xs);
      &:hover { background: var(--color-primary-dark); }
      .search-btn-text { font-weight: var(--font-weight-medium); }
    }
  }
}

.post-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  min-height: 300px;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-5xl) var(--spacing-xl);
  .empty-icon { color: var(--color-border-base); margin-bottom: var(--spacing-lg); }
  .empty-text { color: var(--color-text-tertiary); margin-bottom: var(--spacing-lg); }

  .empty-btn {
    background: var(--color-primary);
    border-color: var(--color-primary);
    border-radius: 20px;
    padding: 10px 32px;
    font-size: 14px;
    font-weight: 500;

    &:hover {
      background: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
  }
}

.post-card {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
  background: var(--color-bg-base);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative; // 为遮罩提供定位上下文

  &:hover {
    border-color: var(--color-primary-lighter);
    box-shadow: var(--shadow-md);
    .post-actions { opacity: 1; }
  }

  // 帖子已删除时的样式
  &.is-deleted {
    cursor: not-allowed;
  }
}

// 已删除帖子遮罩 - 只覆盖封面和内容区域，不覆盖操作按钮
.deleted-mask {
  position: absolute;
  top: var(--spacing-md);
  left: var(--spacing-md);
  right: calc(100px + var(--spacing-xl)); // 为右侧操作按钮区域留出足够空间，并有更大间隙
  bottom: var(--spacing-md);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  pointer-events: none;
  border: 1px dashed var(--color-border-base);

  .deleted-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-xs);
    color: var(--color-text-secondary);
    pointer-events: auto;

    .el-icon {
      color: var(--color-warning);
      font-size: 28px;
    }

    .deleted-text {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      font-weight: var(--font-weight-medium);
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
  
  .cover-media { width: 100%; height: 100%; object-fit: cover; }
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
  }
}

.post-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.post-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-xs);
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
  max-width: 100%;
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  .author-avatar { width: 20px; height: 20px; border-radius: var(--radius-round); object-fit: cover; }
  .author-name { font-size: var(--font-size-xs); color: var(--color-text-tertiary); }
}

.post-meta {
  display: flex;
  gap: var(--spacing-md);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: auto;
  .meta-item { display: flex; align-items: center; gap: 4px; }
  .meta-item.time { margin-left: auto; }

  .like-icon {
    flex-shrink: 0;
  }
}

.post-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  position: relative;
  z-index: 2; // 确保按钮在遮罩之上
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border: none;
    border-radius: var(--radius-md);
    font-size: 12px;
    font-weight: var(--font-weight-medium);
    cursor: pointer;
    white-space: nowrap;
  }
  
  .uncollect-btn {
    background: rgba(255, 193, 7, 0.1);
    color: var(--color-warning);
    &:hover { background: rgba(255, 193, 7, 0.2); }
  }
}

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

@media (max-width: 640px) {
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .search-box { width: 100%; }
  .post-card { flex-direction: column; }
  .post-cover { width: 100%; height: 180px; }
  .post-actions { opacity: 1; }
  .post-meta .meta-item.time { margin-left: 0; }

  // 移动端遮罩适配 - 覆盖整个卡片但不覆盖底部操作按钮
  .deleted-mask {
    right: var(--spacing-md);
    bottom: 70px; // 为底部操作按钮留出更大空间
    border-radius: var(--radius-md);
    z-index: 1;
  }
}
</style>
