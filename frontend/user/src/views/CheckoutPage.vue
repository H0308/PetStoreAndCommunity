<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Location, Warning, Edit, Close, Search, Loading, ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ORDER_API, request } from '@/api/config.js'
import { markAsBackNavigation } from '@/router'


const route = useRoute()
const router = useRouter()

const props = defineProps({
  isLoggedIn: { type: Boolean, default: false },
  userInfo: { type: Object, default: () => null }
})

const emit = defineEmits(['openLogin'])

// 数据状态
const loading = ref(true)
const submitting = ref(false)
const preOrderDetails = ref([]) // 预创建订单详情列表（支持多商品）
const isConfirmed = ref(false) // 是否已确认订单（区分预创建和已创建）
const confirmedOrders = ref([]) // 已确认的订单详情列表（支持多商品）
const isMultiProduct = ref(false) // 是否为多商品结算（购物车结算）

// 编辑状态
const showQuantityDialog = ref(false)
const quantityModalMouseDown = ref(false)
const showContactDialog = ref(false)
const newQuantity = ref('1')
const quantitySaving = ref(false)
const quantityError = ref('')
const editingProductIndex = ref(-1) // 当前正在编辑的商品索引

// 百度地图相关
const mapContainer = ref(null)
let baiduMap = null
let mapMarker = null
let localSearch = null
let geoCoder = null
const mapSearching = ref(false)
const mapLoading = ref(false) // 地图操作加载状态（防止API并发超限）
let lastApiCallTime = 0 // 上次API调用时间

// 地址搜索结果列表
const addressSuggestions = ref([])
const showSuggestions = ref(false)

// 获取当前位置状态
const gettingLocation = ref(false)

// 表单校验错误信息
const phoneError = ref('')
const savingUserInfo = ref(false) // 保存用户信息提交中状态

// 编辑表单数据
const editForm = ref({
  receiptId: null,
  receiptName: '',
  receiptAddress: '',
  phone: '',
  totalCount: 1
})

// 订单状态映射（与后端Constants对应）
// ORDER_TO_PAY = 1 待支付
// ORDER_TO_DELIVER = 2 已支付/待发货
// ORDER_DELIVERED = 3 已发货
// ORDER_TO_SIGN = 4 待签收
// ORDER_SIGNED = 5 已收货
// ORDER_CANCELLED = 6 订单取消
const statusMap = {
  1: { text: '待支付', type: 'warning', color: '#E6A23C' },
  2: { text: '已支付/待发货', type: 'info', color: '#909399' },
  3: { text: '已发货', type: 'primary', color: '#409EFF' },
  4: { text: '待签收', type: 'primary', color: '#409EFF' },
  5: { text: '已收货', type: 'success', color: '#67C23A' },
  6: { text: '已取消', type: 'info', color: '#909399' },
}

// 待支付状态值
const ORDER_TO_PAY = 1
// 订单取消状态值
const ORDER_CANCELLED = 6

// 取消订单loading
const cancelling = ref(false)

// 信息缺失提示弹窗
const showInfoTipDialog = ref(false)
const infoTipType = ref('') // 'phone' 或 'address'
const infoTipMessage = ref('')

// 当前显示的订单数据列表
const currentOrders = computed(() => {
  return isConfirmed.value ? confirmedOrders.value : preOrderDetails.value
})

// 兼容单商品场景的当前订单（取第一个）
const currentOrder = computed(() => {
  const orders = currentOrders.value
  return orders && orders.length > 0 ? orders[0] : null
})

// 商品总额（支持多商品）
const totalAmount = computed(() => {
  if (!currentOrders.value || currentOrders.value.length === 0) return 0
  return currentOrders.value.reduce((sum, order) => {
    return sum + (parseFloat(order.totalPrice) || 0)
  }, 0)
})

// 商品总件数
const totalItemCount = computed(() => {
  if (!currentOrders.value || currentOrders.value.length === 0) return 0
  return currentOrders.value.reduce((sum, order) => {
    return sum + (parseInt(order.totalCount) || 0)
  }, 0)
})

// 获取单个商品的单价
const getUnitPrice = (order) => {
  if (!order || !order.totalCount) return 0
  return parseFloat(order.totalPrice) / order.totalCount
}

// 单价（兼容单商品场景）
const unitPrice = computed(() => {
  if (!currentOrder.value || !currentOrder.value.totalCount) return 0
  return totalAmount.value / totalItemCount.value
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

// 预创建单个商品订单
const preCreateOrder = async (productId, totalCount) => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    emit('openLogin')
    return null
  }
  
  try {
    const response = await request(ORDER_API.PRE_CREATE_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        productId: productId,
        totalCount: totalCount
      })
    })
    const result = await response.json()
    console.log('预创建订单响应:', result)
    
    if (result.code === 0 && result.data) {
      return result.data
    } else {
      // 检查是否是实名认证错误
      if (result.message && result.message.includes('实名认证')) {
        ElMessageBox.confirm(
          '您尚未完成实名认证，无法创建订单。是否前往个人资料页面进行实名认证？',
          '需要实名认证',
          {
            confirmButtonText: '去认证',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          router.push('/profile/profile')
        }).catch(() => {})
        return { _authError: true } // 返回特殊标记，不跳转
      }
      // 检查是否是手机号缺失错误
      if (result.message && result.message.includes('手机号')) {
        infoTipType.value = 'phone'
        infoTipMessage.value = '您尚未绑定手机号，无法下单。'
        showInfoTipDialog.value = true
        return { _infoError: true }
      }
      // 检查是否是收货地址缺失错误
      if (result.message && result.message.includes('收货地址')) {
        infoTipType.value = 'address'
        infoTipMessage.value = '您尚未添加收货地址，无法下单。'
        showInfoTipDialog.value = true
        return { _infoError: true }
      }
      ElMessage.error(result.message || '预创建订单失败')
      return null
    }
  } catch (error) {
    console.error('预创建订单异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
    return null
  }
}

// 批量预创建订单（多商品）
const batchPreCreateOrders = async (items) => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    emit('openLogin')
    return
  }
  
  loading.value = true
  try {
    // 并行调用多个预创建订单接口
    const promises = items.map(item => 
      preCreateOrder(item.productId, item.quantity || item.totalCount || 1)
    )
    const results = await Promise.all(promises)
    
    // 检查是否有实名认证错误
    const hasAuthError = results.some(order => order && order._authError)
    if (hasAuthError) {
      // 实名认证错误时不跳转，保持在当前页面
      return
    }
    
    // 检查是否有信息缺失错误（手机号或收货地址）
    const hasInfoError = results.some(order => order && order._infoError)
    if (hasInfoError) {
      return
    }
    
    // 过滤出成功的预订单
    const validOrders = results.filter(order => order !== null && !order._authError && !order._infoError)
    
    if (validOrders.length === 0) {
      ElMessage.error('所有商品预创建订单失败')
      router.push('/cart')
      return
    }
    
    if (validOrders.length < items.length) {
      ElMessage.warning(`部分商品预创建失败，已过滤 ${items.length - validOrders.length} 件商品`)
    }
    
    preOrderDetails.value = validOrders
    isMultiProduct.value = true
    
    // 初始化编辑表单（使用第一个订单的收货信息）
    if (validOrders.length > 0) {
      const firstOrder = validOrders[0]
      editForm.value = {
        receiptId: firstOrder.receiptId,
        receiptName: firstOrder.receiptName || '',
        receiptAddress: firstOrder.receiptAddress || '',
        phone: firstOrder.phone || '',
        totalCount: totalItemCount.value
      }
    }
    isConfirmed.value = false
  } catch (error) {
    console.error('批量预创建订单异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
    router.push('/cart')
  } finally {
    loading.value = false
  }
}

// 单个商品预创建订单（兼容原有逻辑）
const singlePreCreateOrder = async (productId, totalCount) => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    emit('openLogin')
    return
  }
  
  loading.value = true
  try {
    const orderData = await preCreateOrder(productId, totalCount)
    
    if (orderData && !orderData._authError && !orderData._infoError) {
      preOrderDetails.value = [orderData]
      isMultiProduct.value = false
      // 初始化编辑表单
      editForm.value = {
        receiptId: orderData.receiptId,
        receiptName: orderData.receiptName || '',
        receiptAddress: orderData.receiptAddress || '',
        phone: orderData.phone || '',
        totalCount: orderData.totalCount || 1
      }
      isConfirmed.value = false
    } else if (!orderData) {
      // 只有非实名认证错误才跳转
      router.push('/products')
    }
    // 实名认证错误或信息缺失错误时不跳转，保持在当前页面
  } catch (error) {
    console.error('预创建订单异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
    router.push('/products')
  } finally {
    loading.value = false
  }
}

