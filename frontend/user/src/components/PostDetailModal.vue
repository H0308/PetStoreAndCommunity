<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="post-detail-overlay" @mousedown.self="handleOverlayMouseDown" @mouseup.self="handleOverlayMouseUp" @wheel.self.prevent @touchmove.self.prevent>
        <div class="post-detail-modal" @wheel.stop="handleModalWheel" @mousedown="overlayMouseDownOnSelf = false">
          <!-- 关闭按钮 -->
          <button class="close-btn" @click="close">
            <el-icon :size="20"><Close /></el-icon>
          </button>
          
          <div class="modal-content">
            <!-- 左侧：媒体轮播区 -->
            <div class="media-section">
              <div v-if="loading" class="loading-wrapper">
                <el-icon class="is-loading" :size="32"><Loading /></el-icon>
              </div>
              <div v-else class="carousel-wrapper" @wheel="handleCarouselWheel">
                <!-- 图片计数器 -->
                <div v-if="currentPost?.images?.length > 1" class="image-counter">
                  {{ currentImageIndex + 1 }} / {{ currentPost.images.length }}
                </div>
                <!-- 底部图片指示器 -->
                <div v-if="currentPost?.images?.length > 1" class="image-indicators">
                  <span 
                    v-for="(_, index) in currentPost.images" 
                    :key="index"
                    class="indicator-dot"
                    :class="{ active: index === currentImageIndex }"
                    @click="carouselRef?.setActiveItem(index)"
                  ></span>
                </div>
                <el-carousel 
                  ref="carouselRef"
                  v-if="currentPost?.images?.length"
                  :autoplay="false"
                  :loop="false"
                  indicator-position="outside"
                  height="100%"
                  :arrow="hasMultipleImages ? 'always' : 'never'"
                  @change="handleCarouselChange"
                >
                  <el-carousel-item v-for="(media, index) in currentPost.images" :key="index">
                    <div class="media-container">
                      <!-- 自定义视频播放器 -->
                      <div v-if="isVideo(media)" :ref="el => setPlayerRef(el, index)" class="custom-video-player" @mouseenter="!videoEnded[index] && (showControls[index] = true)" @mouseleave="videoPlaying[index] && (showControls[index] = false)">
                        <video 
                          :ref="el => setVideoRef(el, index)"
                          :src="media" 
                          preload="metadata"
                          class="media-video"
                          @timeupdate="handleTimeUpdate(index)"
                          @loadedmetadata="handleLoadedMetadata(index)"
                          @ended="handleVideoEnded(index)"
                          @click="togglePlay(index)"
                        />
                        <!-- 视频控制栏 -->
                        <div class="video-controls" :class="{ visible: showControls[index] }">
                          <!-- 进度条 -->
                          <div 
                            class="progress-bar" 
                            :class="{ dragging: isDragging[index] }"
                            @mousedown="startDrag($event, index)"
                            @mousemove="handleDragMove($event, index)"
                            @mouseleave="hidePreview(index)"
                          >
                            <div class="progress-track">
                              <div class="progress-fill" :style="{ width: videoProgress[index] + '%' }"></div>
                              <div class="progress-thumb" :style="{ left: videoProgress[index] + '%' }"></div>
                            </div>
                            <!-- 时间预览提示 -->
                            <div 
                              v-if="showTimePreview[index]" 
                              class="time-preview"
                              :style="{ left: previewPosition[index] + '%' }"
                            >
                              {{ formatTime(previewTime[index]) }}
                            </div>
                          </div>
                          <!-- 控制按钮 -->
                          <div class="controls-row">
                            <div class="controls-left">
                              <button class="control-btn play-btn" @click.stop="togglePlay(index)">
                                <svg v-if="!videoPlaying[index]" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                                  <path d="M8 5v14l11-7z"/>
                                </svg>
                                <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                                  <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
                                </svg>
                              </button>
                              <span class="time-display">{{ formatTime(videoCurrentTime[index]) }}/{{ formatTime(videoDuration[index]) }}</span>
                            </div>
                            <div class="controls-right">
                              <!-- 倍速 -->
                              <div class="speed-control" @click.stop="toggleSpeedMenu(index)">
                                <span>倍速</span>
                                <div v-if="showSpeedMenu[index]" class="speed-menu">
                                  <div 
                                    v-for="speed in playbackSpeeds" 
                                    :key="speed" 
                                    class="speed-option"
                                    :class="{ active: videoSpeed[index] === speed }"
                                    @click.stop="setPlaybackSpeed(index, speed)"
                                  >{{ speed }}x</div>
                                </div>
                              </div>
                              <!-- 音量 -->
                              <div class="volume-control">
                                <button class="control-btn volume-btn" @click.stop="toggleMute(index)">
                                  <!-- 静音状态 -->
                                  <svg v-if="videoMuted[index] || getDisplayVolume(index) === 0" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                                    <path d="M3 9v6h4l5 5V4L7 9H3z"/>
                                    <line x1="15" y1="9" x2="21" y2="15" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                                    <line x1="21" y1="9" x2="15" y2="15" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
                                  </svg>
                                  <!-- 低音量 (0-50%) -->
                                  <svg v-else-if="getDisplayVolume(index) <= 0.5" viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
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
                                  <div class="volume-slider" @mousedown.stop="startVolumeChange($event, index)">
                                    <div class="volume-track">
                                      <div class="volume-fill" :style="{ height: getDisplayVolume(index) * 100 + '%' }"></div>
                                      <div class="volume-thumb" :style="{ bottom: getDisplayVolume(index) * 100 + '%' }"></div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                              <!-- 画中画 -->
                              <button class="control-btn pip-btn" @click.stop="togglePiP(index)" title="画中画">
                                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
                                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                  <rect x="11" y="11" width="9" height="9" rx="1" ry="1" fill="currentColor" stroke="none"/>
                                </svg>
                              </button>
                              <!-- 全屏 -->
                              <button class="control-btn fullscreen-btn" @click.stop="toggleFullscreen(index)">
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
                        <div v-if="!videoPlaying[index] && !videoEnded[index]" class="big-play-btn" @click="togglePlay(index)">
                          <svg viewBox="0 0 24 24" width="64" height="64" fill="currentColor">
                            <path d="M8 5v14l11-7z"/>
                          </svg>
                        </div>
                        <!-- 重播按钮（视频结束时显示） -->
                        <div v-if="videoEnded[index]" class="replay-btn" @click="replayVideo(index)">
                          <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
                            <path d="M3 3v5h5"/>
                          </svg>
                        </div>
                      </div>
                      <img 
                        v-else 
                        :src="media" 
                        :alt="`图片${index + 1}`" 
                        class="media-image"
                      />
                    </div>
                  </el-carousel-item>
                </el-carousel>
                <div v-else class="no-image">
                  <el-icon :size="48"><Picture /></el-icon>
                  <span>暂无媒体</span>
                </div>
              </div>
            </div>
            
            <!-- 右侧：信息流区域 -->
            <div class="info-section">
              <!-- 作者信息 -->
              <div class="author-header">
                <div class="author-info">
                  <img :src="currentPost?.author?.avatar" class="author-avatar" />
                  <div class="author-meta">
                    <span class="author-name">{{ currentPost?.author?.nickname }}</span>
                    <span class="post-time">{{ currentPost?.createTime }}</span>
                  </div>
                </div>
              </div>
              
              <!-- 内容区域（可滚动） -->
              <div class="content-scroll">
                <!-- 帖子标题 -->
                <h2 class="post-title">{{ currentPost?.title }}</h2>
                
                <!-- 帖子内容 -->
                <div class="post-content markdown-body" v-html="renderedContent"></div>
                
                <!-- 话题标签 -->
                <div class="topic-tags">
                  <span 
                    v-for="tag in extractTags" 
                    :key="tag.id || tag.name" 
                    class="topic-tag"
                    @click="handleTopicClick(tag)"
                  >
                    {{ tag.name }}
                  </span>
                </div>
                
                <!-- 分割线 -->
                <div class="divider"></div>
                
                <!-- 评论区 -->
                <div class="comments-section">
                  <div class="comments-header">
                    <span class="comments-title">评论</span>
                    <span class="comments-count">{{ comments.length }}</span>
                  </div>
                  
                  <!-- 评论列表 -->
                  <el-skeleton :loading="commentsLoading" animated :rows="3">
                    <template #default>
                      <div v-if="organizedComments.length > 0" class="comments-list">
                        <div 
                          v-for="comment in organizedComments" 
                          :key="comment.commentId" 
                          class="comment-item"
                        >
                          <el-avatar :size="32" :src="comment.avatarUrl" class="comment-avatar">
                            {{ comment.username?.charAt(0) || '匿' }}
                          </el-avatar>
                          <div class="comment-body">
                            <!-- 评论头部 -->
                            <div class="comment-header">
                              <span class="comment-author">{{ comment.username || '匿名用户' }}</span>
                              <span v-if="comment.userId === currentPost?.author?.id" class="author-badge">作者</span>
                            </div>
                            
                            <!-- 评论内容 -->
                            <p class="comment-text" :class="{ 'is-deleted': comment.deleteFlag === 1 }">
                              {{ comment.deleteFlag === 1 ? '该评论已删除' : comment.content }}
                            </p>
                            
                            <!-- 评论媒体（图片/视频）- 已删除的评论不显示媒体 -->
                            <div v-if="comment.deleteFlag !== 1 && comment.mediaVOS && comment.mediaVOS.length > 0" class="comment-media">
                              <template v-for="(media, index) in comment.mediaVOS" :key="index">
                                <el-image
                                  v-if="media.mediaType === 1"
                                  :src="media.mediaUrl"
                                  :preview-src-list="comment.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                  fit="cover"
                                  class="media-image"
                                  lazy
                                />
                                <div 
                                  v-else-if="media.mediaType === 2" 
                                  class="media-video-item"
                                  @click="openVideoPlayer(media.mediaUrl)"
                                >
                                  <video :src="media.mediaUrl" preload="metadata" muted />
                                  <div class="video-play-btn">
                                    <el-icon :size="24"><VideoPlay /></el-icon>
                                  </div>
                                </div>
                              </template>
                            </div>
                            
                            <!-- 评论底部 -->
                            <div class="comment-footer">
                              <span class="comment-time">{{ comment.updateTime }}</span>
                              <div class="comment-actions">
                                <span 
                                  v-if="comment.deleteFlag !== 1"
                                  class="action-btn" 
                                  @click="replyTo(comment)"
                                >
                                  <el-icon><ChatDotRound /></el-icon>
                                  回复
                                </span>
                                <!-- 删除按钮（仅自己的评论可删除） -->
                                <span
                                  v-if="comment.deleteFlag !== 1 && isOwnComment(comment.userId)"
                                  class="action-btn delete-btn"
                                  @click="handleDeleteComment(comment.commentId)"
                                >
                                  <el-icon><Delete /></el-icon>
                                  删除
                                </span>
                                <span 
                                  v-if="getTotalRepliesCount(comment) > 0"
                                  class="action-btn view-replies"
                                  @click="toggleReplies(comment.commentId)"
                                >
                                  {{ expandedReplies[comment.commentId] ? '收起回复' : `查看${getTotalRepliesCount(comment)}条回复` }}
                                </span>
                              </div>
                            </div>
                            
                            <!-- 回复列表（折叠显示，扁平化展示所有回复） -->
                            <div v-if="getTotalRepliesCount(comment) > 0 && expandedReplies[comment.commentId]" class="replies-list">
                              <template v-for="reply in flattenReplies(comment.replies)" :key="reply.commentId">
                                <div class="reply-item">
                                  <el-avatar :size="24" :src="reply.avatarUrl" class="reply-avatar">
                                    {{ reply.username?.charAt(0) || '匿' }}
                                  </el-avatar>
                                  <div class="reply-body">
                                    <span class="reply-author">{{ reply.username || '匿名用户' }}</span>
                                    <span class="reply-to">回复</span>
                                    <span class="reply-target">@{{ reply.replyToUsername || '匿名用户' }}</span>
                                    <span class="reply-text" :class="{ 'is-deleted': reply.deleteFlag === 1 }">
                                      {{ reply.deleteFlag === 1 ? '该评论已删除' : reply.content }}
                                    </span>
                                    
                                    <!-- 回复媒体 -->
                                    <div v-if="reply.deleteFlag !== 1 && reply.mediaVOS && reply.mediaVOS.length > 0" class="reply-media">
                                      <template v-for="(media, index) in reply.mediaVOS" :key="index">
                                        <el-image
                                          v-if="media.mediaType === 1"
                                          :src="media.mediaUrl"
                                          :preview-src-list="reply.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                          fit="cover"
                                          class="media-image-small"
                                          lazy
                                        />
                                        <div 
                                          v-else-if="media.mediaType === 2" 
                                          class="media-video-item small"
                                          @click="openVideoPlayer(media.mediaUrl)"
                                        >
                                          <video :src="media.mediaUrl" preload="metadata" muted />
                                          <div class="video-play-btn">
                                            <el-icon :size="18"><VideoPlay /></el-icon>
                                          </div>
                                        </div>
                                      </template>
                                    </div>
                                    
                                    <div class="reply-footer">
                                      <span class="reply-time">{{ reply.updateTime }}</span>
                                      <span 
                                        v-if="reply.deleteFlag !== 1"
                                        class="action-btn"
                                        @click="replyTo(reply)"
                                      >
                                        回复
                                      </span>
                                      <span
                                        v-if="reply.deleteFlag !== 1 && isOwnComment(reply.userId)"
                                        class="action-btn delete-btn"
                                        @click="handleDeleteComment(reply.commentId)"
                                      >
                                        删除
                                      </span>
                                      <span 
                                        v-if="reply.replies && reply.replies.length > 0"
                                        class="action-btn view-replies"
                                        @click="toggleReplies(reply.commentId)"
                                      >
                                        {{ expandedReplies[reply.commentId] ? '收起' : `${getTotalRepliesCount(reply)}条回复` }}
                                      </span>
                                    </div>
                                  </div>
                                </div>
                              </template>
                            </div>
                          </div>
                        </div>
                      </div>
                      
                      <!-- 空评论状态 -->
                      <div v-else class="empty-comments">
                        <el-icon :size="32"><ChatLineSquare /></el-icon>
                        <p>暂无评论，快来抢沙发~</p>
                      </div>
                    </template>
                  </el-skeleton>
                </div>
              </div>
              
              <!-- 隐藏的文件上传 input -->
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
                style="display: none"
                @change="handleVideoSelect"
              />
              
              <!-- 底部操作栏（吸底）- 小红书风格沉浸式评论框 -->
              <div class="comment-bar-wrapper" :class="{ 'is-active': isInputExpanded }">
                <!-- 主交互行 -->
                <div class="main-row">
                  <!-- 中间输入框容器 -->
                  <div class="input-wrapper" @click="!isInputExpanded && expandInput()">
                    <!-- 回复提示标签 -->
                    <div v-if="replyingTo && !isInputExpanded" class="reply-badge">
                      回复 @{{ replyingTo.username }}
                    </div>
                    
                    <!-- 媒体预览区（展开时显示） -->
                    <div v-if="isInputExpanded && mediaFiles.length > 0" class="media-preview-area">
                      <div 
                        v-for="item in mediaFiles" 
                        :key="item.id"
                        class="preview-thumb"
                        @click.stop="openCommentMediaPreview(item)"
                      >
                        <img v-if="item.type === 'image'" :src="item.url" alt="预览" />
                        <video v-else :src="item.url" muted />
                        <!-- hover 遮罩 -->
                        <div class="thumb-hover-mask">
                          <el-icon :size="16"><component :is="item.type === 'video' ? VideoPlay : ZoomIn" /></el-icon>
                        </div>
                        <button class="thumb-remove" @click.stop="removeMedia(item.id)">
                          <el-icon :size="8"><Close /></el-icon>
                        </button>
                      </div>
                    </div>

                    <!-- 图片预览 -->
                    <el-image-viewer
                      v-if="commentImagePreviewVisible"
                      :url-list="mediaFiles.filter(f => f.type === 'image').map(f => f.url)"
                      :initial-index="commentImagePreviewIndex"
                      @close="commentImagePreviewVisible = false"
                    />

                    <!-- 视频预览弹窗 -->
                    <Teleport to="body">
                      <Transition name="video-preview">
                        <div v-if="commentVideoPreviewVisible" class="comment-video-preview-overlay" @click.self="closeCommentVideoPreview">
                          <div class="comment-video-preview-content">
                            <CustomVideoPlayer
                              ref="commentVideoPreviewRef"
                              :src="commentVideoPreviewUrl"
                              autoplay
                              class="comment-video-player"
                            />
                            <button class="comment-video-close-btn" @click="closeCommentVideoPreview">
                              <el-icon :size="20"><Close /></el-icon>
                            </button>
                          </div>
                        </div>
                      </Transition>
                    </Teleport>
                    
                    <!-- 输入区域 -->
                    <div class="input-area">
                      <textarea
                        ref="commentInputRef"
                        v-model="commentText"
                        :placeholder="replyingTo ? `回复 @${replyingTo.username}: （发布的评论会移交审核，审核通过后才会在列表中显示）` : '说点什么...（发布的评论会移交审核，审核通过后才会在列表中显示，未通过审核的评论会以站内通知的方式进行通知）'"
                        :rows="isInputExpanded ? 3 : 1"
                        maxlength="2048"
                        @focus="expandInput"
                      ></textarea>
                    </div>
                  </div>
                  
                  <!-- 右侧数据区（点赞、点踩、收藏） -->
                  <div class="stats-actions">
                    <!-- 点赞 - 爱心 -->
                    <div 
                      class="stat-item stat-like"
                      :class="{ active: currentPost?.isLiked, 'animate-bounce': likeLoading }"
                      @click="handleLike"
                    >
                      <svg class="stat-icon" viewBox="0 0 24 24" width="20" height="20">
                        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
                      </svg>
                      <span class="stat-count" :class="{ 'animate-count': likeLoading }">{{ currentPost?.likeCount ? formatCount(currentPost.likeCount) : '' }}</span>
                    </div>
                    <!-- 点踩 - 大拇指向下 -->
                    <div 
                      class="stat-item stat-dislike"
                      :class="{ active: currentPost?.isRejected, 'animate-shake': rejectLoading }"
                      @click="handleReject"
                    >
                      <svg class="stat-icon" viewBox="0 0 24 24" width="20" height="20">
                        <path d="M10.88 21.94l5.53-5.54c.37-.37.58-.88.58-1.41V5c0-1.1-.9-2-2-2H6c-.8 0-1.52.48-1.83 1.21L.91 11.82C.06 13.8 1.51 16 3.66 16h5.65l-.95 4.58c-.1.5.05 1.01.41 1.37.59.58 1.53.58 2.11-.01zM21 3c-1.1 0-2 .9-2 2v8c0 1.1.9 2 2 2s2-.9 2-2V5c0-1.1-.9-2-2-2z"/>
                      </svg>
                      <span class="stat-count">{{ currentPost?.rejectCount ? formatCount(currentPost.rejectCount) : '' }}</span>
                    </div>
                    <!-- 收藏 - 星星 -->
                    <div 
                      class="stat-item stat-favorite"
                      :class="{ active: currentPost?.isCollected, 'animate-spin-pop': collectLoading }"
                      @click="handleCollect"
                    >
                      <svg class="stat-icon" viewBox="0 0 24 24" width="20" height="20">
                        <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                      </svg>
                      <span class="stat-count" :class="{ 'animate-count': collectLoading }">{{ currentPost?.collectCount ? formatCount(currentPost.collectCount) : '' }}</span>
                    </div>
                  </div>
                </div>
                
                <!-- 扩展工具栏 -->
                <div class="tool-row">
                  <div class="tool-row-inner">
                    <!-- 回复提示（展开时显示） -->
                    <div v-if="replyingTo" class="reply-hint">
                      <span>回复 <strong>@{{ replyingTo.username }}</strong></span>
                      <button class="hint-cancel" @click="cancelReply">×</button>
                    </div>
                    
                    <div class="tool-content">
                      <!-- 左侧工具按钮 -->
                      <div class="tool-left">
                        <button 
                          class="tool-btn" 
                          :class="{ disabled: imageCount >= 9 }"
                          @click.stop="triggerImageUpload"
                          title="添加图片"
                        >
                          <el-icon :size="20"><Picture /></el-icon>
                        </button>
                        <button 
                          class="tool-btn"
                          :class="{ disabled: hasVideo }"
                          @click.stop="triggerVideoUpload"
                          title="添加视频"
                        >
                          <el-icon :size="20"><VideoCamera /></el-icon>
                        </button>
                      </div>
                      
                      <!-- 右侧操作按钮 -->
                      <div class="tool-right">
                        <button class="cancel-btn" @click.stop="collapseInput">取消</button>
                        <button 
                          class="send-btn"
                          :class="{ active: canSubmitComment, 'is-submitting': commentSubmitting }"
                          :disabled="!canSubmitComment || commentSubmitting"
                          @click.stop="submitComment"
                        >
                          {{ commentSubmitting ? '发送中...' : '发送' }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
    
    <!-- 视频播放器弹窗 -->
    <Transition name="video-modal">
      <div v-if="videoPlayerVisible" class="video-modal-overlay" @mousedown.self="videoModalMouseDown = true" @mouseup.self="videoModalMouseDown && closeVideoPlayer(); videoModalMouseDown = false">
        <div 
          class="video-modal-content" 
          :style="videoModalStyle"
          @mousedown="videoModalMouseDown = false"
        >
          <CustomVideoPlayer
            ref="videoPlayerRef"
            :src="currentVideoUrl"
            class="video-player-custom"
            @loadedmetadata="handleVideoMetadata"
          />
          <button class="video-close-btn" @click="closeVideoPlayer">
            <el-icon :size="24"><Close /></el-icon>
          </button>
        </div>
      </div>
    </Transition>

    <!-- 实名认证提示弹窗 -->
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
              <p>您尚未完成实名认证，无法发布评论。</p>
              <p class="auth-tip-hint">请前往个人资料页面完成实名认证后再进行操作。</p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn-cancel" @click="showAuthTipDialog = false">取消</button>
            <button class="btn-confirm" @click="showAuthTipDialog = false; emit('update:visible', false); router.push('/profile/profile')">去认证</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, VideoCamera, VideoPlay, Close, Loading, ChatDotRound, ChatLineSquare, StarFilled, Delete, Warning, ZoomIn } from '@element-plus/icons-vue'
