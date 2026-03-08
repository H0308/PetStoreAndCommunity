import { createRouter, createWebHistory } from 'vue-router'
import LandingPage from '../views/LandingPage.vue'
import ProductsPage from '../views/ProductsPage.vue'
import ProductDetailPage from '../views/ProductDetailPage.vue'
import CategoryPage from '../views/CategoryPage.vue'
import CartPage from '../views/CartPage.vue'
import CheckoutPage from '../views/CheckoutPage.vue'
import PaymentSuccessPage from '../views/PaymentSuccessPage.vue'
import OrderDetailPage from '../views/OrderDetailPage.vue'
import ProfilePage from '../views/ProfilePage.vue'
import ForumPage from '../views/ForumPage.vue'
import PostEditorPage from '../views/PostEditorPage.vue'

// 个人中心子页面
import ProfileInfo from '../views/profile/ProfileInfo.vue'
import MyOrders from '../views/profile/MyOrders.vue'
import MyPosts from '../views/profile/MyPosts.vue'
import Favorites from '../views/profile/Favorites.vue'
import Notifications from '../views/profile/Notifications.vue'
import Settings from '../views/profile/Settings.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: LandingPage,
    meta: {
      title: '首页 - 小橘岛'
    }
  },
  {
    path: '/products',
    name: 'Products',
    component: ProductsPage,
    meta: {
      title: '宠物商城 - 小橘岛'
    }
  },
  {
    path: '/category',
    name: 'Category',
    component: CategoryPage,
    meta: {
      title: '商品分类 - 小橘岛'
    }
  },
  {
    path: '/search',
    name: 'Search',
    component: CategoryPage,
    meta: {
      title: '搜索结果 - 小橘岛'
    }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetailPage,
    meta: {
      title: '商品详情 - 小橘岛'
    }
  },
  // 购物车
  {
    path: '/cart',
    name: 'Cart',
    component: CartPage,
    meta: {
      title: '购物车 - 小橘岛'
    }
  },
  // 订单确认/结算
  {
    path: '/checkout',
    name: 'Checkout',
    component: CheckoutPage,
    meta: {
      title: '确认订单 - 小橘岛'
    }
  },
  // 支付成功
  {
    path: '/payment-success',
    name: 'PaymentSuccess',
    component: PaymentSuccessPage,
    meta: {
      title: '支付成功 - 小橘岛'
    }
  },
  // 订单详情
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetailPage,
    meta: {
      title: '订单详情 - 小橘岛'
    }
  },
  // 宠物贴吧
  {
    path: '/forum',
    name: 'Forum',
    component: ForumPage,
    meta: {
      title: '宠物贴吧 - 小橘岛'
    }
  },
  // 发帖/编辑帖子
  {
    path: '/post/create',
    name: 'PostEditor',
    component: PostEditorPage,
    meta: {
      title: '发布帖子 - 小橘岛'
    }
  },
  // 个人中心
  {
    path: '/profile',
    name: 'Profile',
    component: ProfilePage,
    redirect: '/profile/profile',
    meta: {
      title: '个人中心 - 小橘岛'
    },
    children: [
      {
        path: 'profile',
        name: 'ProfileInfo',
        component: ProfileInfo,
        meta: { title: '个人资料 - 小橘岛' }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: MyOrders,
        meta: { title: '我的订单 - 小橘岛' }
      },
      {
        path: 'posts',
        name: 'MyPosts',
        component: MyPosts,
        meta: { title: '我的帖子 - 小橘岛' }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: Favorites,
        meta: { title: '收藏帖子 - 小橘岛' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: Notifications,
        meta: { title: '通知箱 - 小橘岛' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings,
        meta: { title: '账号管理 - 小橘岛' }
      }
    ]
  },
  // 兼容旧路由 - 重定向到统一商城页面
  {
    path: '/pets',
    redirect: '/products'
  },
  {
    path: '/supplies',
    redirect: '/products'
  },
  // Google OAuth 回调页面
  {
    path: '/auth/google/callback',
    name: 'GoogleCallback',
    component: () => import('../views/GoogleCallback.vue'),
    meta: {
      title: 'Google 登录中... - 小橘岛'
    }
  }
]

// 保存页面滚动位置
const scrollPositions = {}

// 需要保存滚动位置的页面
const pagesNeedSaveScroll = ['Products', 'MyOrders', 'Category', 'Favorites', 'Home']

// 需要恢复滚动位置的页面
const pagesNeedRestoreScroll = ['Products', 'MyOrders', 'Category', 'Favorites', 'Home', 'Profile']

// 从这些页面返回时，需要恢复目标页面的滚动位置
const pagesNeedRestoreFrom = ['ProductDetail', 'OrderDetail', 'Cart', 'Checkout', 'Profile']

// 需要记录来源页面的页面（用于返回按钮）
// PostEditor 也需要记录来源，这样从编辑页跳转出去时可以继承来源
const pagesNeedTrackSource = ['ProductDetail', 'Cart', 'Checkout', 'Profile', 'OrderDetail', 'PostEditor']

/**
 * 保存页面来源信息到 sessionStorage
 * @param {string} targetPage - 目标页面名称
 * @param {string} sourcePath - 来源页面完整路径
 */
export const savePageSource = (targetPage, sourcePath) => {
  if (targetPage && sourcePath) {
    sessionStorage.setItem(`pageSource_${targetPage}`, sourcePath)
  }
}

/**
 * 获取页面的来源路径
 * @param {string} pageName - 页面名称
 * @returns {string|null} 来源路径
 */
export const getPageSource = (pageName) => {
  return sessionStorage.getItem(`pageSource_${pageName}`)
}

/**
 * 清除页面来源信息
 * @param {string} pageName - 页面名称
 */
export const clearPageSource = (pageName) => {
  sessionStorage.removeItem(`pageSource_${pageName}`)
}

/**
 * 标记页面需要刷新数据
 * @param {string} pagePath - 页面路径（如 '/products', '/cart', '/profile/orders'）
 */
export const markPageNeedRefresh = (pagePath) => {
  if (pagePath) {
    sessionStorage.setItem(`needRefresh_${pagePath}`, '1')
  }
}

/**
 * 检查并消费页面刷新标记
 * @param {string} pagePath - 页面路径
 * @returns {boolean} 是否需要刷新
 */
export const checkAndConsumeRefresh = (pagePath) => {
  const key = `needRefresh_${pagePath}`
  const needRefresh = sessionStorage.getItem(key) === '1'
  if (needRefresh) {
    sessionStorage.removeItem(key)
  }
  return needRefresh
}

/**
 * 标记下一次导航为"返回操作"，不更新来源记录
 */
export const markAsBackNavigation = () => {
  sessionStorage.setItem('_isBackNavigation', '1')
}

/**
 * 检查并消费"返回操作"标记
 * @returns {boolean} 是否是返回操作
 */
const checkAndConsumeBackNavigation = () => {
  const isBack = sessionStorage.getItem('_isBackNavigation') === '1'
  if (isBack) {
    sessionStorage.removeItem('_isBackNavigation')
  }
  return isBack
}

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // 如果有浏览器保存的位置（前进/后退），优先使用
    if (savedPosition) {
      return savedPosition
    }
    
    // 检查目标页面是否需要恢复滚动位置（支持嵌套路由）
    const needRestore = pagesNeedRestoreScroll.includes(to.name) || 
                        to.matched.some(record => pagesNeedRestoreScroll.includes(record.name))
    
    // 检查来源页面是否是需要触发恢复的页面（支持嵌套路由）
    const fromNeedRestore = pagesNeedRestoreFrom.includes(from.name) || 
                            from.matched.some(record => pagesNeedRestoreFrom.includes(record.name))
    
    // 从详情页/购物车/结算页等返回时，恢复到保存的位置
    if (fromNeedRestore && needRestore) {
      const saved = scrollPositions[to.fullPath]
      if (saved !== undefined) {
        return { top: saved, left: 0 }
      }
    }
    
    // 其他情况滚动到页面顶部
    return { top: 0, left: 0 }
  }
})

