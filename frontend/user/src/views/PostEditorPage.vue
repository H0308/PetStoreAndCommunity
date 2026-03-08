<template>
  <div class="post-editor-page">
    <div class="editor-container">
      <!-- 页面头部 -->
      <div class="editor-header">
        <div class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <h1 class="page-title">{{ isEdit ? '编辑帖子' : '发布新帖' }}</h1>
      </div>

      <!-- Tab 切换 -->
      <div class="post-type-tabs">
        <div 
          v-for="tab in postTypes" 
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeType === tab.key, disabled: isEdit }"
          @click="handleTypeChange(tab.key)"
        >
          <el-icon :size="20"><component :is="tab.icon" /></el-icon>
          <span>{{ tab.label }}</span>
        </div>
      </div>

      <!-- 编辑器主体 -->
      <div class="editor-body" :class="{ 'video-layout': activeType === 'video' }">

        <!-- 左栏（视频模式：上传区；其他模式：全宽内容区） -->
        <div class="editor-left">
          <!-- 纯文字模式 -->
          <div v-if="activeType === 'text'" class="text-mode-section">
            <AdvancedCardCreator
              ref="textPostCreatorRef"
              :username="currentUsername"
              :initial-title="postForm.title"
              :initial-content="postForm.content"
              @update:title="postForm.title = $event"
              @update:content="postForm.content = $event"
            />
          </div>

          <!-- 媒体上传区（视频/图文模式） -->
          <div v-if="activeType !== 'text'" class="media-upload-section">
            <div
              class="upload-area"
              :class="{ 'has-files': mediaList.length > 0, 'drag-over': isDragOver, 'no-click': activeType === 'video' && mediaList.length > 0 }"
              @dragover.prevent="activeType === 'video' && mediaList.length > 0 ? null : handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @drop.prevent="activeType === 'video' && mediaList.length > 0 ? null : handleDrop"
            >
              <!-- 透明 input 覆盖整个上传区，直接由用户点击触发，避免 JS 间接 click 被浏览器拦截 -->
              <input
                v-if="mediaList.length === 0"
                ref="fileInputRef"
                type="file"
                :accept="acceptTypes"
                :multiple="activeType === 'image'"
                class="upload-input-overlay"
                @change="handleFileSelect"
              />

              <!-- 空状态 -->
              <div v-if="mediaList.length === 0" class="upload-placeholder">
                <el-icon :size="48" class="upload-icon">
                  <component :is="activeType === 'video' ? VideoCamera : Picture" />
                </el-icon>
                <p class="upload-text">
                  {{ activeType === 'video' ? '点击或拖拽上传视频' : '点击或拖拽上传图片' }}
                </p>
                <p class="upload-hint">
                  {{ activeType === 'video' ? '支持 MP4、MOV 格式，最大 100MB' : '支持 JPG、PNG、GIF 格式，最多 9 张' }}
                </p>
              </div>

              <!-- 视频已上传：缩略图预览 -->
              <div v-else-if="activeType === 'video'" class="video-single-preview media-preview-item">
                <video :src="mediaList[0].url" class="preview-media" preload="metadata" muted />
                <div class="video-play-overlay" @click.stop="openVideoPreview(mediaList[0].url)">
                  <el-icon :size="40"><VideoPlay /></el-icon>
                </div>
                <div class="media-delete" @click.stop="removeMedia(0)">
                  <el-icon><Close /></el-icon>
                </div>
                <div class="video-duration">{{ formatDuration(mediaList[0].duration) }}</div>
              </div>

              <!-- 图片模式：grid -->
              <div v-else class="media-preview-grid">
                <div
                  v-for="(media, index) in mediaList"
                  :key="index"
                  class="media-preview-item"
                  @click.stop="openImagePreview(index)"
                >
                  <img :src="media.url" class="preview-media" />
                  <!-- hover 预览提示遮罩 -->
                  <div class="image-hover-overlay">
                    <el-icon :size="22"><ZoomIn /></el-icon>
                    <span>预览</span>
                  </div>
                  <div class="media-delete" @click.stop="removeMedia(index)">
                    <el-icon><Close /></el-icon>
                  </div>
                </div>
                <label v-if="mediaList.length < 9" class="add-more-btn" @click.stop>
                  <input
                    type="file"
                    :accept="acceptTypes"
                    multiple
                    class="upload-input-overlay"
                    style="border-radius: var(--radius-md);"
                    @change="handleFileSelect"
                  />
                  <el-icon :size="32"><Plus /></el-icon>
                  <span>添加图片</span>
                </label>
              </div>
            </div>

            <!-- 视频模式提示 -->
            <p v-if="activeType === 'video' && mediaList.length > 0" class="video-replace-hint">
              删除当前视频后可重新上传
            </p>
          </div>

          <!-- 图文/纯文字模式下的标题、正文、话题、栏目（非视频模式） -->
          <template v-if="activeType !== 'video'">
            <div v-if="activeType !== 'text'" class="title-input-section">
              <input
                v-model="postForm.title"
                type="text"
                class="title-input"
                placeholder="填写标题会有更多赞哦~"
                maxlength="50"
              />
              <span class="title-count">{{ postForm.title.length }}/50</span>
            </div>
            <div v-if="activeType !== 'text'" class="content-input-section">
              <TextCodeEditor
                v-model="postForm.content"
                placeholder="分享你和毛孩子的故事，支持 Markdown 语法"
                :max-length="2000"
              />
            </div>
            <div class="topic-section">
              <div class="section-label">
                <el-icon><CollectionTag /></el-icon>
                <span>添加话题</span>
                <span class="label-hint">（最多选择3个）</span>
              </div>
              <div class="topic-selector">
                <el-select
                  v-model="postForm.topics"
                  multiple
                  filterable
                  reserve-keyword
                  placeholder="搜索或选择话题"
                  :remote-method="searchTopics"
                  :loading="topicLoading"
                  :multiple-limit="3"
                  class="topic-select"
                >
                  <el-option
                    v-for="topic in topicOptions"
                    :key="topic.id"
                    :label="topic.name"
                    :value="topic.id"
                  >
                    <span class="topic-option">
                      <span class="topic-name">#{{ topic.name }}</span>
                    </span>
                  </el-option>
                </el-select>
                <div v-if="selectedTopics.length > 0" class="selected-topics">
                  <el-tag
                    v-for="topic in selectedTopics"
                    :key="topic.id"
                    closable
                    type="warning"
                    effect="light"
                    @close="removeTopic(topic.id)"
                  >
                    #{{ topic.name }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="column-section">
              <div class="section-label">
                <el-icon><Menu /></el-icon>
                <span>选择栏目</span>
              </div>
              <el-select v-model="postForm.columnId" placeholder="请选择发布栏目" class="column-select">
                <el-option
                  v-for="column in columnOptions"
                  :key="column.id"
                  :label="column.name"
                  :value="column.id"
                />
              </el-select>
            </div>
          </template>
        </div>

        <!-- 右栏（仅视频模式显示：标题、正文） -->
        <div v-if="activeType === 'video'" class="editor-right">
          <div class="title-input-section">
            <input
              v-model="postForm.title"
              type="text"
              class="title-input"
              placeholder="填写标题会有更多赞哦~"
              maxlength="50"
            />
            <span class="title-count">{{ postForm.title.length }}/50</span>
          </div>
          <div class="content-input-section">
            <TextCodeEditor
              v-model="postForm.content"
              placeholder="分享你和毛孩子的故事，支持 Markdown 语法"
              :max-length="2000"
            />
          </div>
        </div>

        <!-- 视频模式：话题和栏目全宽（换行到两栏下方） -->
        <template v-if="activeType === 'video'">
          <div class="video-full-row topic-section">
            <div class="section-label">
              <el-icon><CollectionTag /></el-icon>
              <span>添加话题</span>
              <span class="label-hint">（最多选择3个）</span>
            </div>
            <div class="topic-selector">
              <el-select
                v-model="postForm.topics"
                multiple
                filterable
                reserve-keyword
                placeholder="搜索或选择话题"
                :remote-method="searchTopics"
                :loading="topicLoading"
                :multiple-limit="3"
                class="topic-select"
              >
                <el-option
                  v-for="topic in topicOptions"
                  :key="topic.id"
                  :label="topic.name"
                  :value="topic.id"
                >
                  <span class="topic-option">
                    <span class="topic-name">#{{ topic.name }}</span>
                  </span>
                </el-option>
              </el-select>
              <div v-if="selectedTopics.length > 0" class="selected-topics">
                <el-tag
                  v-for="topic in selectedTopics"
                  :key="topic.id"
                  closable
                  type="warning"
                  effect="light"
                  @close="removeTopic(topic.id)"
                >
                  #{{ topic.name }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="video-full-row column-section">
            <div class="section-label">
              <el-icon><Menu /></el-icon>
              <span>选择栏目</span>
            </div>
            <el-select v-model="postForm.columnId" placeholder="请选择发布栏目" class="column-select">
              <el-option
                v-for="column in columnOptions"
                :key="column.id"
                :label="column.name"
                :value="column.id"
              />
            </el-select>
          </div>
        </template>

      </div>


      <!-- 底部操作栏 -->
      <div class="editor-footer">
        <el-button 
          class="draft-btn"
          :loading="savingDraft"
          @click="saveDraft"
        >
          <el-icon><Document /></el-icon>
          存草稿
        </el-button>
        <el-button 
          type="primary"
          class="publish-btn"
          :loading="publishing"
          :disabled="!canPublish"
          @click="handlePublish"
        >
          发布
        </el-button>
      </div>
    </div>

    <!-- 实名认证提示弹窗 -->
    <Teleport to="body">
      <Transition name="auth-tip-modal">
        <div v-if="showAuthTipDialog" class="auth-tip-modal-overlay">
          <div class="auth-tip-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Warning /></el-icon>
                需要实名认证
              </h3>
              <button class="modal-close" @click="showAuthTipDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="modal-body">
              <div class="auth-tip-content">
                <p>{{ authTipMessage }}</p>
                <p class="auth-tip-hint">请前往个人资料页面完成实名认证后再进行操作。</p>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showAuthTipDialog = false">取消</button>
              <button class="btn-confirm" @click="showAuthTipDialog = false; router.push('/profile/profile')">去认证</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    <!-- 视频预览弹窗 -->
    <Teleport to="body">
      <Transition name="video-preview-fade">
        <div v-if="showVideoPreview" class="video-preview-overlay" @click.self="closeVideoPreview">
          <div class="video-preview-container">
            <button class="video-preview-close-btn" @click="closeVideoPreview">
              <el-icon><Close /></el-icon>
            </button>
            <CustomVideoPlayer
              v-if="previewVideoUrl"
              ref="videoPreviewPlayerRef"
              :src="previewVideoUrl"
              autoplay
              class="video-preview-player"
            />
          </div>
        </div>
      </Transition>
    </Teleport>
    <!-- 图片预览弹窗 -->
    <el-image-viewer
      v-if="showImagePreview"
      :url-list="mediaList.filter(m => m.type === 'image').map(m => m.url)"
      :initial-index="imagePreviewIndex"
      @close="closeImagePreview"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, VideoCamera, Picture, Plus, Close, ZoomIn,
  CollectionTag, Menu, Document, EditPen, Warning, VideoPlay
} from '@element-plus/icons-vue'
import { POST_API, request } from '../api/config.js'
import AdvancedCardCreator from '../components/AdvancedCardCreator.vue'
import TextCodeEditor from '../components/TextCodeEditor.vue'
import CustomVideoPlayer from '../components/CustomVideoPlayer.vue'