import { POST_API, request } from '../api/config.js'
import CustomVideoPlayer from './CustomVideoPlayer.vue'

const router = useRouter()

const props = defineProps({
  visible: Boolean,
  post: Object,
  isLoggedIn: Boolean,
  userInfo: Object
})

const emit = defineEmits(['update:visible', 'like', 'reject', 'collect', 'topic-click', 'open-login', 'close'])

// 实名认证提示弹窗
const showAuthTipDialog = ref(false)

// 状态
const commentText = ref('')
const replyingTo = ref(null)
const loading = ref(false)
const postDetail = ref(null)
const commentsLoading = ref(false)
const expandedReplies = ref({}) // 记录哪些评论的回复已展开

// 点赞、点踩、收藏的加载状态（防抖）
const likeLoading = ref(false)
const rejectLoading = ref(false)
const collectLoading = ref(false)

// 视频播放器状态
const videoPlayerVisible = ref(false)
const videoModalMouseDown = ref(false)
const currentVideoUrl = ref('')
const videoPlayerRef = ref(null)

// 评论数据
const comments = ref([])

// 媒体上传相关
const imageInputRef = ref(null)
const videoInputRef = ref(null)
const mediaFiles = ref([]) // { id, type: 'image'|'video', file, url }
const commentInputRef = ref(null)
const carouselRef = ref(null)
const currentImageIndex = ref(0)

