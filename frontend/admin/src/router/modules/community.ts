import { AppRouteRecord } from '@/types/router'

// 社区管理
export const communityRoutes: AppRouteRecord = {
  path: '/community',
  name: 'Community',
  component: '/index/index',
  meta: {
    title: '社区管理',
    icon: 'ri:chat-3-line'
  },
  children: [
    {
      path: 'post',
      name: 'PostManage',
      component: '/community/post',
      meta: {
        title: '帖子管理',
        keepAlive: true
      }
    },
    {
      path: 'topic',
      name: 'TopicManage',
      component: '/community/topic',
      meta: {
        title: '话题管理',
        keepAlive: true
      }
    },
    {
      path: 'column',
      name: 'ColumnManage',
      component: '/community/column',
      meta: {
        title: '栏目管理',
        keepAlive: true
      }
    }
  ]
}