const router = useRouter()
const route = useRoute()

// 实名认证提示弹窗
const showAuthTipDialog = ref(false)
const authTipMessage = ref('')

// 是否编辑模式
const isEdit = computed(() => !!route.query.postId)

// 编辑模式下的原始帖子数据
const originalPost = ref(null)
// 编辑模式下的媒体数据（包含ID）
const originalMediaMap = ref({}) // { mediaId: url }

// 帖子类型
const postTypes = [
  { key: 'video', label: '发视频', icon: VideoCamera },
  { key: 'image', label: '发图文', icon: Picture },
  { key: 'text', label: '纯文字', icon: EditPen }
]
const activeType = ref('image')

// 表单数据
const postForm = ref({
  title: '',
  content: '',
  topics: [],
  columnId: null
})

// 媒体列表
const mediaList = ref([])
const fileInputRef = ref(null)
const isDragOver = ref(false)

// 视频预览弹窗
const showVideoPreview = ref(false)
const previewVideoUrl = ref('')
const videoPreviewPlayerRef = ref(null)

const openVideoPreview = (url) => {
  previewVideoUrl.value = url
  showVideoPreview.value = true
}
const closeVideoPreview = () => {
  if (videoPreviewPlayerRef.value) videoPreviewPlayerRef.value.pause?.()
  showVideoPreview.value = false
  previewVideoUrl.value = ''
}

