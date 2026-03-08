<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  User, Document, ChatDotRound, Star, Bell, Setting, 
  Right, ArrowLeft
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { getPageSource, clearPageSource, markAsBackNavigation } from '@/router'

const router = useRouter()
const route = useRoute()

const props = defineProps({
  isLoggedIn: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => null }
})

const emit = defineEmits(['openLogin', 'logout'])

// 当前激活的菜单
const activeMenu = ref('profile')

// 菜单配置
const menuItems = [
  { key: 'profile', label: '个人资料', icon: User },
  { key: 'orders', label: '我的订单', icon: Document },
  { key: 'posts', label: '我的帖子', icon: ChatDotRound },
  { key: 'favorites', label: '收藏帖子', icon: Star },
  { key: 'notifications', label: '通知箱', icon: Bell },
  { key: 'settings', label: '账号管理', icon: Setting },
]

// 切换菜单
const handleMenuClick = (key) => {
  activeMenu.value = key
  router.push(`/profile/${key}`)
}

// 注销账号
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '退出前请确认您没有未完成的订单。确定要退出登录吗？',
      '退出确认',
      { confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning' }
    )
    emit('logout')
    router.push('/')
  } catch {}
}

// 根据路由设置激活菜单
watch(() => route.path, (path) => {
  const section = path.split('/').pop() || 'profile'
  activeMenu.value = section
}, { immediate: true })

// 返回到进入个人中心前的页面（使用统一的来源跟踪系统）
const goBack = () => {
  const sourcePath = getPageSource('Profile')
  if (sourcePath && !sourcePath.startsWith('/profile')) {
    clearPageSource('Profile')
    markAsBackNavigation()
    router.push(sourcePath)
  } else {
    router.push('/')
  }
}

onMounted(() => {
  if (!props.isLoggedIn) {
    emit('openLogin')
  }
  const section = route.path.split('/').pop() || 'profile'
  activeMenu.value = section
})
</script>

<template>
  <div class="profile-page">
    <div class="container-base">
      <!-- 导航栏：返回按钮 + 面包屑 -->
      <div class="nav-header">
        <div class="back-nav" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          <span>返回</span>
        </div>
        <div class="breadcrumb-nav">
          <span class="breadcrumb-item" @click="router.push('/')">首页</span>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-current">个人中心</span>
        </div>
      </div>
      
      <div class="profile-layout">
        <!-- 左侧菜单 -->
        <aside class="profile-sidebar">
          <div class="user-card">
            <el-avatar :size="64" :src="userInfo?.avatar" :fit="'cover'">
              {{ userInfo?.nickname?.charAt(0) || '用' }}
            </el-avatar>
            <div class="user-info">
              <h3>{{ userInfo?.nickname || '用户' }}</h3>
            </div>
          </div>
          
          <nav class="menu-list">
            <div
              v-for="item in menuItems"
              :key="item.key"
              class="menu-item"
              :class="{ active: activeMenu === item.key }"
              @click="handleMenuClick(item.key)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
              <el-icon class="arrow"><Right /></el-icon>
            </div>
          </nav>
          
          <div class="logout-btn" @click="handleLogout">
            退出登录
          </div>
        </aside>
        
        <!-- 右侧内容区 -->
        <main class="profile-content">
          <router-view v-slot="{ Component }">
            <component 
              :is="Component" 
              :user-info="userInfo"
              :is-logged-in="isLoggedIn"
            />
          </router-view>
        </main>
      </div>
    </div>
  </div>
</template>



<style scoped lang="scss">
.profile-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-base);
  padding-bottom: var(--spacing-2xl);
}

// 导航头部
.nav-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-base) 0;
  margin-bottom: var(--spacing-lg);
}

// 返回按钮
.back-nav {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-base);
  transition: all var(--transition-fast);
  flex-shrink: 0;
  
  &:hover {
    color: var(--color-primary);
    background: var(--color-secondary-light);
  }
  
  .el-icon {
    font-size: 14px;
  }
  
  span {
    font-size: var(--font-size-sm);
  }
}

// 面包屑导航
.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  
  .breadcrumb-item {
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  .breadcrumb-separator {
    color: var(--color-text-tertiary);
  }
  
  .breadcrumb-current {
    color: var(--color-text-primary);
  }
}

.profile-layout {
  display: flex;
  gap: var(--spacing-xl);
  min-height: 600px;
}

.profile-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
  height: fit-content;
  position: sticky;
  top: calc(var(--header-height) + var(--spacing-xl));
}

.user-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-base);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border-light);
  margin-bottom: var(--spacing-lg);

  :deep(.el-avatar) {
    flex-shrink: 0;
  }
  
  .user-info {
    h3 {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
      margin-bottom: var(--spacing-xs);
    }
    
    p {
      font-size: var(--font-size-xs);
      color: var(--color-text-tertiary);
    }
  }
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-base);
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
  
  .arrow {
    margin-left: auto;
    opacity: 0;
    transition: opacity var(--transition-fast);
  }
  
  &:hover {
    background: var(--color-secondary-light);
    color: var(--color-primary);
    
    .arrow {
      opacity: 1;
    }
  }
  
  &.active {
    background: var(--color-primary);
    color: white;
    
    .arrow {
      opacity: 1;
    }
  }
}

.logout-btn {
  margin-top: var(--spacing-xl);
  padding: var(--spacing-md);
  text-align: center;
  color: var(--color-danger);
  cursor: pointer;
  border-radius: var(--radius-base);
  transition: background var(--transition-fast);
  
  &:hover {
    background: rgba(239, 83, 80, 0.1);
  }
}

.profile-content {
  flex: 1;
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 768px) {
  .profile-layout {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
    position: static;
  }
}
</style>