// 视频播放器相关状态
const playerRefs = ref({})
const videoRefs = ref({})
const videoPlaying = ref({})
const videoEnded = ref({})
const isFullscreen = ref(false)
const videoProgress = ref({})
const videoCurrentTime = ref({})
const videoDuration = ref({})
const videoMuted = ref({})
const videoSpeed = ref({})
const showControls = ref({})
const showSpeedMenu = ref({})
const playbackSpeeds = [0.5, 0.75, 1, 1.25, 1.5, 2]

// 设置播放器容器 ref
const setPlayerRef = (el, index) => {
  if (el) {
    playerRefs.value[index] = el
  }
}

// 设置视频 ref
const setVideoRef = (el, index) => {
  if (el) {
    videoRefs.value[index] = el
    // 初始化状态
    if (videoPlaying.value[index] === undefined) {
      videoPlaying.value[index] = false
      videoEnded.value[index] = false
      videoProgress.value[index] = 0
      videoCurrentTime.value[index] = 0
      videoDuration.value[index] = 0
      videoMuted.value[index] = false
      videoVolume.value[index] = 1
      videoSpeed.value[index] = 1
      showControls.value[index] = true
      showSpeedMenu.value[index] = false
    }
  }
}

// 格式化时间
const formatTime = (seconds) => {
  if (!seconds || isNaN(seconds)) return '00:00'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 播放/暂停
const togglePlay = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  if (video.paused) {
    // 如果视频已结束，从头开始播放
    if (videoEnded.value[index]) {
      video.currentTime = 0
      videoEnded.value[index] = false
      videoProgress.value[index] = 0
      videoCurrentTime.value[index] = 0
    }
    video.play()
    videoPlaying.value[index] = true
    startProgressLoop(index)
  } else {
    video.pause()
    videoPlaying.value[index] = false
    stopProgressLoop(index)
  }
}

// 进度更新动画帧
const progressAnimationFrames = ref({})

// 进度条拖动相关
const isDragging = ref({})
const showTimePreview = ref({})
const previewTime = ref({})
const previewPosition = ref({})

// 音量相关
const videoVolume = ref({})

// 获取显示音量（静音时返回0）
const getDisplayVolume = (index) => {
  if (videoMuted.value[index]) {
    return 0
  }
  return videoVolume.value[index] ?? 1
}

// 开始进度更新循环
const startProgressLoop = (index) => {
  const updateProgress = () => {
    const video = videoRefs.value[index]
    if (!video || !video.duration || video.paused) {
      progressAnimationFrames.value[index] = null
      return
    }
    
    videoCurrentTime.value[index] = video.currentTime
    videoProgress.value[index] = (video.currentTime / video.duration) * 100
    progressAnimationFrames.value[index] = requestAnimationFrame(updateProgress)
  }
  
  if (!progressAnimationFrames.value[index]) {
    progressAnimationFrames.value[index] = requestAnimationFrame(updateProgress)
  }
}

// 停止进度更新循环
const stopProgressLoop = (index) => {
  if (progressAnimationFrames.value[index]) {
    cancelAnimationFrame(progressAnimationFrames.value[index])
    progressAnimationFrames.value[index] = null
  }
}

// 时间更新（仅用于暂停时同步）
const handleTimeUpdate = (index) => {
  const video = videoRefs.value[index]
  if (!video || !video.duration) return
  
  if (video.paused) {
    videoCurrentTime.value[index] = video.currentTime
    videoProgress.value[index] = (video.currentTime / video.duration) * 100
  }
}

// 加载元数据
const handleLoadedMetadata = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  videoDuration.value[index] = video.duration
}

// 视频结束
const handleVideoEnded = (index) => {
  stopProgressLoop(index)
  videoPlaying.value[index] = false
  videoEnded.value[index] = true
  videoProgress.value[index] = 100
  showControls.value[index] = false  // 视频结束时隐藏控制栏
}

// 重播视频
const replayVideo = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  video.currentTime = 0
  videoEnded.value[index] = false
  videoProgress.value[index] = 0
  videoCurrentTime.value[index] = 0
  showControls.value[index] = true  // 重播时显示控制栏
  video.play()
  videoPlaying.value[index] = true
  startProgressLoop(index)
}

