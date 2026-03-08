import { AppRouteRecord } from '@/types/router'

// 评论管理
export const commentRoutes: AppRouteRecord = {
  path: '/comment',
  name: 'Comment',
  component: '/index/index',
  meta: {
    title: '评论管理',
    icon: 'ri:chat-quote-line'
  },
  children: [
    {
      path: 'list',
      name: 'CommentList',
      component: '/comment/list',
      meta: {
        title: '评论列表',
        keepAlive: true
      }
    }
  ]
}
