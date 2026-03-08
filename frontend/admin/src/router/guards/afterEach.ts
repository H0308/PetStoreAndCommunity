import { nextTick } from 'vue'
import { useSettingStore } from '@/store/modules/setting'
import { Router } from 'vue-router'
import NProgress from 'nprogress'
import { useCommon } from '@/hooks/core/useCommon'
import { loadingService } from '@/utils/ui'
import { getPendingLoading, resetPendingLoading } from './beforeEach'

/** 路由全局后置守卫 */
export function setupAfterEachGuard(router: Router) {
  const { scrollToTop } = useCommon()

  router.afterEach((to) => {
    scrollToTop()

    // 清理 Element Plus 组件意外添加的 el-id 参数
    const query = { ...to.query }
    let hasElId = false
    for (const key of Object.keys(query)) {
      if (key.startsWith('el-id-')) {
        delete query[key]
        hasElId = true
      }
    }
    if (hasElId) {
      router.replace({ path: to.path, query, hash: to.hash })
    }

    // 关闭进度条
    const settingStore = useSettingStore()
    if (settingStore.showNprogress) {
      NProgress.done()
      // 确保进度条完全移除，避免残影
      setTimeout(() => {
        NProgress.remove()
      }, 600)
    }

    // 关闭 loading 效果
    if (getPendingLoading()) {
      nextTick(() => {
        loadingService.hideLoading()
        resetPendingLoading()
      })
    }
  })
}