// 开始拖动进度条
const startDrag = (e, index) => {
  const video = videoRefs.value[index]
  if (!video || !video.duration) return
  
  e.preventDefault()
  isDragging.value[index] = true
  showTimePreview.value[index] = true
  
  const progressBar = e.currentTarget
  const rect = progressBar.getBoundingClientRect()
  
  // 立即更新到点击位置
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  video.currentTime = percent * video.duration
  videoProgress.value[index] = percent * 100
  videoCurrentTime.value[index] = video.currentTime
  previewPosition.value[index] = percent * 100
  previewTime.value[index] = video.currentTime
  
  const onMouseMove = (moveEvent) => {
    // 实时获取进度条位置（全屏时位置会变化）
    const currentRect = progressBar.getBoundingClientRect()
    const movePercent = Math.max(0, Math.min(1, (moveEvent.clientX - currentRect.left) / currentRect.width))
    video.currentTime = movePercent * video.duration
    videoProgress.value[index] = movePercent * 100
    videoCurrentTime.value[index] = video.currentTime
    previewPosition.value[index] = movePercent * 100
    previewTime.value[index] = video.currentTime
  }
  
  const onMouseUp = () => {
    isDragging.value[index] = false
    showTimePreview.value[index] = false
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// 鼠标移动时显示预览
const handleDragMove = (e, index) => {
  if (isDragging.value[index]) return
  
  const video = videoRefs.value[index]
  if (!video || !video.duration) return
  
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  previewPosition.value[index] = percent * 100
  previewTime.value[index] = percent * video.duration
  showTimePreview.value[index] = true
}

// 隐藏预览
const hidePreview = (index) => {
  if (!isDragging.value[index]) {
    showTimePreview.value[index] = false
  }
}

// 静音切换
const toggleMute = (index) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  video.muted = !video.muted
  videoMuted.value[index] = video.muted
}

// 开始调节音量
const startVolumeChange = (e, index) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  e.preventDefault()
  const slider = e.currentTarget
  const rect = slider.getBoundingClientRect()
  
  const updateVolume = (clientY) => {
    const percent = Math.max(0, Math.min(1, (rect.bottom - clientY) / rect.height))
    video.volume = percent
    videoVolume.value[index] = percent
    if (percent > 0) {
      video.muted = false
      videoMuted.value[index] = false
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
const toggleSpeedMenu = (index) => {
  showSpeedMenu.value[index] = !showSpeedMenu.value[index]
}

// 设置播放速度
const setPlaybackSpeed = (index, speed) => {
  const video = videoRefs.value[index]
  if (!video) return
  
  video.playbackRate = speed
  videoSpeed.value[index] = speed
  showSpeedMenu.value[index] = false
}

// 画中画
const togglePiP = async (index) => {
  const video = videoRefs.value[index]
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
const toggleFullscreen = (index) => {
  const player = playerRefs.value[index]
  if (!player) return
  
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    player.requestFullscreen()
  }
}

// 输入框展开状态
const isInputExpanded = ref(false)

// 展开输入框
const expandInput = () => {
  isInputExpanded.value = true
  nextTick(() => {
    // 支持原生 textarea 和 el-input 组件
    const inputEl = commentInputRef.value
    if (inputEl) {
      if (inputEl.focus) {
        inputEl.focus()
      } else if (inputEl.$el) {
        inputEl.$el.querySelector('textarea')?.focus()
      }
    }
  })
}

// 收起输入框
const collapseInput = () => {
  isInputExpanded.value = false
  commentText.value = ''
  replyingTo.value = null
  mediaFiles.value.forEach(f => URL.revokeObjectURL(f.url))
  mediaFiles.value = []
}

// 媒体计算属性
const imageCount = computed(() => mediaFiles.value.filter(f => f.type === 'image').length)
const hasVideo = computed(() => mediaFiles.value.some(f => f.type === 'video'))
const canSubmitComment = computed(() => commentText.value.trim().length > 0 || mediaFiles.value.length > 0)

// 触发图片选择
const triggerImageUpload = () => {
  if (imageCount.value >= 9) {
    ElMessage.warning('最多只能上传9张图片')
    return
  }
  imageInputRef.value?.click()
}

// 触发视频选择
const triggerVideoUpload = () => {
  if (hasVideo.value) {
    ElMessage.warning('只能上传1个视频')
    return
  }
  videoInputRef.value?.click()
}

// 文件指纹
const fileKey = (f) => `${f.name}_${f.size}_${f.lastModified}`

// 处理图片选择
const handleImageSelect = (event) => {
  const files = Array.from(event.target.files)
  const existingKeys = new Set(mediaFiles.value.map(f => fileKey(f.file)))
  const remainingSlots = 9 - imageCount.value
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
    mediaFiles.value.push({ id, type: 'image', file, url })
    existingKeys.add(fileKey(file))
    added++
  }

  if (skippedDup > 0) ElMessage.warning('存在相同的文件在上传队列中')
  event.target.value = ''
}

// 处理视频选择
const handleVideoSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('video/')) {
    ElMessage.warning('请选择视频文件')
    event.target.value = ''
    return
  }
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.warning('视频大小不能超过100MB')
    event.target.value = ''
    return
  }
  const existingKeys = new Set(mediaFiles.value.map(f => fileKey(f.file)))
  if (existingKeys.has(fileKey(file))) {
    ElMessage.warning('存在相同的文件在上传队列中')
    event.target.value = ''
    return
  }

  const id = Date.now() + Math.random()
  const url = URL.createObjectURL(file)
  mediaFiles.value.push({ id, type: 'video', file, url })
  event.target.value = ''
}

// 删除媒体
const removeMedia = (id) => {
  const index = mediaFiles.value.findIndex(f => f.id === id)
  if (index > -1) {
    URL.revokeObjectURL(mediaFiles.value[index].url)
    mediaFiles.value.splice(index, 1)
  }
}

// 评论媒体预览
const commentImagePreviewVisible = ref(false)
const commentImagePreviewIndex = ref(0)
const commentVideoPreviewVisible = ref(false)
const commentVideoPreviewUrl = ref('')
const commentVideoPreviewRef = ref(null)

const openCommentMediaPreview = (item) => {
  if (item.type === 'image') {
    const imageFiles = mediaFiles.value.filter(f => f.type === 'image')
    commentImagePreviewIndex.value = imageFiles.findIndex(f => f.id === item.id)
    commentImagePreviewVisible.value = true
  } else {
    commentVideoPreviewUrl.value = item.url
    commentVideoPreviewVisible.value = true
  }
}
const closeCommentVideoPreview = () => {
  commentVideoPreviewRef.value?.pause?.()
  commentVideoPreviewVisible.value = false
  commentVideoPreviewUrl.value = ''
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
}

// 计算属性 - 优先使用详情数据，否则使用传入的post
const currentPost = computed(() => postDetail.value || props.post)

const isAuthor = computed(() => false) // 实际应该判断当前用户是否是作者

// 判断是否有多个非视频媒体（用于控制轮播箭头显示）
const hasMultipleImages = computed(() => {
  const images = currentPost.value?.images || []
  if (images.length <= 1) return false
  // 如果只有一个媒体且是视频，不显示箭头
  const nonVideoCount = images.filter(url => !isVideo(url)).length
  return images.length > 1 && nonVideoCount > 0
})

const contentParagraphs = computed(() => {
  if (!currentPost.value?.content) return []
  return currentPost.value.content.split('\n').filter(p => p.trim())
})

// 渲染内容 - Tiptap 输出的是 HTML，直接使用
const renderedContent = computed(() => {
  if (!currentPost.value?.content) return ''
  return currentPost.value.content
})

// 计算属性：将评论组织成树形结构（支持多层嵌套）
const organizedComments = computed(() => {
  const commentMap = {} // commentId -> comment对象
  const topLevelComments = []
  
  // 第一遍：创建所有评论的副本并建立映射
  comments.value.forEach(comment => {
    commentMap[comment.commentId] = { ...comment, replies: [], replyToUsername: null }
  })
  
  // 第二遍：构建树形结构，并记录被回复者用户名
  comments.value.forEach(comment => {
    const currentComment = commentMap[comment.commentId]
    if (!comment.parentId) {
      // 顶级评论
      topLevelComments.push(currentComment)
    } else {
      // 回复评论，挂载到父评论下
      const parentComment = commentMap[comment.parentId]
      if (parentComment) {
        currentComment.replyToUsername = parentComment.username || '匿名用户'
        parentComment.replies.push(currentComment)
      }
    }
  })
  
  return topLevelComments
})

// 计算某个评论及其所有子孙回复的总数
const getTotalRepliesCount = (comment) => {
  let count = comment.replies.length
  comment.replies.forEach(reply => {
    count += getTotalRepliesCount(reply)
  })
  return count
}

// 扁平化回复列表，根据展开状态决定是否包含子回复
const flattenReplies = (replies) => {
  const result = []
  const flatten = (items) => {
    items.forEach(item => {
      result.push(item)
      // 只有当该回复被展开时，才递归添加其子回复
      if (item.replies && item.replies.length > 0 && expandedReplies.value[item.commentId]) {
        flatten(item.replies)
      }
    })
  }
  flatten(replies)
  return result
}

// 切换回复展开/收起
const toggleReplies = (commentId) => {
  expandedReplies.value[commentId] = !expandedReplies.value[commentId]
}

