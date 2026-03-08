<script>
export default {
  name: 'MyOrders'
}
</script>

<script setup>
import { ref, onMounted, watch, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Money, Van, Check, Plus, View, Loading, Refresh, Filter, ArrowDown, ArrowUp, Close, Headset } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_API, request } from '@/api/config.js'
import { markAsBackNavigation } from '@/router'
import LogisticsMap from '@/components/LogisticsMap.vue'


const router = useRouter()
const route = useRoute()

// 注入设置聊天订单的方法
const setChatOrder = inject('setChatOrder', null)

const props = defineProps({
  userInfo: { type: Object, default: () => null }
})

// sessionStorage key
const STORAGE_KEY = 'myOrdersFilter'

// 从 sessionStorage 恢复筛选条件
const loadFilterFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY)
    if (saved) {
      const data = JSON.parse(saved)
      return {
        activeTab: data.activeTab || 'all',
        filterForm: {
          dateRange: data.dateRange ? [new Date(data.dateRange[0]), new Date(data.dateRange[1])] : null,
          latest: data.latest ?? null
        },
        pagination: {
          currentPage: data.currentPage || 1,
          pageSize: data.pageSize || 10
        }
      }
    }
  } catch (e) {
    console.warn('读取筛选条件失败:', e)
  }
  return null
}

// 初始化时恢复筛选条件
const savedFilter = loadFilterFromStorage()

// 加载状态
const loading = ref(false)

// 分页 - 从 sessionStorage 恢复或使用默认值
const pagination = ref({
  currentPage: savedFilter?.pagination?.currentPage || 1,
  pageSize: savedFilter?.pagination?.pageSize || 10,
  total: 0
})

// Tab状态
const activeTab = ref(savedFilter?.activeTab || 'all')
const tabs = ref([
  { key: 'all', label: '全部', count: 0 },
  { key: 'pending', label: '待支付', count: 0 },
  { key: 'shipped', label: '待发货', count: 0 },
  { key: 'delivery', label: '已发货', count: 0 },
  { key: 'toSign', label: '待收货', count: 0 },
  { key: 'completed', label: '已收货', count: 0 },
  { key: 'refunding', label: '退款中', count: 0 },
  { key: 'cancelled', label: '已取消', count: 0 },
])

// 订单状态映射 (与后端Constants对应: 1-待支付, 2-待发货, 3-已发货, 4-待签收, 5-已收货, 6-订单取消)
const statusMap = {
  1: { text: '待支付', type: 'warning', color: '#E6A23C' },
  2: { text: '待发货', type: 'info', color: '#909399' },
  3: { text: '已发货', type: 'primary', color: '#409EFF' },
  4: { text: '待收货', type: 'primary', color: '#409EFF' },
  5: { text: '已收货', type: 'success', color: '#67C23A' },
  6: { text: '已取消', type: 'info', color: '#909399' },
}

// Tab到后端status的映射
const tabToStatusMap = {
  'all': null,
  'pending': 1,      // 待支付
  'shipped': 2,      // 待发货
  'delivery': 3,     // 已发货
  'toSign': 4,       // 待收货
  'completed': 5,    // 已收货
  'cancelled': 6     // 已取消
}

// 筛选表单 - 从 sessionStorage 恢复或使用默认值
const filterForm = ref(savedFilter?.filterForm || {
  dateRange: null,   // [startTime, endTime]
  latest: null       // 排序方式: 1=最新优先, 0=最早优先, null=默认
})

// 日期快捷选项
const dateShortcuts = [
  { text: '最近一周', value: () => [new Date(Date.now() - 7 * 24 * 3600 * 1000), new Date()] },
  { text: '最近一个月', value: () => [new Date(Date.now() - 30 * 24 * 3600 * 1000), new Date()] },
  { text: '最近三个月', value: () => [new Date(Date.now() - 90 * 24 * 3600 * 1000), new Date()] },
]

// 订单数据
const orders = ref([])

// 筛选面板展开状态
const showFilterPanel = ref(false)

// 是否有筛选条件
const hasActiveFilter = () => {
  return filterForm.value.dateRange !== null || filterForm.value.latest !== null
}

// 物流弹窗
const showLogisticsDialog = ref(false)
const currentLogisticsOrder = ref(null) // 当前查看物流的订单