// 图片预览弹窗
const showImagePreview = ref(false)
const imagePreviewIndex = ref(0)

const openImagePreview = (index) => {
  imagePreviewIndex.value = index
  showImagePreview.value = true
}
const closeImagePreview = () => {
  showImagePreview.value = false
}

// 纯文字模式组件引用
const textPostCreatorRef = ref(null)

// 获取当前用户名
const currentUsername = computed(() => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.username || user.nickname || '匿名用户'
    }
  } catch (e) {}
  return '匿名用户'
})

// 话题相关
const topicLoading = ref(false)
const topicOptions = ref([])
const allTopics = ref([])

// 栏目选项
const columnOptions = ref([])

// 状态
const savingDraft = ref(false)
const publishing = ref(false)

// 计算属性
const acceptTypes = computed(() => {
  return activeType.value === 'video' 
    ? 'video/mp4,video/quicktime,video/x-msvideo'
    : 'image/jpeg,image/png,image/gif,image/webp'
})

const selectedTopics = computed(() => {
  return allTopics.value.filter(t => postForm.value.topics.includes(t.id))
})

const canPublish = computed(() => {
  // 标题必填
  if (!postForm.value.title.trim()) return false
  
  // 视频模式：必须有视频
  if (activeType.value === 'video') {
    const hasVideo = mediaList.value.some(m => m.type === 'video')
    if (!hasVideo) return false
  }
  
  // 图文模式：必须有图片
  if (activeType.value === 'image') {
    const hasImage = mediaList.value.some(m => m.type === 'image')
    if (!hasImage) return false
  }
  
  // 纯文字模式需要有内容
  if (activeType.value === 'text' && !postForm.value.content.trim()) return false
  
  // 需要选择栏目
  if (!postForm.value.columnId) return false
  return true
})

// 方法
const goBack = () => {
  if (postForm.value.title || postForm.value.content || mediaList.value.length > 0) {
    ElMessageBox.confirm('确定要离开吗？未保存的内容将会丢失', '提示', {
      confirmButtonText: '确定离开',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }).then(() => {
      router.back()
    }).catch(() => {})
  } else {
    router.back()
  }
}

const handleTypeChange = (type) => {
  if (type === activeType.value) return
  
  // 编辑模式下不允许切换板式
  if (isEdit.value) {
    ElMessage.warning('编辑模式下不支持切换发布板式')
    return
  }
  
  if (mediaList.value.length > 0) {
    ElMessageBox.confirm('切换类型将清空已上传的媒体，确定继续吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      activeType.value = type
      mediaList.value = []
    }).catch(() => {})
  } else {
    activeType.value = type
  }
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (e) => {
  const files = Array.from(e.target.files || [])
  processFiles(files)
  e.target.value = ''
}

