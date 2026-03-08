import { AppRouteRecord } from '@/types/router'
import { dashboardRoutes } from './dashboard'
import { productRoutes } from './product'
import { orderRoutes } from './order'
import { userRoutes } from './user'
import { communityRoutes } from './community'
import { commentRoutes } from './comment'
import { serviceRoutes } from './service'
import { settingRoutes, userCenterRoutes } from './setting'

/**
 * 导出所有模块化路由
 */
export const routeModules: AppRouteRecord[] = [
  dashboardRoutes,
  userRoutes,
  productRoutes,
  orderRoutes,
  communityRoutes,
  commentRoutes,
  serviceRoutes,
  settingRoutes,
  userCenterRoutes
]