// 退款弹窗
const showRefundDialog = ref(false)
const refundForm = ref({ reason: '', description: '', images: [] })
const refundReasons = ['不想要了', '商品与描述不符', '质量问题', '发货太慢', '其他原因']
const currentRefundOrder = ref(null) // 当前退款的订单
const refundSubmitting = ref(false) // 退款提交中

// 保存筛选条件到 sessionStorage
const saveFilterToStorage = () => {
  try {
    const data = {
      activeTab: activeTab.value,
      dateRange: filterForm.value.dateRange
        ? [filterForm.value.dateRange[0].toISOString(), filterForm.value.dateRange[1].toISOString()]
        : null,
      latest: filterForm.value.latest,
      currentPage: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch (e) {
    console.warn('保存筛选条件失败:', e)
  }
}

// 格式化日期为后端需要的格式 (LocalDateTime)
const formatDateForBackend = (date) => {
  if (!date) return null
  const d = new Date(date)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 转换订单数据格式
const transformOrderData = (order) => ({
  orderId: order.orderId,
  createTime: formatDateTime(order.createTime),
  status: order.status ?? 0,
  refundFlag: order.refundFlag ?? 0,
  totalPrice: parseFloat(order.totalPrice) || 0,
  products: [{
    id: order.productId,
    type: order.productType ?? order.productType,
    name: order.productName,
    image: order.imgUrl || 'https://via.placeholder.com/100x100',
    price: parseFloat(order.totalPrice) / (order.totalCount || 1),
    quantity: order.totalCount || 1
  }],
  receiptName: order.receiptName,
  receiptAddress: order.receiptAddress,
  phone: order.phone
})

// 获取订单列表
const fetchOrders = async () => {
  if (!props.userInfo?.userId) {
    console.warn('用户未登录')
    return
  }
  
  loading.value = true
  try {
    const statusValue = tabToStatusMap[activeTab.value]
    
    // 运输中Tab需要同时获取状态3和4的订单
    if (activeTab.value === 'inTransit') {
      const [res3, res4] = await Promise.all([
        request(ORDER_API.GET_ORDER_LIST, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            ordersDetailDTO: { userId: props.userInfo.userId, currentPage: 1, pageSize: 100 },
            orderFilterDTO: { latest: filterForm.value.latest, status: 3, startTime: filterForm.value.dateRange?.[0] ? formatDateForBackend(filterForm.value.dateRange[0]) : null, endTime: filterForm.value.dateRange?.[1] ? formatDateForBackend(filterForm.value.dateRange[1]) : null }
          })
        }),
        request(ORDER_API.GET_ORDER_LIST, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            ordersDetailDTO: { userId: props.userInfo.userId, currentPage: 1, pageSize: 100 },
            orderFilterDTO: { latest: filterForm.value.latest, status: 4, startTime: filterForm.value.dateRange?.[0] ? formatDateForBackend(filterForm.value.dateRange[0]) : null, endTime: filterForm.value.dateRange?.[1] ? formatDateForBackend(filterForm.value.dateRange[1]) : null }
          })
        })
      ])
      
      const [result3, result4] = await Promise.all([res3.json(), res4.json()])
      const records3 = result3.code === 0 ? (result3.data?.totalRecords || []) : []
      const records4 = result4.code === 0 ? (result4.data?.totalRecords || []) : []
      
      // 合并并按时间排序
      const allRecords = [...records3, ...records4]
      if (filterForm.value.latest === 1) {
        allRecords.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      } else if (filterForm.value.latest === 0) {
        allRecords.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
      }
      
      orders.value = allRecords.map(transformOrderData)
      pagination.value.total = allRecords.length
    } else {
      // 其他Tab正常请求
      const orderFilterDTO = {
        latest: filterForm.value.latest,
        status: activeTab.value === 'refunding' ? null : statusValue,
        refundFlag: activeTab.value === 'refunding' ? 1 : null,
        startTime: filterForm.value.dateRange?.[0] ? formatDateForBackend(filterForm.value.dateRange[0]) : null,
        endTime: filterForm.value.dateRange?.[1] ? formatDateForBackend(filterForm.value.dateRange[1]) : null
      }
      
      const response = await request(ORDER_API.GET_ORDER_LIST, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ordersDetailDTO: {
            userId: props.userInfo.userId,
            currentPage: pagination.value.currentPage,
            pageSize: pagination.value.pageSize
          },
          orderFilterDTO
        })
      })
      
      const result = await response.json()
      if (result.code === 0 && result.data) {
        const pageData = result.data
        orders.value = (pageData.totalRecords || []).map(transformOrderData)
        pagination.value.total = pageData.totalCount || 0
      } else {
        console.error('获取订单列表失败:', result.message)
      }
    }
  } catch (error) {
    console.error('获取订单列表异常:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}


// 重置筛选
const handleReset = () => {
  filterForm.value = {
    dateRange: null,
    latest: null
  }
  pagination.value.currentPage = 1
  sessionStorage.removeItem(STORAGE_KEY)
  fetchOrders()
}

// 获取指定订单详情
const fetchOrderDetail = async (orderId) => {
  if (!props.userInfo?.userId) return null
  
  try {
    const response = await request(
      `${ORDER_API.GET_ORDER_DETAIL}?orderId=${orderId}&userId=${props.userInfo.userId}`
    )
    const result = await response.json()
    
    if (result.code === 0) {
      return result.data
    } else {
      console.error('获取订单详情失败:', result.message)
      return null
    }
  } catch (error) {
    console.error('获取订单详情异常:', error)
    return null
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  if (typeof dateTime === 'string') return dateTime
  // 处理数组格式 [year, month, day, hour, minute, second]
  if (Array.isArray(dateTime)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateTime
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return dateTime
}

// 查看物流
const viewLogistics = (order) => {
  currentLogisticsOrder.value = order
  showLogisticsDialog.value = true
}

// 物流到达处理
const handleLogisticsArrived = () => {
  if (currentLogisticsOrder.value) {
    currentLogisticsOrder.value.status = 4 // 更新为待签收
  }
  fetchOrders()
}

// 取消订单
const cancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    const response = await request(ORDER_API.CANCEL_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: order.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      ElMessage.success('订单已取消')
      order.status = 6 // 已取消
    } else {
      ElMessage.error(result.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单异常:', error)
      ElMessage.error('网络错误')
    }
  }
}

// 去支付
const goToPay = async (order) => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 调用支付接口
    const response = await request(ORDER_API.PAY_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: order.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data?.successFlag) {
      ElMessage.success('支付成功')
      // 更新订单状态为待发货(2)
      order.status = 2
    } else {
      ElMessage.error(result.message || '支付失败')
    }
  } catch (error) {
    console.error('支付异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  }
}

// 确认收货（签收）
const confirmReceive = async (order) => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const response = await request(ORDER_API.RECEIVE_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: order.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('确认收货成功')
      order.status = 5 // 已签收
    } else {
      ElMessage.error(result.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 取消退款
const cancelRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消退款申请吗？', '取消退款', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    const response = await request(ORDER_API.CANCEL_REFUND, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: order.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('已取消退款申请')
      order.refundFlag = 0 // 取消退款标记
      fetchOrders()
    } else {
      ElMessage.error(result.message || '取消退款失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消退款异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 跳转到商品详情页
const goToProductDetail = (productId, productType) => {
  console.log('点击商品:', { productId, productType })
  if (productId) {
    // 标记为返回操作，防止覆盖商品详情页原有的来源记录
    markAsBackNavigation()
    const query = {}
    if (productType) query.type = productType
    router.push({
      path: `/product/${productId}`,
      query
    })
  }
}

// 咨询订单
const consultOrder = (order) => {
  if (!setChatOrder) {
    ElMessage.warning('客服功能暂未就绪，请稍后重试')
    return
  }

  // 设置当前订单信息
  const orderData = {
    orderId: order.orderId,
    productName: order.products?.[0]?.name || '多件商品',
    totalAmount: order.totalPrice,
    status: order.status,
    createTime: order.createTime,
    imgUrl: order.products?.[0]?.image,
    totalCount: order.products?.[0]?.quantity || 1
  }
  setChatOrder(orderData)

  // 触发联系客服按钮点击事件
  const customerServiceBtn = document.querySelector('.sidebar-item:nth-child(2)')
  if (customerServiceBtn) {
    customerServiceBtn.click()
  }
}

// 提交退款申请
const submitRefund = async () => {
  if (!refundForm.value.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  
  // 如果选择"其他原因"，必须填写详细说明
  if (refundForm.value.reason === '其他原因' && !refundForm.value.description?.trim()) {
    ElMessage.warning('请填写退款原因')
    return
  }
  
  if (!currentRefundOrder.value || !props.userInfo?.userId) {
    ElMessage.error('订单信息错误')
    return
  }
  
  // 组合退款原因：其他原因只传输入框内容，其他选项直接传选项文字
  let message
  if (refundForm.value.reason === '其他原因') {
    message = refundForm.value.description.trim()
  } else {
    message = refundForm.value.reason
  }
  
  refundSubmitting.value = true
  try {
    const response = await request(ORDER_API.REFUND_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: currentRefundOrder.value.orderId,
        userId: props.userInfo.userId,
        message: message
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('退款申请已提交')
      // 更新订单退款标记为退款中(refundFlag=1)
      currentRefundOrder.value.refundFlag = 1
      showRefundDialog.value = false
      refundForm.value = { reason: '', description: '', images: [] }
      currentRefundOrder.value = null
      // 刷新订单列表
      fetchOrders()
    } else {
      ElMessage.error(result.message || '退款申请失败')
    }
  } catch (error) {
    console.error('退款申请异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    refundSubmitting.value = false
  }
}

// 打开退款弹窗
const openRefundDialog = (order) => {
  currentRefundOrder.value = order
  refundForm.value = { reason: '', description: '', images: [] }
  showRefundDialog.value = true
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.currentPage = page
  saveFilterToStorage()
  fetchOrders()
}

// 每页条数变化
const handleSizeChange = (size) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  saveFilterToStorage()
  fetchOrders()
}

// 监听Tab变化 - 切换Tab时重新请求后端并保存
watch(activeTab, () => {
  pagination.value.currentPage = 1
  saveFilterToStorage()
  fetchOrders()
})

// 监听筛选条件变化 - 自动请求
watch(
  () => filterForm.value.dateRange,
  () => {
    pagination.value.currentPage = 1
    saveFilterToStorage()
    fetchOrders()
  }
)

watch(
  () => filterForm.value.latest,
  () => {
    pagination.value.currentPage = 1
    saveFilterToStorage()
    fetchOrders()
  }
)

// 监听用户信息变化
watch(() => props.userInfo, (newVal) => {
  if (newVal?.userId) {
    fetchOrders()
  }
}, { immediate: true })

onMounted(() => {
  if (props.userInfo?.userId) {
    fetchOrders()
  }
})
</script>

<template>
  <div class="my-orders">
    <h2 class="section-title">我的订单</h2>
    
    <!-- 头部：Tab + 筛选按钮 -->
    <div class="order-header-bar">
      <!-- Tab切换 -->
      <div class="tabs">
        <div 
          v-for="tab in tabs" 
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </div>
      </div>
      
      <!-- 筛选触发按钮 -->
      <div 
        class="filter-trigger" 
        :class="{ 'has-filter': hasActiveFilter() }"
        @click="showFilterPanel = !showFilterPanel"
      >
        <el-icon><Filter /></el-icon>
        <span>筛选</span>
        <el-icon class="arrow-icon">
          <ArrowDown v-if="!showFilterPanel" />
          <ArrowUp v-else />
        </el-icon>
      </div>
    </div>
    
    <!-- 筛选面板 -->
    <transition name="filter-slide">
      <div v-show="showFilterPanel" class="filter-panel">
        <div class="filter-content">
          <div class="filter-row">
            <div class="filter-item">
              <span class="filter-label">下单时间</span>
              <el-date-picker
                v-model="filterForm.dateRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                :shortcuts="dateShortcuts"
                :default-time="[new Date(0, 0, 0, 0, 0, 0), new Date(0, 0, 0, 23, 59, 59)]"
              />
            </div>
            <div class="filter-item">
              <span class="filter-label">排序方式</span>
              <el-select v-model="filterForm.latest" placeholder="默认排序" clearable style="width: 140px">
                <el-option label="最新优先" :value="1" />
                <el-option label="最早优先" :value="0" />
              </el-select>
            </div>
            <el-button class="reset-btn" @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </div>
    </transition>
    
    <!-- 订单列表 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>订单加载中...</span>
      </div>
    </div>
    
    <div v-else class="order-list">
          <div v-if="orders.length === 0" class="empty-state">
            <p>暂无订单</p>
          </div>
          
          <div v-for="order in orders" :key="order.orderId" class="order-card">
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info">
                <span class="order-id">订单号：{{ order.orderId }}</span>
                <span class="order-time">{{ order.createTime }}</span>
              </div>
              <div class="order-status-tags">
                <!-- 退款中时只显示退款标记，否则显示订单状态 -->
                <el-tag v-if="order.refundFlag === 1" type="danger" size="small" class="refund-tag">
                  退款中
                </el-tag>
                <el-tag v-else :type="statusMap[order.status]?.type || 'info'" size="small">
                  {{ statusMap[order.status]?.text || '未知状态' }}
                </el-tag>
              </div>
            </div>
            
            <!-- 商品列表 -->
            <div class="order-products">
              <div 
                v-for="product in order.products" 
                :key="product.id" 
                class="product-item clickable"
                @click="goToProductDetail(product.id, product.type)"
              >
                <img :src="product.image" :alt="product.name" class="product-image" />
                <div class="product-info">
                  <h4>{{ product.name }}</h4>
                  <p>¥{{ product.price?.toFixed(2) }} × {{ product.quantity }}</p>
                </div>
              </div>
            </div>
            
            <!-- 订单底部 -->
            <div class="order-footer">
              <div class="order-total">
                共 {{ order.products.reduce((sum, p) => sum + p.quantity, 0) }} 件商品，
                合计：<span class="price">¥{{ order.totalPrice?.toFixed(2) }}</span>
              </div>
              <div class="order-actions">
                <el-button size="small" @click="router.push(`/order/${order.orderId}`)">
                  <el-icon><View /></el-icon> 查看详情
                </el-button>
                <el-button size="small" type="info" @click="consultOrder(order)">
                  <el-icon><Headset /></el-icon> 咨询订单
                </el-button>
                <el-button v-if="order.status === 3 || order.status === 4" size="small" @click="viewLogistics(order)">
                  <el-icon><Van /></el-icon> 查看物流
                </el-button>
                <el-button v-if="order.status === 1" size="small" type="danger" @click="cancelOrder(order)">
                  取消订单
                </el-button>
                <el-button v-if="order.status === 1" size="small" type="primary" @click="goToPay(order)">
                  <el-icon><Money /></el-icon> 去支付
                </el-button>
                <!-- 签收按钮：只有状态为已发货(3)或待签收(4)且未在退款中时显示 -->
                <el-button v-if="(order.status === 3 || order.status === 4) && order.refundFlag !== 1" size="small" @click="confirmReceive(order)">
                  <el-icon><Check /></el-icon> 确认收货
                </el-button>
                <!-- 退款按钮：非待支付(1)、非已取消(6)的订单，且未在退款中(refundFlag!=1)时可申请退款 -->
                <el-button 
                  v-if="order.status !== 1 && order.status !== 6 && order.refundFlag !== 1" 
                  size="small" 
                  class="refund-btn"
                  @click="openRefundDialog(order)"
                >
                  申请退款
                </el-button>
                <!-- 取消退款按钮：只有退款中(refundFlag=1)的订单才显示 -->
                <el-button 
                  v-if="order.refundFlag === 1" 
                  size="small" 
                  type="warning"
                  @click="cancelRefund(order)"
                >
                  取消退款
                </el-button>
              </div>
            </div>
          </div>
        </div>
    
    <!-- 分页固定在底部 -->
    <div class="pagination-fixed">
      <el-pagination
        v-if="pagination.total > 0"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 30]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
    
    <!-- 物流弹窗 - 自定义模态框 -->
    <Teleport to="body">
      <Transition name="logistics-modal">
        <div v-if="showLogisticsDialog" class="logistics-modal-overlay">
          <div class="logistics-modal">
            <!-- 模态框头部 -->
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Van /></el-icon>
                物流追踪
              </h3>
              <button class="modal-close" @click="showLogisticsDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <!-- 模态框内容 -->
            <div class="modal-body">
              <LogisticsMap
                v-if="currentLogisticsOrder && showLogisticsDialog"
                :order-id="currentLogisticsOrder.orderId"
                :user-id="props.userInfo?.userId"
                :product-type="currentLogisticsOrder.products?.[0]?.type || 2"
                :order-status="currentLogisticsOrder.status"
                :visible="showLogisticsDialog"
                @arrived="handleLogisticsArrived"
              />
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
    
    <!-- 退款弹窗 - 自定义模态框 -->
    <Teleport to="body">
      <Transition name="refund-modal">
        <div v-if="showRefundDialog" class="refund-modal-overlay">
          <div class="refund-modal">
            <!-- 模态框头部 -->
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Money /></el-icon>
                申请退款
              </h3>
              <button class="modal-close" :disabled="refundSubmitting" @click="showRefundDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <!-- 模态框内容 -->
            <div class="modal-body">
              <!-- 退款原因 -->
              <div class="form-item">
                <label class="form-label">
                  <span class="required">*</span>退款原因
                </label>
                <div class="reason-options">
                  <div 
                    v-for="reason in refundReasons" 
                    :key="reason"
                    class="reason-option"
                    :class="{ active: refundForm.reason === reason }"
                    @click="refundForm.reason = reason"
                  >
                    {{ reason }}
                  </div>
                </div>
              </div>
              
              <!-- 详细说明 - 仅在选择"其他原因"时显示 -->
              <div class="form-item" v-if="refundForm.reason === '其他原因'">
                <label class="form-label">详细说明</label>
                <textarea 
                  v-model="refundForm.description"
                  class="form-textarea"
                  placeholder="请描述退款原因"
                  maxlength="200"
                  rows="4"
                ></textarea>
                <div class="textarea-count">{{ refundForm.description?.length || 0 }} / 200</div>
              </div>
            </div>
            
            <!-- 模态框底部 -->
            <div class="modal-footer">
              <button class="btn-cancel" :disabled="refundSubmitting" @click="showRefundDialog = false">取消</button>
              <button class="btn-confirm" :disabled="refundSubmitting || !refundForm.reason || (refundForm.reason === '其他原因' && !refundForm.description?.trim())" @click="submitRefund">
                <span v-if="refundSubmitting" class="loading-spinner"></span>
                {{ refundSubmitting ? '提交中...' : '提交申请' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.my-orders {
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 600px;
  
  .section-title {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-lg);
  }
}

// 头部栏：Tab + 筛选按钮
.order-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-lg);
}

// Tab 样式
.tabs {
  display: flex;
  gap: var(--spacing-xs);
}

.tab-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  
  &:hover {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.08);
  }
  
  &.active {
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.12);
    font-weight: var(--font-weight-medium);
  }
}

// 筛选触发按钮
.filter-trigger {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: 8px 16px;
  background: var(--color-bg-surface);
  border: 1px solid var(--color-border-base);
  border-radius: var(--radius-circle);
  cursor: pointer;
  transition: all var(--transition-base) var(--ease-out);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  position: relative;
  margin-bottom: 10px;
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
    background: var(--color-secondary-light);
  }
  
  // 有筛选条件时的高亮样式
  &.has-filter {
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
    border-color: var(--color-primary);
    color: #fff;
    box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
    
    &:hover {
      background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.4);
    }
  }
  
  .arrow-icon {
    font-size: 12px;
    transition: transform var(--transition-base);
  }
}

