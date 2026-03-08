<script setup>
import { ref, computed, onMounted, onUnmounted, inject, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Minus, Star, ArrowLeft, ArrowRight, VideoPlay, VideoPause, Back, Close, Delete, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentEditor from '@/components/CommentEditor.vue'
import CustomVideoPlayer from '@/components/CustomVideoPlayer.vue'
import { getPageSource, clearPageSource, markAsBackNavigation } from '@/router'
import { request } from '@/api/config.js'

// 注入刷新购物车数量的方法
const refreshCartCount = inject('refreshCartCount', () => {})

// 注入设置聊天商品的方法
const setChatProduct = inject('setChatProduct', null)

// 获取路由参数
const route = useRoute()
const router = useRouter()
const productId = computed(() => route.params.id)

// Props
const props = defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => null
  }
})

// Emits
const emit = defineEmits(['openLogin'])

// API基础URL
const API_BASE_URL = 'http://localhost:8080'

// 实名认证提示弹窗
const showAuthTipDialog = ref(false)
const authTipMessage = ref('')

// 信息缺失提示弹窗（手机号/收货地址）
const showInfoTipDialog = ref(false)
const infoTipType = ref('') // 'phone' 或 'address'
const infoTipMessage = ref('')

// 数据状态
const loading = ref(false)
const productDetail = ref(null)
const currentImageIndex = ref(0)
const quantity = ref(1)
const activeTab = ref('detail') // 'detail' or 'comments'
const autoPlayTimer = ref(null)
const isAutoPlaying = ref(true)
const thumbScrollIndex = ref(0)

// 计算最大滚动索引
const maxThumbScrollIndex = computed(() => {
  if (!productDetail.value?.productImages?.length) return 0
  return Math.max(0, productDetail.value.productImages.length - 4)
})

// 滚动缩略图
const scrollThumbnails = (direction) => {
  if (direction === 'prev') {
    thumbScrollIndex.value = Math.max(0, thumbScrollIndex.value - 1)
  } else {
    thumbScrollIndex.value = Math.min(maxThumbScrollIndex.value, thumbScrollIndex.value + 1)
  }
}

// 评论数据
const comments = ref([])
const commentsLoading = ref(false)
const expandedReplies = ref({}) // 记录哪些评论的回复已展开

// 计算属性：将评论组织成树形结构（支持多层嵌套）
const organizedComments = computed(() => {
  const commentMap = {} // commentId -> comment对象
  const topLevelComments = []
  
  // 第一遍：创建所有评论的副本并建立映射
  comments.value.forEach(comment => {
    commentMap[comment.commentId] = { ...comment, replies: [] }
  })
  
  // 第二遍：构建树形结构
  comments.value.forEach(comment => {
    const currentComment = commentMap[comment.commentId]
    if (!comment.parentId) {
      // 顶级评论
      topLevelComments.push(currentComment)
    } else {
      // 回复评论，挂载到父评论下
      const parentComment = commentMap[comment.parentId]
      if (parentComment) {
        parentComment.replies.push(currentComment)
      }
    }
  })
  
  return topLevelComments
})

// 计算某个评论及其所有子孙回复的总数
const getTotalRepliesCount = (comment) => {
  let count = comment.replies.length
  comment.replies.forEach(reply => {
    count += getTotalRepliesCount(reply)
  })
  return count
}

// 切换回复展开/收起
const toggleReplies = (commentId) => {
  expandedReplies.value[commentId] = !expandedReplies.value[commentId]
}

// 视频播放器状态
const videoPlayerVisible = ref(false)
const videoModalMouseDown = ref(false)
const currentVideoUrl = ref('')
const videoPlayerRef = ref(null)

// 视频弹窗尺寸
const videoModalSize = ref({ width: 0, height: 0 })
const videoModalStyle = computed(() => {
  if (!videoModalSize.value.width || !videoModalSize.value.height) {
    return {}
  }
  const maxWidth = window.innerWidth * 0.9
  const maxHeight = window.innerHeight * 0.8
  const minWidth = 480 // 最小宽度，确保控制栏有足够空间
  const videoRatio = videoModalSize.value.width / videoModalSize.value.height
  
  let width, height
  if (videoModalSize.value.width > maxWidth || videoModalSize.value.height > maxHeight) {
    const widthByMaxWidth = maxWidth
    const heightByMaxWidth = maxWidth / videoRatio
    const widthByMaxHeight = maxHeight * videoRatio
    const heightByMaxHeight = maxHeight
    
    if (heightByMaxWidth <= maxHeight) {
      width = widthByMaxWidth
      height = heightByMaxWidth
    } else {
      width = widthByMaxHeight
      height = heightByMaxHeight
    }
  } else {
    width = videoModalSize.value.width
    height = videoModalSize.value.height
  }
  
  // 确保最小宽度
  if (width < minWidth) {
    width = minWidth
  }
  
  return {
    width: `${width}px`,
    height: `${height}px`
  }
})

// 处理视频元数据加载
const handleVideoMetadata = (data) => {
  videoModalSize.value = { width: data.width, height: data.height }
}

// 打开视频播放器
const openVideoPlayer = (videoUrl) => {
  videoModalSize.value = { width: 0, height: 0 }
  currentVideoUrl.value = videoUrl
  videoPlayerVisible.value = true
  setTimeout(() => {
    if (videoPlayerRef.value) {
      videoPlayerRef.value.play()
    }
  }, 100)
}

// 关闭视频播放器
const closeVideoPlayer = () => {
  if (videoPlayerRef.value) {
    videoPlayerRef.value.pause()
  }
  videoPlayerVisible.value = false
  currentVideoUrl.value = ''
  videoModalSize.value = { width: 0, height: 0 }
}

// 计算属性：当前显示的图片
const currentImage = computed(() => {
  if (!productDetail.value || !productDetail.value.productImages?.length) {
    return 'https://via.placeholder.com/600x600/f5f7fa/999?text=No+Image'
  }
  return productDetail.value.productImages[currentImageIndex.value]
})

// 计算属性：判断是否为宠物商品
const isPet = computed(() => {
  return productDetail.value?.productType === 1
})

// 计算属性：格式化健康状态
const healthStatusText = computed(() => {
  if (!isPet.value || !productDetail.value?.healthStatus) return ''
  const statusMap = {
    1: '健康',
    2: '良好',
    3: '治疗中'
  }
  return statusMap[productDetail.value.healthStatus] || '未知'
})

// 计算属性：健康状态标签类型
const healthStatusType = computed(() => {
  if (!isPet.value || !productDetail.value?.healthStatus) return 'info'
  const typeMap = {
    1: 'success', // 健康 - 绿色
    2: 'warning', // 良好 - 黄色
    3: 'danger' // 治疗中 - 红色
  }
  return typeMap[productDetail.value.healthStatus] || 'info'
})

// 计算属性：商品状态文本 (1-出售中, 2-售罄, 3-已下架)
const productStatusText = computed(() => {
  if (!productDetail.value?.status) return ''
  const statusMap = {
    1: '出售中',
    2: '售罄',
    3: '已下架'
  }
  return statusMap[productDetail.value.status] || '未知'
})

// 计算属性：商品状态标签类型
const productStatusType = computed(() => {
  if (!productDetail.value?.status) return 'info'
  const typeMap = {
    1: 'success', // 出售中 - 绿色
    2: 'warning', // 售罄 - 黄色
    3: 'info' // 已下架 - 灰色
  }
  return typeMap[productDetail.value.status] || 'info'
})

// 计算属性：是否可购买（出售中且有库存）
const canPurchase = computed(() => {
  return productDetail.value?.status === 1 && productDetail.value?.stock > 0
})

// 计算属性：格式化疫苗状态
const vaccineStatusText = computed(() => {
  if (!isPet.value) return ''
  return productDetail.value?.vaccineFlag === 1 ? '已接种' : '未接种'
})

// 格式化日期（去掉时间部分）
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}

// 获取商品类型（从路由 query 参数）
const productType = computed(() => route.query.type)

// 来源页面信息（用于面包屑导航）
const fromCategory = computed(() => route.query.from === 'category')
const categoryId = computed(() => route.query.categoryId)
const categoryName = computed(() => route.query.categoryName)

