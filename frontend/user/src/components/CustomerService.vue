<script setup>
import { ref } from 'vue'
import { Service, Close } from '@element-plus/icons-vue'

const showChat = ref(false)
const message = ref('')
const messages = ref([
  { id: 1, type: 'system', content: '您好！有什么可以帮助您的吗？', time: '10:00' }
])

const sendMessage = () => {
  if (!message.value.trim()) return
  messages.value.push({
    id: Date.now(),
    type: 'user',
    content: message.value,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  message.value = ''
  // 模拟客服回复
  setTimeout(() => {
    messages.value.push({
      id: Date.now(),
      type: 'system',
      content: '感谢您的咨询，客服正在为您处理中...',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  }, 1000)
}
</script>

<template>
  <div class="customer-service">
    <transition name="chat-fade">
      <div v-if="showChat" class="chat-window">
        <div class="chat-header">
          <span>在线客服</span>
          <el-icon @click="showChat = false"><Close /></el-icon>
        </div>
        <div class="chat-messages">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="chat-message"
            :class="msg.type"
          >
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="message"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </transition>
    
    <div class="service-btn" @click="showChat = !showChat">
      <el-icon :size="24"><Service /></el-icon>
    </div>
  </div>
</template>

<style scoped lang="scss">
.customer-service {
  position: fixed;
  right: var(--spacing-xl);
  bottom: var(--spacing-xl);
  z-index: 1000;
}

.service-btn {
  width: 56px;
  height: 56px;
  background: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  transition: transform var(--transition-fast);
  
  &:hover {
    transform: scale(1.1);
  }
}

.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 360px;
  height: 480px;
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-base) var(--spacing-lg);
  background: var(--color-primary);
  color: white;
  font-weight: var(--font-weight-semibold);
  
  .el-icon {
    cursor: pointer;
  }
}

.chat-messages {
  flex: 1;
  padding: var(--spacing-base);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-base);
}

.chat-message {
  max-width: 80%;
  
  &.system {
    align-self: flex-start;
    
    .message-content {
      background: #f0f0f0;
      color: var(--color-text-primary);
    }
  }
  
  &.user {
    align-self: flex-end;
    
    .message-content {
      background: var(--color-primary);
      color: white;
    }
  }
  
  .message-content {
    padding: var(--spacing-sm) var(--spacing-base);
    border-radius: var(--radius-base);
    line-height: 1.5;
  }
  
  .message-time {
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
    margin-top: var(--spacing-xs);
  }
}

.chat-input {
  display: flex;
  gap: var(--spacing-sm);
  padding: var(--spacing-base);
  border-top: 1px solid var(--color-border-light);
}

.chat-fade-enter-active,
.chat-fade-leave-active {
  transition: all 0.3s ease;
}

.chat-fade-enter-from,
.chat-fade-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

@media (max-width: 768px) {
  .chat-window {
    width: 300px;
    height: 400px;
  }
}
</style>
