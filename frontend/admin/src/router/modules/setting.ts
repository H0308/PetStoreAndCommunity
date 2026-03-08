import { AppRouteRecord } from '@/types/router'

// 系统设置
export const settingRoutes: AppRouteRecord = {
  path: '/setting',
  name: 'Setting',
  component: '/index/index',
  meta: {
    title: '系统设置',
    icon: 'ri:settings-3-line'
  },
  children: [
    {
      path: 'sensitive',
      name: 'SensitiveWord',
      component: '/setting/sensitive',
      meta: {
        title: '敏感词管理',
        keepAlive: true
      }
    },
    {
      path: 'sensitive-category',
      name: 'SensitiveCategory',
      component: '/setting/sensitive-category',
      meta: {
        title: '敏感词分类管理',
        keepAlive: true
      }
    },
    {
      path: 'notify-email',
      name: 'NotifyEmail',
      component: '/system/notify-email',
      meta: {
        title: '邮件通知',
        keepAlive: true
      }
    }
  ]
}

// 个人中心（隐藏路由，不在侧边栏显示）
export const userCenterRoutes: AppRouteRecord = {
  path: '/system',
  name: 'System',
  component: '/index/index',
  meta: {
    title: '系统',
    isHide: true
  },
  children: [
    {
      path: 'user-center',
      name: 'UserCenter',
      component: '/system/user-center',
      meta: {
        title: '个人中心',
        keepAlive: true
      }
    }
  ]
}