// 筛选面板
.filter-panel {
  background: linear-gradient(135deg, var(--color-secondary-light) 0%, #fff 100%);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
  overflow: hidden;
  
  .filter-content {
    padding: var(--spacing-lg) var(--spacing-xl);
  }
  
  .filter-row {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: var(--spacing-lg);
    
    .reset-btn {
      margin-left: auto;
      border-radius: var(--radius-circle);
      background: #fff;
      border-color: var(--color-border-base);
      color: var(--color-text-secondary);
      flex-shrink: 0;
      
      &:hover {
        border-color: var(--color-primary);
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
    }
  }
  
  .filter-item {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    flex-shrink: 0;
    
    .filter-label {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
      white-space: nowrap;
      min-width: 60px;
    }
  }
  
  // 日期选择器样式
  :deep(.el-date-editor) {
    border-radius: var(--radius-base);
    max-width: 360px;
    
    .el-range-input {
      font-size: var(--font-size-sm);
    }
  }
  
  // 下拉选择器样式
  :deep(.el-select) {
    .el-select__wrapper {
      border-radius: var(--radius-base);
      
      &:hover,
      &.is-focused {
        box-shadow: 0 0 0 1px var(--color-primary) inset;
      }
    }
  }
}

// 筛选面板动画
.filter-slide-enter-active,
.filter-slide-leave-active {
  transition: opacity var(--transition-base) var(--ease-out), 
              transform var(--transition-base) var(--ease-out);
}

.filter-slide-enter-from,
.filter-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.filter-slide-enter-to,
.filter-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}

// 加载状态
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  
  .loading-spinner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-base);
    color: var(--color-primary);
    
    .el-icon {
      font-size: 40px;
      animation: spin 1s linear infinite;
    }
    
    span {
      font-size: var(--font-size-sm);
      color: var(--color-text-secondary);
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  text-align: center;
  padding: var(--spacing-4xl);
  color: var(--color-text-tertiary);
}