// 从详情数据中获取话题标签（包含ID和名称）
const extractTags = computed(() => {
  // 优先使用后端返回的话题数据
  if (postDetail.value?.topicName?.length && postDetail.value?.topicId?.length) {
    return postDetail.value.topicName.map((name, index) => ({
      id: postDetail.value.topicId[index],
      name: `#${name}`
    }))
  }
  // 否则从内容中提取（无ID）
  if (!currentPost.value?.content) return []
  const matches = currentPost.value.content.match(/#[^\s#]+/g)
  return (matches || []).map(name => ({ id: null, name }))
})

const replyPlaceholder = computed(() => {
  if (replyingTo.value) {
    return `回复 @${replyingTo.value.username}`
  }
  return '说点什么...'
})

// 获取帖子详情
const fetchPostDetail = async (postId) => {
  if (!postId) return
  
  loading.value = true
  try {
    const response = await request(`${POST_API.GET_POST_DETAIL}?postId=${postId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      const data = result.data
      // 从 mediaUrlWithId (Map<Long, String>) 中提取媒体URL数组
      const mediaImages = data.mediaUrlWithId ? Object.values(data.mediaUrlWithId) : []
      
      // 初始化点赞/点踩状态
      let isLiked = false
      let isRejected = false
      let isCollected = false
      
      // 如果用户已登录，获取点赞/点踩/收藏状态
      if (props.isLoggedIn && props.userInfo?.userId) {
        const [likeRes, dislikeRes, favorRes] = await Promise.all([
          request(`${POST_API.GET_LIKE_STATUS}?userId=${props.userInfo.userId}&postId=${postId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
          }),
          request(`${POST_API.GET_DISLIKE_STATUS}?userId=${props.userInfo.userId}&postId=${postId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
          }),
          request(`${POST_API.GET_FAVOR_STATUS}?userId=${props.userInfo.userId}&postId=${postId}`, {
            method: 'GET'
          })
        ])
        
        const likeResult = await likeRes.json()
        const dislikeResult = await dislikeRes.json()
        const favorResult = await favorRes.json()
        
        if (likeResult.code === 0) {
          isLiked = likeResult.data === true
        }
        if (dislikeResult.code === 0) {
          isRejected = dislikeResult.data === true
        }
        if (favorResult.code === 0) {
          isCollected = favorResult.data === true
        }
      }
      
      postDetail.value = {
        id: data.postId,
        title: data.title,
        content: data.content,
        // 将后端的mediaUrlWithId转换为前端的images格式
        images: mediaImages,
        author: {
          id: data.userId,
          nickname: data.username || '匿名用户',
          avatar: data.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${data.userId}`
        },
        createTime: data.updateTime,
        likeCount: data.likeCount || 0,
        rejectCount: data.rejectCount || 0,
        collectCount: data.favorCount || 0,
        isLiked,
        isRejected,
        isCollected,
        topicId: data.topicId || [],
        topicName: data.topicName || []
      }
      
      // 如果第一个媒体是视频，自动播放
      if (postDetail.value.images?.length > 0 && isVideo(postDetail.value.images[0])) {
        nextTick(() => {
          setTimeout(() => {
            if (videoRefs.value[0]) {
              togglePlay(0)
            }
          }, 300)
        })
      }
    }
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    ElMessage.error('获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

// 获取帖子评论
const fetchPostComments = async (postId) => {
  if (!postId) return
  
  commentsLoading.value = true
  try {
    const response = await request(`${POST_API.GET_POST_COMMENTS}?postId=${postId}`)
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      comments.value = result.data || []
    } else {
      // 没有评论时不报错，只是清空
      comments.value = []
    }
  } catch (error) {
    console.error('获取帖子评论失败:', error)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

// 判断媒体是否为视频
const isVideo = (url) => {
  if (!url) return false
  const videoExtensions = ['.mp4', '.webm', '.ogg', '.mov', '.avi', '.mkv']
  const lowerUrl = url.toLowerCase()
  return videoExtensions.some(ext => lowerUrl.includes(ext))
}

// 视频弹窗尺寸
const videoModalSize = ref({ width: 0, height: 0 })
const videoModalStyle = computed(() => {
  if (!videoModalSize.value.width || !videoModalSize.value.height) {
    return {}
  }
  const maxWidth = window.innerWidth * 0.9
  const maxHeight = window.innerHeight * 0.8
  const minWidth = 480 // 最小宽度，确保控制栏有足够空间
  const videoRatio = videoModalSize.value.width / videoModalSize.value.height
  
  let width, height
  if (videoModalSize.value.width > maxWidth || videoModalSize.value.height > maxHeight) {
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
    width = videoModalSize.value.width
    height = videoModalSize.value.height
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
  videoModalSize.value = { width: data.width, height: data.height }
}

// 打开视频播放器
const openVideoPlayer = (videoUrl) => {
  videoModalSize.value = { width: 0, height: 0 }
  currentVideoUrl.value = videoUrl
  videoPlayerVisible.value = true
  setTimeout(() => {
    if (videoPlayerRef.value) {
      videoPlayerRef.value.play()
    }
  }, 100)
}

// 关闭视频播放器
const closeVideoPlayer = () => {
  if (videoPlayerRef.value) {
    videoPlayerRef.value.pause()
  }
  videoPlayerVisible.value = false
  currentVideoUrl.value = ''
  videoModalSize.value = { width: 0, height: 0 }
}

// 遮罩层点击关闭逻辑 - 确保按下和抬起都在遮罩层上
let overlayMouseDownOnSelf = false

const handleOverlayMouseDown = (e) => {
  // 记录鼠标是在遮罩层上按下的
  overlayMouseDownOnSelf = true
}

const handleOverlayMouseUp = (e) => {
  // 只有当按下和抬起都在遮罩层上时才关闭
  if (overlayMouseDownOnSelf) {
    close()
  }
  overlayMouseDownOnSelf = false
}



// 轮播图切换回调
const handleCarouselChange = (index) => {
  currentImageIndex.value = index
}

// 轮播图滚轮切换
let carouselWheelTimer = null
const handleCarouselWheel = (e) => {
  e.preventDefault()
  e.stopPropagation()
  
  if (!carouselRef.value || !hasMultipleImages.value) return
  
  const totalImages = currentPost.value?.images?.length || 0
  
  // 防抖，避免滚动过快
  if (carouselWheelTimer) return
  carouselWheelTimer = setTimeout(() => {
    carouselWheelTimer = null
  }, 200)
  
  // 不循环切换，到边界时不再切换
  if (e.deltaY > 0 && currentImageIndex.value < totalImages - 1) {
    carouselRef.value.next()
  } else if (e.deltaY < 0 && currentImageIndex.value > 0) {
    carouselRef.value.prev()
  }
}

// 处理模态框内滚动，防止边界穿透
const handleModalWheel = (e) => {
  const target = e.target
  // 找到最近的可滚动容器
  let scrollContainer = target.closest('.content-scroll')
  
  // 如果不在可滚动区域内，不做处理，让事件正常传播
  if (!scrollContainer) {
    return
  }
  
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer
  const hasScroll = scrollHeight > clientHeight
  
  // 如果没有滚动内容，不阻止
  if (!hasScroll) {
    return
  }
  
  const isAtTop = scrollTop <= 0
  const isAtBottom = scrollTop + clientHeight >= scrollHeight - 1
  const isScrollingUp = e.deltaY < 0
  const isScrollingDown = e.deltaY > 0
  
  // 到达边界时阻止滚动穿透到背景页面
  if ((isAtTop && isScrollingUp) || (isAtBottom && isScrollingDown)) {
    e.preventDefault()
  }
}

// 方法
const close = () => {
  emit('update:visible', false)
  replyingTo.value = null
  commentText.value = ''
  postDetail.value = null
}

const formatCount = (count) => {
  if (!count) return 0
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  } else if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}

const handleLike = async () => {
  if (!props.isLoggedIn) {
    emit('open-login')
    return
  }
  
  // 防抖：如果正在加载中，直接返回
  if (likeLoading.value) return
  
  const post = currentPost.value
  if (!post) return
  
  likeLoading.value = true
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
      // 如果点赞成功且之前是点踩状态，需要取消点踩状态并刷新点踩数
      if (post.isLiked && post.isRejected) {
        post.isRejected = false
        post.rejectCount = Math.max(0, (post.rejectCount || 0) - 1)
      }
      // 同步更新父组件的帖子数据
      emit('like', post)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    // 动画结束后解除防抖（600ms）
    setTimeout(() => { likeLoading.value = false }, 600)
  }
}

const handleReject = async () => {
  if (!props.isLoggedIn) {
    emit('open-login')
    return
  }
  
  // 防抖：如果正在加载中，直接返回
  if (rejectLoading.value) return
  
  const post = currentPost.value
  if (!post) return
  
  rejectLoading.value = true
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
      // 如果点踩成功且之前是点赞状态，需要取消点赞状态并刷新点赞数
      if (post.isRejected && post.isLiked) {
        post.isLiked = false
        post.likeCount = Math.max(0, (post.likeCount || 0) - 1)
      }
      // 同步更新父组件的帖子数据
      emit('reject', post)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('点踩操作失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    // 动画结束后解除防抖（600ms）
    setTimeout(() => { rejectLoading.value = false }, 600)
  }
}

const handleCollect = async () => {
  const post = currentPost.value
  if (!post) return
  
  if (!props.isLoggedIn) {
    emit('open-login')
    return
  }
  
  // 防抖：如果正在加载中，直接返回
  if (collectLoading.value) return
  
  collectLoading.value = true
  try {
    const response = await request(POST_API.FAVOR_POST, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo?.userId,
        postId: post.id,
        opFlag: post.isCollected ? 1 : 0 // 0-收藏，1-取消收藏
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      post.isCollected = !post.isCollected
      post.collectCount = result.data // 后端返回最新的收藏数
      // 同步更新父组件的帖子数据
      emit('collect', post)
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    // 动画结束后解除防抖（600ms）
    setTimeout(() => { collectLoading.value = false }, 600)
  }
}

const handleTopicClick = (tag) => {
  if (tag.id) {
    // 关闭弹窗并触发话题点击事件
    close()
    emit('topic-click', tag)
  }
}

const toggleCommentLike = (comment) => {
  comment.isLiked = !comment.isLiked
  comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1)
}

const replyTo = (comment) => {
  replyingTo.value = {
    commentId: comment.commentId,
    username: comment.username || '匿名用户'
  }
  commentText.value = ''
  expandInput()
}

// 判断是否是自己的评论
const isOwnComment = (userId) => {
  return props.userInfo?.userId === userId
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  if (!props.isLoggedIn || !props.userInfo) {
    emit('open-login')
    return
  }
  
  try {
    // 确认对话框
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'post-delete-confirm-dialog'
    })
    
    // 发送删除请求（与商品评论删除保持一致）
    const formData = new FormData()
    formData.append('commentId', commentId)
    formData.append('userId', props.userInfo.userId)
    
    const response = await request(POST_API.DELETE_POST_COMMENT, {
      method: 'POST',
      headers: {},
      body: formData
    })
    const result = await response.json()
    console.log('删除帖子评论响应:', result)
    
    if (result.code === 0 && result.data) {
      ElMessage.success('评论删除成功')
      // 刷新评论列表
      await fetchPostComments(currentPost.value.id)
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

const loadMoreReplies = (comment) => {
  ElMessage.info('加载更多回复...')
}

// 评论提交状态
const commentSubmitting = ref(false)

const submitComment = async () => {
  // 去除空白字符
  const trimmedContent = commentText.value?.trim() || ''
  
  // 空白字符校验
  if (commentText.value && !trimmedContent && mediaFiles.value.length === 0) {
    ElMessage.warning('评论内容不能只包含空白字符')
    return
  }
  
  if (!trimmedContent && mediaFiles.value.length === 0) return
  
  // 检查登录状态
  if (!props.isLoggedIn || !props.userInfo) {
    emit('open-login')
    return
  }
  
  commentSubmitting.value = true
  
  try {
    // 构建评论 DTO
    const commentData = {
      objectId: currentPost.value.id,
      userId: props.userInfo.userId,
      content: trimmedContent || ' '
    }
    if (replyingTo.value) {
      commentData.parentId = replyingTo.value.commentId
    }

    // 单次 multipart/form-data 请求，DTO 放 content 字段，文件放 files 字段
    const formData = new FormData()
    formData.append('content', new Blob([JSON.stringify(commentData)], { type: 'application/json' }))
    if (mediaFiles.value.length > 0) {
      mediaFiles.value.forEach(fileItem => formData.append('files', fileItem.file))
    }

    const response = await request(POST_API.POST_POST_COMMENT, {
      method: 'POST',
      headers: {},
      body: formData
    })
    
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('评论发布成功，审核通过后将显示在列表中')
      
      const parentCommentId = replyingTo.value?.commentId
      
      commentText.value = ''
      replyingTo.value = null
      mediaFiles.value.forEach(f => URL.revokeObjectURL(f.url))
      mediaFiles.value = []
      
      await fetchPostComments(currentPost.value.id)
      
      if (parentCommentId) {
        expandedReplies.value[parentCommentId] = true
      }
    } else {
      if (result.message && result.message.includes('实名认证')) {
        showAuthTipDialog.value = true
      } else {
        ElMessage.error(result.message || '评论发布失败')
      }
    }
  } catch (error) {
    console.error('评论提交异常:', error)
  } finally {
    commentSubmitting.value = false
  }
}

// 监听弹窗显示时获取详情和评论，并控制body滚动
watch(() => props.visible, (val) => {
  if (val) {
    if (props.post?.id) {
      fetchPostDetail(props.post.id)
      fetchPostComments(props.post.id)
    }
  } else {
    replyingTo.value = null
    commentText.value = ''
    postDetail.value = null
    comments.value = []
    expandedReplies.value = {}
    currentImageIndex.value = 0
    // 清理视频动画帧
    Object.keys(progressAnimationFrames.value).forEach(key => {
      if (progressAnimationFrames.value[key]) {
        cancelAnimationFrame(progressAnimationFrames.value[key])
      }
    })
    // 重置视频状态
    playerRefs.value = {}
    videoRefs.value = {}
    videoPlaying.value = {}
    videoEnded.value = {}
    videoProgress.value = {}
    videoCurrentTime.value = {}
    videoDuration.value = {}
    videoMuted.value = {}
    videoVolume.value = {}
    videoSpeed.value = {}
    showControls.value = {}
    showSpeedMenu.value = {}
    progressAnimationFrames.value = {}
    isDragging.value = {}
    showTimePreview.value = {}
    previewTime.value = {}
    previewPosition.value = {}
    // 清理媒体文件
    mediaFiles.value.forEach(f => URL.revokeObjectURL(f.url))
    mediaFiles.value = []
  }
})

// 监听全屏变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>


<style lang="scss" scoped>
/* 遮罩层 */
.post-detail-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: var(--spacing-xl);
  overflow: hidden;
}

/* 弹窗主体 */
.post-detail-modal {
  position: relative;
  width: 100%;
  max-width: 1100px;
  height: 85vh;
  max-height: 750px;
  background: var(--color-bg-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-2xl);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: var(--spacing-base);
  right: var(--spacing-base);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border: none;
  border-radius: var(--radius-round);
  color: var(--color-text-inverse);
  cursor: pointer;
  z-index: 10;
  transition: all var(--transition-base) var(--ease-out);
  
  &:hover {
    background: rgba(0, 0, 0, 0.5);
    transform: scale(1.1);
  }
}

/* 内容布局 */
.modal-content {
  display: flex;
  height: 100%;
}

/* 左侧媒体区 */
.media-section {
  flex: 0 0 60%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(255, 255, 255, 0.6);
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-md);
  color: rgba(255, 255, 255, 0.4);
  
  span {
    font-size: var(--font-size-sm);
  }
}

.carousel-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  
  :deep(.el-carousel) {
    height: 100%;
  }
  
  :deep(.el-carousel__container) {
    height: 100%;
  }
  
  :deep(.el-carousel__indicators) {
    display: none;
  }
  
  :deep(.el-carousel__indicator) {
    .el-carousel__button {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.4);
    }
    
    &.is-active .el-carousel__button {
      background: var(--color-primary);
    }
  }
  
  :deep(.el-carousel__arrow) {
    background: rgba(0, 0, 0, 0.4);
    opacity: 0;
    transition: opacity 0.3s ease, background 0.2s ease;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    
    &.el-carousel__arrow--left {
      left: 16px;
    }
    
    &.el-carousel__arrow--right {
      right: 16px;
    }
    
    &:hover {
      background: rgba(0, 0, 0, 0.6);
    }
  }
  
  &:hover :deep(.el-carousel__arrow) {
    opacity: 1;
  }
}

