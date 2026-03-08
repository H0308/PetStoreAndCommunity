<template>
  <ElDialog
    v-model="visible"
    title="评论详情"
    width="600px"
    destroy-on-close
  >
    <div v-if="comment" class="comment-detail">
      <!-- 用户信息 -->
      <div class="user-section flex items-center gap-3 mb-4">
        <ElAvatar :src="comment.avatarUrl" :size="48">
          <template #default>
            <ArtSvgIcon icon="ri:user-line" class="text-xl" />
          </template>
        </ElAvatar>
        <div class="user-info">
          <div class="font-medium text-base">{{ comment.username }}</div>
          <div class="text-xs text-g-500">{{ comment.createTime }}</div>
        </div>
        <ElTag :class="comment.commentType === 1 ? 'tag-product' : 'tag-post'" class="ml-auto">
          {{ comment.commentType === 1 ? '商品评论' : '帖子评论' }}
        </ElTag>
      </div>

      <!-- 评论对象 -->
      <div class="object-section mb-4 p-3 bg-gray-50 rounded">
        <span class="text-g-500">{{ comment.commentType === 1 ? '评论商品' : '评论帖子' }}：</span>
        <span class="font-medium">{{ comment.objectName }}</span>
      </div>

      <!-- 评分（仅商品评论） -->
      <div v-if="comment.commentType === 1 && comment.stars" class="rating-section mb-4">
        <span class="text-g-500 mr-2">评分：</span>
        <ElRate v-model="comment.stars" disabled :max="5" />
      </div>

      <!-- 评论内容 -->
      <div class="content-section mb-4">
        <div class="text-g-500 mb-2">评论内容：</div>
        <div class="content-text p-3 bg-gray-50 rounded">{{ comment.content }}</div>
      </div>

      <!-- 媒体附件 -->
      <div v-if="comment.mediaUrls && comment.mediaUrls.length > 0" class="media-section mb-4">
        <div class="text-g-500 mb-2">附件（{{ comment.mediaUrls.length }}）：</div>
        <div class="media-grid">
          <template v-for="(url, index) in comment.mediaUrls" :key="index">
            <!-- 视频 -->
            <div v-if="isVideo(url)" class="media-item video-item" @click="handlePlayVideo(url)">
              <video :src="url" class="video-cover" />
              <div class="video-play-btn">
                <ArtSvgIcon icon="ri:play-circle-fill" class="play-icon" />
              </div>
            </div>
            <!-- 图片 -->
            <div v-else class="media-item">
              <ElImage
                :src="url"
                :preview-src-list="imageList"
                :initial-index="getImageIndex(url)"
                fit="cover"
                class="w-full h-full rounded"
              />
            </div>
          </template>
        </div>
      </div>

      <!-- 状态信息 -->
      <div class="status-section flex items-center gap-4">
        <div>
          <span class="text-g-500">状态：</span>
          <ElTag :class="getStatusClass(comment.status)">
            {{ getStatusText(comment.status) }}
          </ElTag>
        </div>
        <div v-if="comment.deleteFlag === 1">
          <ElTag type="danger">已删除</ElTag>
        </div>
        <div>
          <span class="text-g-500">回复数：</span>
          <span class="font-medium">{{ comment.replyCount }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <ElButton @click="visible = false">关闭</ElButton>
    </template>
  </ElDialog>

  <!-- 视频播放弹窗 -->
  <ElDialog
    v-model="videoDialogVisible"
    title="视频播放"
    width="800px"
    destroy-on-close
    class="video-dialog"
  >
    <div class="video-player-wrapper">
      <video 
        :src="currentVideoUrl" 
        controls 
        autoplay
        class="video-player"
      />
    </div>
  </ElDialog>
</template>

<script setup lang="ts">
import type { CommentItem } from '@/api/comment'
import { CommentStatus } from '@/api/comment'

defineOptions({ name: 'CommentDetailDialog' })

const props = defineProps<{
  comment: CommentItem | null
}>()

const visible = defineModel<boolean>({ default: false })

// 视频播放弹窗
const videoDialogVisible = ref(false)
const currentVideoUrl = ref('')

// 判断是否为视频
const isVideo = (url: string) => {
  const videoExts = ['.mp4', '.webm', '.ogg', '.mov', '.avi']
  return videoExts.some(ext => url.toLowerCase().includes(ext))
}

// 播放视频
const handlePlayVideo = (url: string) => {
  currentVideoUrl.value = url
  videoDialogVisible.value = true
}

// 获取图片列表用于预览（排除视频）
const imageList = computed(() => {
  if (!props.comment?.mediaUrls) return []
  return props.comment.mediaUrls.filter(url => !isVideo(url))
})

// 获取图片在图片列表中的索引
const getImageIndex = (url: string) => {
  return imageList.value.indexOf(url)
}

// 获取状态文本
const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    [CommentStatus.DRAFT]: '草稿',
    [CommentStatus.PENDING]: '审核中',
    [CommentStatus.APPROVED]: '审核成功',
    [CommentStatus.REJECTED]: '审核失败'
  }
  return map[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    [CommentStatus.DRAFT]: 'tag-draft',
    [CommentStatus.PENDING]: 'tag-pending',
    [CommentStatus.APPROVED]: 'tag-approved',
    [CommentStatus.REJECTED]: 'tag-rejected'
  }
  return map[status] || ''
}
</script>

<style scoped lang="scss">
.comment-detail {
  .content-text {
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .media-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;

    .media-item {
      aspect-ratio: 1;
      overflow: hidden;
      border-radius: 8px;
      background: #f5f5f5;

      &.video-item {
        aspect-ratio: 16/9;
        grid-column: span 2;
        position: relative;
        cursor: pointer;

        .video-cover {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 8px;
        }

        .video-play-btn {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.3);
          border-radius: 8px;
          transition: background 0.2s;

          .play-icon {
            font-size: 48px;
            color: #fff;
            filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
          }
        }

        &:hover .video-play-btn {
          background: rgba(0, 0, 0, 0.5);
        }
      }
    }
  }
}

.tag-product {
  background-color: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.tag-post {
  background-color: #f6ffed;
  color: #52c41a;
  border-color: #b7eb8f;
}

.tag-draft { background-color: #f5f5f5; color: #8c8c8c; border-color: #d9d9d9; }
.tag-pending { background-color: #fff7e6; color: #fa8c16; border-color: #ffd591; }
.tag-approved { background-color: #f6ffed; color: #52c41a; border-color: #b7eb8f; }
.tag-rejected { background-color: #fff1f0; color: #f5222d; border-color: #ffa39e; }

// 视频播放器样式
.video-player-wrapper {
  background: #000;
  border-radius: 8px;
  overflow: hidden;

  .video-player {
    width: 100%;
    max-height: 70vh;
    display: block;
  }
}

:deep(.video-dialog) {
  .el-dialog__body {
    padding: 0;
  }
}
</style>
