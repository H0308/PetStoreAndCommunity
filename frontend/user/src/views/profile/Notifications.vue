<script setup>
import { ref, computed, onMounted, watch, inject } from 'vue'
import { Bell, Check, Delete, View, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchNotifyList,
  readNotify,
  readAllNotify,
  deleteNotify,
  fetchUnreadCount,
  NOTIFY_TYPE_ORDER,
  NOTIFY_TYPE_AUDIT,
  NOTIFY_TYPE_LIKE_REPLY,
  NOTIFY_TYPE_BAN,
  NOTIFY_TYPE_MAP
} from '../../api/notify.js'
import { MESSAGE_TYPE_SYSTEM } from '../../api/chat.js'

// 消息列表
const messages = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

// 筛选条件
const filterType = ref(null)

// 未读数量
const unreadTotalCount = ref(0)

// 详情弹窗
const detailDialogVisible = ref(false)
const currentDetail = ref(null)

// 计算未读消息数量
const unreadCount = computed(() => unreadTotalCount.value)

// 筛选选项
const filterOptions = [
  { label: '全部通知', value: null },
  { label: NOTIFY_TYPE_MAP[NOTIFY_TYPE_ORDER].label, value: NOTIFY_TYPE_ORDER },
  { label: NOTIFY_TYPE_MAP[NOTIFY_TYPE_AUDIT].label, value: NOTIFY_TYPE_AUDIT },
  { label: NOTIFY_TYPE_MAP[NOTIFY_TYPE_LIKE_REPLY].label, value: NOTIFY_TYPE_LIKE_REPLY },
  { label: NOTIFY_TYPE_MAP[NOTIFY_TYPE_BAN].label, value: NOTIFY_TYPE_BAN }
]

// 获取类型标签
const getTypeLabel = (type) => {
  return NOTIFY_TYPE_MAP[type]?.label || '系统通知'
}

// 获取类型颜色
const getTypeColor = (type) => {
  return NOTIFY_TYPE_MAP[type]?.color || '#909399'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载消息列表
const loadMessages = async () => {
  loading.value = true
  try {
    const result = await fetchNotifyList({
      currentPage: currentPage.value,
      pageSize: pageSize.value,
      type: filterType.value
    })
    if (result.code === 0) {
      const data = result.data
      messages.value = (data.totalRecords || []).map(item => ({
        id: item.notifyId,
        notifyId: item.notifyId,
        title: item.title,
        content: item.content,
        type: item.type,
        status: item.status,
        time: item.createTime,
        read: item.status === 1
      }))
      total.value = data.totalCount || 0
      totalPages.value = data.totalPages || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '获取通知列表失败')
  } finally {
    loading.value = false
  }
}

// 加载未读数量
const loadUnreadCount = async () => {
  try {
    const count = await fetchUnreadCount()
    unreadTotalCount.value = count
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 标记单条已读
const markAsRead = async (msg) => {
  if (msg.read) return
  try {
    await readNotify(msg.notifyId)
    msg.read = true
    msg.status = 1
    // 重新获取真实未读数量
    await loadUnreadCount()
    // 通知导航栏更新红点
    window.dispatchEvent(new CustomEvent('notify-count-changed', { detail: { count: unreadTotalCount.value } }))
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '标记已读失败')
  }
}

// 标记全部已读
const markAllAsRead = async () => {
  const unreadIds = messages.value.filter(msg => !msg.read).map(msg => msg.notifyId)
  if (unreadIds.length === 0) {
    ElMessage.info('没有未读消息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定将 ${unreadIds.length} 条消息标记为已读吗？`,
      '确认操作',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )

    await readAllNotify(unreadIds)
    messages.value.forEach(msg => {
      msg.read = true
      msg.status = 1
    })
    // 重新获取真实未读数量（因为其他页可能还有未读）
    await loadUnreadCount()
    // 通知导航栏更新红点
    window.dispatchEvent(new CustomEvent('notify-count-changed', { detail: { count: unreadTotalCount.value } }))
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '批量标记已读失败')
    }
  }
}