.image-counter {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 12px;
  z-index: 10;
  user-select: none;
}

.image-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
  
  .indicator-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.4);
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.7);
    }
    
    &.active {
      background: #fff;
      transform: scale(1.2);
    }
  }
}

.media-container {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000;
  
  .media-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
  
  .media-video {
    width: 100%;
    height: 100%;
    object-fit: contain;
    outline: none;
    background: #000;
    cursor: pointer;
  }
}

/* 自定义视频播放器 */
.custom-video-player {
  position: relative;
  width: 100%;
  height: 100%;
  background: #000;
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

/* 右侧信息区 */
.info-section {
  flex: 0 0 40%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-surface);
}

/* 作者头部 */
.author-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-base);
  border-bottom: 1px solid var(--color-border-light);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-round);
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.post-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* 内容滚动区 */
.content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-base);
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: var(--color-border-base);
    border-radius: 2px;
  }
}

.post-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  line-height: var(--line-height-base);
  margin: 0 0 var(--spacing-md);
}

.post-content {
  font-size: 15px;
  color: #2A2A2A;
  line-height: 1.8;
  
  // 使用 :deep() 穿透 scoped，影响 v-html 渲染的内容
  &.markdown-body {
    :deep(p) {
      margin: 0 0 0.75em;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    // 标题样式
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      line-height: 1.4;
      color: #1a1a1a;
      margin: 1em 0 0.5em;
      
      &:first-child {
        margin-top: 0;
      }
    }

    :deep(h1) {
      font-size: 1.75em;
      font-weight: 600;
    }

    :deep(h2) {
      font-size: 1.5em;
      font-weight: 600;
    }

    :deep(h3) {
      font-size: 1.25em;
      font-weight: 600;
    }
    
    :deep(h4), :deep(h5), :deep(h6) {
      font-size: 1.1em;
      font-weight: 600;
    }
    
    // 粗体、斜体、删除线
    :deep(strong) {
      font-weight: 600;
      color: #1a1a1a;
    }

    :deep(em) {
      font-style: italic;
    }

    :deep(s), :deep(del) {
      text-decoration: line-through;
      color: #999;
    }
    
    // 链接
    :deep(a) {
      color: #FF8A5B;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    // 列表样式
    :deep(ul), :deep(ol) {
      padding-left: 1.5em;
      margin: 0.75em 0;
      
      li {
        margin: 0.25em 0;
        
        p {
          margin: 0;
        }
      }
    }

    :deep(ul) {
      list-style-type: disc;
    }

    :deep(ol) {
      list-style-type: decimal;
    }
    
    // 引用块 - 主色边框
    :deep(blockquote) {
      background: #F9F9F9;
      border-left: 4px solid #FF8A5B;
      padding: 12px 16px;
      margin: 1em 0;
      border-radius: 0 8px 8px 0;
      
      p {
        font-style: italic;
        color: #666;
        margin: 0;
        
        &:not(:last-child) {
          margin-bottom: 0.5em;
        }
      }
    }
    
    // 行内代码
    :deep(code) {
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 0.9em;
      padding: 2px 6px;
      background: #F5F5F5;
      border-radius: 4px;
      color: #E45649;
    }
    
    // 代码块
    :deep(pre) {
      margin: 1em 0;
      padding: 16px;
      background: #1e1e1e;
      border-radius: 8px;
      overflow-x: auto;
      
      code {
        padding: 0;
        background: transparent;
        color: #d4d4d4;
        font-size: 14px;
        line-height: 1.6;
        border-radius: 0;
      }
    }
    
    // 图片
    :deep(img) {
      max-width: 100%;
      border-radius: 8px;
      margin: 0.75em 0;
    }
    
    // 分割线
    :deep(hr) {
      border: none;
      border-top: 2px solid #EDE1D9;
      margin: 2em 0;
    }
    
    // 表格
    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 1em 0;
      font-size: 14px;
      
      th, td {
        padding: 10px 14px;
        border: 1px solid #EDE1D9;
        text-align: left;
      }
      
      th {
        background: #F9F9F9;
        font-weight: 600;
        color: #1a1a1a;
      }
      
      tr:hover td {
        background: rgba(255, 138, 91, 0.03);
      }
    }
  }
}

/* 话题标签 */
.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
}

.topic-tag {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  cursor: pointer;
  
  &:hover {
    text-decoration: underline;
  }
}

/* 分割线 */
.divider {
  height: 1px;
  background: var(--color-border-light);
  margin: var(--spacing-lg) 0;
}

/* 评论区 */
.comments-section {
  .comments-header {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    margin-bottom: var(--spacing-md);
  }
  
  .comments-title {
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
  }
  
  .comments-count {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-base);
}

.comment-item {
  display: flex;
  gap: var(--spacing-md);
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-round);
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xs);
}

.comment-author {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.author-badge {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: rgba(255, 127, 80, 0.12);
  padding: 1px 6px;
  border-radius: var(--radius-xs);
}

.comment-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: var(--line-height-base);
  margin: 0 0 var(--spacing-xs);
  word-break: break-word;
  
  &.is-deleted {
    color: var(--color-text-tertiary);
    font-style: italic;
  }
}

/* 评论媒体 */
.comment-media {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin: var(--spacing-sm) 0;
  
  :deep(.media-image) {
    width: 80px;
    height: 80px;
    border-radius: var(--radius-sm);
    cursor: pointer;
  }
  
  .media-video {
    width: 120px;
    height: 80px;
    border-radius: var(--radius-sm);
    object-fit: cover;
  }
}

