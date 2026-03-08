<template>
  <div class="forum-page">
    <!-- 左侧边栏 - 创作小组件，仅登录用户可见 -->
    <aside class="forum-sidebar" v-if="isLoggedIn">
      <div class="sidebar-content">
        <div class="widget-header">
          <img 
            :src="userInfo?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=guest'" 
            class="widget-avatar" 
          />
          <span class="widget-greeting">{{ greeting }}，{{ userInfo?.nickname || '铲屎官' }}</span>
        </div>
        <p class="widget-hint">今天也是元气满满的一天，分享你和毛孩子的故事吧~</p>
        <button class="widget-publish-btn" @click="handlePublish">
          <el-icon :size="16"><Plus /></el-icon>
          <span>发布新帖</span>
        </button>
      </div>
    </aside>
    <!-- 未登录时的占位，保持内容居中 -->
    <div v-else class="forum-sidebar-placeholder"></div>

    <!-- 右侧内容区 -->
    <main class="forum-main">
      <!-- 话题导航 -->
      <div class="topic-nav">
        <div class="topic-scroll">
          <!-- 全部 -->
          <div
            class="topic-tag"
            :class="{ active: activeTopic === null }"
            @click="handleTopicChange(null)"
          >全部</div>
          <!-- 推荐，仅登录用户可见 -->
          <div
            v-if="isLoggedIn"
            class="topic-tag recommend-tag"
            :class="{ active: activeTopic === RECOMMEND_TOPIC_ID }"
            @click="handleTopicChange(RECOMMEND_TOPIC_ID)"
          >推荐</div>
          <!-- 其余栏目（跳过"全部"，因为已单独渲染） -->
          <div 
            v-for="topic in topics.filter(t => t.id !== null)" 
            :key="topic.id"
            class="topic-tag"
            :class="{ active: activeTopic === topic.id }"
            @click="handleTopicChange(topic.id)"
          >{{ topic.name }}</div>
        </div>
      </div>

      <!-- 话题筛选提示 -->
      <div v-if="activeTopicId" class="topic-filter-bar">
        <span class="filter-label">当前话题：</span>
        <span class="filter-tag">{{ activeTopicName }}</span>
        <span class="clear-btn" @click="clearTopicFilter">
          <el-icon><Close /></el-icon>
          清除筛选
        </span>
      </div>

      <!-- 瀑布流内容区 -->
      <div class="forum-content">
        <div class="masonry-grid" ref="masonryRef">
          <div 
            v-for="(column, colIndex) in columns" 
            :key="colIndex"
            class="masonry-column"
          >
            <div 
              v-for="post in column" 
              :key="post.id"
              class="post-card"
              @click="openPostDetail(post)"
            >
              <div class="post-cover">
                <video 
                  v-if="isVideo(post.cover)" 
                  :src="`${post.cover}#t=1`"
                  muted
                  preload="metadata"
                  class="cover-video"
                  @loadeddata="$event.target.currentTime = 1"
                />
                <img 
                  v-else 
                  :src="post.cover" 
                  :alt="post.title" 
                  loading="lazy" 
                />
                <div v-if="isVideo(post.cover)" class="video-badge">
                  <el-icon><VideoCamera /></el-icon>
                </div>
                <div v-else-if="post.imageCount > 1" class="image-count">
                  <el-icon><Picture /></el-icon>
                  {{ post.imageCount }}
                </div>
              </div>
              <div class="post-title">{{ post.title }}</div>
              <div class="post-footer">
                <div class="user-info">
                  <img :src="post.author.avatar" class="user-avatar" />
                  <span class="user-name">{{ post.author.nickname }}</span>
                </div>
                <div 
                  class="like-btn"
                  :class="{ liked: post.isLiked }"
                  @click.stop="toggleLike(post)"
                >
                  <svg class="heart-icon" viewBox="0 0 24 24" width="16" height="16" :fill="post.isLiked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                  </svg>
                  <span>{{ formatCount(post.likeCount) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 滚动加载状态 -->
        <div class="scroll-load-status" v-if="postList.length > 0 && loading">
          <div class="loading-indicator">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="postList.length === 0 && !loading" class="empty-state">
          <el-icon :size="64"><Document /></el-icon>
          <p>暂无帖子，快来发布第一篇吧~</p>
        </div>
      </div>
      
      <!-- 没有更多数据提示 - 放在内容区底部 -->
      <div v-if="postList.length > 0 && !hasMore && !loading" class="end-hint">
        <span>— 汪汪，好像到了死胡同了~ —</span>
      </div>
    </main>

    <!-- 帖子详情弹窗 -->
    <PostDetailModal 
      v-model:visible="showDetailModal"
      :post="currentPost"
      :is-logged-in="isLoggedIn"
      :user-info="userInfo"
      @like="handleDetailLike"
      @reject="handleDetailReject"
      @collect="handleDetailCollect"
      @topic-click="handleTopicClick"
      @open-login="emit('open-login')"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PostDetailModal from '../components/PostDetailModal.vue'
import { POST_API, RECOMMEND_API, request } from '../api/config.js'

const route = useRoute()
const router = useRouter()

const props = defineProps({
  isLoggedIn: Boolean,
  userInfo: Object
})

const emit = defineEmits(['open-login'])

// 状态
const RECOMMEND_TOPIC_ID = 'recommend' // 推荐栏目的特殊标识
const activeTopic = ref(null) // null表示全部，否则为columnId
const activeTopicId = ref(null) // 话题ID筛选
const activeTopicName = ref('') // 当前话题名称
const loading = ref(false)
const hasMore = ref(true)
const showDetailModal = ref(false)
const currentPost = ref(null)
const columnCount = ref(4)

// 分页状态
const currentPage = ref(1)
const pageSize = ref(10) // 每次加载10条
const totalPages = ref(1)
const totalCount = ref(0)

// 栏目列表（从后端获取）
const topics = ref([])

// 帖子列表
const posts = ref([])

// 推荐模式：true=老用户展示推荐帖子，false=新用户/未登录展示全部
const isRecommendMode = ref(false)

// 响应式列数计算
const updateColumnCount = () => {
  const width = window.innerWidth
  if (width <= 600) {
    columnCount.value = 2
  } else if (width <= 900) {
    columnCount.value = 3
  } else if (width <= 1200) {
    columnCount.value = 4
  } else {
    columnCount.value = 5
  }
}

// 尝试拉取帖子推荐，决定展示模式
// 返回 null  → 新用户，展示全部帖子
// 返回 []    → 老用户但暂无推荐，展示全部帖子
// 返回 [...] → 老用户，展示推荐帖子
const fetchPostRecommendations = async () => {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return  // 未登录，直接用全部帖子模式

    const user = JSON.parse(raw)
    if (!user?.userId || !user?.token) return

    const response = await request(`${RECOMMEND_API.GET_POST_RECOMMENDATIONS}?userId=${user.userId}`)
    const result = await response.json()

    if (result.code === 0 && result.data && result.data.length > 0) {
      // 老用户且有推荐结果：转换格式后直接填充
      isRecommendMode.value = true
      posts.value = result.data.map(item => ({
        id: item.postId,
        title: item.title,
        cover: item.mediaUrl || 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=500&fit=crop',
        imageCount: 1,
        estimatedHeight: 400 + Math.random() * 200,
        columnId: item.columnId,
        author: {
          id: item.userId,
          nickname: item.username || '匿名用户',
          avatar: item.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`
        },
        likeCount: item.likeCount || 0,
        rejectCount: 0,
        isLiked: false,
        isRejected: false,
        isCollected: false
      }))
    }
    // data=null 或 data=[] 时 isRecommendMode 保持 false，走正常 fetchPosts
  } catch (error) {
    console.error('获取帖子推荐失败:', error)
  }
}

// 获取栏目列表
const fetchColumns = async () => {
  try {
    const response = await request(POST_API.GET_COLUMNS, {
      method: 'GET'
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      // 添加"全部"选项，然后追加后端返回的栏目
      topics.value = [
        { id: null, name: '全部' },
        ...result.data.map(item => ({
          id: item.columnId,
          name: item.name
        }))
      ]
    }
  } catch (error) {
    console.error('获取栏目失败:', error)
    ElMessage.error('获取栏目失败')
  }
}

// 获取帖子列表
const fetchPosts = async (isLoadMore = false) => {
  loading.value = true
  try {
    const requestBody = {
      currentPage: isLoadMore ? currentPage.value : 1,
      pageSize: pageSize.value
    }
    
    // 统一使用 list 接口，通过 columnId 区分全部和栏目筛选
    let apiUrl = POST_API.GET_POST_LIST

    // 优先使用话题筛选
    if (activeTopicId.value !== null) {
      requestBody.topicId = activeTopicId.value
      apiUrl = POST_API.GET_POSTS_UNDER_TOPIC
    } else if (activeTopic.value !== null && activeTopic.value !== RECOMMEND_TOPIC_ID) {
      // 栏目筛选：传 columnId，全部时不传（null）
      requestBody.columnId = activeTopic.value
    }
    // 全部 或 推荐降级：不传 columnId，后端返回全部
    
    const response = await request(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      const pageData = result.data
      // 转换后端数据格式为前端需要的格式
      const newPosts = (pageData.totalRecords || []).map(item => ({
        id: item.postId,
        title: item.title,
        cover: item.mediaUrl || 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=500&fit=crop',
        imageCount: 1,
        estimatedHeight: 400 + Math.random() * 200, // 随机高度用于瀑布流
        columnId: item.columnId,
        author: {
          id: item.userId,
          nickname: item.username || '匿名用户',
          avatar: item.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${item.userId}`
        },
        likeCount: item.likeCount || 0,
        rejectCount: item.rejectCount || 0,
        collectCount: 0,
        isLiked: false,
        isRejected: false,
        isCollected: false
      }))
      
      // 如果用户已登录，批量获取每个帖子的点赞状态
      if (props.isLoggedIn && props.userInfo?.userId && newPosts.length > 0) {
        const likeStatusPromises = newPosts.map(post => 
          request(`${POST_API.GET_LIKE_STATUS}?userId=${props.userInfo.userId}&postId=${post.id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
          }).then(res => res.json()).catch(() => ({ code: -1 }))
        )
        
        const likeResults = await Promise.all(likeStatusPromises)
        likeResults.forEach((result, index) => {
          if (result.code === 0) {
            newPosts[index].isLiked = result.data === true
          }
        })
      }
      
      if (isLoadMore) {
        posts.value = [...posts.value, ...newPosts]
      } else {
        posts.value = newPosts
      }
      
      currentPage.value = pageData.currentPage
      totalPages.value = pageData.totalPages
      totalCount.value = pageData.totalCount
      hasMore.value = pageData.currentPage < pageData.totalPages
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
    ElMessage.error('获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

// 切换栏目时重新加载
const handleTopicChange = async (topicId) => {
  activeTopicId.value = null
  activeTopicName.value = ''
  currentPage.value = 1
  router.replace({ query: {} })

  if (topicId === RECOMMEND_TOPIC_ID) {
    // 点击推荐标签：activeTopic 不传给后端，单独走推荐逻辑
    activeTopic.value = RECOMMEND_TOPIC_ID // 仅用于高亮 tab
    posts.value = []
    await fetchPostRecommendations()
    // 推荐失败或新用户时降级为全部帖子（activeTopic 临时置 null 避免传给后端）
    if (!isRecommendMode.value) {
      activeTopic.value = null
      fetchPosts(false)
    }
  } else {
    // 其他栏目：退出推荐模式，正常请求
    activeTopic.value = topicId
    isRecommendMode.value = false
    fetchPosts(false)
  }
}

// 处理话题标签点击（从帖子详情中）
const handleTopicClick = (tag) => {
  if (tag.id) {
    // 清除栏目筛选，设置话题筛选
    activeTopic.value = null
    activeTopicId.value = tag.id
    activeTopicName.value = tag.name
    currentPage.value = 1
    // 更新URL
    router.replace({ query: { topicId: tag.id, topicName: tag.name.replace('#', '') } })
    fetchPosts(false)
  }
}

// 清除话题筛选
const clearTopicFilter = () => {
  activeTopicId.value = null
  activeTopicName.value = ''
  currentPage.value = 1
  router.replace({ query: {} })
  fetchPosts(false)
}

// 从URL参数初始化话题筛选或打开帖子详情
const initFromQuery = async () => {
  const topicId = route.query.topicId
  const topicName = route.query.topicName
  const postId = route.query.postId

  if (postId) {
    // 如果有postId参数，打开对应帖子详情
    await openPostDetailById(Number(postId))
  } else if (topicId) {
    activeTopicId.value = Number(topicId)
    activeTopicName.value = topicName ? `#${topicName}` : ''
  }
}

// 根据帖子ID打开详情
const openPostDetailById = async (postId) => {
  try {
    // 先尝试从当前列表中查找
    let post = posts.value.find(p => p.id === postId)

    // 如果列表中没有，通过API获取帖子详情
    if (!post) {
      const response = await request(`${POST_API.GET_POST_DETAIL}?postId=${postId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      })
      const result = await response.json()

      if (result.code === 0 && result.data) {
        const data = result.data
        post = {
          id: data.postId,
          userId: data.userId,
          username: data.username,
          avatar: data.avatar,
          title: data.title,
          content: data.content,
          cover: data.mediaUrlWithId ? Object.values(data.mediaUrlWithId)[0] : null,
          mediaList: data.mediaUrlWithId ? Object.values(data.mediaUrlWithId) : [],
          mediaCount: data.mediaUrlWithId ? Object.values(data.mediaUrlWithId).length : 0,
          likeCount: data.likeCount,
          rejectCount: data.rejectCount,
          favorCount: data.favorCount,
          isLiked: false,
          isRejected: false,
          isCollected: false,
          columnId: data.columnId,
          columnName: data.columnName,
          topicIds: data.topicId,
          topicNames: data.topicName,
          updateTime: data.updateTime
        }
      }
    }

    if (post) {
      currentPost.value = post
      // 使用 nextTick 确保 DOM 更新后再显示弹框
      await nextTick()
      showDetailModal.value = true
    }
  } catch (error) {
    console.error('打开帖子详情失败:', error)
  }
}

// 滚动加载处理
const handleScroll = () => {
  if (loading.value || !hasMore.value) return
  
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 距离底部100px时触发加载
  if (scrollTop + windowHeight >= documentHeight - 100) {
    loadMore()
  }
}

onMounted(async () => {
  updateColumnCount()
  window.addEventListener('resize', updateColumnCount)
  window.addEventListener('scroll', handleScroll)
  // 初始化加载数据
  await fetchColumns()
  // 默认加载全部帖子，推荐由用户主动点击触发
  await fetchPosts()
  await initFromQuery()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateColumnCount)
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化，当在论坛页内切换帖子时触发
watch(() => route.query.postId, async (newPostId) => {
  if (newPostId) {
    await openPostDetailById(Number(newPostId))
  }
})

// 监听弹窗关闭，关闭时清除 URL 中的 postId 参数
watch(showDetailModal, (newVal) => {
  if (!newVal && route.query.postId) {
    // 弹窗关闭且有 postId 参数时，清除参数
    router.replace({ query: {} })
  }
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 12) return '早上好'
  if (hour >= 12 && hour < 18) return '下午好'
  return '晚上好'
})

// 帖子列表（直接使用posts）
const postList = computed(() => posts.value)

// 瀑布流分列：最短列优先，确保卡片紧密贴合
const columns = computed(() => {
  const cols = Array.from({ length: columnCount.value }, () => [])
  const heights = Array(columnCount.value).fill(0)
  const cardExtraHeight = 80 // 标题+底部区域的预估高度
  
  postList.value.forEach(post => {
    // 找到最短的列
    const minHeight = Math.min(...heights)
    const minIndex = heights.indexOf(minHeight)
    
    // 将卡片放入最短列
    cols[minIndex].push(post)
    heights[minIndex] += post.estimatedHeight + cardExtraHeight + 20
  })
  
  return cols
})

const formatCount = (count) => {
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count
}

// 判断媒体是否为视频
const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.includes(ext))
}

const toggleLike = async (post) => {
  if (!props.isLoggedIn) { emit('open-login'); return }
  
  try {
    const response = await request(POST_API.LIKE_POST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        postId: post.id,
        opFlag: post.isLiked ? 1 : 0 // 0-点赞，1-取消点赞
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      post.isLiked = !post.isLiked
      post.likeCount = result.data // 后端返回最新的点赞数
      // 如果点赞成功且之前是点踩状态，需要取消点踩状态
      if (post.isLiked && post.isRejected) {
        post.isRejected = false
        // 点踩数需要减1（后端已处理）
      }
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  }
}

const toggleReject = async (post) => {
  if (!props.isLoggedIn) { emit('open-login'); return }
  
  try {
    const response = await request(POST_API.DISLIKE_POST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        postId: post.id,
        opFlag: post.isRejected ? 1 : 0 // 0-点踩，1-取消点踩
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      post.isRejected = !post.isRejected
      post.rejectCount = result.data // 后端返回最新的点踩数
      // 如果点踩成功且之前是点赞状态，需要取消点赞状态
      if (post.isRejected && post.isLiked) {
        post.isLiked = false
        // 点赞数需要减1（后端已处理）
      }
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('点踩操作失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  }
}

const openPostDetail = (post) => {
  if (!props.isLoggedIn) { ElMessage.warning('请先登录再操作'); emit('open-login'); return }
  currentPost.value = post
  showDetailModal.value = true
}

// 从详情弹窗同步点赞状态到列表
const handleDetailLike = (post) => {
  // 找到列表中对应的帖子并同步状态
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
  // 找到列表中对应的帖子并同步状态
  const listPost = posts.value.find(p => p.id === post.id)
  if (listPost) {
    listPost.isRejected = post.isRejected
    listPost.rejectCount = post.rejectCount
    listPost.isLiked = post.isLiked
    listPost.likeCount = post.likeCount
  }
}

const handleDetailCollect = (post) => {
  // PostDetailModal 已经处理了接口调用，这里只需同步列表中的帖子状态
  const listPost = posts.value.find(p => p.id === post.id)
  if (listPost) {
    listPost.isCollected = post.isCollected
    listPost.collectCount = post.collectCount
  }
}

const loadMore = () => {
  if (loading.value || !hasMore.value) return
  currentPage.value++
  fetchPosts(true)
}

const handlePublish = () => {
  if (!props.isLoggedIn) { emit('open-login'); return }
  router.push('/post/create')
}
</script>

<style lang="scss" scoped>
.forum-page {
  display: flex;
  background: var(--color-bg-base);
}

.forum-sidebar-placeholder {
  width: 220px;
  flex-shrink: 0;
}

.forum-sidebar {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: var(--header-height);
  max-height: calc(100vh - var(--header-height));
  align-self: flex-start;
  padding: var(--spacing-xl) var(--spacing-lg) var(--spacing-xl) 0;
  
  .sidebar-content {
    background: var(--color-bg-surface);
    border-radius: 0 var(--radius-xl) var(--radius-xl) 0;
    padding: var(--spacing-xl);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
}

.widget-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-base);
}

.widget-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-round);
  border: 2px solid var(--color-primary-lighter);
  object-fit: cover;
}

.widget-greeting {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.widget-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  line-height: var(--line-height-relaxed);
  margin: 0 0 var(--spacing-lg);
}

.widget-publish-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: #FF8A5B;
  background: #FFF0E6;
  border: 1px solid rgba(255, 138, 91, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  
  &:hover {
    color: #fff;
    background: linear-gradient(135deg, #FF8A5B 0%, #E6784E 100%);
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
  }
}

.forum-main {
  flex: 1;
  min-width: 0;
}

.topic-nav {
  padding: var(--spacing-sm) var(--spacing-lg);
  padding-bottom: var(--spacing-md); // 给按钮阴影留出空间
}

.topic-filter-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  background: rgba(255, 138, 91, 0.08);
  border: 1px solid rgba(255, 138, 91, 0.15);
  border-radius: var(--radius-lg);
  margin: 0 var(--spacing-lg) var(--spacing-md);
  
  .filter-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
  
  .filter-tag {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.1);
    padding: 4px 12px;
    border-radius: var(--radius-md);
  }
  
  .clear-btn {
    margin-left: auto;
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: color var(--transition-fast);
    
    &:hover {
      color: var(--color-primary);
    }
  }
}

.topic-scroll {
  display: flex;
  gap: var(--spacing-md);
  overflow-x: auto;
  padding: 4px 0 8px; // 上下留出空间给阴影
  margin: -4px 0 -8px; // 用负 margin 抵消额外空间
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }
}

.topic-tag {
  flex-shrink: 0;
  padding: 10px 18px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-base);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  white-space: nowrap;
  box-shadow: var(--shadow-sm);
  position: relative;
  
  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary-light);
    background: rgba(255, 138, 91, 0.08);
    transform: translateY(-1px);
    z-index: 1;
  }
  
  &.active {
    color: var(--color-text-inverse);
    border-color: transparent;
    font-weight: var(--font-weight-medium);
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
    z-index: 1;
  }

  // 推荐标签默认带橙色描边，与普通标签区分
  &.recommend-tag {
    color: var(--color-primary);
    border-color: var(--color-primary-light);
    background: rgba(255, 138, 91, 0.06);

    &.active {
      color: var(--color-text-inverse);
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border-color: transparent;
    }
  }
}

.forum-content {
  padding: var(--spacing-lg);
}

/* 瀑布流容器 - Flex 多列布局 */
.masonry-grid {
  display: flex;
  gap: 16px;
}

.masonry-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 帖子卡片 */
.post-card {
  background: var(--color-bg-surface);
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  }
}

.post-cover {
  position: relative;
  width: 100%;
  
  img, .cover-video {
    width: 100%;
    height: auto;
    display: block;
    border-radius: 10px 10px 0 0;
  }
  
  .cover-video {
    object-fit: cover;
    aspect-ratio: 4/5;
  }
}

.video-badge {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: var(--radius-round);
  color: #fff;
}

.image-count {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  font-size: var(--font-size-xs);
  color: var(--color-text-inverse);
  background: rgba(0, 0, 0, 0.5);
  border-radius: var(--radius-sm);
}

.post-title {
  padding: var(--spacing-sm) var(--spacing-sm) var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  line-height: var(--line-height-base);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-xs) var(--spacing-sm) var(--spacing-sm);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  min-width: 0;
  flex: 1;
}

.user-avatar {
  width: 20px;
  height: 20px;
  border-radius: var(--radius-round);
  object-fit: cover;
  flex-shrink: 0;
}

.user-name {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.like-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  cursor: pointer;
  padding: var(--spacing-xs);
  margin: calc(-1 * var(--spacing-xs));
  border-radius: var(--radius-sm);
  transition: all var(--transition-base) var(--ease-out);
  
  &:hover { color: var(--color-danger); }
  
  &.liked {
    color: var(--color-danger);
    .heart-icon { animation: heartBeat 0.3s var(--ease-spring); }
  }
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.load-more {
  text-align: center;
  padding: var(--spacing-xl) 0;
  
  .el-button {
    background: var(--color-bg-surface);
    border-color: var(--color-border-base);
    color: var(--color-text-secondary);
    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
    }
  }
}

.scroll-load-status {
  text-align: center;
  padding: var(--spacing-xl) 0;
  
  .loading-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: var(--spacing-sm);
    color: var(--color-text-tertiary);
    font-size: var(--font-size-sm);
    
    .el-icon {
      font-size: 18px;
      color: var(--color-primary);
    }
  }
}

.end-hint {
  text-align: center;
  padding: var(--spacing-lg) 0 0;
  margin-bottom: -20px;
  color: var(--color-text-quaternary);
  font-size: var(--font-size-sm);
}

.empty-state {
  text-align: center;
  padding: var(--spacing-5xl) var(--spacing-xl);
  color: var(--color-text-tertiary);
  .el-icon { color: var(--color-border-base); margin-bottom: var(--spacing-base); }
  p { margin: 0; font-size: var(--font-size-base); }
}

@media (max-width: 768px) {
  .forum-sidebar { display: none; }
  
  .forum-page::after {
    content: '+';
    position: fixed;
    right: var(--spacing-xl);
    bottom: var(--spacing-xl);
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
    color: var(--color-text-inverse);
    font-size: 28px;
    border-radius: var(--radius-round);
    box-shadow: var(--shadow-lg);
    z-index: var(--z-index-fixed);
  }
}
</style>
