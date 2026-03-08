<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Warning, Van, ArrowLeft, Check, Headset } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_API, request } from '@/api/config.js'
import { getPageSource, clearPageSource, markAsBackNavigation } from '@/router'
import LogisticsMap from '@/components/LogisticsMap.vue'


const route = useRoute()
const router = useRouter()

const props = defineProps({
  isLoggedIn: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => null }
})

const emit = defineEmits(['openLogin'])

// 注入设置聊天订单的方法
const setChatOrder = inject('setChatOrder', null)

// 数据状态
const loading = ref(true)
const orderDetail = ref(null)

// 订单状态映射（与后端Constants对应: 1-待支付, 2-待发货, 3-已发货, 4-待签收, 5-已收货, 6-订单取消）
const statusMap = {
  1: { text: '待支付', type: 'warning', color: '#E6A23C' },
  2: { text: '待发货', type: 'info', color: '#909399' },
  3: { text: '已发货', type: 'primary', color: '#409EFF' },
  4: { text: '待签收', type: 'primary', color: '#409EFF' },
  5: { text: '已收货', type: 'success', color: '#67C23A' },
  6: { text: '已取消', type: 'info', color: '#909399' },
}

// 操作loading
const paying = ref(false)
const cancelling = ref(false)
const confirming = ref(false)
const refunding = ref(false)
const cancellingRefund = ref(false)

// 退款弹窗
const showRefundDialog = ref(false)
const refundForm = ref({ reason: '', description: '' })
const refundReasons = ['不想要了', '商品与描述不符', '质量问题', '发货太慢', '其他原因']

// 物流弹窗
const showLogisticsDialog = ref(false)

// 商品总额
const totalAmount = computed(() => {
  if (!orderDetail.value) return 0
  return parseFloat(orderDetail.value.totalPrice) || 0
})

