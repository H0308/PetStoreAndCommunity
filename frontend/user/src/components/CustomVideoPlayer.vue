<template>
  <div ref="playerRef" class="custom-video-player" @mouseenter="showControls = true" @mouseleave="videoPlaying && (showControls = false)">
    <video 
      ref="videoRef"
      :src="src"
      :poster="poster"
      preload="metadata"
      class="video-element"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoadedMetadata"
      @ended="handleVideoEnded"
      @click="togglePlay"
    />
    
    <!-- 视频控制栏 -->
    <div class="video-controls" :class="{ visible: showControls }">
      <!-- 进度条 -->
      <div 
        class="progress-bar" 
        :class="{ dragging: isDragging }"
        @mousedown="startDrag"
        @mousemove="handleDragMove"
        @mouseleave="hidePreview"
      >
        <div class="progress-track">
          <div class="progress-fill" :style="{ width: videoProgress + '%' }"></div>
          <div class="progress-thumb" :style="{ left: videoProgress + '%' }"></div>
        </div>
        <!-- 时间预览提示 -->
        <div 
          v-if="showTimePreview" 
          class="time-preview"
          :style="{ left: previewPosition + '%' }"
        >
          {{ formatTime(previewTime) }}
        </div>
      </div>
      
      <!-- 控制按钮 -->
      <div class="controls-row">
        <div class="controls-left">
          <button class="control-btn play-btn" @click.stop="togglePlay">
            <svg v-if="!videoPlaying" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M8 5v14l11-7z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
            </svg>
          </button>
          <span class="time-display">{{ formatTime(videoCurrentTime) }}/{{ formatTime(videoDuration) }}</span>
        </div>
        <div class="controls-right">
          <!-- 倍速 -->
          <div class="speed-control" @click.stop="toggleSpeedMenu">
            <span>倍速</span>
            <div v-if="showSpeedMenuVisible" class="speed-menu">
              <div 
                v-for="speed in playbackSpeeds" 
                :key="speed" 
                class="speed-option"
                :class="{ active: videoSpeed === speed }"
                @click.stop="setPlaybackSpeed(speed)"
              >{{ speed }}x</div>
            </div>
          </div>
          <!-- 音量 -->
          <div class="volume-control">
            <button class="control-btn volume-btn" @click.stop="toggleMute">
              <!-- 静音状态 -->
              <svg v-if="videoMuted || displayVolume === 0" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M3 9v6h4l5 5V4L7 9H3z"/>
                <line x1="15" y1="9" x2="21" y2="15" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                <line x1="21" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
              </svg>
              <!-- 低音量 (0-50%) -->
              <svg v-else-if="displayVolume <= 0.5" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M3 9v6h4l5 5V4L7 9H3z"/>
                <path d="M14 12c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z" transform="translate(2.5, 0)"/>
              </svg>
              <!-- 高音量 (50-100%) -->
              <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                <path d="M3 9v6h4l5 5V4L7 9H3z"/>
                <path d="M14 12c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02z" transform="translate(2.5, 0)"/>
                <path d="M14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
              </svg>
            </button>
            <!-- 音量滑块 -->
            <div class="volume-slider-wrapper">
              <div class="volume-slider" @mousedown.stop="startVolumeChange">
                <div class="volume-track">
                  <div class="volume-fill" :style="{ height: displayVolume * 100 + '%' }"></div>
                  <div class="volume-thumb" :style="{ bottom: displayVolume * 100 + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
          <!-- 画中画 -->
          <button class="control-btn pip-btn" @click.stop="togglePiP" title="画中画">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <rect x="11" y="11" width="9" height="9" rx="1" ry="1" fill="currentColor" stroke="none"/>
            </svg>
          </button>
          <!-- 全屏 -->
          <button class="control-btn fullscreen-btn" @click.stop="toggleFullscreen">
            <!-- 进入全屏图标 -->
            <svg v-if="!isFullscreen" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M7 4H4v3"/>
              <path d="M17 4h3v3"/>
              <path d="M7 20H4v-3"/>
              <path d="M17 20h3v-3"/>
            </svg>
            <!-- 退出全屏图标 -->
            <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 7h3V4"/>
              <path d="M20 7h-3V4"/>
              <path d="M4 17h3v3"/>
              <path d="M20 17h-3v3"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 大播放按钮（暂停时显示，非结束状态） -->
    <div v-if="!videoPlaying && !videoEnded" class="big-play-btn" @click="togglePlay">
      <svg viewBox="0 0 24 24" width="64" height="64" fill="currentColor">
        <path d="M8 5v14l11-7z"/>
      </svg>
    </div>
    
    <!-- 重播按钮（视频结束时显示） -->
    <div v-if="videoEnded" class="replay-btn" @click="replay">
      <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
        <path d="M3 3v5h5"/>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  poster: {
    type: String,
    default: ''
  },
  autoplay: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['play', 'pause', 'ended', 'loadedmetadata'])