.order-card {
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-base) var(--spacing-lg);
  background: #fafafa;
  border-bottom: 1px solid var(--color-border-light);
}

.order-status-tags {
  display: flex;
  gap: var(--spacing-xs);
  align-items: center;
  
  .refund-tag {
    animation: pulse 2s infinite;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.order-info {
  display: flex;
  gap: var(--spacing-xl);
  
  .order-id {
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
  }
  
  .order-time {
    color: var(--color-text-tertiary);
    font-size: var(--font-size-sm);
  }
}

.order-products {
  padding: var(--spacing-lg);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-base);
}

.product-item {
  display: flex;
  gap: var(--spacing-base);
  width: calc(50% - var(--spacing-sm));
  position: relative;
  z-index: 1;
  
  &.clickable {
    cursor: pointer;
    padding: var(--spacing-sm);
    border-radius: var(--radius-base);
    transition: all var(--transition-base) var(--ease-out);
    
    &:hover {
      background: var(--color-secondary-light, #fff8f5);
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      
      .product-image {
        transform: scale(1.02);
      }
      
      .product-info h4 {
        color: var(--color-primary);
      }
    }
  }
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  transition: transform var(--transition-base) var(--ease-out);
}

.product-info {
  h4 {
    font-size: var(--font-size-sm);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-xs);
    line-height: 1.4;
    transition: color var(--transition-base) var(--ease-out);
  }
  
  p {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-base) var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
  background: #fafafa;
}

.order-total {
  color: var(--color-text-secondary);
  
  .price {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-bold);
    color: var(--color-primary);
  }
}