const handleDragOver = () => {
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

const handleDrop = (e) => {
  isDragOver.value = false
  const files = Array.from(e.dataTransfer.files || [])
  processFiles(files)
}

const processFiles = (files) => {
  // 文件指纹：name + size + lastModified
  const fileKey = (f) => `${f.name}_${f.size}_${f.lastModified}`

  if (activeType.value === 'video') {
    const videoFile = files.find(f => f.type.startsWith('video/'))
    if (videoFile) {
      if (videoFile.size > 100 * 1024 * 1024) {
        ElMessage.warning('视频大小不能超过100MB')
        return
      }
      // 视频模式只有一个，直接替换，无需查重
      const url = URL.createObjectURL(videoFile)
      mediaList.value = [{ file: videoFile, url, type: 'video', duration: 0 }]
      const video = document.createElement('video')
      video.src = url
      video.onloadedmetadata = () => { mediaList.value[0].duration = video.duration }
    }
  } else {
    const imageFiles = files.filter(f => f.type.startsWith('image/'))
    const existingKeys = new Set(mediaList.value.filter(m => m.file).map(m => fileKey(m.file)))
    const remaining = 9 - mediaList.value.length
    let skippedDup = 0
    let added = 0

    for (const file of imageFiles) {
      if (added >= remaining) break
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.warning(`图片 ${file.name} 超过10MB，已跳过`)
        continue
      }
      if (existingKeys.has(fileKey(file))) {
        skippedDup++
        continue
      }
      mediaList.value.push({ file, url: URL.createObjectURL(file), type: 'image' })
      existingKeys.add(fileKey(file))
      added++
    }

    if (skippedDup > 0) ElMessage.warning('存在相同的文件在上传队列中')
    if (imageFiles.length - skippedDup > remaining) ElMessage.warning('最多上传9张图片，已自动截取')
  }
}

const removeMedia = (index) => {
  const media = mediaList.value[index]
  
  // 如果是新上传的文件，释放URL
  if (media.file) {
    URL.revokeObjectURL(media.url)
  }
  
  // 标记为已删除（编辑模式下有mediaId的媒体）
  if (media.mediaId) {
    media.deleted = true
  }
  
  mediaList.value.splice(index, 1)
}

const formatDuration = (seconds) => {
  if (!seconds) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 搜索话题
const searchTopics = async (query) => {
  if (!query) {
    topicOptions.value = allTopics.value.slice(0, 20)
    return
  }
  topicLoading.value = true
  // 本地过滤
  topicOptions.value = allTopics.value.filter(t => 
    t.name.toLowerCase().includes(query.toLowerCase())
  ).slice(0, 20)
  topicLoading.value = false
}

const removeTopic = (topicId) => {
  postForm.value.topics = postForm.value.topics.filter(id => id !== topicId)
}

// 获取栏目列表
const fetchColumns = async () => {
  try {
    const response = await request(POST_API.GET_COLUMNS, { method: 'GET' })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      // 过滤掉 columnId 为 null 的数据
      columnOptions.value = result.data
        .filter(item => item.columnId != null)
        .map(item => ({
          id: item.columnId,
          name: item.name
        }))
    }
  } catch (error) {
    console.error('获取栏目失败:', error)
  }
}

// 获取话题列表
const fetchTopics = async () => {
  topicLoading.value = true
  try {
    const response = await request(POST_API.GET_TOPICS, {
      method: 'GET'
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      // 后端返回 { topicId, name }，转换为前端需要的格式，过滤掉无效数据
      allTopics.value = result.data
        .filter(item => item.topicId != null)
        .map(item => ({
          id: item.topicId,
          name: item.name
        }))
      topicOptions.value = allTopics.value.slice(0, 20)
    }
  } catch (error) {
    console.error('获取话题列表失败:', error)
    ElMessage.error('获取话题列表失败')
  } finally {
    topicLoading.value = false
  }
}

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const user = JSON.parse(userInfo)
      return user.userId || user.id
    }
  } catch (e) {}
  return null
}