// 单价
const unitPrice = computed(() => {
  if (!orderDetail.value || !orderDetail.value.totalCount) return 0
  return totalAmount.value / orderDetail.value.totalCount
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  if (typeof dateTime === 'string') return dateTime
  if (Array.isArray(dateTime)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = dateTime
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}`
  }
  return dateTime
}


// 获取订单详情
const fetchOrderDetail = async (orderId) => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    emit('openLogin')
    return
  }
  
  loading.value = true
  try {
    const response = await request(
      `${ORDER_API.GET_ORDER_DETAIL}?orderId=${orderId}&userId=${props.userInfo.userId}`
    )
    const result = await response.json()
    console.log('订单详情响应:', result)
    
    if (result.code === 0 && result.data) {
      orderDetail.value = result.data

      // 设置当前订单给聊天组件使用
      if (setChatOrder) {
        const orderData = {
          orderId: result.data.orderId,
          productName: result.data.productName,
          totalAmount: parseFloat(result.data.totalPrice) || 0,
          status: result.data.status,
          createTime: formatDateTime(result.data.createTime),
          imgUrl: result.data.imgUrl,
          totalCount: result.data.totalCount
        }
        setChatOrder(orderData)
      }
    } else {
      ElMessage.error(result.message || '获取订单详情失败')
      router.push('/profile/orders')
    }
  } catch (error) {
    console.error('获取订单详情异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
    router.push('/profile/orders')
  } finally {
    loading.value = false
  }
}

// 去支付
const handlePay = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    emit('openLogin')
    return
  }
  
  paying.value = true
  try {
    // 调用真实支付接口
    const response = await request(ORDER_API.PAY_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderDetail.value.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data?.successFlag) {
      ElMessage.success('支付成功')
      // 更新订单状态为待发货(2)
      orderDetail.value.status = 2
    } else {
      ElMessage.error(result.message || '支付失败，请重试')
    }
  } catch (error) {
    console.error('支付失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    paying.value = false
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    cancelling.value = true
    const response = await request(ORDER_API.CANCEL_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderDetail.value.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      ElMessage.success('订单已取消')
      orderDetail.value.status = 6
    } else {
      ElMessage.error(result.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单异常:', error)
      ElMessage.error('网络错误')
    }
  } finally {
    cancelling.value = false
  }
}

// 确认收货（签收）
const handleConfirmReceive = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    confirming.value = true
    const response = await request(ORDER_API.RECEIVE_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderDetail.value.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('确认收货成功')
      orderDetail.value.status = 5
    } else {
      ElMessage.error(result.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    confirming.value = false
  }
}

// 打开退款弹窗
const openRefundDialog = () => {
  refundForm.value = { reason: '', description: '' }
  showRefundDialog.value = true
}

// 咨询订单
const handleConsultOrder = () => {
  if (!setChatOrder) {
    ElMessage.warning('客服功能暂未就绪，请稍后重试')
    return
  }

  // 设置当前订单信息
  const orderData = {
    orderId: orderDetail.value.orderId,
    productName: orderDetail.value.productName,
    totalAmount: parseFloat(orderDetail.value.totalPrice) || 0,
    status: orderDetail.value.status,
    createTime: formatDateTime(orderDetail.value.createTime),
    imgUrl: orderDetail.value.imgUrl,
    totalCount: orderDetail.value.totalCount
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
  
  if (refundForm.value.reason === '其他原因' && !refundForm.value.description?.trim()) {
    ElMessage.warning('请填写退款原因')
    return
  }
  
  let message = refundForm.value.reason === '其他原因' 
    ? refundForm.value.description.trim() 
    : refundForm.value.reason
  
  refunding.value = true
  try {
    const response = await request(ORDER_API.REFUND_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderDetail.value.orderId,
        userId: props.userInfo.userId,
        message: message
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('退款申请已提交')
      orderDetail.value.refundFlag = 1
      showRefundDialog.value = false
    } else {
      ElMessage.error(result.message || '退款申请失败')
    }
  } catch (error) {
    console.error('退款申请异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    refunding.value = false
  }
}

// 取消退款
const handleCancelRefund = async () => {
  try {
    await ElMessageBox.confirm('确定要取消退款申请吗？', '取消退款', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    
    cancellingRefund.value = true
    const response = await request(ORDER_API.CANCEL_REFUND, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderDetail.value.orderId,
        userId: props.userInfo.userId
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('已取消退款申请')
      orderDetail.value.refundFlag = 0
    } else {
      ElMessage.error(result.message || '取消退款失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消退款异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    cancellingRefund.value = false
  }
}

// 物流到达处理
const handleLogisticsArrived = () => {
  if (orderDetail.value) {
    orderDetail.value.status = 4 // 更新为待签收
  }
}

// 返回上一页（使用统一的来源跟踪系统）
const goBack = () => {
  const sourcePath = getPageSource('OrderDetail')
  if (sourcePath) {
    clearPageSource('OrderDetail')
    markAsBackNavigation() // 标记为返回操作，防止目标页面更新来源记录
    router.push(sourcePath)
  } else {
    router.push('/profile/orders')
  }
}

// 初始化
onMounted(() => {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.warning('订单不存在')
    router.push('/profile/orders')
    return
  }
  fetchOrderDetail(orderId)
})
</script>

<template>
  <div class="order-detail-page">
    <div class="container-base">
      <!-- 返回按钮 -->
      <div class="back-nav" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
      
      <h1 class="page-title">订单详情</h1>
      
      <el-skeleton :loading="loading" animated :rows="8">
        <template #default>
          <template v-if="orderDetail">
            <!-- 订单状态 -->
            <div class="order-status-banner" :class="orderDetail.refundFlag === 1 ? 'status-refunding' : `status-${orderDetail.status}`">
              <el-icon><Warning /></el-icon>
              <!-- 退款中时只显示退款标记，否则显示订单状态 -->
              <span v-if="orderDetail.refundFlag === 1" class="status-text">退款中</span>
              <span v-else class="status-text">{{ statusMap[orderDetail.status]?.text || '未知状态' }}</span>
              <span class="order-id">订单号：{{ orderDetail.orderId }}</span>
              <span class="order-time">下单时间：{{ formatDateTime(orderDetail.createTime) }}</span>
            </div>
            
            <!-- 收货地址 -->
            <div class="section">
              <div class="section-header">
                <h2><el-icon><Location /></el-icon> 收货信息</h2>
              </div>
              <div class="address-card">
                <div class="address-info">
                  <span class="name">{{ orderDetail.receiptName || '收货人' }}</span>
                  <span class="phone">{{ orderDetail.phone || '' }}</span>
                </div>
                <div class="address-detail">{{ orderDetail.receiptAddress || '暂无地址信息' }}</div>
              </div>
            </div>
            
            <!-- 商品信息 -->
            <div class="section">
              <div class="section-header">
                <h2>商品信息</h2>
              </div>
              <div class="goods-item">
                <img :src="orderDetail.imgUrl || 'https://via.placeholder.com/100x100'" :alt="orderDetail.productName" class="goods-image" />
                <div class="goods-info">
                  <h3 class="goods-name">{{ orderDetail.productName }}</h3>
                  <div class="goods-meta">
                    <span class="price">¥{{ unitPrice.toFixed(2) }}</span>
                    <span class="quantity">x{{ orderDetail.totalCount }}</span>
                  </div>
                </div>
                <div class="goods-subtotal">¥{{ totalAmount.toFixed(2) }}</div>
              </div>
            </div>
            
            <!-- 金额明细 -->
            <div class="section">
              <div class="section-header">
                <h2>金额明细</h2>
              </div>
              <div class="amount-list">
                <div class="amount-row">
                  <span>商品总额</span>
                  <span>¥{{ totalAmount.toFixed(2) }}</span>
                </div>
                <div class="amount-row">
                  <span>运费</span>
                  <span class="free">免运费</span>
                </div>
                <div class="amount-row total">
                  <span>实付金额</span>
                  <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
                </div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div v-if="orderDetail.status === 1 || orderDetail.status === 3 || orderDetail.status === 4 || orderDetail.refundFlag === 1 || (orderDetail.status !== 6 && orderDetail.refundFlag !== 1)" class="action-section">
              <!-- 咨询客服按钮（所有状态都显示） -->
              <el-button size="large" @click="handleConsultOrder">
                <el-icon><Headset /></el-icon> 咨询客服
              </el-button>
              <!-- 待支付 -->
              <template v-if="orderDetail.status === 1">
                <el-button size="large" :loading="cancelling" @click="handleCancel">取消订单</el-button>
                <el-button type="primary" size="large" :loading="paying" @click="handlePay">立即支付</el-button>
              </template>
              <!-- 待收货（已发货或待签收）且未退款中 -->
              <template v-else-if="(orderDetail.status === 3 || orderDetail.status === 4) && orderDetail.refundFlag !== 1">
                <el-button size="large" @click="showLogisticsDialog = true">
                  <el-icon><Van /></el-icon> 查看物流
                </el-button>
                <el-button type="success" size="large" :loading="confirming" @click="handleConfirmReceive">
                  <el-icon><Check /></el-icon> 确认收货
                </el-button>
                <el-button size="large" class="refund-btn" @click="openRefundDialog">申请退款</el-button>
              </template>
              <!-- 其他非待支付、非已取消状态且未退款中：可申请退款 -->
              <template v-else-if="orderDetail.status !== 1 && orderDetail.status !== 6 && orderDetail.refundFlag !== 1">
                <el-button size="large" class="refund-btn" @click="openRefundDialog">申请退款</el-button>
              </template>
              <!-- 退款中：可取消退款 -->
              <template v-if="orderDetail.refundFlag === 1">
                <el-button type="warning" size="large" :loading="cancellingRefund" @click="handleCancelRefund">取消退款</el-button>
              </template>
            </div>
          </template>
        </template>
      </el-skeleton>
    </div>
    
    <!-- 退款弹窗 -->
    <el-dialog v-model="showRefundDialog" title="申请退款" width="500px" :close-on-click-modal="false">
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="退款原因" required>
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
            <el-option v-for="reason in refundReasons" :key="reason" :label="reason" :value="reason" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="refundForm.reason === '其他原因'" label="详细说明" required>
          <el-input
            v-model="refundForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述退款原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" :loading="refunding" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>
    
    <!-- 物流弹窗 -->
    <el-dialog 
      v-model="showLogisticsDialog" 
      title="物流追踪" 
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <LogisticsMap
        v-if="orderDetail && showLogisticsDialog"
        :order-id="orderDetail.orderId"
        :user-id="props.userInfo?.userId"
        :product-type="orderDetail.productType || 2"
        :order-status="orderDetail.status"
        :visible="showLogisticsDialog"
        @arrived="handleLogisticsArrived"
      />
    </el-dialog>
  </div>
</template>


<style scoped lang="scss">
.order-detail-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-base);
  padding: var(--spacing-2xl) 0 var(--spacing-4xl);
}

.back-nav {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-text-secondary);
  cursor: pointer;
  margin-bottom: var(--spacing-lg);
  transition: color var(--transition-fast);
  
  &:hover {
    color: var(--color-primary);
  }
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xl);
}

.order-status-banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
  
  &.status-1 { background: linear-gradient(135deg, #fdf6ec 0%, #fef0e0 100%); color: #E6A23C; }
  &.status-2 { background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%); color: #909399; }
  &.status-3, &.status-4 { background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%); color: #409EFF; }
  &.status-5 { background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%); color: #67C23A; }
  &.status-6 { background: linear-gradient(135deg, #f4f4f5 0%, #e9e9eb 100%); color: #909399; }
  &.status-refunding { background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%); color: #F56C6C; }
  
  .status-text {
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
  }
  
  .order-id, .order-time {
    font-size: var(--font-size-sm);
    opacity: 0.8;
  }
}

.section {
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.section-header {
  margin-bottom: var(--spacing-lg);
  
  h2 {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }
}

.address-card {
  padding: var(--spacing-lg);
  background: #fafafa;
  border-radius: var(--radius-base);
}

.address-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
  
  .name {
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
  }
  
  .phone {
    color: var(--color-text-secondary);
  }
}

.address-detail {
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-base);
  background: #fafafa;
  border-radius: var(--radius-base);
}

.goods-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  margin-right: var(--spacing-lg);
}

.goods-info {
  flex: 1;
}

.goods-name {
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  line-height: 1.4;
}

.goods-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  color: var(--color-text-secondary);
}

.goods-subtotal {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
}

.amount-list {
  .amount-row {
    display: flex;
    justify-content: space-between;
    padding: var(--spacing-sm) 0;
    color: var(--color-text-secondary);
    
    &.total {
      padding-top: var(--spacing-base);
      margin-top: var(--spacing-base);
      border-top: 1px solid var(--color-border-light);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
    }
  }
  
  .free {
    color: var(--color-accent);
  }
  
  .total-price {
    font-size: var(--font-size-xl);
    color: var(--color-accent);
  }
}

.action-section {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-base);
  padding: var(--spacing-xl);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  
  .refund-btn {
    color: #F56C6C;
    border-color: #F56C6C;
    
    &:hover {
      background: #fef0f0;
      color: #F56C6C;
      border-color: #F56C6C;
    }
  }
}
</style>