.reply-media {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin: var(--spacing-xs) 0;
  
  :deep(.media-image-small) {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-xs);
    cursor: pointer;
  }
  
  .media-video-small {
    width: 90px;
    height: 60px;
    border-radius: var(--radius-xs);
    object-fit: cover;
  }
}

.comment-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.comment-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.comment-actions {
  display: flex;
  gap: var(--spacing-md);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  cursor: pointer;
  
  &:hover, &.active {
    color: var(--color-primary);
  }
}

/* 回复列表 */
.replies-list {
  margin-top: var(--spacing-md);
  padding-left: var(--spacing-sm);
  border-left: 2px solid var(--color-border-light);
}

.reply-item {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-sm) 0;
  
  &:first-child {
    padding-top: 0;
  }
}

.reply-avatar {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-round);
  object-fit: cover;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
  font-size: var(--font-size-sm);
  line-height: var(--line-height-base);
}

.reply-author {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  margin-right: var(--spacing-xs);
}

.reply-to {
  color: var(--color-text-tertiary);
  margin-right: var(--spacing-xs);
  
  em {
    color: var(--color-primary);
    font-style: normal;
  }
}

.reply-target {
  color: var(--color-primary);
  margin-right: var(--spacing-xs);
}



.reply-text {
  color: var(--color-text-secondary);
  
  &.is-deleted {
    color: var(--color-text-tertiary);
    font-style: italic;
  }
}

.view-replies {
  color: var(--color-primary) !important;
}

.reply-footer {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xs);
}

.reply-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

.load-more-replies {
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  cursor: pointer;
  padding: var(--spacing-sm) 0;
  
  &:hover {
    text-decoration: underline;
  }
}

/* 空评论 */
.empty-comments {
  text-align: center;
  padding: var(--spacing-2xl) 0;
  color: var(--color-text-tertiary);
  
  .el-icon {
    color: var(--color-border-base);
    margin-bottom: var(--spacing-sm);
  }
  
  p {
    margin: 0;
    font-size: var(--font-size-sm);
  }
}

/* ========================================
   小红书风格沉浸式评论框 - ImmersiveCommentBox
   ======================================== */

// 动画曲线：Expo Ease Out - 快速展开、缓慢刹车
$expo-ease-out: cubic-bezier(0.19, 1, 0.22, 1);
$transition-duration: 0.7s;

.comment-bar-wrapper {
  position: relative;
  padding: var(--spacing-sm) var(--spacing-base);
  background: var(--color-bg-surface);
  border-top: 1px solid var(--color-border-light);
  z-index: 10;
}

/* 主交互行 */
.main-row {
  display: flex;
  align-items: center;
}

/* 中间输入框容器 - 动画核心 */
.input-wrapper {
  flex: 1;
  min-width: 0;
  background: var(--color-bg-base);
  border-radius: 16px;
  padding: 6px 14px;
  cursor: text;
  // 关闭时：无延迟
  transition: 
    border-radius $transition-duration $expo-ease-out,
    padding $transition-duration $expo-ease-out,
    background $transition-duration $expo-ease-out;
  
  .is-active & {
    border-radius: var(--radius-md);
    padding: var(--spacing-sm) var(--spacing-md);
    background: var(--color-bg-surface);
    border: 1px solid var(--color-border-light);
    // 展开时：延迟，等图标消失和工具栏撑开后再执行
    transition: 
      border-radius $transition-duration $expo-ease-out 0.3s,
      padding $transition-duration $expo-ease-out 0.3s,
      background $transition-duration $expo-ease-out 0.3s;
  }
}

/* 回复标签（收起状态） */
.reply-badge {
  display: inline-block;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: rgba(255, 138, 91, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
  margin-bottom: var(--spacing-xs);
}

/* 媒体预览区 */
.media-preview-area {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border-light);
  
  .preview-thumb {
    position: relative;
    width: 80px;
    height: 80px;
    border-radius: var(--radius-sm);
    overflow: hidden;
    cursor: pointer;
    
    img, video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    // hover 预览遮罩
    .thumb-hover-mask {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.2s ease;
      pointer-events: none;
    }

    &:hover .thumb-hover-mask {
      opacity: 1;
    }
    
    .thumb-remove {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 20px;
      height: 20px;
      background: rgba(0, 0, 0, 0.6);
      border: none;
      border-radius: var(--radius-round);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: all 0.2s ease;
      z-index: 1;
      
      &:hover {
        background: var(--color-danger);
      }
    }
    
    &:hover .thumb-remove {
      opacity: 1;
    }
  }
}

/* 评论视频预览弹窗 */
.comment-video-preview-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.comment-video-preview-content {
  position: relative;
  max-width: min(90vw, 960px);
  max-height: min(85vh, 720px);
  min-width: 480px;
  min-height: 270px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-video-player {
  width: 100%;
  height: 100%;
  min-width: 480px;
  min-height: 270px;
  max-width: min(90vw, 960px);
  max-height: min(85vh, 720px);
  border-radius: 8px;
  overflow: hidden;
}

.comment-video-close-btn {
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
  z-index: 10000;
  transition: background 0.2s ease;

  &:hover {
    background: rgba(220, 50, 50, 0.8);
  }
}

.video-preview-enter-active,
.video-preview-leave-active {
  transition: opacity 0.25s ease;
}
.video-preview-enter-from,
.video-preview-leave-to {
  opacity: 0;
}

/* 输入区域 */
.input-area {
  display: flex;
  align-items: center;
  
  textarea {
    width: 100%;
    border: none;
    background: transparent;
    font-size: var(--font-size-sm);
    line-height: 20px;
    color: var(--color-text-primary);
    resize: none;
    outline: none;
    font-family: inherit;
    padding: 0;
    margin: 0;
    
    &::placeholder {
      color: var(--color-text-placeholder);
    }
    
    // 收起状态：单行
    .comment-bar-wrapper:not(.is-active) & {
      height: 20px;
      overflow: hidden;
    }
    
    // 展开状态：多行
    .is-active & {
      min-height: 60px;
      max-height: 120px;
      overflow-y: auto;
    }
  }
}

/* 右侧数据区 - 挤压动画 */
.stats-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  max-width: 200px;
  overflow: hidden;
  margin-left: var(--spacing-sm);
  // 关闭时：正常速度
  transition: 
    max-width $transition-duration $expo-ease-out,
    opacity $transition-duration $expo-ease-out,
    transform $transition-duration $expo-ease-out,
    margin $transition-duration $expo-ease-out;
  
  // 激活时：快速消失，与工具栏撑开同步
  .is-active & {
    max-width: 0;
    opacity: 0;
    transform: translateX(10px);
    margin-left: 0;
    pointer-events: none;
    transition: 
      max-width 0.35s $expo-ease-out,
      opacity 0.25s ease-out,
      transform 0.35s $expo-ease-out,
      margin 0.35s $expo-ease-out;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--spacing-xs) var(--spacing-sm);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  white-space: nowrap;
  
  .stat-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    fill: none;
    stroke: #8F8F8F;
    stroke-width: 2;
    transition: all 0.3s ease;
  }
  
  .stat-count {
    font-size: 14px;
    color: #8F8F8F;
    min-width: 0;
    transition: all 0.3s ease;
  }
  
  &:hover {
    background: rgba(0, 0, 0, 0.04);
    .stat-icon { stroke: #666; }
    .stat-count { color: #666; }
  }
  
  // 动画进行中禁用点击
  &.animate-bounce,
  &.animate-spin-pop,
  &.animate-shake {
    pointer-events: none;
  }
}

// 点赞按钮 - 爱心
.stat-like {
  &.active {
    .stat-icon {
      fill: #FF8A5B;
      stroke: #FF8A5B;
    }
    .stat-count { color: #FF8A5B; }
  }
  
  &.animate-bounce .stat-icon {
    animation: bounce 0.6s ease-out;
    fill: #FF8A5B;
    stroke: #FF8A5B;
  }
}

// 收藏按钮 - 星星
.stat-favorite {
  &.active {
    .stat-icon {
      fill: #F7BA2A;
      stroke: #F7BA2A;
    }
    .stat-count { color: #F7BA2A; }
  }
  
  &.animate-spin-pop .stat-icon {
    animation: spinPop 0.6s ease-out;
    fill: #F7BA2A;
    stroke: #F7BA2A;
  }
}

// 点踩按钮 - 空心变实心
.stat-dislike {
  .stat-icon {
    fill: none;
    stroke: #8F8F8F;
    stroke-width: 1.5;
  }
  
  &.active {
    .stat-icon { 
      fill: #2A2A2A; 
      stroke: #2A2A2A;
    }
    .stat-count { color: #2A2A2A; }
  }
  
  &.animate-shake .stat-icon {
    animation: shake 0.6s ease-out;
    fill: #2A2A2A;
    stroke: #2A2A2A;
  }
}

// 数字上浮动画
.animate-count {
  animation: countPop 0.3s ease-out;
}

// A. 点赞动画 - 心脏跳动
@keyframes bounce {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

// B. 收藏动画 - 星星旋转点亮
@keyframes spinPop {
  0% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.2) rotate(15deg); }
  100% { transform: scale(1) rotate(0deg); }
}

// C. 点踩动画 - 左右抖动
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-3px); }
  40%, 80% { transform: translateX(3px); }
}

// 数字上浮淡入
@keyframes countPop {
  0% { transform: translateY(4px); opacity: 0.5; }
  100% { transform: translateY(0); opacity: 1; }
}

/* 扩展工具栏 */
.tool-row {
  display: grid;
  grid-template-rows: 0fr;
  opacity: 0;
  // 关闭时：正常速度
  transition: 
    grid-template-rows $transition-duration $expo-ease-out,
    opacity $transition-duration $expo-ease-out,
    margin $transition-duration $expo-ease-out;
  margin-top: 0;
  
  .is-active & {
    grid-template-rows: 1fr;
    opacity: 1;
    margin-top: var(--spacing-sm);
    // 展开时：快速撑开，与图标消失同步
    transition: 
      grid-template-rows 0.35s $expo-ease-out,
      opacity 0.35s $expo-ease-out,
      margin 0.35s $expo-ease-out;
  }
}

