<template>
  <ElConfigProvider size="default" :locale="locales[language]" :z-index="3000">
    <RouterView></RouterView>
  </ElConfigProvider>
</template>

<script setup lang="ts">
  import { useUserStore } from './store/modules/user'
  import zh from 'element-plus/es/locale/lang/zh-cn'
  import en from 'element-plus/es/locale/lang/en'
  import { toggleTransition } from './utils/ui/animation'
  import { checkStorageCompatibility } from './utils/storage'
  import { initializeTheme } from './hooks/core/useTheme'
  import { provide, ref, onUnmounted, watch, computed } from 'vue'
  import {
    getWebSocketUrl,
    type CommonMessage
  } from './api/chat'

  const userStore = useUserStore()
  const { language, isLogin, accessToken, info } = storeToRefs(userStore)

  const locales = {
    zh: zh,
    en: en
  }

  onBeforeMount(() => {
    toggleTransition(true)
    initializeTheme()
  })

  onMounted(() => {
    checkStorageCompatibility()
    toggleTransition(false)
    // 如果已登录，立即建立 WebSocket 连接
    if (isLogin.value && accessToken.value) {
      initWebSocket()
    }
  })

  onUnmounted(() => {
    closeWebSocket()
  })

  // ============ WebSocket 连接管理 ============
  let ws: WebSocket | null = null
  const wsStatus = ref<'disconnected' | 'connecting' | 'connected'>('disconnected')
  const wsMessages = ref<CommonMessage[]>([])
  const RECONNECT_INTERVAL = 5000
  const MAX_RECONNECT_ATTEMPTS = 10
  let reconnectTimer: number | null = null
  let reconnectAttempts = 0

  // 初始化 WebSocket 连接
  const initWebSocket = () => {
    // 只有在登录状态下才连接
    if (!isLogin.value || !accessToken.value) {
      return
    }

    // 避免重复连接
    if (ws?.readyState === WebSocket.OPEN || ws?.readyState === WebSocket.CONNECTING) {
      return
    }

    // 如果超过最大重连次数，停止重连
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      return
    }

    wsStatus.value = 'connecting'

    try {
      ws = new WebSocket(getWebSocketUrl(accessToken.value))

      ws.onopen = () => {
        wsStatus.value = 'connected'
        reconnectAttempts = 0
        if (reconnectTimer) {
          clearTimeout(reconnectTimer)
          reconnectTimer = null
        }
      }

      ws.onmessage = (event) => {
        try {
          const message: CommonMessage = JSON.parse(event.data)
          handleWebSocketMessage(message)
        } catch (error) {
          console.error('[App] 解析消息失败:', error)
        }
      }

      ws.onerror = (error) => {
        console.error('[App] WebSocket 错误:', error)
        wsStatus.value = 'disconnected'
      }

      ws.onclose = (event) => {
        wsStatus.value = 'disconnected'
        ws = null
        // 如果仍在登录状态，且不是正常关闭(1000, 1001)，才自动重连
        // 1000 = 正常关闭, 1001 = 终端离开（如浏览器关闭）
        if (isLogin.value && event.code !== 1000 && event.code !== 1001) {
          scheduleReconnect()
        }
      }
    } catch (error) {
      console.error('[App] WebSocket 初始化失败:', error)
      wsStatus.value = 'disconnected'
      if (isLogin.value) {
        scheduleReconnect()
      }
    }
  }

  // 处理收到的消息
  const handleWebSocketMessage = (message: CommonMessage) => {
    // 将消息添加到消息列表
    wsMessages.value.push(message)

    // 限制消息列表长度，避免内存无限增长
    if (wsMessages.value.length > 1000) {
      wsMessages.value = wsMessages.value.slice(-500)
    }
  }

  // 发送消息（支持等待重连）
  const sendWebSocketMessage = async (message: CommonMessage): Promise<boolean> => {
    // 如果当前已连接，直接发送
    if (ws && ws.readyState === WebSocket.OPEN) {
      try {
        ws.send(JSON.stringify(message))
        return true
      } catch (error) {
        console.error('[App] 发送消息失败:', error)
        return false
      }
    }

    // 如果正在重连中，等待连接恢复后再发送（最多等 6 秒）
    if (isLogin.value && (wsStatus.value === 'connecting' || reconnectTimer)) {
      return new Promise((resolve) => {
        const maxWait = 6000
        const interval = 200
        let waited = 0
        const timer = setInterval(() => {
          waited += interval
          if (ws && ws.readyState === WebSocket.OPEN) {
            clearInterval(timer)
            try {
              ws.send(JSON.stringify(message))
              resolve(true)
            } catch {
              resolve(false)
            }
          } else if (waited >= maxWait) {
            clearInterval(timer)
            console.error('[App] WebSocket 重连超时，消息发送失败')
            resolve(false)
          }
        }, interval)
      })
    }

    console.error('[App] WebSocket 未连接')
    return false
  }

  // 计划重连
  const scheduleReconnect = () => {
    if (reconnectTimer) return
    reconnectAttempts++
    reconnectTimer = window.setTimeout(() => {
      reconnectTimer = null
      initWebSocket()
    }, RECONNECT_INTERVAL)
  }

  // 关闭 WebSocket
  const closeWebSocket = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    reconnectAttempts = 0
    if (ws) {
      ws.close()
      ws = null
    }
    wsStatus.value = 'disconnected'
  }

  // 监听登录状态变化
  watch(isLogin, (newVal) => {
    if (newVal && accessToken.value) {
      // 登录后建立连接
      initWebSocket()
    } else {
      // 登出后关闭连接
      closeWebSocket()
    }
  })

  // 提供给子组件使用
  provide('webSocket', {
    status: wsStatus,
    messages: wsMessages,
    sendMessage: sendWebSocketMessage,
    initWebSocket,
    closeWebSocket,
    currentUserId: computed(() => info.value?.userId)
  })
</script>