// 保存草稿
const saveDraft = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 去除空白字符
  const trimmedTitle = postForm.value.title?.trim() || ''
  const trimmedContent = postForm.value.content?.trim() || ''
  
  // 空白字符校验
  if (postForm.value.title && !trimmedTitle) {
    ElMessage.warning('标题不能只包含空白字符')
    return
  }
  if (postForm.value.content && !trimmedContent && activeType.value === 'text') {
    ElMessage.warning('内容不能只包含空白字符')
    return
  }
  
  if (!trimmedTitle) {
    ElMessage.warning('请填写标题')
    return
  }
  
  if (!postForm.value.columnId) {
    ElMessage.warning('请选择栏目')
    return
  }
  
  // 视频模式必须有视频
  if (activeType.value === 'video') {
    const hasVideo = mediaList.value.some(m => m.type === 'video')
    if (!hasVideo) {
      ElMessage.warning('请上传视频')
      return
    }
  }
  
  // 图文模式必须有图片
  if (activeType.value === 'image') {
    const hasImage = mediaList.value.some(m => m.type === 'image')
    if (!hasImage) {
      ElMessage.warning('请上传图片')
      return
    }
  }
  
  // 纯文字模式必须有内容
  if (activeType.value === 'text' && !trimmedContent) {
    ElMessage.warning('请填写内容')
    return
  }
  
  savingDraft.value = true
  try {
    if (isEdit.value) {
      // 编辑模式：调用修改接口保存草稿
      const postId = route.query.postId

      // 构建媒体删除标志
      const originalMediaIds = Object.keys(originalMediaMap.value).map(Number)
      const currentMediaIds = mediaList.value.filter(m => m.mediaId).map(m => m.mediaId)
      const mediaUrlWithDeleteFlag = {}
      originalMediaIds.forEach(mediaId => {
        mediaUrlWithDeleteFlag[mediaId] = currentMediaIds.includes(mediaId) ? 0 : 1
      })

      const newFiles = mediaList.value.filter(m => m.file).map(m => m.file)
      // 所有原有媒体都被删除且没有新文件时才拦截
      const allDeleted = originalMediaIds.length > 0 &&
        originalMediaIds.every(id => mediaUrlWithDeleteFlag[id] === 1)
      if (allDeleted && newFiles.length === 0) {
        ElMessage.warning('请至少保留或上传一个媒体文件')
        savingDraft.value = false
        return
      }

      const mediaTypeNum = activeType.value === 'video' ? 2 : 1
      if (newFiles.length > 0) {
        if (mediaTypeNum === 1 && newFiles.length > 9) {
          ElMessage.warning('图片最多上传9张')
          savingDraft.value = false
          return
        }
        if (mediaTypeNum === 2 && newFiles.length > 1) {
          ElMessage.warning('视频最多上传1个')
          savingDraft.value = false
          return
        }
      }

      const editData = {
        userId,
        postId: Number(postId),
        opFlag: 0,
        mediaUrlWithDeleteFlag
      }
      if (postForm.value.columnId !== originalPost.value.columnId) {
        editData.columnId = postForm.value.columnId
      }
      const originalTopicIds = originalPost.value.topicIds || []
      const currentTopicIds = postForm.value.topics || []
      const topicsChanged = originalTopicIds.length !== currentTopicIds.length ||
        !originalTopicIds.every(id => currentTopicIds.includes(id))
      if (topicsChanged) {
        editData.topicIds = currentTopicIds.length > 0 ? currentTopicIds : null
      }
      if (postForm.value.title !== originalPost.value.title) {
        editData.title = postForm.value.title
      }
      if (postForm.value.content !== originalPost.value.content) {
        editData.content = postForm.value.content
      }

      const formData = new FormData()
      const contentBlob = new Blob([JSON.stringify(editData)], { type: 'application/json' })
      formData.append('content', contentBlob)
      if (newFiles.length > 0) {
        formData.append('mediaType', mediaTypeNum)
        newFiles.forEach(f => formData.append('files', f))
      }

      const response = await request(POST_API.EDIT_POST, {
        method: 'POST',
        headers: {},
        body: formData
      })
      const result = await response.json()
      
      if (result.code !== 0) {
        ElMessage.error(result.message || '保存失败')
        return
      }
      
      ElMessage.success('草稿已保存')
    } else {
      // 新建模式：调用创建接口
      let filesToUpload = []
      let mediaTypeNum = 1

      if (activeType.value === 'text') {
        if (textPostCreatorRef.value) {
          const validation = textPostCreatorRef.value.validate()
          if (validation.valid) {
            const imageBlob = await textPostCreatorRef.value.generateImageBlob()
            if (imageBlob) {
              filesToUpload = [new File([imageBlob], 'cover.png', { type: 'image/png' })]
              mediaTypeNum = 1
            }
          }
        }
      } else {
        filesToUpload = mediaList.value.filter(m => m.file).map(m => m.file)
        mediaTypeNum = activeType.value === 'video' ? 2 : 1
      }

      if (filesToUpload.length === 0) {
        ElMessage.warning('请上传媒体文件')
        return
      }

      const formData = new FormData()
      const contentBlob = new Blob([JSON.stringify({
        userId,
        columnId: postForm.value.columnId,
        topicIds: postForm.value.topics.length > 0 ? postForm.value.topics : null,
        title: postForm.value.title,
        content: postForm.value.content || '',
        opFlag: 0
      })], { type: 'application/json' })
      formData.append('content', contentBlob)
      formData.append('mediaType', mediaTypeNum)
      filesToUpload.forEach(f => formData.append('files', f))

      const response = await request(POST_API.CREATE_POST, {
        method: 'POST',
        headers: {},
        body: formData
      })
      const result = await response.json()
      
      if (result.code !== 0 || !result.data?.successFlag) {
        if (result.message && result.message.includes('实名认证')) {
          authTipMessage.value = '您尚未完成实名认证，无法保存草稿。'
          showAuthTipDialog.value = true
        } else {
          ElMessage.error(result.message || '保存失败')
        }
        return
      }
      
      ElMessage.success('草稿已保存')
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingDraft.value = false
  }
}

