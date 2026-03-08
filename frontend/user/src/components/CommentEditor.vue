<script setup>
import { ref, computed, watch } from 'vue'
import { Picture, VideoCamera, Close, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import CustomVideoPlayer from './CustomVideoPlayer.vue'

const props = defineProps({
  // 评论内容 v-model
  modelValue: {
    type: String,
    default: ''
  },
  // 模式：'product' 商品评价 | 'reply' 回复评论 | 'post' 贴吧评论
  mode: {
    type: String,
    default: 'product',
    validator: (val) => ['product', 'reply', 'post'].includes(val)
  },
  // 回复目标用户名（mode='reply'时使用）
  replyTo: {
    type: String,
    default: ''
  },
  // 最大字数
  maxLength: {
    type: Number,
    default: 2048
  },
  // 最大文件总数（图片+视频合计）
  maxFiles: {
    type: Number,
    default: 9
  },
  // 是否显示评分（仅商品评价模式）
  showRating: {
    type: Boolean,
    default: true
  },
  // 是否正在提交
  loading: {
    type: Boolean,
    default: false
  },
  // 占位符
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel'])

// 内部状态
const content = ref(props.modelValue)
const rating = ref(5)
const isFocused = ref(false)
const fileList = ref([]) // { id, type: 'image'|'video', file, url, progress, status }

// 文件上传 input ref
const imageInputRef = ref(null)
const videoInputRef = ref(null)
const ratingWrapperRef = ref(null)

// 预览状态
const previewVisible = ref(false)
const videoPreviewMouseDown = ref(false)
const previewType = ref('') // 'image' | 'video'
const previewUrl = ref('')
const previewIndex = ref(0)
const videoPreviewRef = ref(null)

// 同步 v-model
watch(() => props.modelValue, (val) => {
  content.value = val
})

watch(content, (val) => {
  emit('update:modelValue', val)
})

// 计算属性
const computedPlaceholder = computed(() => {
  if (props.placeholder) return props.placeholder
  if (props.mode === 'reply' && props.replyTo) {
    return `回复 @${props.replyTo}: （发布的评论会移交审核，审核通过后才会在列表中显示，未通过审核的评论会以站内通知的方式进行通知）`
  }
  return '分享你的看法，或者晒出萌宠的美照/视频...（发布的评论会移交审核，审核通过后才会在列表中显示，未通过审核的评论会以站内通知的方式进行通知）'
})

const showRatingSection = computed(() => {
  return props.mode === 'product' && props.showRating
})

const imageCount = computed(() => {
  return fileList.value.filter(f => f.type === 'image').length
})

const videoCount = computed(() => {
  return fileList.value.filter(f => f.type === 'video').length
})

const hasVideo = computed(() => {
  return videoCount.value > 0
})

const canAddImage = computed(() => {
  return fileList.value.length < props.maxFiles
})

const canAddVideo = computed(() => {
  return fileList.value.length < props.maxFiles
})

const canSubmit = computed(() => {
  return content.value.trim().length > 0 && !props.loading
})

// 评分颜色：1星红色，2-3星黄色，4-5星绿色
const ratingColors = computed(() => {
  // colors 数组对应 [低分, 中分, 高分] 的颜色
  return ['#F56C6C', '#E6A23C', '#2F8F6A']
})

// 悬停时的评分值
const hoverRating = ref(0)

// 当前显示的评分值（悬停时显示悬停值，否则显示实际值）
const displayRating = computed(() => {
  return hoverRating.value > 0 ? hoverRating.value : rating.value
})

// 评分文字颜色（根据显示的评分动态变化）
const ratingTextColor = computed(() => {
  const val = displayRating.value
  if (val <= 1) return '#F56C6C' // 红色
  if (val <= 3) return '#E6A23C' // 黄色
  return '#2F8F6A' // 绿色
})

// 评分文字
const ratingText = computed(() => {
  const texts = ['很差', '较差', '一般', '满意', '非常满意']
  return texts[displayRating.value - 1] || ''
})

// 处理评分区域鼠标移动，计算悬停的星级
const handleRatingMouseMove = (event) => {
  if (!ratingWrapperRef.value) return
  const rect = ratingWrapperRef.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const starWidth = rect.width / 5 // 5颗星
  const hoveredStar = Math.ceil(x / starWidth)
  hoverRating.value = Math.max(1, Math.min(5, hoveredStar))
}

// 处理鼠标离开评分区域
const handleRatingLeave = () => {
  hoverRating.value = 0
}

// 方法
const handleFocus = () => {
  isFocused.value = true
}

const handleBlur = () => {
  isFocused.value = false
}

// 触发图片选择
const triggerImageUpload = () => {
  if (!canAddImage.value) {
    ElMessage.warning(`最多上传${props.maxFiles}个文件`)
    return
  }
  imageInputRef.value?.click()
}

// 触发视频选择
const triggerVideoUpload = () => {
  if (!canAddVideo.value) {
    ElMessage.warning(`最多上传${props.maxFiles}个文件`)
    return
  }
  videoInputRef.value?.click()
}

// 文件指纹：name + size + lastModified
const fileKey = (file) => `${file.name}_${file.size}_${file.lastModified}`

// 处理图片选择
const handleImageSelect = (event) => {
  const files = Array.from(event.target.files)
  const existingKeys = new Set(fileList.value.map(f => fileKey(f.file)))
  const remainingSlots = props.maxFiles - fileList.value.length
  let skippedDup = 0
  let added = 0

  for (const file of files) {
    if (added >= remainingSlots) break
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('请选择图片文件')
      continue
    }
    if (existingKeys.has(fileKey(file))) {
      skippedDup++
      continue
    }
    const id = Date.now() + Math.random()
    const url = URL.createObjectURL(file)
    fileList.value.push({ id, type: 'image', file, url, progress: 0, status: 'ready' })
    existingKeys.add(fileKey(file))
    added++
  }

  if (skippedDup > 0) ElMessage.warning(`存在相同的文件在上传队列中`)
  if (files.length - skippedDup > remainingSlots) ElMessage.warning(`最多上传${props.maxFiles}个文件，已自动截取`)

  event.target.value = ''
}

// 处理视频选择
const handleVideoSelect = (event) => {
  const files = Array.from(event.target.files)
  const existingKeys = new Set(fileList.value.map(f => fileKey(f.file)))
  const remainingSlots = props.maxFiles - fileList.value.length
  let skippedDup = 0
  let added = 0

  for (const file of files) {
    if (added >= remainingSlots) break
    if (!file.type.startsWith('video/')) {
      ElMessage.warning('请选择视频文件')
      continue
    }
    if (file.size > 100 * 1024 * 1024) {
      ElMessage.warning(`视频 ${file.name} 超过100MB，已跳过`)
      continue
    }
    if (existingKeys.has(fileKey(file))) {
      skippedDup++
      continue
    }
    const id = Date.now() + Math.random()
    const url = URL.createObjectURL(file)
    fileList.value.push({ id, type: 'video', file, url, progress: 0, status: 'ready' })
    existingKeys.add(fileKey(file))
    added++
  }

  if (skippedDup > 0) ElMessage.warning(`存在相同的文件在上传队列中`)
  if (files.length - skippedDup > remainingSlots) ElMessage.warning(`最多上传${props.maxFiles}个文件，已自动截取`)

  event.target.value = ''
}

// 删除文件
const removeFile = (id) => {
  const index = fileList.value.findIndex(f => f.id === id)
  if (index > -1) {
    // 释放 URL
    URL.revokeObjectURL(fileList.value[index].url)
    fileList.value.splice(index, 1)
  }
}

// 预览图片列表
const imagePreviewList = computed(() => {
  return fileList.value.filter(f => f.type === 'image').map(f => f.url)
})

// 视频预览尺寸
const videoPreviewSize = ref({ width: 0, height: 0 })
const videoPreviewStyle = computed(() => {
  if (!videoPreviewSize.value.width || !videoPreviewSize.value.height) {
    return {}
  }
  const maxWidth = window.innerWidth * 0.9
  const maxHeight = window.innerHeight * 0.8
  const minWidth = 480 // 最小宽度，确保控制栏有足够空间
  const videoRatio = videoPreviewSize.value.width / videoPreviewSize.value.height
  
  let width, height
  if (videoPreviewSize.value.width > maxWidth || videoPreviewSize.value.height > maxHeight) {
    const widthByMaxWidth = maxWidth
    const heightByMaxWidth = maxWidth / videoRatio
    const widthByMaxHeight = maxHeight * videoRatio
    const heightByMaxHeight = maxHeight
    
    if (heightByMaxWidth <= maxHeight) {
      width = widthByMaxWidth
      height = heightByMaxWidth
    } else {
      width = widthByMaxHeight
      height = heightByMaxHeight
    }
  } else {
    width = videoPreviewSize.value.width
    height = videoPreviewSize.value.height
  }
  
  // 确保最小宽度
  if (width < minWidth) {
    width = minWidth
  }
  
  return {
    width: `${width}px`,
    height: `${height}px`
  }
})

// 处理视频元数据加载
const handleVideoMetadata = (data) => {
  videoPreviewSize.value = { width: data.width, height: data.height }
}

// 打开预览
const openPreview = (item) => {
  previewType.value = item.type
  previewUrl.value = item.url
  if (item.type === 'image') {
    // 计算在图片列表中的索引
    const imageFiles = fileList.value.filter(f => f.type === 'image')
    previewIndex.value = imageFiles.findIndex(f => f.id === item.id)
  }
  if (item.type === 'video') {
    videoPreviewSize.value = { width: 0, height: 0 }
  }
  previewVisible.value = true
  
  // 视频自动播放
  if (item.type === 'video') {
    setTimeout(() => {
      videoPreviewRef.value?.play()
    }, 100)
  }
}

// 关闭预览
const closePreview = () => {
  if (videoPreviewRef.value) {
    videoPreviewRef.value.pause()
  }
  previewVisible.value = false
  previewUrl.value = ''
  previewType.value = ''
  videoPreviewSize.value = { width: 0, height: 0 }
}

// 提交评论
const handleSubmit = () => {
  if (!canSubmit.value) return
  
  // 去除空白字符
  const trimmedContent = content.value?.trim() || ''
  
  // 空白字符校验
  if (content.value && !trimmedContent) {
    ElMessage.warning('评论内容不能只包含空白字符')
    return
  }
  
  emit('submit', {
    content: trimmedContent,
    rating: showRatingSection.value ? rating.value : null,
    files: fileList.value.map(f => ({
      type: f.type,
      file: f.file
    }))
  })
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 清空表单
const reset = () => {
  content.value = ''
  rating.value = 5
  fileList.value.forEach(f => URL.revokeObjectURL(f.url))
  fileList.value = []
}

// 暴露方法给父组件
defineExpose({
  reset,
  getFiles: () => fileList.value
})
</script>

<template>
  <div 
    class="comment-editor"
    :class="{ 'is-focused': isFocused }"
  >
    <!-- 评分区域 -->
    <div v-if="showRatingSection" class="rating-section">
      <span class="rating-label">给它打个分吧：</span>
      <div 
        ref="ratingWrapperRef"
        class="rating-wrapper" 
        @mousemove="handleRatingMouseMove"
        @mouseleave="handleRatingLeave"
      >
        <el-rate
          v-model="rating"
          :colors="ratingColors"
          :low-threshold="1"
          :high-threshold="4"
        />
      </div>
      <span class="rating-text" :style="{ color: ratingTextColor }">{{ ratingText }}</span>
    </div>

    <!-- 文本输入区 -->
    <div class="input-section">
      <el-input
        v-model="content"
        type="textarea"
        :placeholder="computedPlaceholder"
        :maxlength="maxLength"
        :autosize="{ minRows: 3, maxRows: 8 }"
        resize="none"
        class="content-input"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <div class="char-count">
        <span :class="{ 'is-warning': content.length > maxLength * 0.9 }">
          {{ content.length }}
        </span>
        <span>/{{ maxLength }}</span>
      </div>
    </div>

    <!-- 媒体预览区 -->
    <div v-if="fileList.length > 0" class="media-preview">
      <div 
        v-for="(item, index) in fileList" 
        :key="item.id"
        class="preview-item"
        :class="item.type"
        @click="openPreview(item)"
      >
        <!-- 图片预览 -->
        <template v-if="item.type === 'image'">
          <img :src="item.url" alt="预览图" />
          <div class="preview-mask">
            <span>点击预览</span>
          </div>
        </template>
        
        <!-- 视频预览 -->
        <template v-else>
          <video :src="item.url" muted />
          <div class="video-overlay">
            <el-icon :size="24"><VideoPlay /></el-icon>
            <span class="play-text">点击播放</span>
          </div>
        </template>
        
        <!-- 上传进度 -->
        <div v-if="item.status === 'uploading'" class="upload-progress">
          <el-progress
            type="circle"
            :percentage="item.progress"
            :width="40"
            :stroke-width="3"
          />
        </div>
        
        <!-- 删除按钮 -->
        <button class="remove-btn" @click.stop="removeFile(item.id)">
          <el-icon :size="14"><Close /></el-icon>
        </button>
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <el-image-viewer
      v-if="previewVisible && previewType === 'image'"
      :url-list="imagePreviewList"
      :initial-index="previewIndex"
      @close="closePreview"
    />

    <!-- 视频预览弹窗 -->
    <Teleport to="body">
      <Transition name="video-preview">
        <div v-if="previewVisible && previewType === 'video'" class="video-preview-overlay" @mousedown.self="videoPreviewMouseDown = true" @mouseup.self="videoPreviewMouseDown && closePreview(); videoPreviewMouseDown = false">
          <div 
            class="video-preview-content" 
            :style="videoPreviewStyle"
            @mousedown="videoPreviewMouseDown = false"
          >
            <CustomVideoPlayer
              ref="videoPreviewRef"
              :src="previewUrl"
              class="video-player-custom"
              @loadedmetadata="handleVideoMetadata"
            />
            <button class="close-btn" @click="closePreview">
              <el-icon :size="24"><Close /></el-icon>
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="upload-actions">
        <button 
          class="upload-btn"
          :class="{ disabled: !canAddImage }"
          @click="triggerImageUpload"
        >
          <el-icon :size="18"><Picture /></el-icon>
          <span>图片</span>
        </button>
        
        <button 
          class="upload-btn"
          :class="{ disabled: !canAddVideo }"
          @click="triggerVideoUpload"
        >
          <el-icon :size="18"><VideoCamera /></el-icon>
          <span>视频</span>
        </button>

        <span v-if="fileList.length > 0" class="total-count">{{ fileList.length }}/{{ maxFiles }}</span>
      </div>
      
      <div class="submit-actions">
        <el-button 
          v-if="mode === 'reply'"
          class="cancel-btn"
          @click="handleCancel"
        >
          取消
        </el-button>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ loading ? '发布中...' : '发布' }}
        </el-button>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleImageSelect"
    />
    <input
      ref="videoInputRef"
      type="file"
      accept="video/*"
      multiple
      style="display: none"
      @change="handleVideoSelect"
    />
  </div>
</template>

<style scoped lang="scss">
.comment-editor {
  background: #FFFFFF;
  border: 1px solid #EDE1D9;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;

  &.is-focused {
    border-color: #FF8A5B;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
}

// 评分区域
.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F5F0ED;

  .rating-label {
    font-size: 14px;
    color: #666;
  }

  .rating-wrapper {
    display: inline-flex;
    align-items: center;
  }

  :deep(.el-rate) {
    .el-rate__icon {
      font-size: 20px;
    }
  }

  .rating-text {
    font-size: 13px;
    margin-left: 8px;
    transition: color 0.2s ease;
  }
}

// 输入区域
.input-section {
  position: relative;

  .content-input {
    :deep(.el-textarea__inner) {
      border: none;
      background: transparent;
      padding: 0;
      font-size: 14px;
      line-height: 1.6;
      color: #333;
      resize: none;
      box-shadow: none !important;

      &::placeholder {
        color: #999;
      }

      &:focus {
        box-shadow: none !important;
      }
    }
  }

  .char-count {
    position: absolute;
    right: 0;
    bottom: -20px;
    font-size: 12px;
    color: #8F8F8F;

    .is-warning {
      color: #FF8A5B;
    }
  }
}

// 媒体预览区
.media-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 24px;
  margin-bottom: 12px;

  .preview-item {
    position: relative;
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f5f5;
    cursor: pointer;

    img, video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    // 图片预览遮罩
    .preview-mask {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 12px;
      opacity: 0;
      transition: opacity 0.2s ease;
    }

    &:hover .preview-mask {
      opacity: 1;
    }

    .video-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.4);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      gap: 4px;
      transition: background 0.2s ease;

      .play-text {
        font-size: 10px;
        opacity: 0.9;
      }
    }

    &:hover .video-overlay {
      background: rgba(0, 0, 0, 0.6);
    }

    .upload-progress {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .remove-btn {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      border-radius: 50%;
      background: rgba(0, 0, 0, 0.6);
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      transition: all 0.2s ease;
      z-index: 10;

      &:hover {
        background: #ff4d4f;
        transform: scale(1.1);
      }
    }
  }
}