// 打开修改联系方式弹窗
const openContactDialog = () => {
  if (isConfirmed.value) return
  const firstOrder = currentOrders.value[0]
  if (!firstOrder) return
  editForm.value.phone = firstOrder.phone || ''
  editForm.value.receiptName = firstOrder.receiptName || ''
  editForm.value.receiptAddress = firstOrder.receiptAddress || ''
  phoneError.value = '' // 清除错误提示
  showContactDialog.value = true
  // 弹窗打开后初始化地图
  nextTick(() => {
    setTimeout(() => initBaiduMap(), 100)
  })
}

// 初始化百度地图
const initBaiduMap = () => {
  if (!mapContainer.value || !window.BMap) {
    console.warn('百度地图API未加载')
    return
  }
  
  // 创建地图实例，默认中心点为北京
  baiduMap = new window.BMap.Map(mapContainer.value)
  const defaultPoint = new window.BMap.Point(116.404, 39.915)
  baiduMap.centerAndZoom(defaultPoint, 15)
  
  // 启用滚轮缩放
  baiduMap.enableScrollWheelZoom(true)
  
  // 添加地图控件
  baiduMap.addControl(new window.BMap.NavigationControl())
  baiduMap.addControl(new window.BMap.ScaleControl())
  
  // 创建标记点
  mapMarker = new window.BMap.Marker(defaultPoint)
  baiduMap.addOverlay(mapMarker)
  mapMarker.enableDragging() // 允许拖拽标记
  
  // 创建地址解析器
  geoCoder = new window.BMap.Geocoder()
  
  // 创建本地搜索对象
  localSearch = new window.BMap.LocalSearch(baiduMap, {
    onSearchComplete: (results) => {
      // 延迟1秒后关闭加载状态
      setTimeout(() => {
        mapSearching.value = false
        mapLoading.value = false
      }, 1000)
      
      if (localSearch.getStatus() === window.BMAP_STATUS_SUCCESS && results.getCurrentNumPois() > 0) {
        // 获取多个搜索结果（最多5个）
        const numPois = Math.min(results.getCurrentNumPois(), 5)
        const suggestions = []
        for (let i = 0; i < numPois; i++) {
          const poi = results.getPoi(i)
          if (poi) {
            suggestions.push({
              title: poi.title || '',
              address: poi.address || '',
              point: poi.point,
              fullAddress: poi.address ? (poi.address + (poi.title ? ' ' + poi.title : '')) : poi.title
            })
          }
        }
        
        if (suggestions.length > 0) {
          addressSuggestions.value = suggestions
          showSuggestions.value = true
          
          // 如果只有一个结果，直接选中
          if (suggestions.length === 1) {
            selectAddressSuggestion(suggestions[0])
          }
        }
      } else {
        addressSuggestions.value = []
        showSuggestions.value = false
      }
    }
  })
  
  // 点击地图事件 - 获取点击位置的地址
  baiduMap.addEventListener('click', (e) => {
    const point = e.point
    mapMarker.setPosition(point)
    
    // 显示加载状态，延迟1秒调用API
    mapLoading.value = true
    setTimeout(() => {
      geoCoder.getLocation(point, (result) => {
        mapLoading.value = false
        if (result) {
          editForm.value.receiptAddress = result.address || ''
        }
      })
    }, 1000)
  })
  
  // 标记拖拽结束事件
  mapMarker.addEventListener('dragend', (e) => {
    const point = e.point
    
    // 显示加载状态，延迟1秒调用API
    mapLoading.value = true
    setTimeout(() => {
      geoCoder.getLocation(point, (result) => {
        mapLoading.value = false
        if (result) {
          editForm.value.receiptAddress = result.address || ''
        }
      })
    }, 1000)
  })
  
  // 如果已有地址，搜索并定位
  if (editForm.value.receiptAddress) {
    searchAddressOnMap(editForm.value.receiptAddress)
  }
}

// 在地图上搜索地址
const searchAddressOnMap = (address) => {
  if (!localSearch || !address) return
  mapSearching.value = true
  mapLoading.value = true
  // 延迟1秒后调用API，防止并发超限
  setTimeout(() => {
    localSearch.search(address)
  }, 1000)
}

// 选择地址建议
const selectAddressSuggestion = (suggestion) => {
  if (!suggestion || !baiduMap) return
  
  // 更新地图位置
  if (suggestion.point) {
    baiduMap.centerAndZoom(suggestion.point, 16)
    mapMarker.setPosition(suggestion.point)
  }
  
  // 更新地址输入框
  editForm.value.receiptAddress = suggestion.fullAddress || suggestion.address || suggestion.title
  
  // 关闭下拉列表
  showSuggestions.value = false
  addressSuggestions.value = []
}

// 关闭地址建议列表（延迟关闭，用于输入框失焦）
const closeSuggestions = () => {
  // 延迟关闭，确保点击事件能触发
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

// 立即关闭地址建议列表（用于关闭按钮）
const closeSuggestionsNow = () => {
  showSuggestions.value = false
  addressSuggestions.value = []
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持定位功能')
    return
  }
  
  if (!window.BMap || !geoCoder) {
    ElMessage.warning('地图服务未就绪，请稍后再试')
    return
  }
  
  gettingLocation.value = true
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude } = position.coords
      
      // 将 WGS84 坐标转换为百度坐标
      const wgs84Point = new window.BMap.Point(longitude, latitude)
      const convertor = new window.BMap.Convertor()
      
      convertor.translate([wgs84Point], 1, 5, (data) => {
        if (data.status === 0 && data.points && data.points.length > 0) {
          const bdPoint = data.points[0]
          
          // 更新地图位置和标记
          if (baiduMap && mapMarker) {
            baiduMap.centerAndZoom(bdPoint, 16)
            mapMarker.setPosition(bdPoint)
          }
          
          // 逆地理编码获取地址
          geoCoder.getLocation(bdPoint, (result) => {
            gettingLocation.value = false
            if (result && result.address) {
              editForm.value.receiptAddress = result.address
              ElMessage.success('已获取当前位置')
            } else {
              ElMessage.warning('无法解析当前位置地址')
            }
          })
        } else {
          // 坐标转换失败，直接使用原坐标尝试
          const point = new window.BMap.Point(longitude, latitude)
          if (baiduMap && mapMarker) {
            baiduMap.centerAndZoom(point, 16)
            mapMarker.setPosition(point)
          }
          
          geoCoder.getLocation(point, (result) => {
            gettingLocation.value = false
            if (result && result.address) {
              editForm.value.receiptAddress = result.address
              ElMessage.success('已获取当前位置')
            } else {
              ElMessage.warning('无法解析当前位置地址')
            }
          })
        }
      })
    },
    (error) => {
      gettingLocation.value = false
      let errorMsg = '获取位置失败'
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '您拒绝了位置访问请求，请在浏览器设置中允许访问位置'
          break
        case error.POSITION_UNAVAILABLE:
          errorMsg = '无法获取位置信息'
          break
        case error.TIMEOUT:
          errorMsg = '获取位置超时，请重试'
          break
      }
      ElMessage.warning(errorMsg)
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

// 手机号校验正则（中国大陡11位手机号）
const phoneRegex = /^1[3-9]\d{9}$/

// 校验手机号格式
const validatePhone = (phone) => {
  if (!phone) return { valid: false, message: '请输入手机号' }
  if (!/^\d+$/.test(phone)) return { valid: false, message: '手机号只能包含数字' }
  if (phone.length !== 11) return { valid: false, message: '手机号必须为11位' }
  if (!phoneRegex.test(phone)) return { valid: false, message: '请输入正确的手机号格式' }
  return { valid: true, message: '' }
}

// 手机号输入框失焦校验
const handlePhoneBlur = () => {
  const result = validatePhone(editForm.value.phone)
  phoneError.value = result.message
}

// 手机号输入时清除错误提示
const handlePhoneInput = () => {
  if (phoneError.value) {
    phoneError.value = ''
  }
}