// 发布帖子
const handlePublish = async () => {
  if (!canPublish.value) return
  
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  publishing.value = true
  try {
    if (isEdit.value) {
      // 编辑模式：调用修改接口
      await handleEditPost()
    } else {
      // 新建模式：调用创建接口
      await handleCreatePost(userId, 1)
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请重试')
  } finally {
    publishing.value = false
  }
}

// 处理编辑帖子
const handleEditPost = async () => {
  const postId = route.query.postId
  
  // 去除空白字符
  const trimmedTitle = postForm.value.title?.trim() || ''
  const trimmedContent = postForm.value.content?.trim() || ''
  
  // 空白字符校验
  if (postForm.value.title && !trimmedTitle) {
    ElMessage.warning('标题不能只包含空白字符')
    return
  }
  if (postForm.value.content && !trimmedContent && activeType.value === 'text') {
    ElMessage.warning('内容不能只包含空白字符')
    return
  }
  
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 构建媒体删除标志
  const originalMediaIds = Object.keys(originalMediaMap.value).map(Number)
  const currentMediaIds = mediaList.value.filter(m => m.mediaId).map(m => m.mediaId)
  const mediaUrlWithDeleteFlag = {}
  originalMediaIds.forEach(mediaId => {
    mediaUrlWithDeleteFlag[mediaId] = currentMediaIds.includes(mediaId) ? 0 : 1
  })

  // 新上传的文件
  const newFiles = mediaList.value.filter(m => m.file).map(m => m.file)
  // 所有原有媒体都被删除且没有新文件时才拦截
  const allDeleted = originalMediaIds.length > 0 &&
    originalMediaIds.every(id => mediaUrlWithDeleteFlag[id] === 1)
  if (allDeleted && newFiles.length === 0) {
    ElMessage.warning('请至少保留或上传一个媒体文件')
    return
  }

  // 图片/视频数量校验
  const mediaTypeNum = activeType.value === 'video' ? 2 : 1
  if (newFiles.length > 0) {
    if (mediaTypeNum === 1 && newFiles.length > 9) {
      ElMessage.warning('图片最多上传9张')
      return
    }
    if (mediaTypeNum === 2 && newFiles.length > 1) {
      ElMessage.warning('视频最多上传1个')
      return
    }
  }

  // 构建 editData
  const editData = {
    userId,
    postId: Number(postId),
    opFlag: 1,
    mediaUrlWithDeleteFlag
  }
  if (postForm.value.columnId !== originalPost.value.columnId) {
    editData.columnId = postForm.value.columnId
  }
  const originalTopicIds = originalPost.value.topicIds || []
  const currentTopicIds = postForm.value.topics || []
  const topicsChanged = originalTopicIds.length !== currentTopicIds.length ||
    !originalTopicIds.every(id => currentTopicIds.includes(id))
  if (topicsChanged) {
    editData.topicIds = currentTopicIds.length > 0 ? currentTopicIds : null
  }
  if (trimmedTitle !== (originalPost.value.title?.trim() || '')) {
    editData.title = trimmedTitle
  }
  if (trimmedContent !== (originalPost.value.content?.trim() || '')) {
    editData.content = trimmedContent
  }

  // 构建 multipart/form-data
  const formData = new FormData()
  const contentBlob = new Blob([JSON.stringify(editData)], { type: 'application/json' })
  formData.append('content', contentBlob)

  if (newFiles.length > 0) {
    formData.append('mediaType', mediaTypeNum)
    newFiles.forEach(f => formData.append('files', f))
  }

  const response = await request(POST_API.EDIT_POST, {
    method: 'POST',
    headers: {},
    body: formData
  })
  const result = await response.json()
  
  if (result.code !== 0) {
    if (result.message && result.message.includes('实名认证')) {
      authTipMessage.value = '您尚未完成实名认证，无法修改帖子。'
      showAuthTipDialog.value = true
    } else {
      ElMessage.error(result.message || '修改失败')
    }
    return
  }
  
  ElMessage.success('修改成功')
  router.push('/forum')
}

// 处理创建帖子
const handleCreatePost = async (userId, opFlag) => {
  // 去除空白字符
  const trimmedTitle = postForm.value.title?.trim() || ''
  const trimmedContent = postForm.value.content?.trim() || ''
  
  // 空白字符校验
  if (postForm.value.title && !trimmedTitle) {
    ElMessage.warning('标题不能只包含空白字符')
    return
  }
  if (postForm.value.content && !trimmedContent && activeType.value === 'text') {
    ElMessage.warning('内容不能只包含空白字符')
    return
  }

  // 收集媒体文件
  let filesToUpload = []
  let mediaTypeNum = 1

  if (activeType.value === 'text') {
    if (textPostCreatorRef.value) {
      const validation = textPostCreatorRef.value.validate()
      if (validation.valid) {
        const imageBlob = await textPostCreatorRef.value.generateImageBlob()
        if (imageBlob) {
          filesToUpload = [new File([imageBlob], 'cover.png', { type: 'image/png' })]
          mediaTypeNum = 1
        }
      }
    }
  } else {
    filesToUpload = mediaList.value.filter(m => m.file).map(m => m.file)
    mediaTypeNum = activeType.value === 'video' ? 2 : 1
  }

  if (filesToUpload.length === 0) {
    ElMessage.warning('请上传媒体文件')
    return
  }

  // 图片最多9张，视频最多1个（前端二次校验）
  if (mediaTypeNum === 1 && filesToUpload.length > 9) {
    ElMessage.warning('图片最多上传9张')
    return
  }
  if (mediaTypeNum === 2 && filesToUpload.length > 1) {
    ElMessage.warning('视频最多上传1个')
    return
  }

  // 构建 multipart/form-data
  const formData = new FormData()
  const contentBlob = new Blob([JSON.stringify({
    userId,
    columnId: postForm.value.columnId,
    topicIds: postForm.value.topics.length > 0 ? postForm.value.topics : null,
    title: trimmedTitle,
    content: trimmedContent,
    opFlag
  })], { type: 'application/json' })
  formData.append('content', contentBlob)
  formData.append('mediaType', mediaTypeNum)
  filesToUpload.forEach(f => formData.append('files', f))

  const response = await request(POST_API.CREATE_POST, {
    method: 'POST',
    headers: {},
    body: formData
  })
  const result = await response.json()

  if (result.code !== 0 || !result.data?.successFlag) {
    if (result.message && result.message.includes('实名认证')) {
      authTipMessage.value = '您尚未完成实名认证，无法发布帖子。'
      showAuthTipDialog.value = true
    } else {
      ElMessage.error(result.message || '发布失败')
    }
    return
  }

  ElMessage.success('发布成功，等待审核')
  router.push('/forum')
}

// 加载帖子数据（编辑模式）
const loadPostData = async () => {
  if (!route.query.postId) return
  
  try {
    // 后端 detail 接口通过请求参数接收 postId
    const formData = new FormData()
    formData.append('postId', route.query.postId)
    
    const response = await request(POST_API.GET_POST_DETAIL, {
      method: 'POST',
      body: formData
    })
    const result = await response.json()
    if (result.code === 0 && result.data) {
      const post = result.data
      
      // 保存原始帖子数据
      originalPost.value = {
        postId: post.postId,
        columnId: post.columnId,
        topicIds: post.topicId || [],
        title: post.title,
        content: post.content
      }
      
      // 填充表单数据
      postForm.value.title = post.title || ''
      postForm.value.content = post.content || ''
      postForm.value.columnId = post.columnId
      postForm.value.topics = post.topicId || [] // 后端返回的是 topicId 数组
      
      // 设置媒体（后端返回 mediaUrlWithId: Map<Long, String>）
      if (post.mediaUrlWithId && Object.keys(post.mediaUrlWithId).length > 0) {
        // 保存原有媒体Map
        originalMediaMap.value = { ...post.mediaUrlWithId }
        
        // 转换为数组格式
        const mediaEntries = Object.entries(post.mediaUrlWithId)
        const firstUrl = mediaEntries[0][1]
        const isVideoUrl = firstUrl.match(/\.(mp4|mov|avi|webm)$/i)
        
        if (isVideoUrl) {
          // 视频模式
          activeType.value = 'video'
          mediaList.value = [{
            mediaId: Number(mediaEntries[0][0]),
            url: firstUrl,
            type: 'video',
            duration: 0
          }]
          // 读取实际视频时长
          const vid = document.createElement('video')
          vid.src = firstUrl
          vid.onloadedmetadata = () => {
            mediaList.value[0].duration = vid.duration
          }
        } else {
          // 图文模式
          activeType.value = 'image'
          mediaList.value = mediaEntries.map(([id, url]) => ({
            mediaId: Number(id),
            url: url,
            type: 'image'
          }))
        }
      } else {
        // 纯文字模式
        activeType.value = 'text'
        originalMediaMap.value = {}
      }
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  }
}

onMounted(async () => {
  // 先加载栏目和话题数据
  await Promise.all([fetchColumns(), fetchTopics()])
  // 再加载帖子数据（编辑模式）
  if (isEdit.value) {
    loadPostData()
  }
})
</script>

<style lang="scss" scoped>
.post-editor-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-base);
  padding: var(--spacing-xl) 0 var(--spacing-4xl);
}