// 路由守卫 - 保存滚动位置、记录页面来源和更新页面标题
router.beforeEach((to, from, next) => {
  // 离开需要保存滚动位置的页面时保存位置
  if (pagesNeedSaveScroll.includes(from.name) || from.matched.some(record => pagesNeedSaveScroll.includes(record.name))) {
    scrollPositions[from.fullPath] = window.scrollY || document.documentElement.scrollTop
  }
  
  // 记录页面来源（用于返回按钮）
  // 检查是否是"返回"操作，如果是则不更新来源记录
  const isBackNavigation = checkAndConsumeBackNavigation()
  
  // 支持嵌套路由：检查目标页面或其父路由是否需要跟踪来源
  const targetTrackPage = pagesNeedTrackSource.find(page => 
    to.name === page || to.matched.some(record => record.name === page)
  )
    
  if (targetTrackPage && !isBackNavigation) {
    // 排除一些不应作为来源的页面（如登录页、支付成功页、订单创建页、帖子编辑页等）
    // 这些页面不应作为返回目标，返回时会跳过它们
    const excludePages = ['PaymentSuccess', 'Login', 'Checkout', 'PostEditor']
      
    // 检查来源页面是否在排除列表中（支持嵌套路由）
    const fromPageInExclude = excludePages.includes(from.name) || 
      from.matched.some(record => excludePages.includes(record.name))
      
    if (from.name && !fromPageInExclude) {
      // 不要记录同一页面为来源（包括嵌套路由的情况）
      const isSamePage = from.name === to.name || 
        from.matched.some(record => pagesNeedTrackSource.includes(record.name) && record.name === targetTrackPage)
      if (!isSamePage) {
        savePageSource(targetTrackPage, from.fullPath)
      }
    } else if (fromPageInExclude) {
      // 如果从排除页面（如 Checkout）跳转过来，继承该页面的来源
      // 这样目标页面的返回按钮会跳转到进入 Checkout 之前的页面
      // 首先尝试从排除页面本身获取来源
      let inheritedSource = sessionStorage.getItem(`pageSource_${from.name}`)
      // 如果没有，尝试从父路由获取
      if (!inheritedSource) {
        for (const record of from.matched) {
          if (excludePages.includes(record.name)) {
            inheritedSource = sessionStorage.getItem(`pageSource_${record.name}`)
            if (inheritedSource) break
          }
        }
      }
      if (inheritedSource) {
        savePageSource(targetTrackPage, inheritedSource)
      }
    }
  }
  
  // 更新页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router
