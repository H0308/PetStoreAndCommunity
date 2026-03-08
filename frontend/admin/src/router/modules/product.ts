import { AppRouteRecord } from '@/types/router'

// 商品管理
export const productRoutes: AppRouteRecord = {
  path: '/product',
  name: 'Product',
  component: '/index/index',
  meta: {
    title: '商品管理',
    icon: 'ri:shopping-bag-line'
  },
  children: [
    {
      path: 'list',
      name: 'ProductList',
      component: '/product/list',
      meta: {
        title: '商品管理',
        keepAlive: true
      }
    },
    {
      path: 'category',
      name: 'ProductCategory',
      component: '/product/category',
      meta: {
        title: '商品分类管理',
        keepAlive: true
      }
    }
  ]
}