.editor-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
}

// 头部
.editor-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-base);
  transition: all var(--transition-fast);
  
  &:hover {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.1);
  }
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
}

// Tab 切换
.post-type-tabs {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-surface);
  border-radius: var(--radius-xl);
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-lg);
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-base) var(--ease-out);
  
  &:hover:not(.disabled):not(.active) {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.08);
  }
  
  &.active {
    color: var(--color-text-inverse);
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
    box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
    cursor: default;
  }
  
  &.disabled:not(.active) {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 编辑器主体
.editor-body {
  background: var(--color-bg-surface);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-base);

  // 视频模式：左右两栏 + 下方全宽
  &.video-layout {
    display: flex;
    flex-wrap: wrap;
    gap: var(--spacing-xl);
    align-items: stretch;
  }
}

.editor-left {
  flex: 0 0 45%;
  min-width: 0;
  display: flex;
  flex-direction: column;

  // 视频模式下上传区撑满左栏高度
  .media-upload-section {
    flex: 1;
    display: flex;
    flex-direction: column;

    .upload-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
  }
}

.editor-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);

  .topic-section,
  .column-section {
    margin-bottom: 0;
  }

  .topic-select,
  .column-select {
    width: 100%;

    :deep(.el-select__wrapper) {
      width: 100%;
    }
  }
}

// 非视频模式下也确保撑满
.topic-select,
.column-select {
  width: 100%;

  :deep(.el-select__wrapper) {
    width: 100%;
    box-sizing: border-box;
  }
}

// 视频模式下换行到两栏下方的全宽行
.video-full-row {
  width: 100%;
  flex-shrink: 0;
}

// 视频单图预览（左栏内）
.video-single-preview {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.video-replace-hint {
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-sm);
}

// 纯文字模式
.text-mode-section {
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--color-border-light);
  
  // 撑满容器宽度
  margin-left: calc(-1 * var(--spacing-xl));
  margin-right: calc(-1 * var(--spacing-xl));
  padding-left: var(--spacing-xl);
  padding-right: var(--spacing-xl);
}

// 媒体上传区
.media-upload-section {
  margin-bottom: var(--spacing-xl);
}

.upload-area {
  position: relative;
  border: 2px dashed var(--color-border-base);
  border-radius: var(--radius-lg);
  padding: var(--spacing-2xl);
  cursor: pointer;
  transition: all var(--transition-base);
  background: var(--color-bg-base);
  
  &:hover, &.drag-over {
    border-color: var(--color-primary);
    background: rgba(255, 138, 91, 0.05);
  }
  
  &.has-files {
    padding: var(--spacing-lg);
    border-style: solid;
    border-color: var(--color-border-light);
    cursor: default;
  }
}

// 透明 input 覆盖整个上传区，直接由用户点击触发
.upload-input-overlay {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 1;
}

.upload-placeholder {
  text-align: center;
  
  .upload-icon {
    color: var(--color-primary-light);
    margin-bottom: var(--spacing-md);
  }
  
  .upload-text {
    font-size: var(--font-size-md);
    color: var(--color-text-secondary);
    margin: 0 0 var(--spacing-sm);
  }
  
  .upload-hint {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
    margin: 0;
  }
}

.media-preview-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-sm);
}