.tool-row-inner {
  overflow: hidden;
  min-height: 0;
}

/* 回复提示（展开时） */
.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background: rgba(255, 138, 91, 0.08);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  
  strong {
    color: var(--color-primary);
  }
  
  .hint-cancel {
    width: 20px;
    height: 20px;
    border: none;
    background: transparent;
    color: var(--color-text-tertiary);
    font-size: 18px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--radius-round);
    transition: all 0.2s;
    
    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: var(--color-text-secondary);
    }
  }
}

.tool-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tool-left {
  display: flex;
  gap: var(--spacing-xs);
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s ease;
  
  &:hover:not(.disabled) {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.08);
  }
  
  &.disabled {
    color: var(--color-text-disabled);
    cursor: not-allowed;
    opacity: 0.5;
  }
}

.tool-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.cancel-btn {
  padding: var(--spacing-xs) var(--spacing-md);
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  cursor: pointer;
  border-radius: var(--radius-circle);
  transition: all 0.2s ease;
  
  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

.send-btn {
  padding: var(--spacing-xs) var(--spacing-lg);
  border: none;
  background: var(--color-border-base);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: not-allowed;
  border-radius: var(--radius-circle);
  transition: all 0.25s $expo-ease-out;
  
  &.active {
    background: var(--color-primary);
    color: #fff;
    cursor: pointer;
    
    &:hover {
      background: var(--color-primary-dark);
      transform: scale(1.02);
    }
    
    &:active {
      transform: scale(0.98);
    }
  }
  
  &.is-submitting {
    opacity: 0.7;
    cursor: wait;
  }
}

/* ======================================== */

/* 底部操作栏（保留兼容） */
.action-bar {
  padding: var(--spacing-sm) var(--spacing-base);
  border-top: 1px solid var(--color-border-light);
  background: var(--color-bg-surface);
  overflow: hidden;
  position: relative;
}

/* 第一行布局 */
.input-row-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  position: relative;
}

/* 统一的输入框容器 */
.comment-input-box {
  flex: 1;
  min-width: 0;
  background: var(--color-bg-base);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-circle);
  padding: 6px var(--spacing-sm);
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  transform-origin: left center;
  
  &:hover:not(.expanded) {
    border-color: var(--color-primary);
    background: var(--color-bg-surface);
  }
  
  /* 展开状态 */
  &.expanded {
    border-radius: var(--radius-md);
    padding: var(--spacing-sm);
    cursor: default;
    
    &:focus-within {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(255, 138, 91, 0.1);
    }
  }
}

/* 收起状态的内容 */
.mini-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  
  span {
    font-size: var(--font-size-xs);
    color: var(--color-text-placeholder);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .el-icon {
    color: var(--color-text-tertiary);
    flex-shrink: 0;
  }
}

/* 展开状态的内容 */
.expanded-content {
  :deep(.el-textarea__inner) {
    background: transparent;
    border: none;
    padding: var(--spacing-xs) var(--spacing-sm);
    font-size: var(--font-size-sm);
    resize: none;
    box-shadow: none !important;
  }
  
  :deep(.el-input__count) {
    background: transparent !important;
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
    right: var(--spacing-sm);
    bottom: var(--spacing-xs);
  }
}

/* 输入框内的工具栏 */
.input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--spacing-sm);
  margin-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
}

.input-actions {
  display: flex;
  gap: var(--spacing-sm);
  
  :deep(.el-button--primary) {
    background: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover:not(:disabled) {
      background: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
  }
}



/* 操作按钮 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-shrink: 0;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1), 
              opacity 0.3s ease,
              width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.pushed-out {
    transform: translateX(50px);
    opacity: 0;
    width: 0;
    gap: 0;
    overflow: hidden;
    pointer-events: none;
  }
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
  padding: var(--spacing-xs);
  
  span {
    font-size: var(--font-size-xs);
  }
  
  &:hover {
    color: var(--color-primary);
  }
  
  &.active {
    color: var(--color-primary);
    
    .action-icon {
      animation: heartBeat 0.3s var(--ease-spring);
    }
  }
}

/* 展开状态 */
.action-bar.expanded {
  padding: var(--spacing-md) var(--spacing-base);
}

/* 回复提示 */
.replying-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background: rgba(255, 138, 91, 0.08);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  
  strong {
    color: var(--color-primary);
  }
}

/* 媒体预览条 */
.media-preview-bar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-bg-base);
  border-radius: var(--radius-sm);
  
  .preview-item {
    position: relative;
    width: 52px;
    height: 52px;
    border-radius: var(--radius-xs);
    overflow: hidden;
    
    img, video {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    
    .video-badge {
      position: absolute;
      bottom: 2px;
      right: 2px;
      width: 18px;
      height: 18px;
      background: rgba(0, 0, 0, 0.6);
      border-radius: var(--radius-xs);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }
    
    .remove-btn {
      position: absolute;
      top: 2px;
      right: 2px;
      width: 16px;
      height: 16px;
      background: rgba(0, 0, 0, 0.6);
      border: none;
      border-radius: var(--radius-round);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      opacity: 0;
      transition: opacity 0.2s;
      
      &:hover {
        background: var(--color-danger);
      }
    }
    
    &:hover .remove-btn {
      opacity: 1;
    }
  }
}

/* 展开的评论输入区 */
.comment-input-expanded {
  :deep(.el-textarea__inner) {
    background: var(--color-bg-base);
    border: 1px solid var(--color-border-light);
    border-radius: var(--radius-md);
    padding: var(--spacing-sm) var(--spacing-md);
    font-size: var(--font-size-sm);
    resize: none;
    
    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(255, 138, 91, 0.1);
    }
  }
  
  :deep(.el-input__count) {
    background: transparent !important;
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }
}

.expanded-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
}

.media-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.media-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--color-text-tertiary);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
  
  &:hover:not(.disabled) {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.08);
  }
  
  &.disabled {
    color: var(--color-text-disabled);
    cursor: not-allowed;
  }
}

.expanded-actions {
  display: flex;
  gap: var(--spacing-sm);
  
  :deep(.el-button--primary) {
    background: var(--color-primary);
    border-color: var(--color-primary);
    
    &:hover:not(:disabled) {
      background: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
    
    &:disabled {
      background: var(--color-primary-lighter);
      border-color: var(--color-primary-lighter);
    }
  }
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s var(--ease-out);
  
  .post-detail-modal {
    transition: all 0.3s var(--ease-out);
  }
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  
  .post-detail-modal {
    transform: scale(0.9);
    opacity: 0;
  }
}

/* 视频缩略图样式 */
.media-video-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  cursor: pointer;
  
  video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .video-play-btn {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.4);
    color: #fff;
    transition: background 0.2s;
  }
  
  &:hover .video-play-btn {
    background: rgba(0, 0, 0, 0.6);
  }
  
  &.small {
    width: 60px;
    height: 60px;
  }
}

/* 视频播放器弹窗 */
.video-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 24px;
}

.video-modal-content {
  position: relative;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  // 默认尺寸，加载后会被动态样式覆盖
  width: auto;
  max-width: 90vw;
  max-height: 80vh;
}

.video-player-custom {
  width: 100%;
  height: 100%;
  display: block;
  outline: none;
}

.video-close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
  
  &:hover {
    background: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
  }
}

/* 视频弹窗动画 */
.video-modal-enter-active,
.video-modal-leave-active {
  transition: all 0.3s ease;
}

.video-modal-enter-active .video-modal-content,
.video-modal-leave-active .video-modal-content {
  transition: all 0.3s ease;
}

.video-modal-enter-from,
.video-modal-leave-to {
  opacity: 0;
}

.video-modal-enter-from .video-modal-content,
.video-modal-leave-to .video-modal-content {
  transform: scale(0.9);
  opacity: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .post-detail-overlay {
    padding: 0;
  }
  
  .post-detail-modal {
    height: 100vh;
    max-height: none;
    border-radius: 0;
  }
  
  .modal-content {
    flex-direction: column;
  }
  
  .media-section {
    flex: 0 0 45%;
  }
  
  .info-section {
    flex: 1;
  }
}

/* 实名认证提示弹窗样式 */
.auth-tip-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 10002;
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
  animation: authModalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.auth-tip-modal .modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.auth-tip-modal .modal-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.auth-tip-modal .modal-title .el-icon {
  color: #e6a23c;
}

.auth-tip-modal .modal-close {
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
}

.auth-tip-modal .modal-close:hover {
  background: #eee;
  transform: rotate(90deg);
}

.auth-tip-modal .modal-close .el-icon {
  font-size: 16px;
  color: #666;
}

.auth-tip-modal .modal-body {
  padding: 24px;
}

.auth-tip-modal .auth-tip-content {
  text-align: center;
}

.auth-tip-modal .auth-tip-content p {
  margin: 0 0 12px;
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.auth-tip-modal .auth-tip-content .auth-tip-hint {
  color: #909399;
  font-size: 13px;
}

.auth-tip-modal .modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px 24px;
}

.auth-tip-modal .btn-cancel,
.auth-tip-modal .btn-confirm {
  flex: 1;
  height: 44px;
  font-size: 15px;
  border-radius: 22px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.auth-tip-modal .btn-cancel {
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.auth-tip-modal .btn-cancel:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
  color: #333;
}

.auth-tip-modal .btn-confirm {
  background: linear-gradient(135deg, #FF8A5B 0%, #E67A4D 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
}

.auth-tip-modal .btn-confirm:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
}

@keyframes authModalSlideIn {
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
</style>

<!-- 全局样式 - 删除确认对话框层级 -->
<style>
.post-delete-confirm-dialog {
  z-index: 10001 !important;
}

.el-overlay.is-message-box {
  z-index: 10000 !important;
}
</style>
