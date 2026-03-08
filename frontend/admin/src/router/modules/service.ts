import { AppRouteRecord } from '@/types/router'

// 客服中心
export const serviceRoutes: AppRouteRecord = {
  path: '/service',
  name: 'Service',
  component: '/index/index',
  meta: {
    title: '客服中心',
    icon: 'ri:customer-service-2-line'
  },
  children: [
    {
      path: 'message',
      name: 'MessageReply',
      component: '/service/message',
      meta: {
        title: '消息回复',
        keepAlive: true
      }
    }
  ]
}