.media-preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  
  .preview-media {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  // hover 预览提示遮罩
  .image-hover-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    background: rgba(0, 0, 0, 0.45);
    color: #fff;
    opacity: 0;
    transition: opacity var(--transition-fast);
    pointer-events: none;

    span {
      font-size: var(--font-size-xs);
    }
  }
  
  .media-delete {
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
    cursor: pointer;
    opacity: 0;
    transition: opacity var(--transition-fast);
    z-index: 1;
    
    &:hover {
      background: var(--color-danger);
    }
  }
  
  &:hover .image-hover-overlay,
  &:hover .media-delete {
    opacity: 1;
  }
  
  .video-duration {
    position: absolute;
    bottom: var(--spacing-xs);
    right: var(--spacing-xs);
    padding: 2px 6px;
    background: rgba(0, 0, 0, 0.6);
    border-radius: var(--radius-xs);
    color: #fff;
    font-size: var(--font-size-xs);
  }
}

.add-more-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  aspect-ratio: 1;
  border: 2px dashed var(--color-border-base);
  border-radius: var(--radius-md);
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
  
  span {
    font-size: var(--font-size-xs);
  }
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.05);
  }
}

// 标题输入
.title-input-section {
  position: relative;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
}

.title-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  background: transparent;
  padding: 0;
  
  &::placeholder {
    color: var(--color-text-placeholder);
    font-weight: var(--font-weight-normal);
  }
}

.title-count {
  position: absolute;
  right: 0;
  bottom: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

// 正文输入
.content-input-section {
  position: relative;
  margin-bottom: var(--spacing-xl);
}

// 话题选择
.topic-section, .column-section {
  margin-bottom: var(--spacing-xl);
}

.section-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  
  .label-hint {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-normal);
    color: var(--color-text-tertiary);
  }
}

.topic-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  
  .topic-name {
    color: var(--color-text-primary);
  }
  
  .topic-count {
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }
}

.selected-topics {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  
  .el-tag {
    border-radius: var(--radius-lg);
  }
}

// 底部操作栏
.editor-footer {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border-light);
}

.draft-btn {
  flex: 1;
  height: 48px;
  font-size: var(--font-size-base);
  border-radius: var(--radius-lg);
  background: #fff;
  border: 1px solid var(--color-border-base);
  color: var(--color-text-secondary);
  
  &:hover, &:focus {
    border-color: var(--color-primary);
    color: var(--color-primary);
    background: #fff;
  }
}

.publish-btn {
  flex: 2;
  height: 48px;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  
  &:hover:not(:disabled) {
    background: linear-gradient(135deg, var(--color-primary-dark) 0%, var(--color-primary-darker) 100%);
  }
  
  &:disabled {
    background: var(--color-border-base) !important;
    color: var(--color-text-tertiary) !important;
    cursor: not-allowed;
    
    &:hover {
      background: var(--color-border-base) !important;
      color: var(--color-text-tertiary) !important;
    }
  }
}

// 响应式
@media (max-width: 640px) {
  .editor-container {
    padding: 0 var(--spacing-base);
  }
  
  .post-type-tabs {
    padding: var(--spacing-sm);
  }
  
  .tab-item {
    padding: var(--spacing-sm) var(--spacing-md);
    font-size: var(--font-size-sm);
    
    .el-icon {
      display: none;
    }
  }
  
  .media-preview-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-xs);
  }
  
  .editor-footer {
    flex-direction: column;
    
    .draft-btn, .publish-btn {
      flex: none;
      width: 100%;
    }
  }
}

// 实名认证提示弹窗样式
.auth-tip-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-tip-modal {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;
    
    .modal-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
      
      .el-icon {
        color: #e6a23c;
      }
    }
    
    .modal-close {
      width: 32px;
      height: 32px;
      border: none;
      background: #f5f5f5;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      &:hover {
        background: #eee;
        transform: rotate(90deg);
      }
      
      .el-icon {
        font-size: 16px;
        color: #666;
      }
    }
  }
  
  .modal-body {
    padding: 24px;
  }
  
  .auth-tip-content {
    text-align: center;
    
    p {
      margin: 0 0 12px;
      font-size: 15px;
      color: #333;
      line-height: 1.6;
    }
    
    .auth-tip-hint {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    
    .btn-cancel,
    .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
    }
  }
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.auth-tip-modal-enter-active,
.auth-tip-modal-leave-active {
  transition: all 0.3s ease;
}

.auth-tip-modal-enter-from,
.auth-tip-modal-leave-to {
  opacity: 0;
}

.auth-tip-modal-enter-from .auth-tip-modal,
.auth-tip-modal-leave-to .auth-tip-modal {
  transform: scale(0.9);
  opacity: 0;
}

// 视频播放覆盖层（预览区）
.video-play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  cursor: pointer;
  transition: background var(--transition-fast);
  color: #fff;

  &:hover {
    background: rgba(0, 0, 0, 0.5);
  }
}

// upload-area 禁止点击态
.upload-area.no-click {
  cursor: default;

  &:hover {
    border-color: var(--color-border-light);
    background: var(--color-bg-base);
  }
}

// 视频预览弹窗
.video-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.video-preview-container {
  position: relative;
  max-width: min(90vw, 960px);
  max-height: min(85vh, 720px);
  min-width: 480px;
  min-height: 270px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-preview-close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.55);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 10000;

  &:hover {
    background: rgba(220, 50, 50, 0.8);
    transform: scale(1.1);
  }

  .el-icon {
    font-size: 18px;
  }
}

.video-preview-player {
  width: 100%;
  height: 100%;
  min-width: 480px;
  min-height: 270px;
  max-width: min(90vw, 960px);
  max-height: min(85vh, 720px);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.video-preview-fade-enter-active,
.video-preview-fade-leave-active {
  transition: opacity 0.25s ease;
}

.video-preview-fade-enter-from,
.video-preview-fade-leave-to {
  opacity: 0;
}
</style>