// 元素引用
const playerRef = ref(null)
const videoRef = ref(null)

// 播放状态
const videoPlaying = ref(false)
const videoEnded = ref(false)
const isFullscreen = ref(false)
const videoProgress = ref(0)
const videoCurrentTime = ref(0)
const videoDuration = ref(0)
const videoMuted = ref(false)
const videoVolume = ref(1)
const videoSpeed = ref(1)
const showControls = ref(true)
const showSpeedMenuVisible = ref(false)
const playbackSpeeds = [0.5, 0.75, 1, 1.25, 1.5, 2]

// 进度条拖动相关
const isDragging = ref(false)
const showTimePreview = ref(false)
const previewTime = ref(0)
const previewPosition = ref(0)

// 进度更新动画帧
let progressAnimationFrame = null

// 计算显示音量
const displayVolume = computed(() => {
  if (videoMuted.value) return 0
  return videoVolume.value
})

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 播放/暂停
const togglePlay = () => {
  const video = videoRef.value
  if (!video) return
  
  if (video.paused) {
    // 如果视频已结束，从头开始播放
    if (videoEnded.value) {
      video.currentTime = 0
      videoEnded.value = false
      videoProgress.value = 0
      videoCurrentTime.value = 0
    }
    video.play()
    videoPlaying.value = true
    startProgressLoop()
    emit('play')
  } else {
    video.pause()
    videoPlaying.value = false
    stopProgressLoop()
    emit('pause')
  }
}

// 开始进度更新循环
const startProgressLoop = () => {
  const updateProgress = () => {
    const video = videoRef.value
    if (!video || !video.duration || video.paused) {
      progressAnimationFrame = null
      return
    }
    
    videoCurrentTime.value = video.currentTime
    videoProgress.value = (video.currentTime / video.duration) * 100
    progressAnimationFrame = requestAnimationFrame(updateProgress)
  }
  
  if (!progressAnimationFrame) {
    progressAnimationFrame = requestAnimationFrame(updateProgress)
  }
}

// 停止进度更新循环
const stopProgressLoop = () => {
  if (progressAnimationFrame) {
    cancelAnimationFrame(progressAnimationFrame)
    progressAnimationFrame = null
  }
}

// 时间更新
const handleTimeUpdate = () => {
  const video = videoRef.value
  if (!video || !video.duration) return
  
  if (video.paused) {
    videoCurrentTime.value = video.currentTime
    videoProgress.value = (video.currentTime / video.duration) * 100
  }
}

// 加载元数据
const handleLoadedMetadata = () => {
  const video = videoRef.value
  if (!video) return
  videoDuration.value = video.duration
  emit('loadedmetadata', {
    width: video.videoWidth,
    height: video.videoHeight,
    duration: video.duration
  })
  // autoplay：元数据就绪后尝试播放（处理视频加载慢的情况）
  if (props.autoplay && video.paused && !videoEnded.value) {
    video.play().then(() => {
      videoPlaying.value = true
      startProgressLoop()
    }).catch(() => {})
  }
}