// 视频预览弹窗
.video-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2100;
}

.video-preview-content {
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  // 默认尺寸，加载后会被动态样式覆盖
  width: auto;
  max-width: 90vw;
  max-height: 80vh;

  .video-player-custom {
    width: 100%;
    height: 100%;
    display: block;
    outline: none;
  }

  .close-btn {
    position: absolute;
    top: -40px;
    right: 0;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }
  }
}

// 视频预览动画
.video-preview-enter-active,
.video-preview-leave-active {
  transition: opacity 0.3s ease;
}

.video-preview-enter-from,
.video-preview-leave-to {
  opacity: 0;
}

// 工具栏
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #F5F0ED;

  .upload-actions {
    display: flex;
    gap: 16px;
    align-items: center;
  }

  .total-count {
    font-size: 12px;
    color: #999;
  }

  .upload-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border: none;
    background: transparent;
    color: #666;
    font-size: 14px;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover:not(.disabled) {
      color: #FF8A5B;
      background: rgba(255, 138, 91, 0.08);
    }

    &.disabled {
      color: #ccc;
      cursor: not-allowed;
    }

    .count {
      font-size: 12px;
      color: #999;
    }
  }

  .submit-actions {
    display: flex;
    gap: 8px;
  }

  .cancel-btn {
    background: transparent;
    border: 1px solid #EDE1D9;
    border-radius: 20px;
    padding: 8px 20px;
    color: #666;
    font-weight: 500;

    &:hover {
      color: #FF8A5B;
      border-color: #FF8A5B;
      background: rgba(255, 138, 91, 0.05);
    }
  }

  .submit-btn {
    background: #FF8A5B;
    border-color: #FF8A5B;
    border-radius: 20px;
    padding: 8px 24px;
    font-weight: 500;

    &:hover:not(:disabled) {
      background: #ff7043;
      border-color: #ff7043;
    }

    &:disabled {
      background: #ffcbb8;
      border-color: #ffcbb8;
    }
  }
}
</style>