.order-actions {
  display: flex;
  gap: var(--spacing-sm);

  // 统一按钮样式
  :deep(.el-button) {
    border-radius: var(--radius-circle);
    font-weight: var(--font-weight-medium);
    padding: 8px 16px;
    transition: all var(--transition-base) var(--ease-out);

    // 查看详情 - 默认按钮
    &:not(.el-button--primary):not(.el-button--success):not(.el-button--danger) {
      background: var(--color-bg-surface);
      border-color: var(--color-border-base);
      color: var(--color-text-secondary);

      &:hover {
        background: var(--color-secondary-light);
        border-color: var(--color-primary);
        color: var(--color-primary);
      }
    }

    // 去支付 - 主要按钮（橘色）
    &.el-button--primary {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border-color: var(--color-primary);
      color: #fff;
      box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);

      &:hover {
        background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(255, 138, 91, 0.4);
      }
    }

    // 取消订单 - 危险按钮（柔和红色）
    &.el-button--danger {
      background: #fff;
      border-color: #ffccc7;
      color: #ff7875;

      &:hover {
        background: #fff2f0;
        border-color: #ff7875;
        color: #ff4d4f;
      }
    }
  }
  
  // 退款按钮样式
  .refund-btn {
    background: #fff;
    border-color: #faad14;
    color: #faad14;

    &:hover {
      background: #fffbe6;
      border-color: #d48806;
      color: #d48806;
    }
  }
}

