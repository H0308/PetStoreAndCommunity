import { AppRouteRecord } from '@/types/router'

// 用户管理
export const userRoutes: AppRouteRecord = {
  path: '/user',
  name: 'User',
  component: '/index/index',
  meta: {
    title: '用户管理',
    icon: 'ri:user-3-line'
  },
  children: [
    {
      path: 'list',
      name: 'UserList',
      component: '/user/list',
      meta: {
        title: '用户管理',
        keepAlive: true
      }
    }
  ]
}