// 删除消息
const deleteMessage = async (msg) => {
  try {
    await ElMessageBox.confirm(
      '确定删除这条消息吗？删除后将无法恢复。',
      '确认删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    await deleteNotify(msg.notifyId)
    messages.value = messages.value.filter(item => item.id !== msg.id)
    if (!msg.read) {
      // 重新获取真实未读数量
      await loadUnreadCount()
      // 通知导航栏更新红点
      window.dispatchEvent(new CustomEvent('notify-count-changed', { detail: { count: unreadTotalCount.value } }))
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 查看详情
const viewDetail = async (msg) => {
  currentDetail.value = { ...msg }
  detailDialogVisible.value = true
  // 如果是未读消息，自动标记为已读
  if (!msg.read) {
    await markAsRead(msg)
  }
}

// 关闭详情
const closeDetail = () => {
  detailDialogVisible.value = false
  currentDetail.value = null
}

// 处理页码变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadMessages()
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  loadMessages()
}

// 监听筛选条件变化
watch(filterType, handleFilterChange)

// 注入 WebSocket
const webSocket = inject('webSocket', null)

// 监听 WebSocket 消息，收到系统通知时刷新列表
watch(
  () => webSocket?.messages?.value,
  (messages) => {
    if (!messages?.length) return
    const latestMessage = messages[messages.length - 1]
    // 收到系统通知时刷新列表和未读数量
    if (latestMessage?.type === MESSAGE_TYPE_SYSTEM) {
      loadMessages()
      loadUnreadCount()
    }
  },
  { deep: true }
)

onMounted(() => {
  loadMessages()
  loadUnreadCount()
})

// 暴露刷新方法给父组件
defineExpose({
  refresh: () => {
    loadMessages()
    loadUnreadCount()
  }
})
</script>

<template>
  <div class="notifications">
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><Bell /></el-icon>
        通知箱
        <span v-if="unreadCount > 0" class="unread-tag">{{ unreadCount > 99 ? '99+' : unreadCount }}条未读</span>
      </h2>
      <div class="header-actions">
        <div class="filter-item">
          <span class="filter-label">通知类型</span>
          <el-select v-model="filterType" placeholder="全部类型" clearable>
            <el-option
              v-for="option in filterOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </div>
        <el-button
          class="read-all-btn"
          type="primary"
          @click="markAllAsRead"
          :disabled="unreadCount === 0"
        >
          <el-icon><Check /></el-icon>
          全部已读
        </el-button>
      </div>
    </div>

    <div class="message-list" v-loading="loading">
      <div v-if="messages.length === 0" class="empty-state">
        <el-empty description="暂无消息" />
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-item"
        :class="{ unread: !msg.read }"
      >
        <div class="message-indicator" v-if="!msg.read"></div>
        <div class="message-content">
          <div class="message-header">
            <h4 class="message-title">{{ msg.title }}</h4>
            <span class="message-time">{{ formatTime(msg.time) }}</span>
          </div>
          <p class="message-text">{{ msg.content }}</p>
        </div>
        <div class="message-actions">
          <el-button text size="small" @click="viewDetail(msg)">
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
          <el-button v-if="!msg.read" text size="small" @click="markAsRead(msg)">
            标记已读
          </el-button>
          <el-button text size="small" type="danger" @click="deleteMessage(msg)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleFilterChange"
      />
    </div>

    <!-- 详情弹窗 - 自定义模态框 -->
    <Transition name="notify-modal">
      <div v-if="detailDialogVisible" class="notify-modal-overlay" @click.self="closeDetail">
        <div class="notify-modal">
          <div class="modal-header">
            <h3 class="modal-title">通知详情</h3>
            <button class="modal-close" @click="closeDetail">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          <div v-if="currentDetail" class="modal-body">
            <div class="detail-time">{{ formatTime(currentDetail.time) }}</div>
            <h3 class="detail-title">{{ currentDetail.title }}</h3>
            <div class="detail-content-text">
              {{ currentDetail.content }}
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped lang="scss">
.notifications {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 600px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xl);
    padding-bottom: var(--spacing-base);
    border-bottom: 1px solid var(--color-border-light);
    flex-shrink: 0;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: var(--spacing-base);

    .filter-item {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      .filter-label {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
        white-space: nowrap;
      }

      :deep(.el-select) {
        width: 140px;

        .el-input__wrapper {
          border-radius: var(--radius-sm);
          box-shadow: 0 0 0 1px var(--color-border-base) inset;

          &:hover {
            box-shadow: 0 0 0 1px var(--color-primary) inset;
          }

          &.is-focus,
          &.is-focused {
            box-shadow: 0 0 0 1px var(--color-primary) inset !important;
          }
        }

        .el-input.is-focus .el-input__wrapper {
          box-shadow: 0 0 0 1px var(--color-primary) inset !important;
        }
      }
    }

    .read-all-btn {
      border-radius: var(--radius-sm);
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border-color: var(--color-primary);
      color: #fff;
      padding: 8px 16px;
      font-size: var(--font-size-sm);
      transition: all var(--transition-fast);

      &:hover:not(:disabled) {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(255, 138, 91, 0.3);
      }

      &:disabled {
        background: linear-gradient(135deg, #ccc 0%, #999 100%);
        border-color: #ccc;
        opacity: 0.6;
      }

      .el-icon {
        margin-right: 4px;
      }
    }
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  flex: 1;
  overflow-y: auto;
  min-height: 300px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-base);
  padding: var(--spacing-lg);
  border-radius: var(--radius-base);
  background: #fafafa;
  transition: all var(--transition-fast);
  position: relative;

  &.unread {
    background: var(--color-secondary-light);

    .message-title {
      font-weight: var(--font-weight-bold);
    }
  }

  &.ban-notice {
    background: #fafafa;
  }

  &:hover {
    box-shadow: var(--shadow-sm);
  }
}

.message-indicator {
  position: absolute;
  left: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border-radius: 50%;
}

.message-content {
  flex: 1;
  padding-left: var(--spacing-base);
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xs);
  gap: var(--spacing-sm);
}

.message-title-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex: 1;
  min-width: 0;
}

