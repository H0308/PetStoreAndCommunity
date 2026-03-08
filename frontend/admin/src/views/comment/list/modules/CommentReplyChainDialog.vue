<template>
  <ElDialog
    v-model="visible"
    title="评论回复列表"
    width="700px"
    destroy-on-close
  >
    <div v-if="comment" class="reply-chain">
      <!-- 原评论 -->
      <div class="original-comment mb-4 p-4 bg-blue-50 rounded-lg border-l-4 border-blue-400">
        <div class="flex items-center gap-3 mb-2">
          <ElAvatar :src="comment.avatarUrl" :size="40">
            <template #default>
              <ArtSvgIcon icon="ri:user-line" class="text-base" />
            </template>
          </ElAvatar>
          <div class="flex-1">
            <div class="font-medium">{{ comment.username }}</div>
            <div class="text-xs text-g-500">{{ comment.createTime }}</div>
          </div>
          <ElTag size="small" type="primary">原评论</ElTag>
        </div>
        <div class="content-text">{{ comment.content }}</div>
      </div>

      <!-- 回复列表 -->
      <div class="reply-list">
        <div class="text-g-500 mb-3 flex items-center">
          <ArtSvgIcon icon="ri:chat-3-line" class="mr-1" />
          回复列表（{{ pagination.total }}）
        </div>
        
        <ElSkeleton v-if="loading" :rows="3" animated />
        
        <ElEmpty v-else-if="replyList.length === 0" description="暂无回复" />
        
        <div v-else class="reply-items">
          <div 
            v-for="reply in replyList" 
            :key="reply.commentId" 
            class="reply-item p-3 mb-3 bg-gray-50 rounded-lg"
          >
            <div class="flex items-start gap-3">
              <ElAvatar :src="reply.avatarUrl" :size="32">
                <template #default>
                  <ArtSvgIcon icon="ri:user-line" class="text-sm" />
                </template>
              </ElAvatar>
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-medium">{{ reply.username }}</span>
                  <ElTag v-if="reply.deleteFlag === 1" size="small" type="danger">已删除</ElTag>
                  <ElTag v-else :class="getStatusClass(reply.status)" size="small">
                    {{ getStatusText(reply.status) }}
                  </ElTag>
                </div>
                <div class="content-text text-sm" :class="{ 'text-g-400 line-through': reply.deleteFlag === 1 }">
                  {{ reply.deleteFlag === 1 ? '该回复已删除' : reply.content }}
                </div>
                <!-- 媒体预览 -->
                <div v-if="reply.deleteFlag !== 1 && reply.mediaUrls && reply.mediaUrls.length > 0" class="media-preview mt-2">
                  <div class="media-grid">
                    <template v-for="(url, index) in reply.mediaUrls" :key="index">
                      <!-- 视频 -->
                      <div v-if="isVideo(url)" class="media-thumb video-thumb" @click="handlePlayVideo(url)">
                        <video :src="url" class="video-cover" />
                        <div class="video-play-btn">
                          <ArtSvgIcon icon="ri:play-circle-fill" class="play-icon" />
                        </div>
                      </div>
                      <!-- 图片 -->
                      <div v-else class="media-thumb">
                        <ElImage
                          :src="url"
                          :preview-src-list="getImageList(reply.mediaUrls)"
                          :initial-index="getImageIndex(reply.mediaUrls, url)"
                          fit="cover"
                          class="w-full h-full rounded"
                        />
                      </div>
                    </template>
                  </div>
                </div>
                <div class="flex items-center gap-4 text-xs text-g-500 mt-2">
                  <span>{{ reply.createTime }}</span>
                  <span v-if="reply.replyCount > 0">{{ reply.replyCount }} 条回复</span>
                  <ElButton type="primary" link size="small" @click="handleGoToComment(reply.commentId)">
                    查看详情
                  </ElButton>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="pagination.total > 0" class="flex justify-center mt-4">
          <ElPagination
            :current-page="pagination.current"
            :page-size="pagination.size"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @current-change="handlePageChange"
          />
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
import type { CommentItem, CommentReplyItem } from '@/api/comment'
import { CommentStatus } from '@/api/comment'

defineOptions({ name: 'CommentReplyChainDialog' })

const props = defineProps<{
  comment: CommentItem | null
  replyList: CommentReplyItem[]
  loading: boolean
  pagination: { current: number; size: number; total: number }
}>()

const emit = defineEmits<{
  (e: 'pageChange', page: number): void
  (e: 'goToComment', commentId: number): void
}>()

const visible = defineModel<boolean>({ default: false })

// 视频播放
const videoDialogVisible = ref(false)
const currentVideoUrl = ref('')

const handlePageChange = (page: number) => {
  emit('pageChange', page)
}

// 跳转到指定评论
const handleGoToComment = (commentId: number) => {
  emit('goToComment', commentId)
}

// 判断是否为视频
const isVideo = (url: string) => {
  const videoExts = ['.mp4', '.webm', '.ogg', '.mov', '.avi']
  return videoExts.some(ext => url.toLowerCase().includes(ext))
}

// 获取图片列表（排除视频）
const getImageList = (urls: string[]) => {
  return urls.filter(url => !isVideo(url))
}

// 获取图片在图片列表中的索引
const getImageIndex = (urls: string[], currentUrl: string) => {
  const imageList = getImageList(urls)
  return imageList.indexOf(currentUrl)
}

// 播放视频
const handlePlayVideo = (url: string) => {
  currentVideoUrl.value = url
  videoDialogVisible.value = true
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
.reply-chain {
  max-height: 60vh;
  overflow-y: auto;

  .content-text {
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .reply-item {
    transition: background-color 0.2s;

    &:hover {
      background-color: #f0f0f0;
    }
  }

  .media-grid {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    .media-thumb {
      width: 80px;
      height: 80px;
      overflow: hidden;
      border-radius: 8px;
      background: #f0f0f0;

      &.video-thumb {
        position: relative;
        cursor: pointer;
        
        .video-cover {
          width: 100%;
          height: 100%;
          object-fit: cover;
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
          transition: background 0.2s;

          .play-icon {
            font-size: 32px;
            color: #fff;
          }
        }

        &:hover .video-play-btn {
          background: rgba(0, 0, 0, 0.5);
        }
      }
    }
  }
}

.tag-draft { background-color: #f5f5f5; color: #8c8c8c; border-color: #d9d9d9; }
.tag-pending { background-color: #fff7e6; color: #fa8c16; border-color: #ffd591; }
.tag-approved { background-color: #f6ffed; color: #52c41a; border-color: #b7eb8f; }
.tag-rejected { background-color: #fff1f0; color: #f5222d; border-color: #ffa39e; }

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