// 地址输入框搜索按钮点击
const handleSearchAddress = () => {
  if (editForm.value.receiptAddress) {
    searchAddressOnMap(editForm.value.receiptAddress)
  } else {
    ElMessage.warning('请输入地址进行搜索')
  }
}

// 同步用户信息到个人资料
const syncUserInfoToProfile = async (receiptId, phone, receiptName, isPhoneChanged, isNameChanged) => {
  try {
    const response = await request(ORDER_API.SYNC_USER_INFO, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        // 只有手机号变化时才传递，否则传null
        phone: isPhoneChanged ? phone : null,
        receiptId: receiptId,
        // 只有收货人名称变化时才传递，否则传null
        receiptName: isNameChanged ? receiptName : null
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('已同步到个人资料')
    } else {
      ElMessage.error(result.message || '同步失败')
    }
  } catch (error) {
    console.error('同步用户信息异常:', error)
    ElMessage.error('网络错误，同步失败')
  }
}

// 修改用户信息（地址、电话）- 整体提交
const handleSaveUserInfo = async () => {
  if (!preOrderDetails.value || preOrderDetails.value.length === 0) return
  const firstOrder = preOrderDetails.value[0]
  
  // 防止重复提交
  if (savingUserInfo.value) return
  
  // 去除空白字符
  const trimmedPhone = editForm.value.phone?.trim() || ''
  const trimmedName = editForm.value.receiptName?.trim() || ''
  const trimmedAddress = editForm.value.receiptAddress?.trim() || ''
  
  // 空白字符校验
  if (editForm.value.phone && !trimmedPhone) {
    ElMessage.warning('手机号不能只包含空白字符')
    return
  }
  if (editForm.value.receiptName && !trimmedName) {
    ElMessage.warning('收货人名称不能只包含空白字符')
    return
  }
  if (editForm.value.receiptAddress && !trimmedAddress) {
    ElMessage.warning('收货地址不能只包含空白字符')
    return
  }
  
  // 手机号校验
  const phoneValidation = validatePhone(trimmedPhone)
  if (!phoneValidation.valid) {
    ElMessage.warning(phoneValidation.message)
    phoneError.value = phoneValidation.message
    return
  }
  
  if (!trimmedAddress) {
    ElMessage.warning('请输入收货地址')
    return
  }
  
  // 判断地址、电话和收货人名称是否发生变化
  const isAddressChanged = trimmedAddress !== (firstOrder.receiptAddress?.trim() || '')
  const isPhoneChanged = trimmedPhone !== (firstOrder.phone?.trim() || '')
  const isNameChanged = trimmedName !== (firstOrder.receiptName?.trim() || '')
  
  // 设置提交中状态
  savingUserInfo.value = true
  
  try {
    // 延迟1秒防止快速重复点击
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const response = await request(ORDER_API.CHANGE_USER_INFO, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        // 地址变化时传null，否则传原receiptId
        receiptId: isAddressChanged ? null : editForm.value.receiptId,
        // 地址变化时传新地址，否则可以不传
        receiptAddress: isAddressChanged ? trimmedAddress : null,
        // 收货人名称变化时传新名称，否则传null
        receiptName: isNameChanged ? trimmedName : null,
        phone: isPhoneChanged ? trimmedPhone : null
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 更新所有预订单信息（后端可能返回空值，使用前端传入值作为兜底）
      const newReceiptId = result.data.receiptId || editForm.value.receiptId
      const newReceiptName = result.data.receiptName || trimmedName
      const newReceiptAddress = result.data.receiptAddress || trimmedAddress
      const newPhone = result.data.phone || trimmedPhone
      
      // 更新所有预订单的收货信息
      preOrderDetails.value.forEach(order => {
        order.receiptId = newReceiptId
        order.receiptName = newReceiptName
        order.receiptAddress = newReceiptAddress
        order.phone = newPhone
      })
      
      // 同步更新editForm以保持一致性
      editForm.value.receiptId = newReceiptId
      editForm.value.receiptName = newReceiptName
      editForm.value.receiptAddress = newReceiptAddress
      editForm.value.phone = newPhone
      
      showContactDialog.value = false
      ElMessage.success('联系方式修改成功')
      
      // 如果地址、电话或收货人名称发生变化，询问用户是否同步到个人资料
      if (isAddressChanged || isPhoneChanged || isNameChanged) {
        try {
          await ElMessageBox.confirm(
            '是否将新的收货信息同步到个人资料中？',
            '同步个人资料',
            {
              confirmButtonText: '同步',
              cancelButtonText: '不同步',
              type: 'info'
            }
          )
          // 用户点击确定，调用同步接口
          await syncUserInfoToProfile(newReceiptId, newPhone, newReceiptName, isPhoneChanged, isNameChanged)
        } catch {
          // 用户点击取消，不做任何操作
        }
      }
    } else {
      ElMessage.error(result.message || '修改失败')
    }
  } catch (error) {
    console.error('修改用户信息异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    savingUserInfo.value = false
  }
}

// 打开修改数量对话框（支持多商品）
const openQuantityDialog = (index = 0) => {
  if (isConfirmed.value) return
  const orders = preOrderDetails.value
  if (!orders || orders.length === 0) return
  
  editingProductIndex.value = index
  const order = orders[index]
  newQuantity.value = String(order.totalCount || 1)
  quantityError.value = ''
  showQuantityDialog.value = true
}

// 处理数量输入（只允许输入数字）
const handleQuantityInput = (e) => {
  const val = e.target ? e.target.value : e
  newQuantity.value = String(val).replace(/[^0-9]/g, '')
  quantityError.value = ''
}

// 验证数量
const validateQuantity = () => {
  const num = parseInt(newQuantity.value)
  
  if (!newQuantity.value || isNaN(num)) {
    quantityError.value = '请输入有效数量'
    return false
  }
  if (num < 1) {
    quantityError.value = '数量不能小于1'
    return false
  }
  return true
}

// 修改商品数量 - 调用后端接口（支持多商品）
const handleSaveProductCount = async () => {
  const orders = preOrderDetails.value
  if (!orders || orders.length === 0) return
  if (editingProductIndex.value < 0 || editingProductIndex.value >= orders.length) return
  
  // 验证数量
  if (!validateQuantity()) return
  
  const quantity = parseInt(newQuantity.value)
  const order = orders[editingProductIndex.value]
  
  // 如果数量没有变化，直接关闭
  if (quantity === order.totalCount) {
    showQuantityDialog.value = false
    return
  }
  
  quantitySaving.value = true
  try {
    const response = await request(ORDER_API.CHANGE_PRODUCT_COUNT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        productId: order.productId,
        newCount: quantity
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      // 更新预订单数量和总价（使用后端返回的值）
      const newCount = result.data.totalCount || quantity
      const newTotalPrice = result.data.totalPrice
      const newStock = result.data.stock
      
      order.totalCount = newCount
      // 直接使用后端返回的总价
      if (newTotalPrice !== undefined && newTotalPrice !== null) {
        order.totalPrice = parseFloat(newTotalPrice)
      }
      // 使用后端返回的库存更新最大可购数量
      if (newStock !== undefined && newStock !== null) {
        order.stock = newStock
      }
      ElMessage.success('数量修改成功')
      showQuantityDialog.value = false
    } else {
      ElMessage.error(result.message || '修改失败')
    }
  } catch (error) {
    console.error('修改商品数量异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    quantitySaving.value = false
  }
}

// 确认创建订单（使用批量接口）
const handleConfirmOrder = async () => {
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  
  if (!preOrderDetails.value || preOrderDetails.value.length === 0) {
    ElMessage.warning('订单信息不完整')
    return
  }
  
  submitting.value = true
  try {
    // 构建批量创建订单的请求数据
    const orderDTOs = preOrderDetails.value.map(order => ({
      userId: props.userInfo.userId,
      productId: order.productId,
      totalCount: order.totalCount,
      receiptId: editForm.value.receiptId,
      receiptName: editForm.value.receiptName,
      phone: editForm.value.phone
    }))
    
    const response = await request(ORDER_API.BATCH_CREATE_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderDTOs)
    })
    const result = await response.json()
    console.log('批量创建订单响应:', result)
    
    if (result.code !== 0 || !result.data) {
      ElMessage.error(result.message || '创建订单失败')
      return
    }
    
    // 处理返回结果，检查哪些商品失败（返回 null）
    const orderResults = result.data
    const successOrders = []
    const failedProducts = []
    
    orderResults.forEach((orderData, index) => {
      if (orderData === null) {
        // 从预订单获取失败商品信息
        const preOrder = preOrderDetails.value[index]
        failedProducts.push(preOrder?.productName || `商品${index + 1}`)
      } else {
        if (orderData.status === null || orderData.status === undefined) {
          orderData.status = ORDER_TO_PAY
        }
        successOrders.push(orderData)
      }
    })
    
    if (successOrders.length === 0) {
      ElMessage.error('所有订单创建失败')
      return
    }
    
    if (failedProducts.length > 0) {
      ElMessage.warning({
        message: `以下商品创建订单失败：${failedProducts.join('、')}，已成功创建 ${successOrders.length} 个订单`,
        duration: 5000
      })
    } else {
      ElMessage.success(`订单创建成功，共 ${successOrders.length} 个订单`)
    }
    
    confirmedOrders.value = successOrders
    isConfirmed.value = true
    
    // 存储订单信息用于支付页面
    sessionStorage.setItem('currentOrders', JSON.stringify(successOrders))
    // 兼容单订单场景
    if (successOrders.length === 1) {
      sessionStorage.setItem('currentOrder', JSON.stringify(successOrders[0]))
    }
  } catch (error) {
    console.error('确认订单异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 去支付（使用批量接口）
const handlePayOrder = async () => {
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  
  if (!confirmedOrders.value || confirmedOrders.value.length === 0) {
    ElMessage.warning('订单信息不完整')
    return
  }
  
  submitting.value = true
  try {
    // 构建批量支付请求数据
    const payDTOs = confirmedOrders.value.map(order => ({
      orderId: order.orderId,
      userId: props.userInfo.userId
    }))
    
    const response = await request(ORDER_API.BATCH_PAY_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payDTOs)
    })
    const result = await response.json()
    console.log('批量支付响应:', result)
    
    if (result.code !== 0 || !result.data) {
      ElMessage.error(result.message || '支付失败')
      return
    }
    
    // 处理返回结果，检查哪些订单支付失败（返回 null）
    const payResults = result.data
    const failedProducts = []
    let successCount = 0
    
    payResults.forEach((payData, index) => {
      if (payData === null || !payData.successFlag) {
        // 从已确认订单获取失败商品信息
        const order = confirmedOrders.value[index]
        failedProducts.push(order?.productName || `订单${index + 1}`)
      } else {
        // 更新订单状态为待发货(2)
        confirmedOrders.value[index].status = 2
        successCount++
      }
    })
    
    if (successCount === 0) {
      ElMessage.error('支付失败，请重试')
      return
    }
    
    if (failedProducts.length > 0) {
      ElMessage.warning({
        message: `以下商品支付失败：${failedProducts.join('、')}，已成功支付 ${successCount} 个订单`,
        duration: 5000
      })
    } else {
      ElMessage.success('支付成功')
    }
    
    // 跳转到支付成功页面
    const successOrderIds = confirmedOrders.value
      .filter((_, index) => payResults[index] !== null && payResults[index]?.successFlag)
      .map(o => o.orderId)
      .join(',')
    router.push({ path: '/payment-success', query: { orderId: successOrderIds } })
  } catch (error) {
    console.error('支付失败:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消订单（使用批量接口）
const handleCancelOrder = async () => {
  if (!props.isLoggedIn) {
    emit('openLogin')
    return
  }
  
  try {
    const orderCount = confirmedOrders.value.length
    await ElMessageBox.confirm(
      `确定要取消${orderCount > 1 ? '这' + orderCount + '个' : '该'}订单吗？取消后库存将恢复。`, 
      '取消订单', 
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )
    
    cancelling.value = true
    
    // 构建批量取消请求数据
    const cancelDTOs = confirmedOrders.value.map(order => ({
      orderId: order.orderId,
      userId: props.userInfo.userId
    }))
    
    const response = await request(ORDER_API.BATCH_CANCEL_ORDER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(cancelDTOs)
    })
    const result = await response.json()
    console.log('批量取消响应:', result)
    
    if (result.code !== 0 || !result.data) {
      ElMessage.error(result.message || '取消订单失败')
      return
    }
    
    // 处理返回结果，检查哪些订单取消失败（返回 null 或 false）
    const cancelResults = result.data
    const failedProducts = []
    let successCount = 0
    
    cancelResults.forEach((cancelSuccess, index) => {
      if (cancelSuccess === null || cancelSuccess === false) {
        // 从已确认订单获取失败商品信息
        const order = confirmedOrders.value[index]
        failedProducts.push(order?.productName || `订单${index + 1}`)
      } else {
        confirmedOrders.value[index].status = ORDER_CANCELLED
        successCount++
      }
    })
    
    if (successCount === orderCount) {
      ElMessage.success('订单已取消')
    } else if (successCount > 0) {
      ElMessage.warning({
        message: `以下商品取消失败：${failedProducts.join('、')}，已成功取消 ${successCount} 个订单`,
        duration: 5000
      })
    } else {
      ElMessage.error('取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    cancelling.value = false
  }
}

// 返回商品详情页或购物车
const goBackToProduct = () => {
  // 标记为返回操作，防止覆盖目标页面的来源记录
  markAsBackNavigation()
  
  // 如果是多商品结算，返回购物车
  if (isMultiProduct.value) {
    router.push('/cart')
    return
  }
  
  // 单商品场景：优先从路由参数获取，其次从已确认订单获取，最后从预创建订单获取
  const firstConfirmed = confirmedOrders.value[0]
  const firstPreOrder = preOrderDetails.value[0]
  const productId = route.query.productId || firstConfirmed?.productId || firstPreOrder?.productId
  const productType = route.query.type || firstConfirmed?.productType || firstPreOrder?.productType
  
  if (productId) {
    const query = {}
    if (productType) query.type = productType
    router.push({
      path: `/product/${productId}`,
      query
    })
  } else {
    router.push('/products')
  }
}

// 初始化
onMounted(async () => {
  // 从路由参数获取商品信息
  const productId = route.query.productId
  const productType = route.query.type // 商品类型：1-宠物，2-用品
  const totalCount = parseInt(route.query.count) || 1
  const orderId = route.query.orderId
  
  // 如果有orderId，说明是查看已有订单
  if (orderId) {
    // 尝试从 sessionStorage 获取多订单数据
    const storedOrders = sessionStorage.getItem('currentOrders')
    if (storedOrders) {
      try {
        const parsed = JSON.parse(storedOrders)
        if (Array.isArray(parsed) && parsed.length > 0) {
          confirmedOrders.value = parsed
          isConfirmed.value = true
          isMultiProduct.value = parsed.length > 1
          loading.value = false
          return
        }
      } catch (e) {
        console.error('解析多订单数据失败:', e)
      }
    }
    
    // 兼容单订单场景
    const storedOrder = sessionStorage.getItem('currentOrder')
    if (storedOrder) {
      try {
        const parsed = JSON.parse(storedOrder)
        if (String(parsed.orderId) === String(orderId)) {
          confirmedOrders.value = [parsed]
          isConfirmed.value = true
          isMultiProduct.value = false
          loading.value = false
          return
        }
      } catch (e) {
        console.error('解析订单数据失败:', e)
      }
    }
    // TODO: 调用API获取订单详情
    ElMessage.warning('订单信息不存在')
    router.push('/products')
    return
  }
  
  // 检查是否是从购物车批量结算（后端已返回预创建订单详情）
  const preOrderDetailsStr = sessionStorage.getItem('preOrderDetails')
  if (preOrderDetailsStr) {
    try {
      const preOrders = JSON.parse(preOrderDetailsStr)
      if (Array.isArray(preOrders) && preOrders.length > 0) {
        // 清除 sessionStorage 中的数据，防止重复使用
        sessionStorage.removeItem('preOrderDetails')
        // 直接使用后端返回的预创建订单详情
        preOrderDetails.value = preOrders
        isMultiProduct.value = preOrders.length > 1
        // 初始化编辑表单（使用第一个订单的收货信息）
        const firstOrder = preOrders[0]
        editForm.value = {
          receiptId: firstOrder.receiptId,
          receiptName: firstOrder.receiptName || '',
          receiptAddress: firstOrder.receiptAddress || '',
          phone: firstOrder.phone || '',
          totalCount: preOrders.reduce((sum, o) => sum + (o.totalCount || 0), 0)
        }
        isConfirmed.value = false
        loading.value = false
        return
      }
    } catch (e) {
      console.error('解析预创建订单数据失败:', e)
    }
  }
  
  // 兼容旧的购物车结算方式（如果有 checkoutItems）
  const checkoutItemsStr = sessionStorage.getItem('checkoutItems')
  if (checkoutItemsStr) {
    try {
      const checkoutItems = JSON.parse(checkoutItemsStr)
      if (Array.isArray(checkoutItems) && checkoutItems.length > 0) {
        // 清除 sessionStorage 中的数据，防止重复使用
        sessionStorage.removeItem('checkoutItems')
        // 批量预创建订单
        await batchPreCreateOrders(checkoutItems)
        return
      }
    } catch (e) {
      console.error('解析购物车结算数据失败:', e)
    }
  }
  
  // 单商品预创建订单流程
  if (!productId) {
    ElMessage.warning('商品信息不存在')
    router.push('/products')
    return
  }
  
  await singlePreCreateOrder(parseInt(productId), totalCount)
})
</script>

<template>
  <div class="checkout-page">
    <div class="container-base">
      <!-- 页面标题栏 -->
      <div class="page-header">
        <el-button 
          v-if="!isConfirmed" 
          class="back-btn" 
          text 
          @click="goBackToProduct"
        >
          <el-icon><ArrowLeft /></el-icon>
          <span>{{ isMultiProduct ? '返回购物车' : '返回商品详情' }}</span>
        </el-button>
      </div>
      <h1 class="page-title">{{ isConfirmed ? '确认订单' : '订单预览' }}</h1>
      
      <el-skeleton :loading="loading" animated :rows="8">
        <template #default>
          <template v-if="currentOrders.length > 0">
            <!-- 订单状态提示 -->
            <div v-if="isConfirmed" class="order-status-banner" :class="`status-${currentOrders[0].status ?? 1}`">
              <el-icon><Warning /></el-icon>
              <span>订单状态：{{ statusMap[currentOrders[0].status ?? 1]?.text || '待支付' }}</span>
              <span v-if="currentOrders.length === 1" class="order-id">订单号：{{ currentOrders[0].orderId }}</span>
              <span v-else class="order-id">共 {{ currentOrders.length }} 个订单</span>
              <span class="order-time">下单时间：{{ formatDateTime(currentOrders[0].createTime) }}</span>
            </div>
            
            <!-- 预创建订单提示 -->
            <div v-else class="order-status-banner status-preview">
              <el-icon><Warning /></el-icon>
              <span>订单预览 - 您可以修改收货信息和商品数量，确认无误后提交订单</span>
            </div>
            
            <!-- 收货信息 -->
            <div class="section address-section">
              <div class="section-header">
                <h2><el-icon><Location /></el-icon> 收货信息</h2>
                <el-button 
                  v-if="!isConfirmed" 
                  text 
                  type="primary" 
                  @click="openContactDialog"
                >
                  <el-icon><Edit /></el-icon> 修改
                </el-button>
              </div>
              
              <!-- 显示模式 -->
              <div v-if="currentOrder.receiptName || currentOrder.receiptAddress || currentOrder.phone" class="address-card">
                <div class="address-info">
                  <span class="name">{{ currentOrder.receiptName || '收货人' }}</span>
                  <span class="phone">{{ currentOrder.phone || '未设置电话' }}</span>
                </div>
                <div class="address-detail">
                  <el-icon><Location /></el-icon>
                  {{ currentOrder.receiptAddress || '未设置地址' }}
                </div>
              </div>
              
              <div v-else class="no-address" @click="openContactDialog">
                <el-icon><Warning /></el-icon>
                <span>{{ isConfirmed ? '暂无收货信息' : '点击添加收货信息' }}</span>
              </div>
            </div>
            
            <!-- 商品清单 -->
            <div class="section goods-section">
              <div class="section-header">
                <h2>商品清单 <span v-if="currentOrders.length > 1" class="goods-count">(共 {{ currentOrders.length }} 件商品)</span></h2>
              </div>
              
              <div class="goods-list">
                <div 
                  v-for="(order, index) in currentOrders" 
                  :key="order.productId" 
                  class="goods-item"
                >
                  <img :src="order.imgUrl || 'https://via.placeholder.com/100x100'" :alt="order.productName" class="goods-image" />
                  <div class="goods-info">
                    <h3 class="goods-name">{{ order.productName }}</h3>
                    <div class="goods-meta">
                      <span class="price">¥{{ getUnitPrice(order).toFixed(2) }}</span>
                      <!-- 数量显示（点击弹出修改对话框） -->
                      <span 
                        class="quantity"
                        :class="{ 'is-editable': !isConfirmed }"
                        @click="openQuantityDialog(index)"
                      >
                        x{{ order.totalCount }}
                      </span>
                    </div>
                  </div>
                  <div class="goods-subtotal">¥{{ parseFloat(order.totalPrice).toFixed(2) }}</div>
                </div>
              </div>
            </div>
            
            <!-- 金额明细 -->
            <div class="section amount-section">
              <div class="amount-row">
                <span>商品总额</span>
                <span>¥{{ totalAmount.toFixed(2) }}</span>
              </div>
              <div class="amount-row">
                <span>运费</span>
                <span class="free">免运费</span>
              </div>
              <div class="amount-row total">
                <span>应付金额</span>
                <span class="total-price">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
            </div>
            
            <!-- 提交按钮 -->
            <div class="submit-section">
              <div class="submit-info">
                共 <span class="count">{{ totalItemCount }}</span> 件商品，
                合计：<span class="price">¥{{ totalAmount.toFixed(2) }}</span>
              </div>
              
              <div class="submit-buttons">
                <!-- 预创建订单：显示确认按钮 -->
                <el-button
                  v-if="!isConfirmed"
                  type="primary"
                  size="large"
                  :loading="submitting"
                  @click="handleConfirmOrder"
                >
                  确认订单
                </el-button>
                
                <!-- 已确认订单：待支付状态(1)显示取消和支付按钮 -->
                <template v-else>
                  <template v-if="currentOrders[0].status === 1 || currentOrders[0].status === null || currentOrders[0].status === undefined">
                    <el-button
                      size="large"
                      :loading="cancelling"
                      @click="handleCancelOrder"
                    >
                      <el-icon><Close /></el-icon>
                      取消订单
                    </el-button>
                    <el-button
                      type="primary"
                      size="large"
                      :loading="submitting"
                      @click="handlePayOrder"
                    >
                      立即支付
                    </el-button>
                  </template>
                  <!-- 订单已取消(6) -->
                  <template v-else-if="currentOrders[0].status === 6">
                    <el-button
                      size="large"
                      @click="goBackToProduct"
                    >
                      <el-icon><ArrowLeft /></el-icon>
                      {{ isMultiProduct ? '返回购物车' : '返回商品详情' }}
                    </el-button>
                    <el-button
                      type="info"
                      size="large"
                      disabled
                    >
                      订单已取消
                    </el-button>
                  </template>
                  <!-- 其他状态 -->
                  <el-button
                    v-else
                    type="info"
                    size="large"
                    disabled
                  >
                    {{ statusMap[currentOrders[0].status]?.text || '已处理' }}
                  </el-button>
                </template>
              </div>
            </div>
          </template>
        </template>
      </el-skeleton>
      

      
    </div>
  </div>
  
  <!-- 修改数量模态框 -->
  <Teleport to="body">
    <Transition name="quantity-modal">
      <div v-if="showQuantityDialog" class="quantity-modal-overlay" @mousedown.self="quantityModalMouseDown = true" @mouseup.self="quantityModalMouseDown && (showQuantityDialog = false); quantityModalMouseDown = false">
        <div class="quantity-modal" @mousedown="quantityModalMouseDown = false">
          <!-- 模态框头部 -->
          <div class="modal-header">
            <h3 class="modal-title">
              <el-icon><ShoppingCart /></el-icon>
              修改数量
            </h3>
            <button class="modal-close" @click="showQuantityDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <!-- 商品信息 -->
          <div class="modal-body" v-if="editingProductIndex >= 0 && preOrderDetails[editingProductIndex]">
            <div class="product-preview">
              <img :src="preOrderDetails[editingProductIndex].imgUrl || 'https://via.placeholder.com/80x80'" :alt="preOrderDetails[editingProductIndex].productName" class="preview-image" />
              <div class="preview-info">
                <h4 class="preview-name">{{ preOrderDetails[editingProductIndex].productName }}</h4>
                <div class="preview-price">¥{{ getUnitPrice(preOrderDetails[editingProductIndex]).toFixed(2) }}</div>
              </div>
            </div>
            
            <!-- 数量输入 -->
            <div class="quantity-row">
              <span class="quantity-label">购买数量</span>
              <input 
                type="text" 
                v-model="newQuantity" 
                class="qty-input-simple"
                placeholder="请输入数量"
                @input="handleQuantityInput"
              />
            </div>
            <p v-if="quantityError" class="quantity-error">{{ quantityError }}</p>
            
            <!-- 小计 -->
            <div class="subtotal-row">
              <span class="subtotal-label">小计</span>
              <span class="subtotal-price">¥{{ (getUnitPrice(preOrderDetails[editingProductIndex]) * (parseInt(newQuantity) || 0)).toFixed(2) }}</span>
            </div>
          </div>
          
          <!-- 模态框底部 -->
          <div class="modal-footer">
            <button class="btn-cancel" @click="showQuantityDialog = false">取消</button>
            <button class="btn-confirm" :disabled="quantitySaving" @click="handleSaveProductCount">
              <span v-if="quantitySaving" class="loading-spinner"></span>
              {{ quantitySaving ? '保存中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
  
  <!-- 修改联系方式模态框 -->
  <Teleport to="body">
    <Transition name="contact-modal">
      <div v-if="showContactDialog" class="contact-modal-overlay">
        <div class="contact-modal">
          <!-- 模态框头部 -->
          <div class="modal-header">
            <h3 class="modal-title">
              <el-icon><Location /></el-icon>
              修改收货信息
            </h3>
            <button class="modal-close" :disabled="savingUserInfo" @click="showContactDialog = false">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <!-- 模态框内容 -->
          <div class="modal-body">
            <!-- 收货人名称 -->
            <div class="form-item">
              <label class="form-label">
                收货人
              </label>
              <input 
                type="text"
                v-model="editForm.receiptName" 
                placeholder="请输入收货人姓名" 
                maxlength="20"
                class="form-input"
              />
            </div>
            
            <!-- 手机号 -->
            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>手机号
              </label>
              <input 
                type="text"
                v-model="editForm.phone" 
                placeholder="请输入手机号" 
                maxlength="11"
                class="form-input"
                :class="{ 'is-error': phoneError }"
                @blur="handlePhoneBlur"
                @input="handlePhoneInput"
              />
              <div v-if="phoneError" class="form-error">
                <el-icon><Warning /></el-icon>
                {{ phoneError }}
              </div>
            </div>
            
            <!-- 收货地址 -->
            <div class="form-item">
              <label class="form-label">
                <span class="required">*</span>收货地址
              </label>
              <div class="address-input-wrapper">
                <div class="address-input-row">
                  <input
                    type="text"
                    v-model="editForm.receiptAddress"
                    placeholder="请输入地址名称进行搜索，或在地图上点击选择"
                    class="form-input address-input"
                    @keyup.enter="handleSearchAddress"
                    @blur="closeSuggestions"
                  />
                  <button 
                    class="search-btn"
                    :disabled="mapSearching"
                    @click="handleSearchAddress"
                  >
                    <el-icon v-if="!mapSearching"><Search /></el-icon>
                    <span v-else class="loading-spinner-small"></span>
                    <span>搜索</span>
                  </button>
                </div>
                <!-- 地址搜索结果下拉列表 -->
                <div v-if="showSuggestions && addressSuggestions.length > 0" class="address-suggestions">
                  <div class="suggestions-header">
                    <span>找到 {{ addressSuggestions.length }} 个相关地址</span>
                    <button class="suggestions-close" @click="closeSuggestionsNow">
                      <el-icon><Close /></el-icon>
                    </button>
                  </div>
                  <div 
                    v-for="(item, index) in addressSuggestions" 
                    :key="index"
                    class="suggestion-item"
                    @click="selectAddressSuggestion(item)"
                  >
                    <el-icon class="suggestion-icon"><Location /></el-icon>
                    <div class="suggestion-content">
                      <div class="suggestion-title">{{ item.title }}</div>
                      <div class="suggestion-address">{{ item.address }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 百度地图容器 -->
            <div class="form-item">
              <div class="form-label-row">
                <label class="form-label">
                  <el-icon><Location /></el-icon>地图选址
                  <span class="map-tip">（点击地图或拖拽标记选择位置）</span>
                </label>
                <button 
                  class="get-location-btn"
                  :disabled="gettingLocation"
                  @click="getCurrentLocation"
                >
                  <el-icon v-if="!gettingLocation"><Location /></el-icon>
                  <span v-else class="loading-spinner-small"></span>
                  {{ gettingLocation ? '定位中...' : '获取当前位置' }}
                </button>
              </div>
              <div class="map-wrapper">
                <div ref="mapContainer" class="map-container"></div>
                <!-- 地图加载遮罩层 -->
                <div v-if="mapLoading || gettingLocation" class="map-loading-overlay">
                  <div class="loading-spinner">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>{{ gettingLocation ? '正在获取位置...' : '地址解析中...' }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 模态框底部 -->
          <div class="modal-footer">
            <button class="btn-cancel" :disabled="savingUserInfo" @click="showContactDialog = false">取消</button>
            <button class="btn-confirm" :disabled="savingUserInfo" @click="handleSaveUserInfo">
              <span v-if="savingUserInfo" class="loading-spinner"></span>
              {{ savingUserInfo ? '保存中...' : '保存修改' }}
            </button>
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
</template>

<style scoped lang="scss">
.checkout-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-base);
  padding: var(--spacing-2xl) 0 var(--spacing-4xl);
}

// 页面头部
.page-header {
  margin-bottom: var(--spacing-sm);
  
  .back-btn {
    color: var(--color-text-secondary);
    font-size: var(--font-size-base);
    padding: var(--spacing-sm) 0;
    border-radius: var(--radius-base);
    transition: all var(--transition-fast);
    background: transparent !important;
    
    &:hover {
      color: var(--color-primary);
      background: transparent !important;
    }
    
    .el-icon {
      font-size: 18px;
      margin-right: 4px;
    }
    
    span {
      font-weight: var(--font-weight-medium);
    }
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
  padding: var(--spacing-base) var(--spacing-xl);
  border-radius: var(--radius-base);
  margin-bottom: var(--spacing-lg);
  background: #fdf6ec;
  color: #E6A23C;
  
  &.status-preview { background: #ecf5ff; color: #409EFF; }
  &.status-1 { background: #fdf6ec; color: #E6A23C; } // 待支付
  &.status-2 { background: #f4f4f5; color: #909399; } // 已支付/待发货
  &.status-3 { background: #ecf5ff; color: #409EFF; } // 已发货
  &.status-4 { background: #ecf5ff; color: #409EFF; } // 待签收
  &.status-5 { background: #f0f9eb; color: #67C23A; } // 已收货/订单完成
  &.status-6 { background: #fef0f0; color: #F56C6C; } // 订单取消
  
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  
  h2 {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    
    .goods-count {
      font-size: var(--font-size-sm);
      font-weight: var(--font-weight-normal);
      color: var(--color-text-secondary);
    }
  }
}

.edit-form {
  padding: var(--spacing-lg);
  background: #fafafa;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-primary);
  
  .edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--spacing-sm);
    margin-top: var(--spacing-base);
  }
}

.address-card {
  padding: var(--spacing-lg);
  background: #fafafa;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-border-light);
}

.address-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-base);
  margin-bottom: var(--spacing-sm);
  flex-wrap: wrap;
  
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

.no-address {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-2xl);
  border: 2px dashed var(--color-border-base);
  border-radius: var(--radius-base);
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: all var(--transition-fast);
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

.goods-list {
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-base);
  overflow: hidden;
}

.goods-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-base);
  border-bottom: 1px solid var(--color-border-light);
  
  &:last-child {
    border-bottom: none;
  }
}

.goods-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  margin-right: var(--spacing-base);
}

.goods-info {
  flex: 1;
}

.goods-name {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
  line-height: 1.4;
}

.goods-meta {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  flex-wrap: wrap;
  
  .quantity {
    &.is-editable {
      cursor: pointer;
      padding: 4px 12px;
      background: var(--color-secondary-light);
      border-radius: var(--radius-sm);
      color: var(--color-primary);
      font-weight: var(--font-weight-medium);
      transition: all var(--transition-fast);
      
      &:hover {
        background: var(--color-primary);
        color: #fff;
      }
    }
  }
}

.goods-subtotal {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary);
}

.amount-section {
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

.submit-section {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--spacing-2xl);
  padding: var(--spacing-xl);
  background: var(--color-bg-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.submit-info {
  color: var(--color-text-secondary);
  
  .count {
    color: var(--color-primary);
    font-weight: var(--font-weight-semibold);
  }
  
  .price {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-accent);
  }
}

.submit-buttons {
  display: flex;
  gap: var(--spacing-base);
  
  // 确认订单按钮样式
  :deep(.el-button) {
    min-width: 140px;
    height: 48px;
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-semibold);
    border-radius: 24px;
    transition: all var(--transition-base) var(--ease-out);
    
    // 主要按钮（确认订单/立即支付）
    &.el-button--primary {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 16px rgba(255, 138, 91, 0.35);
      
      &:hover {
        background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(255, 138, 91, 0.45);
      }
      
      &:active {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
      }
    }
    
    // 取消订单按钮
    &:not(.el-button--primary):not(.el-button--info) {
      background: #fff;
      border: 1px solid var(--color-border-base);
      color: var(--color-text-secondary);
      
      &:hover {
        border-color: var(--color-primary);
        color: var(--color-primary);
        background: var(--color-secondary-light);
      }
    }
    
    // 禁用状态按钮
    &.el-button--info.is-disabled {
      background: #f5f5f5;
      border: 1px solid #e0e0e0;
      color: #999;
      opacity: 1;
    }
  }
}

/* 弹窗样式优化 */
.contact-dialog {
  :deep(.el-dialog) {
    border-radius: var(--radius-xl);
    overflow: hidden;
  }
  
  :deep(.el-dialog__header) {
    padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
    margin: 0;
    border-bottom: 1px solid var(--color-border-light);
    background: linear-gradient(135deg, var(--color-secondary-light) 0%, var(--color-bg-surface) 100%);
    
    .el-dialog__title {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      color: var(--color-text-primary);
    }
    
    .el-dialog__headerbtn {
      top: var(--spacing-lg);
      right: var(--spacing-lg);
      
      .el-dialog__close {
        color: var(--color-text-tertiary);
        font-size: 18px;
        
        &:hover {
          color: var(--color-primary);
        }
      }
    }
  }
  
  :deep(.el-dialog__body) {
    padding: var(--spacing-xl);
  }
  
  :deep(.el-dialog__footer) {
    padding: var(--spacing-lg) var(--spacing-xl) var(--spacing-xl);
    border-top: 1px solid var(--color-border-light);
    background: var(--color-bg-base);
  }
}

.dialog-form {
  .form-item {
    margin-bottom: var(--spacing-xl);
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .form-label {
    display: block;
    margin-bottom: var(--spacing-sm);
    font-size: var(--font-size-base);
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
    
    .required {
      color: var(--color-danger);
      margin-right: var(--spacing-xs);
    }
  }
  
  .form-input {
    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner) {
      border-radius: var(--radius-base);
      box-shadow: none;
      border: 1px solid var(--color-border-base);
      transition: all var(--transition-fast);
      
      &:hover {
        border-color: var(--color-primary-light);
      }
      
      &:focus,
      &.is-focus {
        border-color: var(--color-primary);
        box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1);
      }
    }
    
    :deep(.el-textarea__inner) {
      padding: var(--spacing-md);
      line-height: 1.6;
    }
    
    // 错误状态
    &.is-error {
      :deep(.el-input__wrapper) {
        border-color: var(--color-danger) !important;
        
        &:focus,
        &.is-focus {
          box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1);
        }
      }
    }
  }
  
  // 表单错误提示
  .form-error {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 6px;
    font-size: 12px;
    color: var(--color-danger);
    
    .el-icon {
      font-size: 14px;
    }
  }
}

// 地址输入框包装器
.address-input-wrapper {
  width: 100%;
  position: relative;
  
  :deep(.el-input) {
    .el-input__wrapper {
      border-radius: 8px 0 0 8px !important;
      border-right: none !important;
      box-shadow: none !important;
      border: 1px solid #dcdfe6 !important;
      
      &:hover {
        border-color: var(--color-primary-light) !important;
      }
      
      &.is-focus {
        border-color: var(--color-primary) !important;
      }
    }
  }
  
  :deep(.el-input-group__append) {
    background: linear-gradient(135deg, #ff8a5b 0%, #ff6b3d 100%) !important;
    border: none !important;
    border-radius: 0 8px 8px 0 !important;
    padding: 0 !important;
    box-shadow: none !important;
    overflow: hidden;
    
    .el-button {
      color: #fff !important;
      border: none !important;
      background: transparent !important;
      padding: 0 20px !important;
      height: 100% !important;
      margin: 0 !important;
      font-size: 14px;
      font-weight: 500;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      white-space: nowrap;
      border-radius: 0 !important;
      transition: all 0.2s ease;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2) !important;
      }
      
      &:active {
        background: rgba(0, 0, 0, 0.05) !important;
      }
      
      .el-icon {
        font-size: 15px;
        margin: 0;
      }
      
      span {
        line-height: 1;
      }
    }
  }
}

// 地址搜索结果下拉列表
.address-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: #fff;
  border-radius: var(--radius-base);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  z-index: 100;
  max-height: 280px;
  overflow-y: auto;
  border: 1px solid var(--color-border-light);
  
  .suggestions-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    font-size: 12px;
    color: var(--color-text-tertiary);
    border-bottom: 1px solid var(--color-border-light);
    background: #fafafa;
    
    .suggestions-close {
      width: 20px;
      height: 20px;
      border: none;
      background: transparent;
      border-radius: 50%;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.2s ease;
      color: var(--color-text-tertiary);
      
      &:hover {
        background: var(--color-border-light);
        color: var(--color-text-secondary);
      }
      
      .el-icon {
        font-size: 14px;
      }
    }
  }
  
  .suggestion-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 12px 14px;
    cursor: pointer;
    transition: all 0.15s ease;
    border-bottom: 1px solid var(--color-border-light);
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: var(--color-secondary-light);
    }
    
    .suggestion-icon {
      color: var(--color-primary);
      font-size: 18px;
      margin-top: 2px;
      flex-shrink: 0;
    }
    
    .suggestion-content {
      flex: 1;
      min-width: 0;
    }
    
    .suggestion-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-primary);
      margin-bottom: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    
    .suggestion-address {
      font-size: 12px;
      color: var(--color-text-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

// 地图包装器
.map-wrapper {
  position: relative;
  width: 100%;
}

// 地图容器
.map-container {
  width: 100%;
  height: 300px;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-border-base);
  overflow: hidden;
  background: #f5f5f5;
}

// 地图加载遮罩层
.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  
  .loading-spinner {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: var(--color-primary);
    
    .el-icon {
      font-size: 32px;
      animation: spin 1s linear infinite;
    }
    
    span {
      font-size: 14px;
      color: var(--color-text-secondary);
    }
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 地图标签行（包含标签和获取位置按钮）
.form-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

// 地图提示文字
.map-tip {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  font-weight: normal;
  margin-left: var(--spacing-xs);
}

// 获取当前位置按钮
.get-location-btn {
  font-size: var(--font-size-sm);
  padding: var(--spacing-xs) var(--spacing-sm);
  
  &.el-button--primary {
    color: var(--color-primary);
  }
  
  :deep(span) {
    color: var(--color-primary);
  }
  
  .el-icon {
    margin-right: 4px;
    color: var(--color-primary);
  }
  
  &:hover {
    :deep(span) {
      color: var(--color-primary-dark);
    }
    
    .el-icon {
      color: var(--color-primary-dark);
    }
    
    background: var(--color-secondary-light);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-base);
  
  .btn-cancel {
    padding: var(--spacing-md) var(--spacing-xl);
    border-radius: var(--radius-base);
    border: 1px solid var(--color-border-base);
    background: var(--color-bg-surface);
    color: var(--color-text-secondary);
    
    &:hover {
      border-color: var(--color-primary-light);
      color: var(--color-primary);
      background: var(--color-secondary-light);
    }
  }
  
  .btn-confirm {
    padding: var(--spacing-md) var(--spacing-2xl);
    border-radius: var(--radius-base);
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
    border: none;
    box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.4);
    }
  }
}

// 数量修改模态框动画
.quantity-modal-enter-active,
.quantity-modal-leave-active {
  transition: all 0.3s ease;
}

.quantity-modal-enter-from,
.quantity-modal-leave-to {
  opacity: 0;
  
  .quantity-modal {
    transform: scale(0.9) translateY(-20px);
  }
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

<!-- 数量修改模态框样式 - 非 scoped -->
<style lang="scss">
.quantity-modal-overlay {
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
}

.quantity-modal {
  width: 420px;
  max-width: 90%;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
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

.quantity-modal .modal-header {
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
      color: #FF8A5B;
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

.quantity-modal .modal-body {
  padding: 24px;
}

.quantity-modal .product-preview {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
  
  .preview-image {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border-radius: 12px;
    background: #f5f5f5;
  }
  
  .preview-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .preview-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .preview-price {
    font-size: 18px;
    font-weight: 700;
    color: #FF8A5B;
    margin-top: auto;
  }
}

.quantity-modal .quantity-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  
  .quantity-label {
    font-size: 15px;
    color: #333;
    font-weight: 500;
  }
  
  .qty-input-simple {
    width: 120px;
    height: 44px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    background: #fff;
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    outline: none;
    transition: all 0.2s ease;
    
    &::placeholder {
      color: #c0c4cc;
      font-weight: 400;
      font-size: 14px;
    }
    
    &:hover {
      border-color: #c0c4cc;
    }
    
    &:focus {
      border-color: #FF8A5B;
      box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1);
    }
  }
}

.quantity-modal .quantity-error {
  font-size: 12px;
  color: #F56C6C;
  text-align: center;
  margin: -8px 0 16px;
}

.quantity-modal .subtotal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px dashed #eee;
  
  .subtotal-label {
    font-size: 14px;
    color: #666;
  }
  
  .subtotal-price {
    font-size: 24px;
    font-weight: 700;
    color: #2F8F6A;
  }
}

.quantity-modal .modal-footer {
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
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
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
    background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
    border: none;
    color: #fff;
    box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
    }
    
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    
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

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 联系方式模态框动画
.contact-modal-enter-active,
.contact-modal-leave-active {
  transition: all 0.3s ease;
}

.contact-modal-enter-from,
.contact-modal-leave-to {
  opacity: 0;
  
  .contact-modal {
    transform: scale(0.9) translateY(-20px);
  }
}

// 联系方式模态框样式
.contact-modal-overlay {
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

.contact-modal {
  width: 680px;
  max-width: 100%;
  max-height: 90vh;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
      
      .el-icon {
        color: #FF8A5B;
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
      
      &:disabled {
        cursor: not-allowed;
        opacity: 0.6;
      }
      
      .el-icon {
        font-size: 16px;
        color: #666;
      }
    }
  }
  
  .modal-body {
    padding: 24px;
    overflow-y: auto;
    flex: 1;
  }
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    border-top: 1px solid #eee;
    flex-shrink: 0;
    
    .btn-cancel,
    .btn-confirm {
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
      
      &:hover:not(:disabled) {
        background: #f5f7fa;
        border-color: #c0c4cc;
        color: #333;
      }
      
      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      
      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
      }
      
      &:disabled {
        opacity: 0.7;
        cursor: not-allowed;
      }
      
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
  
  // 表单项样式
  .form-item {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    
    .required {
      color: #F56C6C;
    }
    
    .el-icon {
      color: #FF8A5B;
    }
  }
  
  .form-label-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 8px;
  }
  
  .map-tip {
    font-size: 12px;
    color: #999;
    font-weight: 400;
    margin-left: 4px;
  }
  
  .form-input {
    width: 100%;
    height: 44px;
    padding: 0 14px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    color: #333;
    outline: none;
    transition: all 0.2s ease;
    box-sizing: border-box;
    
    &::placeholder {
      color: #c0c4cc;
    }
    
    &:hover {
      border-color: #c0c4cc;
    }
    
    &:focus {
      border-color: #FF8A5B;
      box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1);
    }
    
    &.is-error {
      border-color: #F56C6C;
      
      &:focus {
        box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1);
      }
    }
  }
  
  .form-error {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-top: 6px;
    font-size: 12px;
    color: #F56C6C;
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  // 地址输入行
  .address-input-wrapper {
    position: relative;
  }
  
  .address-input-row {
    display: flex;
    align-items: center;
  }
  
  .address-input {
    flex: 1;
    height: 44px;
    border: 1px solid #dcdfe6;
    border-right: none;
    border-radius: 8px 0 0 8px;
    padding: 0 14px;
    font-size: 14px;
    color: #333;
    outline: none;
    box-sizing: border-box;
    
    &::placeholder {
      color: #c0c4cc;
    }
    
    &:hover {
      border-color: #c0c4cc;
    }
    
    &:focus {
      border-color: #FF8A5B;
      box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1);
    }
  }
  
  .search-btn {
    height: 44px;
    min-width: 80px;
    padding: 0 20px;
    margin: 0;
    margin-left: 0 !important;
    background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
    border: none;
    border-radius: 0 8px 8px 0;
    color: #fff;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    transition: all 0.2s ease;
    white-space: nowrap;
    box-sizing: border-box;
    flex-shrink: 0;
    
    &:hover:not(:disabled) {
      background: linear-gradient(135deg, #ff9b70 0%, #FF8A5B 100%);
    }
    
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    
    .el-icon {
      font-size: 15px;
    }
  }
  
  // 获取当前位置按钮
  .get-location-btn {
    height: 32px;
    padding: 0 12px;
    background: transparent;
    border: 1px solid #FF8A5B;
    border-radius: 16px;
    color: #FF8A5B;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
    transition: all 0.2s ease;
    white-space: nowrap;
    
    &:hover:not(:disabled) {
      background: rgba(255, 138, 91, 0.1);
    }
    
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    
    .el-icon {
      font-size: 14px;
    }
  }
  
  // 地址搜索结果下拉列表
  .address-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    margin-top: 4px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    z-index: 100;
    max-height: 240px;
    overflow-y: auto;
    border: 1px solid #eee;
    
    .suggestions-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 14px;
      font-size: 12px;
      color: #999;
      border-bottom: 1px solid #f0f0f0;
      background: #fafafa;
      
      .suggestions-close {
        width: 20px;
        height: 20px;
        border: none;
        background: transparent;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s ease;
        color: #999;
        
        &:hover {
          background: #eee;
          color: #666;
        }
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
    
    .suggestion-item {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 12px 14px;
      cursor: pointer;
      transition: all 0.15s ease;
      border-bottom: 1px solid #f5f5f5;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background: #fff8f5;
      }
      
      .suggestion-icon {
        color: #FF8A5B;
        font-size: 18px;
        margin-top: 2px;
        flex-shrink: 0;
      }
      
      .suggestion-content {
        flex: 1;
        min-width: 0;
      }
      
      .suggestion-title {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .suggestion-address {
        font-size: 12px;
        color: #999;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
  
  // 地图包装器
  .map-wrapper {
    position: relative;
    width: 100%;
  }
  
  // 地图容器
  .map-container {
    width: 100%;
    height: 280px;
    border-radius: 8px;
    border: 1px solid #eee;
    overflow: hidden;
    background: #f5f5f5;
  }
  
  // 地图加载遮罩层
  .map-loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.85);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    
    .loading-spinner {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;
      color: #FF8A5B;
      
      .el-icon {
        font-size: 28px;
        animation: spin 1s linear infinite;
      }
      
      span {
        font-size: 13px;
        color: #666;
      }
    }
  }
  
  // 小型加载动画（用于按钮内）
  .loading-spinner-small {
    width: 14px;
    height: 14px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    display: inline-block;
  }
}
</style>