// 返回分类页
const goBackToCategory = () => {
  if (categoryId.value) {
    router.push({
      path: '/category',
      query: { categoryId: categoryId.value }
    })
  } else {
    router.push('/products')
  }
}

// 返回上一页（使用记录的来源页面，避免循环跳转）
const goBack = () => {
  const sourcePath = getPageSource('ProductDetail')
  if (sourcePath) {
    clearPageSource('ProductDetail')
    markAsBackNavigation() // 标记为返回操作，防止目标页面更新来源记录
    router.push(sourcePath)
  } else {
    router.push('/products')
  }
}

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true
    
    // 根据商品类型选择接口：1=宠物，2=用品
    const type = productType.value
    let url
    
    if (type === '1' || type === 1) {
      // 宠物接口
      url = `${API_BASE_URL}/api/user/product/pet/getPetDetail?productId=${productId.value}`
      console.log('请求宠物商品详情:', url)
    } else if (type === '2' || type === 2) {
      // 用品接口
      url = `${API_BASE_URL}/api/user/product/supply/getSupplyDetail?productId=${productId.value}`
      console.log('请求用品商品详情:', url)
    } else {
      // 未知类型，无法确定调用哪个接口
      console.warn('商品类型未知，请从商品列表进入')
      ElMessage.warning('请从商品列表进入查看详情')
      return
    }
    
    const response = await request(url)
    const result = await response.json()
    console.log('商品详情响应:', result)
    
    if (result.code === 0) {
      productDetail.value = result.data
      // 根据库存调整数量初始值
      if (result.data.stock <= 0) {
        quantity.value = 0
      } else {
        quantity.value = 1
      }

      // 设置当前商品给聊天组件使用
      if (setChatProduct) {
        // productImages 数组中存储的是图片 URL 字符串，直接取第一张
        const firstImage = result.data.productImages?.[0] || ''
        const product = {
          id: result.data.productId || result.data.id,
          name: result.data.productName || result.data.name,
          price: result.data.price,
          image: firstImage,
          status: result.data.status
        }
        console.log('[ProductDetailPage] 设置聊天商品:', product)
        setChatProduct(product)
      }
    } else {
      console.error('获取商品详情失败:', result.message)
      ElMessage.error(result.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('获取商品详情异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取商品评论
const fetchProductComments = async () => {
  try {
    commentsLoading.value = true
    const url = `${API_BASE_URL}/api/user/comment/getProductComment?productId=${productId.value}`
    console.log('请求商品评论:', url)
    
    const response = await request(url)
    const result = await response.json()
    console.log('商品评论响应:', result)
    
    if (result.code === 0) {
      comments.value = result.data || []
    } else {
      console.error('获取评论失败:', result.message)
    }
  } catch (error) {
    console.error('获取评论异常:', error)
  } finally {
    commentsLoading.value = false
  }
}

// 切换图片
const selectImage = (index) => {
  currentImageIndex.value = index
  stopAutoPlay()
}

// 上一张图片
const prevImage = () => {
  if (!productDetail.value?.productImages?.length) return
  const total = productDetail.value.productImages.length
  currentImageIndex.value = (currentImageIndex.value - 1 + total) % total
  stopAutoPlay()
}

// 下一张图片
const nextImage = () => {
  if (!productDetail.value?.productImages?.length) return
  const total = productDetail.value.productImages.length
  currentImageIndex.value = (currentImageIndex.value + 1) % total
  stopAutoPlay()
}

// 开始自动播放
const startAutoPlay = () => {
  if (!productDetail.value?.productImages?.length || productDetail.value.productImages.length <= 1) return
  
  stopAutoPlay()
  isAutoPlaying.value = true
  autoPlayTimer.value = setInterval(() => {
    const total = productDetail.value.productImages.length
    currentImageIndex.value = (currentImageIndex.value + 1) % total
  }, 3000) // 每3秒切换一次
}

// 停止自动播放
const stopAutoPlay = () => {
  if (autoPlayTimer.value) {
    clearInterval(autoPlayTimer.value)
    autoPlayTimer.value = null
  }
  isAutoPlaying.value = false
}

// 切换自动播放状态
const toggleAutoPlay = () => {
  if (isAutoPlaying.value) {
    stopAutoPlay()
  } else {
    startAutoPlay()
  }
}

// 数量增减
const increaseQuantity = () => {
  if (productDetail.value && quantity.value < productDetail.value.stock) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 立即购买 - 先预创建订单，成功后再跳转到结算页
const buyLoading = ref(false)
const handleBuyNow = async () => {
  if (!props.isLoggedIn) {
    console.log('请先登录')
    emit('openLogin')
    return
  }
  
  if (!productDetail.value) {
    ElMessage.warning('商品信息加载中，请稍后')
    return
  }
  
  if (!props.userInfo?.userId) {
    ElMessage.warning('用户信息异常，请重新登录')
    return
  }
  
  buyLoading.value = true
  try {
    // 先调用预创建订单接口检查是否可以下单
    const response = await request(`${API_BASE_URL}/api/user/order/preCreate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        productId: parseInt(productId.value),
        totalCount: quantity.value
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 预创建成功，跳转到结算页
      router.push({ 
        path: '/checkout', 
        query: { 
          productId: productId.value,
          count: quantity.value
        } 
      })
    } else {
      // 检查是否是实名认证错误
      if (result.message && result.message.includes('实名认证')) {
        authTipMessage.value = '您尚未完成实名认证，无法购买商品。'
        showAuthTipDialog.value = true
      } else if (result.message && result.message.includes('手机号')) {
        // 检查是否是手机号缺失错误
        infoTipType.value = 'phone'
        infoTipMessage.value = '您尚未绑定手机号，无法下单。'
        showInfoTipDialog.value = true
      } else if (result.message && result.message.includes('收货地址')) {
        // 检查是否是收货地址缺失错误
        infoTipType.value = 'address'
        infoTipMessage.value = '您尚未添加收货地址，无法下单。'
        showInfoTipDialog.value = true
      } else {
        ElMessage.error(result.message || '创建订单失败')
      }
    }
  } catch (error) {
    console.error('预创建订单异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    buyLoading.value = false
  }
}

// 加入购物车
const addToCartLoading = ref(false)
const handleAddToCart = async () => {
  if (!props.isLoggedIn) {
    console.log('请先登录')
    emit('openLogin')
    return
  }
  
  if (!props.userInfo?.userId) {
    ElMessage.warning('用户信息异常，请重新登录')
    return
  }
  
  addToCartLoading.value = true
  try {
    const response = await request(`${API_BASE_URL}/api/user/cart/add`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        productId: parseInt(productId.value),
        totalCount: quantity.value
      })
    })
    const result = await response.json()
    console.log('加入购物车响应:', result)
    
    if (result.code === 0 && result.data) {
      ElMessage.success('已成功加入购物车')
      // 刷新导航栏购物车数量
      refreshCartCount()
    } else {
      ElMessage.error(result.message || '加入购物车失败')
    }
  } catch (error) {
    console.error('加入购物车异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    addToCartLoading.value = false
  }
}

// 评论编辑器相关
const commentEditorRef = ref(null)
const commentContent = ref('')
const commentSubmitting = ref(false)
const replyingTo = ref(null) // { commentId, username }

// 获取当前登录用户信息
const currentUser = computed(() => {
  if (props.userInfo) return props.userInfo
  // 尝试从 localStorage 获取
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    try {
      return JSON.parse(stored)
    } catch (e) {
      return null
    }
  }
  return null
})

// 回复评论
const handleReply = (commentId, username) => {
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  replyingTo.value = { commentId, username }
  // 滚动到内联回复编辑器
  setTimeout(() => {
    document.querySelector('.inline-reply-editor')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, 100)
}

// 滚动到回复编辑器
const scrollToReplyEditor = () => {
  document.querySelector('.inline-reply-editor')?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

// 取消回复
const cancelReply = () => {
  replyingTo.value = null
  commentContent.value = ''
}

// 提交评论（multipart/form-data，DTO 放 content 字段，文件放 files 字段）
const handleCommentSubmit = async (data) => {
  if (!props.isLoggedIn || !currentUser.value) {
    emit('openLogin')
    return
  }

  try {
    commentSubmitting.value = true

    const commentData = {
      objectId: parseInt(productId.value),
      userId: currentUser.value.userId,
      content: data.content,
      stars: data.rating || 5
    }
    if (replyingTo.value) {
      commentData.parentId = replyingTo.value.commentId
    }

    const formData = new FormData()
    formData.append('content', new Blob([JSON.stringify(commentData)], { type: 'application/json' }))
    if (data.files && data.files.length > 0) {
      data.files.forEach(fileItem => formData.append('files', fileItem.file))
    }

    const response = await request(`${API_BASE_URL}/api/user/comment/postProductComment`, {
      method: 'POST',
      headers: {},
      body: formData
    })

    const result = await response.json()

    if (result.code === 0 && result.data?.successFlag) {
      ElMessage.success('评论发布成功，审核通过后将显示在列表中')
      commentContent.value = ''
      replyingTo.value = null
      commentEditorRef.value?.reset()
      await fetchProductComments()
    } else {
      if (result.message && result.message.includes('实名认证')) {
        authTipMessage.value = '您尚未完成实名认证，无法发布评论。'
        showAuthTipDialog.value = true
      } else {
        ElMessage.error(result.message || '评论发布失败')
      }
    }
  } catch (error) {
    console.error('评论提交异常:', error)
  } finally {
    commentSubmitting.value = false
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  if (!props.isLoggedIn || !currentUser.value) {
    emit('openLogin')
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 发送删除请求
    const formData = new FormData()
    formData.append('commentId', commentId)
    formData.append('userId', currentUser.value.userId)
    
    const response = await request(`${API_BASE_URL}/api/user/comment/deleteProductComment`, {
      method: 'POST',
      headers: {},
      body: formData
    })
    
    const result = await response.json()
    console.log('删除评论响应:', result)
    
    if (result.code === 0 && result.data) {
      ElMessage.success('评论删除成功')
      // 刷新评论列表
      await fetchProductComments()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 判断是否是自己的评论
const isOwnComment = (userId) => {
  return currentUser.value && currentUser.value.userId === userId
}

// 获取回复目标显示文本（如果是回复给当前用户，显示"我"）
const getReplyTargetText = (targetUsername, targetUserId) => {
  if (currentUser.value && currentUser.value.userId === targetUserId) {
    return '我'
  }
  return targetUsername || '匿名用户'
}

// 刷新页面数据
const refreshPageData = async () => {
  await fetchProductDetail()
  await fetchProductComments()
  if (productDetail.value?.productImages?.length > 1) {
    startAutoPlay()
  }
}

// 页面加载
onMounted(async () => {
  await fetchProductDetail()
  // 获取商品评论
  await fetchProductComments()
  // 如果有多张图片，启动自动播放
  if (productDetail.value?.productImages?.length > 1) {
    startAutoPlay()
  }
})

// 页面卸载时清理定时器
onUnmounted(() => {
  stopAutoPlay()
})
</script>

<template>
  <div class="product-detail-page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <div class="container-base">
          <div class="detail-section">
            <div class="left-section">
              <el-skeleton-item variant="image" style="width: 100%; height: 600px;" />
            </div>
            <div class="right-section">
              <el-skeleton-item variant="h1" style="width: 80%; margin-bottom: 20px;" />
              <el-skeleton-item variant="text" style="width: 40%; margin-bottom: 30px;" />
              <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 10px;" />
              <el-skeleton-item variant="text" style="width: 100%; margin-bottom: 10px;" />
            </div>
          </div>
        </div>
      </template>

      <template #default>
        <div class="container-base">
          <!-- 导航栏：返回按钮 + 面包屑 -->
          <div class="nav-header">
            <div class="back-nav" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              <span>返回</span>
            </div>
            <div class="breadcrumb-nav">
              <span class="breadcrumb-item" @click="router.push('/')">首页</span>
              <span class="breadcrumb-separator">/</span>
              <span class="breadcrumb-item" @click="router.push('/products')">商城</span>
              <span class="breadcrumb-separator">/</span>
              <!-- 如果从分类页进入，显示分类名称 -->
              <template v-if="fromCategory && categoryName">
                <span class="breadcrumb-item" @click="goBackToCategory">{{ categoryName }}</span>
                <span class="breadcrumb-separator">/</span>
              </template>
              <span class="breadcrumb-current">{{ productDetail?.name || '商品详情' }}</span>
            </div>
          </div>
          
          <div v-if="productDetail">
          
          <!-- 上半部分：商品概览 -->
          <div class="detail-section">
            <!-- 左侧：图片画廊 -->
            <div class="left-section">
              <div class="image-gallery">
                <!-- 大图展示区 -->
                <div class="main-image">
                  <img :src="currentImage" :alt="productDetail.name" />
                  
                  <!-- 图片切换按钮 -->
                  <div v-if="productDetail.productImages?.length > 1" class="image-controls">
                    <el-button
                      circle
                      class="control-btn prev-btn"
                      @click="prevImage"
                    >
                      <el-icon><ArrowLeft /></el-icon>
                    </el-button>
                    <el-button
                      circle
                      class="control-btn next-btn"
                      @click="nextImage"
                    >
                      <el-icon><ArrowRight /></el-icon>
                    </el-button>
                    <el-button
                      circle
                      class="control-btn play-btn"
                      @click="toggleAutoPlay"
                    >
                      <el-icon>
                        <component :is="isAutoPlaying ? 'VideoPause' : 'VideoPlay'" />
                      </el-icon>
                    </el-button>
                  </div>
                  
                  <!-- 图片指示器 -->
                  <div v-if="productDetail.productImages?.length > 1" class="image-indicators">
                    <span
                      v-for="(image, index) in productDetail.productImages"
                      :key="index"
                      class="indicator"
                      :class="{ active: currentImageIndex === index }"
                      @click="selectImage(index)"
                    />
                  </div>
                </div>
                
                <!-- 缩略图列表 -->
                <div v-if="productDetail.productImages?.length > 1" class="thumbnail-list-wrapper">
                  <button 
                    v-if="productDetail.productImages.length > 4" 
                    class="thumb-nav-btn prev"
                    :disabled="thumbScrollIndex <= 0"
                    @click="scrollThumbnails('prev')"
                  >
                    <el-icon><ArrowLeft /></el-icon>
                  </button>
                  <div class="thumbnail-list">
                    <div 
                      class="thumbnail-track" 
                      :style="{ transform: `translateX(-${thumbScrollIndex * 108}px)` }"
                    >
                      <div
                        v-for="(image, index) in productDetail.productImages"
                        :key="index"
                        class="thumbnail-item"
                        :class="{ active: currentImageIndex === index }"
                        @click="selectImage(index)"
                      >
                        <img :src="image" :alt="`图片${index + 1}`" />
                      </div>
                    </div>
                  </div>
                  <button 
                    v-if="productDetail.productImages.length > 4" 
                    class="thumb-nav-btn next"
                    :disabled="thumbScrollIndex >= maxThumbScrollIndex"
                    @click="scrollThumbnails('next')"
                  >
                    <el-icon><ArrowRight /></el-icon>
                  </button>
                </div>
              </div>
            </div>

            <!-- 右侧：信息与购买 -->
            <div class="right-section">
              <!-- 标题与状态 -->
              <div class="product-title-row">
                <h1 class="product-title">{{ productDetail.name }}</h1>
                <span 
                  class="product-status-badge"
                  :class="{
                    'status-on-sale': productDetail.status === 1,
                    'status-sold-out': productDetail.status === 2,
                    'status-off-shelf': productDetail.status === 3
                  }"
                >
                  <span class="status-dot"></span>
                  {{ productStatusText }}
                </span>
              </div>
              <div class="product-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ productDetail.price }}</span>
              </div>

              <!-- 核心参数区 -->
              <div class="product-params">
                <div class="param-row">
                  <span class="param-label">商品编号：</span>
                  <span class="param-value">{{ productDetail.identifier }}</span>
                </div>
                <div class="param-row">
                  <span class="param-label">分类：</span>
                  <span class="param-value">{{ productDetail.mainCategoryName }} / {{ productDetail.subCategoryName }}</span>
                </div>
                <div class="param-row">
                  <span class="param-label">发货地：</span>
                  <span class="param-value">{{ productDetail.deliverAddress }}</span>
                </div>
                <div class="param-row">
                  <span class="param-label">库存：</span>
                  <span class="param-value">{{ productDetail.stock }} 件</span>
                </div>

                <!-- 宠物特有参数 -->
                <template v-if="isPet">
                  <div class="param-row">
                    <span class="param-label">健康状态：</span>
                    <span class="param-value">
                      <el-tag :type="healthStatusType" size="small">{{ healthStatusText }}</el-tag>
                    </span>
                  </div>
                  <div class="param-row">
                    <span class="param-label">疫苗接种：</span>
                    <span class="param-value">
                      <el-tag :type="productDetail.vaccineFlag === 1 ? 'success' : 'info'" size="small">
                        {{ vaccineStatusText }}
                      </el-tag>
                    </span>
                  </div>

                </template>

                <!-- 宠物用品特有参数 -->
                <template v-if="!isPet">
                  <div v-if="productDetail.brand" class="param-row">
                    <span class="param-label">品牌：</span>
                    <span class="param-value">{{ productDetail.brand }}</span>
                  </div>
                  <div v-if="productDetail.company" class="param-row">
                    <span class="param-label">生产公司：</span>
                    <span class="param-value">{{ productDetail.company }}</span>
                  </div>
                  <div v-if="productDetail.manufactureDate" class="param-row">
                    <span class="param-label">生产日期：</span>
                    <span class="param-value">{{ formatDate(productDetail.manufactureDate) }}</span>
                  </div>
                  <div v-if="productDetail.guaranteeDate" class="param-row">
                    <span class="param-label">保质期至：</span>
                    <span class="param-value">{{ formatDate(productDetail.guaranteeDate) }}</span>
                  </div>
                  <div v-if="productDetail.fitAge" class="param-row">
                    <span class="param-label">适用年龄：</span>
                    <span class="param-value">{{ productDetail.fitAge }}</span>
                  </div>
                  <div v-if="productDetail.figVariety" class="param-row">
                    <span class="param-label">适用品种：</span>
                    <span class="param-value">{{ productDetail.figVariety }}</span>
                  </div>
                </template>
              </div>

              <!-- 规格与数量 -->
              <div class="product-quantity">
                <span class="quantity-label">数量：</span>
                <el-input-number
                  v-model="quantity"
                  :min="canPurchase ? 1 : 0"
                  :max="canPurchase ? productDetail.stock : 0"
                  :disabled="!canPurchase"
                  size="large"
                  class="quantity-input"
                />
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="large"
                  class="buy-now-btn"
                  :loading="buyLoading"
                  :disabled="!canPurchase"
                  @click="handleBuyNow"
                >
                  {{ productDetail.status === 3 ? '已下架' : (productDetail.status === 2 || productDetail.stock <= 0 ? '已售罄' : '立即购买') }}
                </el-button>
                <el-button
                  size="large"
                  class="add-cart-btn"
                  :loading="addToCartLoading"
                  :disabled="!canPurchase"
                  @click="handleAddToCart"
                >
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>

          <!-- 下半部分：详情与评论 -->
          <div class="info-section">
            <!-- Tab 切换 -->
            <el-tabs v-model="activeTab" class="detail-tabs">
              <el-tab-pane label="商品详情" name="detail">
                <div class="detail-content">
                  <!-- 商品描述 -->
                  <div class="detail-block">
                    <h3 class="block-title">商品介绍</h3>
                    <div class="block-content">
                      <p>{{ productDetail.description }}</p>
                    </div>
                  </div>

                  <!-- 宠物驯养说明 -->
                  <div v-if="isPet && productDetail.trainNote" class="detail-block">
                    <h3 class="block-title">驯养说明</h3>
                    <div class="block-content">
                      <p>{{ productDetail.trainNote }}</p>
                    </div>
                  </div>

                  <!-- 宠物领养须知 -->
                  <div v-if="isPet && productDetail.raiseNote" class="detail-block">
                    <h3 class="block-title">领养须知</h3>
                    <div class="block-content">
                      <p>{{ productDetail.raiseNote }}</p>
                    </div>
                  </div>

                  <!-- 其他详情信息可以在这里扩展 -->
                </div>
              </el-tab-pane>

              <el-tab-pane label="用户评价" name="comments">
                <div class="comments-section">
                  <!-- 评论编辑器（仅用于发表新评论） -->
                  <div class="comment-editor-wrapper">
                    <div v-if="!isLoggedIn" class="login-tip">
                      <span>登录后即可发表评论</span>
                      <el-button type="primary" size="small" @click="emit('openLogin')">立即登录</el-button>
                    </div>
                    <template v-else-if="!replyingTo">
                      <CommentEditor
                        ref="commentEditorRef"
                        v-model="commentContent"
                        mode="product"
                        :loading="commentSubmitting"
                        @submit="handleCommentSubmit"
                      />
                    </template>
                    <div v-else class="replying-elsewhere-tip">
                      <span>正在回复 <strong>@{{ replyingTo.username }}</strong> 的评论</span>
                      <el-button text size="small" type="primary" @click="scrollToReplyEditor">查看回复框</el-button>
                      <el-button text size="small" @click="cancelReply">取消回复</el-button>
                    </div>
                  </div>

                  <el-skeleton :loading="commentsLoading" animated :rows="3">
                    <template #default>
                      <div v-if="organizedComments.length > 0" class="comment-list">
                        <div
                          v-for="comment in organizedComments"
                          :key="comment.commentId"
                          class="comment-item"
                        >
                          <!-- 评论头部 -->
                          <div class="comment-header">
                            <div class="user-info">
                              <el-avatar :size="40" :src="comment.avatarUrl">{{ comment.username?.charAt(0) || '匿' }}</el-avatar>
                              <div class="user-meta">
                                <div class="user-name">{{ comment.username || '匿名用户' }}</div>
                                <div class="comment-time">{{ comment.updateTime }}</div>
                              </div>
                            </div>
                            <el-rate
                              v-if="comment.deleteFlag !== 1 && comment.stars"
                              :model-value="comment.stars"
                              disabled
                              show-score
                              text-color="#ff9900"
                            />
                          </div>

                          <!-- 评论内容 -->
                          <div class="comment-content" :class="{ 'is-deleted': comment.deleteFlag === 1 }">
                            {{ comment.deleteFlag === 1 ? '该评论已删除' : comment.content }}
                          </div>

                          <!-- 评论媒体（图片/视频）- 已删除的评论不显示媒体 -->
                          <div v-if="comment.deleteFlag !== 1 && comment.mediaVOS && comment.mediaVOS.length > 0" class="comment-media">
                            <template v-for="(media, index) in comment.mediaVOS" :key="index">
                              <!-- 图片类型 -->
                              <el-image
                                v-if="media.mediaType === 1"
                                :src="media.mediaUrl"
                                :preview-src-list="comment.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                :initial-index="comment.mediaVOS.filter(m => m.mediaType === 1).findIndex(m => m.mediaUrl === media.mediaUrl)"
                                fit="cover"
                                class="media-image"
                                lazy
                              />
                              <!-- 视频类型 -->
                              <div 
                                v-else-if="media.mediaType === 2" 
                                class="media-video-item"
                                @click="openVideoPlayer(media.mediaUrl)"
                              >
                                <video :src="media.mediaUrl" preload="metadata" muted />
                                <div class="video-play-btn">
                                  <el-icon :size="24"><VideoPlay /></el-icon>
                                </div>
                              </div>
                            </template>
                          </div>

                          <!-- 评论操作 - 已删除的评论只显示查看回复按钮 -->
                          <div class="comment-actions">
                            <el-button
                              v-if="comment.deleteFlag !== 1"
                              text
                              size="small"
                              @click="handleReply(comment.commentId, comment.username)"
                            >
                              回复
                            </el-button>
                            <!-- 删除按钮（仅自己的评论可删除，且未删除） -->
                            <el-button
                              v-if="comment.deleteFlag !== 1 && isOwnComment(comment.userId)"
                              text
                              size="small"
                              type="danger"
                              @click="handleDeleteComment(comment.commentId)"
                            >
                              <el-icon><Delete /></el-icon>
                              删除
                            </el-button>
                            <!-- 查看回复入口 -->
                            <el-button
                              v-if="getTotalRepliesCount(comment) > 0"
                              text
                              size="small"
                              type="primary"
                              @click="toggleReplies(comment.commentId)"
                            >
                              {{ expandedReplies[comment.commentId] ? '收起回复' : `查看${getTotalRepliesCount(comment)}条回复` }}
                            </el-button>
                          </div>

                          <!-- 内联回复编辑器 -->
                          <div v-if="replyingTo && replyingTo.commentId === comment.commentId" class="inline-reply-editor">
                            <CommentEditor
                              v-model="commentContent"
                              mode="reply"
                              :reply-to="replyingTo.username"
                              :loading="commentSubmitting"
                              :show-rating="false"
                              @submit="handleCommentSubmit"
                              @cancel="cancelReply"
                            />
                          </div>

                          <!-- 回复列表（折叠显示，支持多层嵌套） -->
                          <div v-if="getTotalRepliesCount(comment) > 0 && expandedReplies[comment.commentId]" class="replies-list">
                            <template v-for="reply in comment.replies" :key="reply.commentId">
                              <!-- 一级回复 -->
                              <div class="reply-item">
                                <div class="reply-header">
                                  <el-avatar :size="28" :src="reply.avatarUrl">{{ reply.username?.charAt(0) || '匿' }}</el-avatar>
                                  <span class="reply-username">{{ reply.username || '匿名用户' }}</span>
                                  <span class="reply-to">回复</span>
                                  <span class="reply-target">@{{ getReplyTargetText(comment.username, comment.userId) }}</span>
                                  <span class="reply-time">{{ reply.updateTime }}</span>
                                </div>
                                <div class="reply-content" :class="{ 'is-deleted': reply.deleteFlag === 1 }">
                                  {{ reply.deleteFlag === 1 ? '该评论已删除' : reply.content }}
                                </div>
                                <!-- 回复媒体 - 已删除的不显示 -->
                                <div v-if="reply.deleteFlag !== 1 && reply.mediaVOS && reply.mediaVOS.length > 0" class="reply-media">
                                  <template v-for="(media, index) in reply.mediaVOS" :key="index">
                                    <el-image
                                      v-if="media.mediaType === 1"
                                      :src="media.mediaUrl"
                                      :preview-src-list="reply.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                      fit="cover"
                                      class="media-image-small"
                                      lazy
                                    />
                                    <div v-else-if="media.mediaType === 2" class="media-video-item small" @click="openVideoPlayer(media.mediaUrl)">
                                      <video :src="media.mediaUrl" preload="metadata" muted />
                                      <div class="video-play-btn"><el-icon :size="18"><VideoPlay /></el-icon></div>
                                    </div>
                                  </template>
                                </div>
                                <!-- 一级回复的操作按钮 -->
                                <div class="reply-actions">
                                  <el-button
                                    v-if="reply.deleteFlag !== 1"
                                    text
                                    size="small"
                                    @click="handleReply(reply.commentId, reply.username)"
                                  >
                                    回复
                                  </el-button>
                                  <el-button
                                    v-if="reply.deleteFlag !== 1 && isOwnComment(reply.userId)"
                                    text
                                    size="small"
                                    type="danger"
                                    @click="handleDeleteComment(reply.commentId)"
                                  >
                                    <el-icon><Delete /></el-icon>
                                    删除
                                  </el-button>
                                  <el-button
                                    v-if="getTotalRepliesCount(reply) > 0"
                                    text
                                    size="small"
                                    type="primary"
                                    @click="toggleReplies(reply.commentId)"
                                  >
                                    {{ expandedReplies[reply.commentId] ? '收起回复' : `查看${getTotalRepliesCount(reply)}条回复` }}
                                  </el-button>
                                </div>
                                <!-- 内联回复编辑器 -->
                                <div v-if="replyingTo && replyingTo.commentId === reply.commentId" class="inline-reply-editor">
                                  <CommentEditor
                                    v-model="commentContent"
                                    mode="reply"
                                    :reply-to="replyingTo.username"
                                    :loading="commentSubmitting"
                                    :show-rating="false"
                                    @submit="handleCommentSubmit"
                                    @cancel="cancelReply"
                                  />
                                </div>
                              </div>
                              <!-- 二级回复（折叠显示） -->
                              <template v-if="reply.replies.length > 0 && expandedReplies[reply.commentId]">
                                <template v-for="subReply in reply.replies" :key="subReply.commentId">
                                  <div class="reply-item nested">
                                    <div class="reply-header">
                                      <el-avatar :size="24" :src="subReply.avatarUrl">{{ subReply.username?.charAt(0) || '匿' }}</el-avatar>
                                      <span class="reply-username">{{ subReply.username || '匿名用户' }}</span>
                                      <span class="reply-to">回复</span>
                                      <span class="reply-target">@{{ getReplyTargetText(reply.username, reply.userId) }}</span>
                                      <span class="reply-time">{{ subReply.updateTime }}</span>
                                    </div>
                                    <div class="reply-content" :class="{ 'is-deleted': subReply.deleteFlag === 1 }">
                                      {{ subReply.deleteFlag === 1 ? '该评论已删除' : subReply.content }}
                                    </div>
                                    <!-- 二级回复媒体 - 已删除的不显示 -->
                                    <div v-if="subReply.deleteFlag !== 1 && subReply.mediaVOS && subReply.mediaVOS.length > 0" class="reply-media">
                                      <template v-for="(media, index) in subReply.mediaVOS" :key="index">
                                        <el-image
                                          v-if="media.mediaType === 1"
                                          :src="media.mediaUrl"
                                          :preview-src-list="subReply.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                          fit="cover"
                                          class="media-image-small"
                                          lazy
                                        />
                                        <div v-else-if="media.mediaType === 2" class="media-video-item small" @click="openVideoPlayer(media.mediaUrl)">
                                          <video :src="media.mediaUrl" preload="metadata" muted />
                                          <div class="video-play-btn"><el-icon :size="18"><VideoPlay /></el-icon></div>
                                        </div>
                                      </template>
                                    </div>
                                    <!-- 二级回复的操作按钮 -->
                                    <div class="reply-actions">
                                      <el-button
                                        v-if="subReply.deleteFlag !== 1"
                                        text
                                        size="small"
                                        @click="handleReply(subReply.commentId, subReply.username)"
                                      >
                                        回复
                                      </el-button>
                                      <el-button
                                        v-if="subReply.deleteFlag !== 1 && isOwnComment(subReply.userId)"
                                        text
                                        size="small"
                                        type="danger"
                                        @click="handleDeleteComment(subReply.commentId)"
                                      >
                                        <el-icon><Delete /></el-icon>
                                        删除
                                      </el-button>
                                      <el-button
                                        v-if="getTotalRepliesCount(subReply) > 0"
                                        text
                                        size="small"
                                        type="primary"
                                        @click="toggleReplies(subReply.commentId)"
                                      >
                                        {{ expandedReplies[subReply.commentId] ? '收起回复' : `查看${getTotalRepliesCount(subReply)}条回复` }}
                                      </el-button>
                                    </div>
                                    <!-- 内联回复编辑器 -->
                                    <div v-if="replyingTo && replyingTo.commentId === subReply.commentId" class="inline-reply-editor">
                                      <CommentEditor
                                        v-model="commentContent"
                                        mode="reply"
                                        :reply-to="replyingTo.username"
                                        :loading="commentSubmitting"
                                        :show-rating="false"
                                        @submit="handleCommentSubmit"
                                        @cancel="cancelReply"
                                      />
                                    </div>
                                  </div>
                                  <!-- 三级回复（折叠显示） -->
                                  <template v-if="subReply.replies.length > 0 && expandedReplies[subReply.commentId]">
                                    <template v-for="deepReply in subReply.replies" :key="deepReply.commentId">
                                      <div class="reply-item nested deep">
                                        <div class="reply-header">
                                          <el-avatar :size="24" :src="deepReply.avatarUrl">{{ deepReply.username?.charAt(0) || '匿' }}</el-avatar>
                                          <span class="reply-username">{{ deepReply.username || '匿名用户' }}</span>
                                          <span class="reply-to">回复</span>
                                          <span class="reply-target">@{{ getReplyTargetText(subReply.username, subReply.userId) }}</span>
                                          <span class="reply-time">{{ deepReply.updateTime }}</span>
                                        </div>
                                        <div class="reply-content" :class="{ 'is-deleted': deepReply.deleteFlag === 1 }">
                                          {{ deepReply.deleteFlag === 1 ? '该评论已删除' : deepReply.content }}
                                        </div>
                                        <!-- 三级回复媒体 - 已删除的不显示 -->
                                        <div v-if="deepReply.deleteFlag !== 1 && deepReply.mediaVOS && deepReply.mediaVOS.length > 0" class="reply-media">
                                          <template v-for="(media, index) in deepReply.mediaVOS" :key="index">
                                            <el-image
                                              v-if="media.mediaType === 1"
                                              :src="media.mediaUrl"
                                              :preview-src-list="deepReply.mediaVOS.filter(m => m.mediaType === 1).map(m => m.mediaUrl)"
                                              fit="cover"
                                              class="media-image-small"
                                              lazy
                                            />
                                            <div v-else-if="media.mediaType === 2" class="media-video-item small" @click="openVideoPlayer(media.mediaUrl)">
                                              <video :src="media.mediaUrl" preload="metadata" muted />
                                              <div class="video-play-btn"><el-icon :size="18"><VideoPlay /></el-icon></div>
                                            </div>
                                          </template>
                                        </div>
                                        <!-- 三级回复的操作按钮 -->
                                        <div class="reply-actions">
                                          <el-button
                                            v-if="deepReply.deleteFlag !== 1"
                                            text
                                            size="small"
                                            @click="handleReply(deepReply.commentId, deepReply.username)"
                                          >
                                            回复
                                          </el-button>
                                          <el-button
                                            v-if="deepReply.deleteFlag !== 1 && isOwnComment(deepReply.userId)"
                                            text
                                            size="small"
                                            type="danger"
                                            @click="handleDeleteComment(deepReply.commentId)"
                                          >
                                            <el-icon><Delete /></el-icon>
                                            删除
                                          </el-button>
                                          <el-button
                                            v-if="getTotalRepliesCount(deepReply) > 0"
                                            text
                                            size="small"
                                            type="primary"
                                            @click="toggleReplies(deepReply.commentId)"
                                          >
                                            {{ expandedReplies[deepReply.commentId] ? '收起回复' : `查看${getTotalRepliesCount(deepReply)}条回复` }}
                                          </el-button>
                                        </div>
                                        <!-- 内联回复编辑器 -->
                                        <div v-if="replyingTo && replyingTo.commentId === deepReply.commentId" class="inline-reply-editor">
                                          <CommentEditor
                                            v-model="commentContent"
                                            mode="reply"
                                            :reply-to="replyingTo.username"
                                            :loading="commentSubmitting"
                                            :show-rating="false"
                                            @submit="handleCommentSubmit"
                                            @cancel="cancelReply"
                                          />
                                        </div>
                                      </div>
                                    </template>
                                  </template>
                                </template>
                              </template>
                            </template>
                          </div>
                        </div>
                      </div>
                      <el-empty v-else description="暂无评价" />
                    </template>
                  </el-skeleton>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
          </div>
          
          <!-- 商品不存在 -->
          <el-empty v-else description="商品不存在或已下架" />
        </div>
      </template>
    </el-skeleton>

    <!-- 视频播放器模态层 -->
    <Teleport to="body">
      <Transition name="video-modal">
        <div v-if="videoPlayerVisible" class="video-modal-overlay" @mousedown.self="videoModalMouseDown = true" @mouseup.self="videoModalMouseDown && closeVideoPlayer(); videoModalMouseDown = false">
          <div 
            class="video-modal-content" 
            :style="videoModalStyle"
            @mousedown="videoModalMouseDown = false"
          >
            <CustomVideoPlayer
              ref="videoPlayerRef"
              :src="currentVideoUrl"
              class="video-player-custom"
              @loadedmetadata="handleVideoMetadata"
            />
            <button class="video-close-btn" @click="closeVideoPlayer">
              <el-icon :size="24"><Close /></el-icon>
            </button>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 实名认证提示弹窗 -->
    <Teleport to="body">
      <Transition name="auth-tip-modal">
        <div v-if="showAuthTipDialog" class="auth-tip-modal-overlay">
          <div class="auth-tip-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Warning /></el-icon>
                需要实名认证
              </h3>
              <button class="modal-close" @click="showAuthTipDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="modal-body">
              <div class="auth-tip-content">
                <p>{{ authTipMessage }}</p>
                <p class="auth-tip-hint">请前往个人资料页面完成实名认证后再进行操作。</p>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showAuthTipDialog = false">取消</button>
              <button class="btn-confirm" @click="showAuthTipDialog = false; router.push('/profile/profile')">去认证</button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 信息缺失提示弹窗（手机号/收货地址） -->
    <Teleport to="body">
      <Transition name="info-tip-modal">
        <div v-if="showInfoTipDialog" class="info-tip-modal-overlay">
          <div class="info-tip-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Warning /></el-icon>
                {{ infoTipType === 'phone' ? '需要绑定手机号' : '需要添加收货地址' }}
              </h3>
              <button class="modal-close" @click="showInfoTipDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            <div class="modal-body">
              <div class="info-tip-content">
                <p>{{ infoTipMessage }}</p>
                <p class="info-tip-hint">
                  {{ infoTipType === 'phone' 
                    ? '请前往账号管理页面绑定手机号后再进行下单。' 
                    : '请前往账号管理页面添加收货地址后再进行下单。' 
                  }}
                </p>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-cancel" @click="showInfoTipDialog = false">取消</button>
              <button class="btn-confirm" @click="showInfoTipDialog = false; router.push('/profile/settings')">
                {{ infoTipType === 'phone' ? '去绑定' : '去添加' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.product-detail-page {
  background: var(--color-bg-base);
  min-height: 100vh;
  padding-bottom: var(--spacing-4xl);
}

// 导航头部
.nav-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-base) 0;
  margin-bottom: var(--spacing-lg);
}

// 返回按钮
.back-nav {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--radius-base);
  transition: all var(--transition-fast);
  flex-shrink: 0;
  
  &:hover {
    color: var(--color-primary);
    background: var(--color-secondary-light);
  }
  
  .el-icon {
    font-size: 14px;
  }
  
  span {
    font-size: var(--font-size-sm);
  }
}

// 面包屑导航
.breadcrumb-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-sm);
  
  .breadcrumb-item {
    color: var(--color-text-secondary);
    cursor: pointer;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--color-primary);
    }
  }
  
  .breadcrumb-separator {
    color: var(--color-text-tertiary);
  }
  
  .breadcrumb-current {
    color: var(--color-text-primary);
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

// 详情区域
.detail-section {
  display: grid;
  grid-template-columns: 600px 1fr;
  gap: var(--spacing-3xl);
  margin-bottom: var(--spacing-4xl);
  background: var(--color-bg-surface);
  padding: var(--spacing-3xl);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-base);
}

// 左侧图片画廊
.left-section {
  .image-gallery {
    position: sticky;
    top: calc(var(--header-height) + var(--spacing-2xl));
    will-change: transform;
    contain: layout style paint;
    isolation: isolate;
  }

  .main-image {
    width: 100%;
    height: 600px;
    border-radius: var(--radius-lg);
    overflow: hidden;
    background: var(--color-bg-elevated);
    margin-bottom: var(--spacing-lg);
    transform: translate3d(0, 0, 0);
    backface-visibility: hidden;
    -webkit-backface-visibility: hidden;
    perspective: 1000px;
    -webkit-perspective: 1000px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
      transform: translate3d(0, 0, 0);
      image-rendering: -webkit-optimize-contrast;
      image-rendering: crisp-edges;
    }
  }

  .thumbnail-list-wrapper {
    display: flex;
    align-items: center;
    gap: var(--spacing-base);

    .thumb-nav-btn {
      flex-shrink: 0;
      width: 32px;
      height: 32px;
      border: none;
      background: var(--color-bg-elevated);
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--color-text-secondary);
      transition: all var(--transition-base);
      box-shadow: var(--shadow-sm);

      &:hover:not(:disabled) {
        background: var(--color-primary);
        color: white;
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }

      .el-icon {
        font-size: 16px;
      }
    }
  }

  .thumbnail-list {
    flex: 1;
    overflow: hidden;
    width: calc(4 * 100px + 3 * var(--spacing-base)); // 4个缩略图 + 3个间距

    .thumbnail-track {
      display: flex;
      gap: var(--spacing-base);
      transition: transform 0.3s ease;
    }
  }

  .thumbnail-item {
    width: 100px;
    height: 100px;
    border-radius: var(--radius-base);
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all var(--transition-base);
    flex-shrink: 0;

    &:hover {
      border-color: var(--color-primary);
    }

    &.active {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(255, 107, 74, 0.2);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

// 右侧信息
.right-section {
  .product-title-row {
    display: flex;
    align-items: flex-start;
    gap: var(--spacing-base);
    margin-bottom: var(--spacing-lg);
  }

  .product-title {
    font-size: var(--font-size-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    line-height: 1.4;
    flex: 1;
    margin: 0;
  }

  .product-status-badge {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: var(--radius-circle);
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    margin-top: 4px;
    box-shadow: var(--shadow-sm);
    transition: all var(--transition-base) var(--ease-out);

    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      animation: pulse 2s infinite;
    }

    // 出售中 - 绿色渐变
    &.status-on-sale {
      background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
      color: var(--color-accent);
      border: 1px solid var(--color-accent-light);

      .status-dot {
        background: var(--color-accent);
        box-shadow: 0 0 0 3px rgba(47, 143, 106, 0.2);
      }
    }

    // 售罄 - 橘色渐变
    &.status-sold-out {
      background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      color: #d68000;
      border: 1px solid #ffb74d;

      .status-dot {
        background: #ff9800;
        box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.2);
        animation: none;
      }
    }

    // 已下架 - 灰色渐变
    &.status-off-shelf {
      background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
      color: #757575;
      border: 1px solid #bdbdbd;

      .status-dot {
        background: #9e9e9e;
        box-shadow: none;
        animation: none;
      }
    }
  }

  @keyframes pulse {
    0%, 100% {
      opacity: 1;
      transform: scale(1);
    }
    50% {
      opacity: 0.7;
      transform: scale(1.1);
    }
  }

  .product-price {
    margin-bottom: var(--spacing-2xl);
    padding: var(--spacing-lg);
    background: linear-gradient(135deg, #fff8f4 0%, #ffe8dc 100%);
    border-radius: var(--radius-base);

    .price-symbol {
      font-size: var(--font-size-xl);
      color: var(--color-accent);
      font-weight: var(--font-weight-bold);
      margin-right: var(--spacing-xs);
    }

    .price-value {
      font-size: 48px;
      color: var(--color-accent);
      font-weight: var(--font-weight-bold);
      font-family: var(--font-family-number);
    }
  }

  .product-params {
    background: #fff8f4;
    padding: var(--spacing-xl);
    border-radius: var(--radius-base);
    margin-bottom: var(--spacing-2xl);

    .param-row {
      display: flex;
      align-items: flex-start;
      padding: var(--spacing-base) 0;
      border-bottom: 1px solid var(--color-border-light);

      &:last-child {
        border-bottom: none;
      }

      .param-label {
        font-size: var(--font-size-base);
        color: var(--color-text-secondary);
        width: 120px;
        flex-shrink: 0;
        padding-top: 2px;
      }

      .param-value {
        font-size: var(--font-size-base);
        color: var(--color-text-primary);
        font-weight: var(--font-weight-medium);
        flex: 1;
        word-break: break-all;
        line-height: 1.6;
      }
    }
  }

  .product-quantity {
    display: flex;
    align-items: center;
    margin-bottom: var(--spacing-2xl);
    padding: var(--spacing-lg) 0;

    .quantity-label {
      font-size: var(--font-size-lg);
      color: var(--color-text-primary);
      margin-right: var(--spacing-lg);
      font-weight: var(--font-weight-medium);
    }

    .quantity-input {
      // 去掉整体的蓝色边框
      :deep(.el-input-number) {
        border: none !important;
        box-shadow: none !important;
        
        &:focus,
        &:hover,
        &.is-focus {
          border: none !important;
          box-shadow: none !important;
        }
      }

      :deep(.el-input-number__decrease),
      :deep(.el-input-number__increase) {
        width: 36px;
        height: 36px;
        border-radius: var(--radius-base);
        background: #f5f5f5;
        border: none;
        color: #666;
        
        &:hover:not(.is-disabled) {
          color: var(--color-primary);
          background: #fff0eb;
        }
        
        &.is-disabled {
          color: #ccc;
          background: #fafafa;
        }
      }

      :deep(.el-input__wrapper) {
        width: 60px;
        height: 36px;
        border: none !important;
        box-shadow: none !important;
        background: transparent;
        
        &:focus,
        &:hover,
        &.is-focus {
          box-shadow: none !important;
        }
        
        .el-input__inner {
          text-align: center;
          font-size: var(--font-size-lg);
          font-weight: var(--font-weight-semibold);
          color: var(--color-text-primary);
        }
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: var(--spacing-lg);

    .buy-now-btn,
    .add-cart-btn {
      flex: 1;
      height: 56px;
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      border-radius: 24px;
    }

    .buy-now-btn {
      background: var(--color-primary);
      border-color: var(--color-primary);
      color: white;

      &:hover {
        background: var(--color-primary-dark);
        border-color: var(--color-primary-dark);
      }
    }

    .add-cart-btn {
      border: 2px solid var(--color-primary);
      color: var(--color-primary);

      &:hover {
        background: rgba(255, 107, 74, 0.1);
      }
    }
  }
}

// 信息区域
.info-section {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-base);
  overflow: hidden;

  .detail-tabs {
    :deep(.el-tabs__header) {
      background: var(--color-bg-elevated);
      margin: 0;
      padding: 0 var(--spacing-3xl);
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-medium);
      padding: var(--spacing-xl) var(--spacing-2xl);
    }

    :deep(.el-tabs__content) {
      padding: var(--spacing-3xl);
    }
  }
}

// 详情内容
.detail-content {
  .detail-block {
    margin-bottom: var(--spacing-3xl);

    &:last-child {
      margin-bottom: 0;
    }

    .block-title {
      font-size: var(--font-size-xl);
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
      margin-bottom: var(--spacing-lg);
      padding-bottom: var(--spacing-base);
      border-bottom: 2px solid var(--color-primary);
    }

    .block-content {
      font-size: var(--font-size-base);
      color: var(--color-text-secondary);
      line-height: 1.8;

      p {
        margin-bottom: var(--spacing-base);
      }
    }
  }
}

// 评论区域
.comments-section {
  // 评论编辑器包装
  .comment-editor-wrapper {
    margin-bottom: var(--spacing-2xl);
    padding-bottom: var(--spacing-2xl);
    border-bottom: 1px solid var(--color-border-light);

    .login-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: var(--spacing-lg);
      padding: var(--spacing-2xl);
      background: var(--color-bg-elevated);
      border-radius: var(--radius-base);
      color: var(--color-text-secondary);
    }

    .replying-tip {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: var(--spacing-sm) var(--spacing-base);
      margin-bottom: var(--spacing-sm);
      background: #fff8f4;
      border-radius: var(--radius-sm);
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);

      strong {
        color: var(--color-primary);
      }
    }

    .replying-elsewhere-tip {
      display: flex;
      align-items: center;
      gap: var(--spacing-base);
      padding: var(--spacing-base);
      background: #fff8f4;
      border-radius: var(--radius-sm);
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);

      strong {
        color: var(--color-primary);
      }
    }
  }

  // 内联回复编辑器
  .inline-reply-editor {
    margin-top: var(--spacing-base);
    padding: var(--spacing-base);
    background: var(--color-bg-base);
    border-radius: var(--radius-base);
    border: 1px solid var(--color-border-light);

    .replying-tip {
      margin-bottom: var(--spacing-sm);
    }
  }

  .comment-list {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-2xl);
  }

  .comment-item {
    padding: var(--spacing-xl);
    background: var(--color-bg-elevated);
    border-radius: var(--radius-base);
  }

  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    .user-info {
      display: flex;
      align-items: center;
      gap: var(--spacing-base);
    }

    .user-meta {
      .user-name {
        font-size: var(--font-size-base);
        font-weight: var(--font-weight-semibold);
        color: var(--color-text-primary);
        margin-bottom: var(--spacing-xs);
      }

      .comment-time {
        font-size: var(--font-size-sm);
        color: var(--color-text-tertiary);
      }
    }
  }

  .comment-content {
    font-size: var(--font-size-base);
    color: var(--color-text-secondary);
    line-height: 1.6;
    margin-bottom: var(--spacing-base);

    &.is-deleted {
      color: #999;
      font-style: italic;
    }
  }

  // 评论媒体样式
  .comment-media {
    display: flex;
    flex-wrap: wrap;
    gap: var(--spacing-base);
    margin-top: var(--spacing-md);
    margin-bottom: var(--spacing-base);

    .media-image {
      width: 120px;
      height: 120px;
      border-radius: var(--radius-base);
      cursor: pointer;
      overflow: hidden;

      :deep(.el-image__inner) {
        transition: transform var(--transition-base);
      }

      &:hover :deep(.el-image__inner) {
        transform: scale(1.05);
      }
    }

    // 视频样式（直接显示视频，播放按钮居中）
    .media-video-item {
      position: relative;
      width: 120px;
      height: 120px;
      border-radius: var(--radius-base);
      overflow: hidden;
      cursor: pointer;
      background: #1a1a2e;
      flex-shrink: 0;

      video {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      &:hover {
        .video-play-btn {
          transform: translate(-50%, -50%) scale(1.1);
          background: var(--color-primary);
          color: white;
        }
      }

      .video-play-btn {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 40px;
        height: 40px;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-primary);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
        transition: all var(--transition-base);

        .el-icon {
          margin-left: 2px;
        }
      }

      // 小尺寸（回复中使用）
      &.small {
        width: 80px;
        height: 80px;

        .video-play-btn {
          width: 28px;
          height: 28px;

          .el-icon {
            margin-left: 1px;
          }
        }
      }
    }
  }

  // 回复媒体样式（较小尺寸）
  .reply-media {
    display: flex;
    flex-wrap: wrap;
    gap: var(--spacing-sm);
    margin-top: var(--spacing-sm);
    padding-left: 36px;

    .media-image-small {
      width: 80px;
      height: 80px;
      border-radius: var(--radius-sm);
      cursor: pointer;
      overflow: hidden;

      :deep(.el-image__inner) {
        transition: transform var(--transition-base);
      }

      &:hover :deep(.el-image__inner) {
        transform: scale(1.05);
      }
    }

    // 回复中的视频样式
    .media-video-item.small {
      position: relative;
      width: 80px;
      height: 80px;
      border-radius: var(--radius-sm);
      overflow: hidden;
      cursor: pointer;
      background: #1a1a2e;
      flex-shrink: 0;

      video {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }

      &:hover {
        .video-play-btn {
          transform: translate(-50%, -50%) scale(1.1);
          background: var(--color-primary);
          color: white;
        }
      }

      .video-play-btn {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 28px;
        height: 28px;
        background: rgba(255, 255, 255, 0.95);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-primary);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
        transition: all var(--transition-base);

        .el-icon {
          margin-left: 1px;
        }
      }
    }
  }

  .comment-actions {
    display: flex;
    gap: var(--spacing-base);
  }

  // 回复列表样式
  .replies-list {
    margin-top: var(--spacing-lg);
    padding-left: var(--spacing-lg);
    border-left: 2px solid var(--color-border-light);

    .reply-item {
      padding: var(--spacing-md) 0;
      border-bottom: 1px solid var(--color-border-light);

      &:last-child {
        border-bottom: none;
      }
    }

    .reply-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-xs);
      flex-wrap: wrap;

      .reply-username {
        font-size: var(--font-size-sm);
        font-weight: var(--font-weight-medium);
        color: var(--color-text-primary);
      }

      .reply-rating {
        height: 16px;
        
        :deep(.el-rate__icon) {
          font-size: 12px;
          margin-right: 2px;
        }
      }

      .reply-time {
        font-size: var(--font-size-xs);
        color: var(--color-text-tertiary);
        margin-left: auto;
      }
    }

    .reply-content {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      line-height: 1.5;
      padding-left: 36px; // 与头像对齐

      &.is-deleted {
        color: #999;
        font-style: italic;
      }
    }

    .reply-actions {
      padding-left: 36px;
      margin-top: var(--spacing-xs);
    }

    // 嵌套回复样式
    .reply-item.nested {
      padding-left: var(--spacing-xl);
      background: rgba(0, 0, 0, 0.02);
      border-radius: var(--radius-sm);
      margin-top: var(--spacing-xs);

      .reply-content {
        padding-left: 32px;
      }
    }

    .reply-item.nested.deep {
      padding-left: var(--spacing-2xl);
    }

    .reply-to {
      font-size: var(--font-size-xs);
      color: var(--color-text-tertiary);
      margin: 0 var(--spacing-xs);
    }

    .reply-target {
      font-size: var(--font-size-xs);
      color: var(--color-primary);
      font-weight: var(--font-weight-medium);
    }
  }
}

// 响应式
@media (max-width: 1024px) {
  .detail-section {
    grid-template-columns: 1fr;
    gap: var(--spacing-2xl);
  }

  .left-section .image-gallery {
    position: static;
  }
}

</style>

<!-- 视频模态层样式（全局，因为使用了Teleport） -->
<style lang="scss">
.video-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 24px;
}

.video-modal-content {
  position: relative;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  // 默认尺寸，加载后会被动态样式覆盖
  width: auto;
  max-width: 90vw;
  max-height: 80vh;
}

.video-player-custom {
  width: 100%;
  height: 100%;
  display: block;
  outline: none;
}

.video-close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;

  &:hover {
    background: rgba(0, 0, 0, 0.8);
    transform: scale(1.1);
  }
}

// 视频模态层动画
.video-modal-enter-active,
.video-modal-leave-active {
  transition: all 0.3s ease;
}

.video-modal-enter-active .video-modal-content,
.video-modal-leave-active .video-modal-content {
  transition: all 0.3s ease;
}

.video-modal-enter-from,
.video-modal-leave-to {
  opacity: 0;
}

.video-modal-enter-from .video-modal-content,
.video-modal-leave-to .video-modal-content {
  transform: scale(0.9);
  opacity: 0;
}

// 实名认证提示弹窗样式
.auth-tip-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.auth-tip-modal {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;
    
    .modal-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
      
      .el-icon {
        color: #e6a23c;
      }
    }
    
    .modal-close {
      width: 32px;
      height: 32px;
      border: none;
      background: #f5f5f5;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      &:hover {
        background: #eee;
        transform: rotate(90deg);
      }
      
      .el-icon {
        font-size: 16px;
        color: #666;
      }
    }
  }
  
  .modal-body {
    padding: 24px;
  }
  
  .auth-tip-content {
    text-align: center;
    
    p {
      margin: 0 0 12px;
      font-size: 15px;
      color: #333;
      line-height: 1.6;
    }
    
    .auth-tip-hint {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    
    .btn-cancel,
    .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
    }
  }
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.auth-tip-modal-enter-active,
.auth-tip-modal-leave-active {
  transition: all 0.3s ease;
}

.auth-tip-modal-enter-from,
.auth-tip-modal-leave-to {
  opacity: 0;
}

.auth-tip-modal-enter-from .auth-tip-modal,
.auth-tip-modal-leave-to .auth-tip-modal {
  transform: scale(0.9);
  opacity: 0;
}

// 信息缺失提示弹窗样式
.info-tip-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.info-tip-modal {
  width: 400px;
  max-width: 100%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  animation: infoModalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;
    
    .modal-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
      
      .el-icon {
        color: #e6a23c;
      }
    }
    
    .modal-close {
      width: 32px;
      height: 32px;
      border: none;
      background: #f5f5f5;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      
      &:hover {
        background: #eee;
        transform: rotate(90deg);
      }
      
      .el-icon {
        font-size: 16px;
        color: #666;
      }
    }
  }
  
  .modal-body {
    padding: 24px;
  }
  
  .info-tip-content {
    text-align: center;
    
    p {
      margin: 0 0 12px;
      font-size: 15px;
      color: #333;
      line-height: 1.6;
    }
    
    .info-tip-hint {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    
    .btn-cancel,
    .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      
      &:hover {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
    }
  }
}

@keyframes infoModalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.info-tip-modal-enter-active,
.info-tip-modal-leave-active {
  transition: all 0.3s ease;
}

.info-tip-modal-enter-from,
.info-tip-modal-leave-to {
  opacity: 0;
}

.info-tip-modal-enter-from .info-tip-modal,
.info-tip-modal-leave-to .info-tip-modal {
  transform: scale(0.9);
  opacity: 0;
}
</style>