.type-tag {
  flex-shrink: 0;
}

.message-title {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  flex-shrink: 0;
}

.message-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.message-actions {
  display: flex;
  gap: var(--spacing-xs);
  flex-shrink: 0;
}

// 分页固定在底部
.pagination-wrapper {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
  margin-top: var(--spacing-xl);
  background: var(--color-bg-surface);
  border-top: 1px solid var(--color-border-light);

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
    }

    // 每页条数选择器
    .el-pagination__sizes {
      .el-select {
        .el-input__wrapper {
          border-radius: var(--radius-sm);
          box-shadow: 0 0 0 1px var(--color-border-base) inset;

          &:hover,
          &.is-focus {
            box-shadow: 0 0 0 1px var(--color-primary) inset;
          }
        }
      }
    }
  }
}

.unread-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  background-color: #e0e0e0;
  color: #666;
  font-size: 12px;
  font-weight: normal;
  border-radius: 12px;
  margin-left: 8px;
}

// 自定义模态框样式
.notify-modal-overlay {
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

.notify-modal {
  width: 560px;
  max-width: 100%;
  max-height: 80vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: notifyModalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;

    .modal-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
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
    overflow-y: auto;
    flex: 1;

    .detail-time {
      font-size: 14px;
      color: #999;
      margin-bottom: 12px;
    }

    .detail-title {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0 0 20px;
      line-height: 1.5;
    }

    .detail-content-text {
      font-size: 15px;
      color: #666;
      line-height: 1.8;
      white-space: pre-wrap;
      word-break: break-all;
      background: #f8f8f8;
      padding: 20px;
      border-radius: 12px;
    }
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 24px 24px;
    border-top: 1px solid #eee;

    .btn-confirm {
      padding: 10px 28px;
      border: none;
      background: var(--color-primary, #FF8A5B);
      color: #fff;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;

      &:hover {
        background: var(--color-primary-dark, #e65c00);
        transform: translateY(-1px);
      }
    }
  }
}

@keyframes notifyModalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.notify-modal-enter-active,
.notify-modal-leave-active {
  transition: opacity 0.3s ease;
}

.notify-modal-enter-from,
.notify-modal-leave-to {
  opacity: 0;
}

.notify-modal-enter-active .notify-modal,
.notify-modal-leave-active .notify-modal {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.notify-modal-enter-from .notify-modal,
.notify-modal-leave-to .notify-modal {
  opacity: 0;
  transform: translateY(-30px) scale(0.95);
}

// 响应式
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-base);
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .message-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-xs);
  }

  .message-title-wrapper {
    width: 100%;
  }

  .message-actions {
    flex-wrap: wrap;
  }
}
</style>