// 视频结束
const handleVideoEnded = () => {
  stopProgressLoop()
  videoPlaying.value = false
  videoEnded.value = true
  videoProgress.value = 100
  emit('ended')
}

// 重播
const replay = () => {
  const video = videoRef.value
  if (!video) return
  
  video.currentTime = 0
  videoEnded.value = false
  videoProgress.value = 0
  videoCurrentTime.value = 0
  video.play()
  videoPlaying.value = true
  startProgressLoop()
  emit('play')
}

// 开始拖动进度条
const startDrag = (e) => {
  const video = videoRef.value
  if (!video || !video.duration) return
  
  e.preventDefault()
  isDragging.value = true
  showTimePreview.value = true
  
  const progressBar = e.currentTarget
  const rect = progressBar.getBoundingClientRect()
  
  // 立即更新到点击位置
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  video.currentTime = percent * video.duration
  videoProgress.value = percent * 100
  videoCurrentTime.value = video.currentTime
  previewPosition.value = percent * 100
  previewTime.value = video.currentTime
  
  const onMouseMove = (moveEvent) => {
    const movePercent = Math.max(0, Math.min(1, (moveEvent.clientX - rect.left) / rect.width))
    video.currentTime = movePercent * video.duration
    videoProgress.value = movePercent * 100
    videoCurrentTime.value = video.currentTime
    previewPosition.value = movePercent * 100
    previewTime.value = video.currentTime
  }
  
  const onMouseUp = () => {
    isDragging.value = false
    showTimePreview.value = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 鼠标移动时显示预览
const handleDragMove = (e) => {
  if (isDragging.value) return
  
  const video = videoRef.value
  if (!video || !video.duration) return
  
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  previewPosition.value = percent * 100
  previewTime.value = percent * video.duration
  showTimePreview.value = true
}

// 隐藏预览
const hidePreview = () => {
  if (!isDragging.value) {
    showTimePreview.value = false
  }
}

// 静音切换
const toggleMute = () => {
  const video = videoRef.value
  if (!video) return
  
  video.muted = !video.muted
  videoMuted.value = video.muted
}

// 开始调节音量
const startVolumeChange = (e) => {
  const video = videoRef.value
  if (!video) return
  
  e.preventDefault()
  const slider = e.currentTarget
  const rect = slider.getBoundingClientRect()
  
  const updateVolume = (clientY) => {
    const percent = Math.max(0, Math.min(1, (rect.bottom - clientY) / rect.height))
    video.volume = percent
    videoVolume.value = percent
    if (percent > 0) {
      video.muted = false
      videoMuted.value = false
    }
  }
  
  updateVolume(e.clientY)
  
  const onMouseMove = (moveEvent) => {
    updateVolume(moveEvent.clientY)
  }
  
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 倍速菜单
const toggleSpeedMenu = () => {
  showSpeedMenuVisible.value = !showSpeedMenuVisible.value
}

// 设置播放速度
const setPlaybackSpeed = (speed) => {
  const video = videoRef.value
  if (!video) return
  
  video.playbackRate = speed
  videoSpeed.value = speed
  showSpeedMenuVisible.value = false
}

// 画中画
const togglePiP = async () => {
  const video = videoRef.value
  if (!video) return
  
  try {
    if (document.pictureInPictureElement) {
      await document.exitPictureInPicture()
    } else {
      await video.requestPictureInPicture()
    }
  } catch (err) {
    console.error('画中画失败:', err)
  }
}

// 全屏
const toggleFullscreen = () => {
  const player = playerRef.value
  if (!player) return
  
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    player.requestFullscreen()
  }
}

// 监听全屏变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 挂载时添加全屏监听，并处理 autoplay
onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  if (props.autoplay) {
    const video = videoRef.value
    if (video) {
      video.play().then(() => {
        videoPlaying.value = true
        startProgressLoop()
      }).catch(() => {
        // 视频还未加载完，等 loadedmetadata 后再播
      })
    }
  }
})