// 物流模态框动画
.logistics-modal-enter-active,
.logistics-modal-leave-active {
  transition: all 0.3s ease;
}
.logistics-modal-enter-from,
.logistics-modal-leave-to {
  opacity: 0;
  .logistics-modal { transform: scale(0.9) translateY(-20px); }
}

// 物流模态框样式
.logistics-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.logistics-modal {
  width: 800px;
  max-width: 95vw;
  max-height: 90vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #eee;
    flex-shrink: 0;
    
    .modal-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
      .el-icon { color: var(--color-primary); }
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
      &:hover { background: #eee; transform: rotate(90deg); }
      .el-icon { font-size: 16px; color: #666; }
    }
  }

  .modal-body {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }
}

// 退款模态框动画
.refund-modal-enter-active,
.refund-modal-leave-active {
  transition: all 0.3s ease;
}
.refund-modal-enter-from,
.refund-modal-leave-to {
  opacity: 0;
  .refund-modal { transform: scale(0.9) translateY(-20px); }
}

// 退款模态框样式
.refund-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.refund-modal {
  width: 500px;
  max-width: 100%;
  max-height: 90vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
      .el-icon { color: var(--color-primary); }
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
      &:hover { background: #eee; transform: rotate(90deg); }
      &:disabled { cursor: not-allowed; opacity: 0.6; }
      .el-icon { font-size: 16px; color: #666; }
    }
  }

  .modal-body {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }
  
  .order-info-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;
    font-size: 14px;
    
    .order-label { color: #909399; }
    .order-id { color: var(--color-primary); font-family: monospace; font-weight: 500; }
    .divider { color: #dcdfe6; }
    .product-name { color: #333; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  }
  
  .form-item {
    margin-bottom: 20px;
    &:last-child { margin-bottom: 0; }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    .required { color: #F56C6C; }
  }
  
  .reason-options {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .reason-option {
    padding: 8px 16px;
    background: #f5f7fa;
    border: 1px solid #e4e7ed;
    border-radius: 20px;
    font-size: 14px;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover {
      border-color: var(--color-primary);
      color: var(--color-primary);
      background: rgba(255, 138, 91, 0.08);
    }
    
    &.active {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border-color: var(--color-primary);
      color: #fff;
      box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
    }
  }
  
  .form-textarea {
    width: 100%;
    padding: 12px 14px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    color: #333;
    resize: none;
    outline: none;
    transition: all 0.2s ease;
    font-family: inherit;
    box-sizing: border-box;
    
    &::placeholder { color: #c0c4cc; }
    &:hover { border-color: #c0c4cc; }
    &:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1); }
  }
  
  .textarea-count {
    text-align: right;
    font-size: 12px;
    color: #909399;
    margin-top: 6px;
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    border-top: 1px solid #eee;
    
    .btn-cancel, .btn-confirm {
      flex: 1;
      height: 44px;
      font-size: 15px;
      border-radius: 22px;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.25s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
    }
    
    .btn-cancel {
      background: #fff;
      border: 1px solid #dcdfe6;
      color: #606266;
      &:hover:not(:disabled) { background: #f5f7fa; border-color: #c0c4cc; }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45); }
      &:disabled { opacity: 0.7; cursor: not-allowed; transform: none; box-shadow: none; }
      
      .loading-spinner {
        width: 16px;
        height: 16px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top-color: #fff;
        border-radius: 50%;
        animation: spin 0.8s linear infinite;
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

.pagination-wrapper {
  display: none; // 隐藏原来的分页器
}

.pagination-fixed {
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
  margin-top: var(--spacing-xl);
  background: var(--color-bg-surface);
  border-top: 1px solid var(--color-border-light);
  
  :deep(.el-pagination) {
    --el-pagination-hover-color: var(--color-primary);
    gap: var(--spacing-sm);
    
    // 页码按钮
    .el-pager li {
      border-radius: var(--radius-sm);
      min-width: 32px;
      height: 32px;
      line-height: 32px;
      font-weight: var(--font-weight-medium);
      
      &.is-active {
        background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
        color: white;
        box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
      }
      
      &:hover:not(.is-active) {
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
    }
    
    // 上一页/下一页按钮
    .btn-prev,
    .btn-next {
      border-radius: var(--radius-sm);
      min-width: 32px;
      height: 32px;
      
      &:hover:not(:disabled) {
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
      
      &:disabled {
        color: var(--color-text-placeholder);
      }
    }
    
    // 总数
    .el-pagination__total {
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
    }
    
    // 每页条数选择器
    .el-pagination__sizes {
      .el-select {
        width: 110px;
        
        .el-select__wrapper {
          border-radius: 8px !important;
          background: #FFFFFF !important;
          box-shadow: 0 0 0 1px var(--color-border-base) inset !important;
          padding: 4px 11px !important;
          height: 32px !important;
          transition: all 0.2s ease !important;
          
          &:hover,
          &.is-focused,
          &.is-hovering {
            box-shadow: 0 0 0 1px var(--color-primary) inset !important;
            
            .el-select__suffix,
            .el-select__caret {
              color: var(--color-primary) !important;
            }
          }
        }
      }
    }
    
    // 跳转输入框
    .el-pagination__jump {
      color: var(--color-text-secondary);
      font-size: var(--font-size-sm);
      
      .el-input {
        width: 50px;
        
        .el-input__wrapper {
          border-radius: var(--radius-sm);
          box-shadow: 0 0 0 1px var(--color-border-base) inset;
          height: 32px;
          
          &:hover,
          &.is-focus {
            box-shadow: 0 0 0 1px var(--color-primary) inset;
          }
        }
      }
    }
  }
}
</style>
