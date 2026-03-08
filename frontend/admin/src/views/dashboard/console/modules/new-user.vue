<template>
  <div class="art-card p-5 h-80 overflow-hidden mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>系统概览</h4>
        <p>宠物商城与社区系统</p>
      </div>
    </div>
    <div class="overview-content">
      <ElRow :gutter="20">
        <ElCol :sm="24" :md="8" v-for="(item, index) in overviewList" :key="index">
          <div class="overview-item">
            <div class="overview-icon" :style="{ background: item.bgColor }">
              <ArtSvgIcon :icon="item.icon" class="text-2xl" :style="{ color: item.iconColor }" />
            </div>
            <div class="overview-info">
              <span class="overview-label">{{ item.label }}</span>
              <span class="overview-value">{{ item.value }}</span>
            </div>
          </div>
        </ElCol>
      </ElRow>
      <div class="system-info mt-6">
        <h5 class="text-base font-medium mb-3">快捷入口</h5>
        <div class="quick-links">
          <ElButton 
            v-for="link in quickLinks" 
            :key="link.path" 
            type="primary" 
            plain 
            @click="navigateTo(link.path)"
          >
            <ArtSvgIcon :icon="link.icon" class="mr-1" />
            {{ link.name }}
          </ElButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { useUserStore } from '@/store/modules/user'

  const router = useRouter()
  const userStore = useUserStore()

  interface OverviewItem {
    label: string
    value: string
    icon: string
    bgColor: string
    iconColor: string
  }

  interface QuickLink {
    name: string
    path: string
    icon: string
  }

  // 系统概览数据
  const overviewList = reactive<OverviewItem[]>([
    {
      label: '当前用户',
      value: userStore.info?.userName || '管理员',
      icon: 'ri:user-line',
      bgColor: 'rgba(var(--art-primary-rgb), 0.1)',
      iconColor: 'var(--art-primary)'
    },
    {
      label: '系统版本',
      value: 'v1.0.0',
      icon: 'ri:code-box-line',
      bgColor: 'rgba(var(--art-success-rgb), 0.1)',
      iconColor: 'var(--art-success)'
    },
    {
      label: '当前时间',
      value: '',
      icon: 'ri:time-line',
      bgColor: 'rgba(var(--art-warning-rgb), 0.1)',
      iconColor: 'var(--art-warning)'
    }
  ])

  // 快捷入口
  const quickLinks: QuickLink[] = [
    { name: '用户管理', path: '/user/list', icon: 'ri:user-settings-line' },
    { name: '商品管理', path: '/product/list', icon: 'ri:shopping-bag-line' },
    { name: '订单管理', path: '/order/list', icon: 'ri:file-list-3-line' },
    { name: '社区管理', path: '/community/post', icon: 'ri:chat-3-line' }
  ]

  // 更新时间
  const updateTime = () => {
    const now = new Date()
    const timeStr = now.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
    overviewList[2].value = timeStr
  }

  // 导航到指定页面
  const navigateTo = (path: string) => {
    router.push(path)
  }

  let timer: NodeJS.Timeout | null = null

  onMounted(() => {
    updateTime()
    timer = setInterval(updateTime, 1000)
  })

  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
    }
  })
</script>

<style lang="scss" scoped>
  .overview-content {
    margin-top: 16px;
  }

  .overview-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: var(--default-bg-color);
    border-radius: 8px;
    margin-bottom: 12px;

    .overview-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 48px;
      height: 48px;
      border-radius: 12px;
      margin-right: 12px;
    }

    .overview-info {
      display: flex;
      flex-direction: column;

      .overview-label {
        font-size: 12px;
        color: var(--art-text-gray-600);
        margin-bottom: 4px;
      }

      .overview-value {
        font-size: 14px;
        font-weight: 500;
        color: var(--art-text-gray-800);
      }
    }
  }

  .quick-links {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
</style>