// 暴露方法给父组件
defineExpose({
  play: () => {
    const video = videoRef.value
    if (video) {
      video.play()
      videoPlaying.value = true
      startProgressLoop()
    }
  },
  pause: () => {
    const video = videoRef.value
    if (video) {
      video.pause()
      videoPlaying.value = false
      stopProgressLoop()
    }
  },
  stop: () => {
    const video = videoRef.value
    if (video) {
      video.pause()
      video.currentTime = 0
      videoPlaying.value = false
      videoProgress.value = 0
      videoCurrentTime.value = 0
      stopProgressLoop()
    }
  }
})

// 清理
onUnmounted(() => {
  stopProgressLoop()
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style lang="scss" scoped>
.custom-video-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: contain;
  outline: none;
  background: #000;
  cursor: pointer;
}

.video-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 40px 16px 12px;
  opacity: 0;
  transform: translateY(100%);
  transition: opacity 0.3s ease, transform 0.3s ease;
  
  &.visible {
    opacity: 1;
    transform: translateY(0);
  }
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-bottom: 8px;
}

.progress-track {
  position: relative;
  width: 100%;
  height: 3px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  transition: height 0.2s ease;
  
  .progress-bar:hover &,
  .progress-bar.dragging & {
    height: 5px;
  }
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 2px;
  will-change: width;
}

.progress-thumb {
  position: absolute;
  top: 50%;
  width: 14px;
  height: 14px;
  background: #fff;
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  transition: transform 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  will-change: left;
  
  .progress-bar:hover &,
  .progress-bar.dragging & {
    transform: translate(-50%, -50%) scale(1);
  }
}

.time-preview {
  position: absolute;
  bottom: 100%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 6px;
  margin-bottom: 12px;
  white-space: nowrap;
  pointer-events: none;
  
  &::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border: 6px solid transparent;
    border-top-color: rgba(0, 0, 0, 0.8);
  }
}

.controls-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.controls-left,
.controls-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.control-btn {
  background: none;
  border: none;
  color: #fff;
  cursor: pointer;
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.9;
  transition: opacity 0.2s ease, transform 0.2s ease;
  
  &:hover {
    opacity: 1;
    transform: scale(1.1);
  }
  
  svg {
    display: block;
  }
}

// 音量控制容器
.volume-control {
  position: relative;
  display: flex;
  align-items: center;
  
  &:hover .volume-slider-wrapper {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(0);
  }
}

.volume-slider-wrapper {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  padding-bottom: 12px;
  opacity: 0;
  visibility: hidden;
  transition: all 0.2s ease;
}

.volume-slider {
  width: 36px;
  height: 120px;
  background: rgba(0, 0, 0, 0.85);
  border-radius: 8px;
  padding: 16px 0;
  display: flex;
  justify-content: center;
  cursor: pointer;
}

.volume-track {
  position: relative;
  width: 4px;
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
}

.volume-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background: #fff;
  border-radius: 2px;
  will-change: height;
}

.volume-thumb {
  position: absolute;
  left: 50%;
  width: 14px;
  height: 14px;
  background: #fff;
  border-radius: 50%;
  transform: translate(-50%, 50%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  will-change: bottom;
}

.time-display {
  color: #fff;
  font-size: 13px;
  user-select: none;
}

.speed-control {
  position: relative;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  user-select: none;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
}

.speed-menu {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.9);
  border-radius: 8px;
  padding: 8px 0;
  margin-bottom: 8px;
  min-width: 80px;
}

.speed-option {
  padding: 8px 16px;
  text-align: center;
  cursor: pointer;
  transition: background 0.2s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  
  &.active {
    color: var(--color-primary);
  }
}

.big-play-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(0, 0, 0, 0.7);
    transform: translate(-50%, -50%) scale(1.1);
  }
  
  svg {
    margin-left: 4px;
  }
}

.replay-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translate(-50%, -50%) scale(1.1);
  }
}
</style>
