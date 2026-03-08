<!-- 布局容器 -->
<template>
  <!-- 路由初始化中显示加载动画 -->
  <div v-if="!isRouteReady" class="app-loading">
    <div class="loading-spinner">
      <svg viewBox="0 0 40 40" class="spinner-svg">
        <circle cx="20" cy="20" r="18" fill="none" stroke-width="3" />
      </svg>
      <div class="loading-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <span class="progress-text">{{ progress }}%</span>
      </div>
      <span class="loading-text">{{ loadingText }}</span>
    </div>
  </div>

  <!-- 路由初始化完成后显示正常布局 -->
  <div v-else class="app-layout">
    <aside id="app-sidebar">
      <ArtSidebarMenu />
    </aside>

    <main id="app-main">
      <div id="app-header">
        <ArtHeaderBar />
      </div>
      <div id="app-content">
        <ArtPageContent />
      </div>
    </main>

    <div id="app-global">
      <ArtGlobalComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
  import {
    getRouteInitCompleted,
    getLoadingStatus,
    setLoadingStatusCallback
  } from '@/router/guards/beforeEach'
  import { useRouter } from 'vue-router'

  defineOptions({ name: 'AppLayout' })

  const router = useRouter()
  const isRouteReady = ref(false)
  const loadingText = ref('正在初始化...')
  const progress = ref(0)

  // 预加载所有已注册路由的组件
  async function preloadAllRouteComponents() {
    const routes = router.getRoutes()
    const loadPromises: Promise<any>[] = []
    const lazyComponents: (() => Promise<any>)[] = []

    routes.forEach((route) => {
      if (!route.components?.default) return
      const component = route.components.default
      if (typeof component === 'function') {
        lazyComponents.push(component as () => Promise<any>)
      }
    })

    const total = lazyComponents.length
    let loaded = 0

    // 并行加载，但追踪进度
    const loadWithProgress = async (loader: () => Promise<any>) => {
      try {
        await loader()
      } catch (err) {
        console.warn('[Preload] 组件预加载失败', err)
      }
      loaded++
      // 进度从70%到100%
      progress.value = Math.round(70 + (loaded / total) * 30)
      loadingText.value = `正在加载页面组件... (${loaded}/${total})`
    }

    await Promise.all(lazyComponents.map(loadWithProgress))
  }

  // 监听加载状态变化
  setLoadingStatusCallback((status) => {
    loadingText.value = status.step
    progress.value = status.progress
  })

  let checkInterval: ReturnType<typeof setInterval> | null = null

  onMounted(async () => {
    if (getRouteInitCompleted()) {
      // 路由已初始化，预加载所有组件后显示
      progress.value = 70
      loadingText.value = '正在加载页面组件...'
      await preloadAllRouteComponents()
      isRouteReady.value = true
    } else {
      // 等待路由初始化完成
      checkInterval = setInterval(async () => {
        // 同步当前加载状态
        const status = getLoadingStatus()
        if (status.step) {
          loadingText.value = status.step
          progress.value = status.progress
        }

        if (getRouteInitCompleted()) {
          if (checkInterval) {
            clearInterval(checkInterval)
            checkInterval = null
          }
          // 预加载所有组件后显示
          progress.value = 70
          loadingText.value = '正在加载页面组件...'
          await preloadAllRouteComponents()
          isRouteReady.value = true
        }
      }, 10)
    }
  })

  onUnmounted(() => {
    if (checkInterval) {
      clearInterval(checkInterval)
    }
    setLoadingStatusCallback(null)
  })
</script>

<style lang="scss" scoped>
  @use './style';
</style>
