import { AppRouteRecord } from '@/types/router'

// 订单管理
export const orderRoutes: AppRouteRecord = {
  path: '/order',
  name: 'Order',
  component: '/index/index',
  meta: {
    title: '订单管理',
    icon: 'ri:file-list-3-line'
  },
  children: [
    {
      path: 'list',
      name: 'OrderList',
      component: '/order/list',
      meta: {
        title: '订单管理',
        keepAlive: true
      }
    }
  ]
}
