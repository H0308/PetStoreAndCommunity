<script setup>
import { ref, onMounted } from 'vue'
import { Setting, Lock, Bell, Delete, Warning, Location, Search, Loading, Close, View, Hide, Phone, Message } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { USER_API, request } from '../../api/config.js'

const props = defineProps({
  userInfo: Object
})

const emit = defineEmits(['logout'])

// 设置项
const settings = ref({
  emailNotify: true,
  smsNotify: false,
  pushNotify: true,
  orderNotify: true,
  activityNotify: false,
})

// 地址管理
const addressLoading = ref(false)
const addressInfo = ref({
  receiptId: null,
  receiptName: '',
  receiptAddress: ''
})
const showAddressDialog = ref(false)
const savingAddress = ref(false)
const editAddressForm = ref({
  receiptName: '',
  receiptAddress: ''
})

// 密码修改
const showPasswordDialog = ref(false)
const savingPassword = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordErrors = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 手机修改
const showPhoneDialog = ref(false)
const savingPhone = ref(false)
const phoneForm = ref({
  phone: ''
})
const phoneError = ref('')
const currentPhone = ref('')

// 邮箱修改
const showEmailDialog = ref(false)
const savingEmail = ref(false)
const emailForm = ref({
  email: ''
})
const emailError = ref('')
const currentEmail = ref('')

// 百度地图相关
let baiduMap = null
let mapMarker = null
let geoCoder = null
let localSearch = null
const mapContainer = ref(null)
const mapLoading = ref(false)
const mapSearching = ref(false)
const gettingLocation = ref(false)
const addressSuggestions = ref([])
const showSuggestions = ref(false)

// 获取快递地址
const fetchReceiptAddress = async () => {
  if (!props.userInfo?.userId) return
  
  addressLoading.value = true
  try {
    const response = await request(`${USER_API.GET_RECEIPT_ADDRESS}?userId=${props.userInfo.userId}`)
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      addressInfo.value = {
        receiptId: result.data.receiptId,
        receiptName: result.data.receiptName || '',
        receiptAddress: result.data.receiptAddress || ''
      }
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  } finally {
    addressLoading.value = false
  }
}

// 打开地址编辑对话框
const openAddressDialog = () => {
  editAddressForm.value = {
    receiptName: addressInfo.value.receiptName,
    receiptAddress: addressInfo.value.receiptAddress
  }
  showAddressDialog.value = true
  // 延迟初始化地图
  setTimeout(() => {
    initBaiduMap()
  }, 100)
}

// 初始化百度地图
const initBaiduMap = () => {
  if (!mapContainer.value || !window.BMap) {
    console.warn('百度地图API未加载')
    return
  }
  
  baiduMap = new window.BMap.Map(mapContainer.value)
  const defaultPoint = new window.BMap.Point(116.404, 39.915)
  baiduMap.centerAndZoom(defaultPoint, 15)
  baiduMap.enableScrollWheelZoom(true)
  baiduMap.addControl(new window.BMap.NavigationControl())
  baiduMap.addControl(new window.BMap.ScaleControl())
  
  mapMarker = new window.BMap.Marker(defaultPoint)
  baiduMap.addOverlay(mapMarker)
  mapMarker.enableDragging()
  
  geoCoder = new window.BMap.Geocoder()
  
  localSearch = new window.BMap.LocalSearch(baiduMap, {
    onSearchComplete: (results) => {
      setTimeout(() => {
        mapSearching.value = false
        mapLoading.value = false
      }, 1000)
      
      if (localSearch.getStatus() === window.BMAP_STATUS_SUCCESS && results.getCurrentNumPois() > 0) {
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

  baiduMap.addEventListener('click', (e) => {
    const point = e.point
    mapMarker.setPosition(point)
    mapLoading.value = true
    setTimeout(() => {
      geoCoder.getLocation(point, (result) => {
        mapLoading.value = false
        if (result) {
          editAddressForm.value.receiptAddress = result.address || ''
        }
      })
    }, 1000)
  })
  
  mapMarker.addEventListener('dragend', (e) => {
    const point = e.point
    mapLoading.value = true
    setTimeout(() => {
      geoCoder.getLocation(point, (result) => {
        mapLoading.value = false
        if (result) {
          editAddressForm.value.receiptAddress = result.address || ''
        }
      })
    }, 1000)
  })
  
  if (editAddressForm.value.receiptAddress) {
    searchAddressOnMap(editAddressForm.value.receiptAddress)
  }
}

// 在地图上搜索地址
const searchAddressOnMap = (address) => {
  if (!localSearch || !address) return
  mapSearching.value = true
  mapLoading.value = true
  setTimeout(() => {
    localSearch.search(address)
  }, 1000)
}

// 选择地址建议
const selectAddressSuggestion = (suggestion) => {
  if (!suggestion || !baiduMap) return
  if (suggestion.point) {
    baiduMap.centerAndZoom(suggestion.point, 16)
    mapMarker.setPosition(suggestion.point)
  }
  editAddressForm.value.receiptAddress = suggestion.fullAddress || suggestion.address || suggestion.title
  showSuggestions.value = false
  addressSuggestions.value = []
}

const closeSuggestions = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

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
      const wgs84Point = new window.BMap.Point(longitude, latitude)
      const convertor = new window.BMap.Convertor()
      
      convertor.translate([wgs84Point], 1, 5, (data) => {
        if (data.status === 0 && data.points && data.points.length > 0) {
          const bdPoint = data.points[0]
          if (baiduMap && mapMarker) {
            baiduMap.centerAndZoom(bdPoint, 16)
            mapMarker.setPosition(bdPoint)
          }
          geoCoder.getLocation(bdPoint, (result) => {
            gettingLocation.value = false
            if (result && result.address) {
              editAddressForm.value.receiptAddress = result.address
              ElMessage.success('已获取当前位置')
            } else {
              ElMessage.warning('无法解析当前位置地址')
            }
          })
        } else {
          gettingLocation.value = false
          ElMessage.warning('坐标转换失败')
        }
      })
    },
    (error) => {
      gettingLocation.value = false
      ElMessage.warning('获取位置失败')
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

// 搜索地址
const handleSearchAddress = () => {
  if (editAddressForm.value.receiptAddress) {
    searchAddressOnMap(editAddressForm.value.receiptAddress)
  } else {
    ElMessage.warning('请输入地址进行搜索')
  }
}

// 保存地址修改
const handleSaveAddress = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 去除空白字符
  const trimmedName = editAddressForm.value.receiptName?.trim() || ''
  const trimmedAddress = editAddressForm.value.receiptAddress?.trim() || ''
  
  // 校验是否为空
  if (editAddressForm.value.receiptName && !trimmedName) {
    ElMessage.warning('收货人不能只包含空白字符')
    return
  }
  if (editAddressForm.value.receiptAddress && !trimmedAddress) {
    ElMessage.warning('收货地址不能只包含空白字符')
    return
  }
  
  // 判断哪些字段发生了变化
  const isNameChanged = trimmedName !== (addressInfo.value.receiptName?.trim() || '')
  const isAddressChanged = trimmedAddress !== (addressInfo.value.receiptAddress?.trim() || '')
  
  if (!isNameChanged && !isAddressChanged) {
    showAddressDialog.value = false
    return
  }
  
  savingAddress.value = true
  try {
    const requestBody = {
      userId: props.userInfo.userId
    }
    // 只传递发生变化的字段
    if (isNameChanged) {
      requestBody.receiptName = trimmedName
    }
    if (isAddressChanged) {
      requestBody.receiptAddress = trimmedAddress
    }
    
    const response = await request(USER_API.CHANGE_RECEIPT_ADDRESS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      addressInfo.value = {
        receiptId: result.data.receiptId,
        receiptName: result.data.receiptName || trimmedName,
        receiptAddress: result.data.receiptAddress || trimmedAddress
      }
      showAddressDialog.value = false
      ElMessage.success('地址修改成功')
    } else {
      ElMessage.error(result.message || '修改失败')
    }
  } catch (error) {
    console.error('修改地址异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    savingAddress.value = false
  }
}

// 注销账号
const handleDeleteAccount = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 第一步：确认注销
    await ElMessageBox.confirm(
      `
        <div style="text-align: left; line-height: 1.8;">
          <p style="margin-bottom: 16px; font-size: 15px; color: #303133;">注销账号前，请确认：</p>
          <div style="padding-left: 8px; margin-bottom: 16px;">
            <p style="margin: 8px 0; color: #606266;">
              <span style="display: inline-block; width: 20px; color: #FF8A5B; font-weight: bold;">1.</span>
              您没有未支付的订单
            </p>
            <p style="margin: 8px 0; color: #606266;">
              <span style="display: inline-block; width: 20px; color: #FF8A5B; font-weight: bold;">2.</span>
              您没有待签收的订单
            </p>
            <p style="margin: 8px 0; color: #606266;">
              <span style="display: inline-block; width: 20px; color: #FF8A5B; font-weight: bold;">3.</span>
              注销后数据将无法恢复
            </p>
          </div>
          <p style="margin-top: 16px; font-size: 15px; color: #303133; font-weight: 500;">确定要注销账号吗？</p>
        </div>
      `,
      '注销账号',
      {
        confirmButtonText: '确认注销',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        customClass: 'delete-account-confirm'
      }
    )
    
    // 第二步：输入密码验证（这里暂时跳过密码验证，因为后端接口不需要）
    // 可以根据需要添加密码验证逻辑
    
    // 第三步：调用注销接口
    const formData = new FormData()
    formData.append('userId', props.userInfo.userId)
    
    const response = await request(USER_API.DEACTIVATE_ACCOUNT, {
      method: 'POST',
      headers: {},
      body: formData
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      ElMessage.success('账号已注销')
      // 清除本地存储的用户信息和token
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      // 触发登出事件
      emit('logout')
    } else {
      ElMessage.error(result.message || '注销失败')
    }
  } catch (error) {
    // 用户取消操作或发生错误
    if (error !== 'cancel') {
      console.error('注销账号异常:', error)
      // ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 保存设置
const saveSettings = () => {
  ElMessage.success('设置已保存')
}

// 打开密码修改对话框
const openPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordErrors.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  showOldPassword.value = false
  showNewPassword.value = false
  showConfirmPassword.value = false
  showPasswordDialog.value = true
}

// 校验密码
const validatePassword = (password, fieldName) => {
  if (!password) {
    return `请输入${fieldName}`
  }
  if (password.length < 6) {
    return `${fieldName}长度不能少于6位`
  }
  if (password.length > 30) {
    return `${fieldName}长度不能超过30位`
  }
  return ''
}

// 校验密码表单
const validatePasswordForm = () => {
  const trimmedOld = passwordForm.value.oldPassword?.trim() || ''
  const trimmedNew = passwordForm.value.newPassword?.trim() || ''
  const trimmedConfirm = passwordForm.value.confirmPassword?.trim() || ''
  
  // 空白字符校验
  if (passwordForm.value.oldPassword && !trimmedOld) {
    passwordErrors.value.oldPassword = '旧密码不能只包含空白字符'
    return false
  }
  if (passwordForm.value.newPassword && !trimmedNew) {
    passwordErrors.value.newPassword = '新密码不能只包含空白字符'
    return false
  }
  if (passwordForm.value.confirmPassword && !trimmedConfirm) {
    passwordErrors.value.confirmPassword = '确认密码不能只包含空白字符'
    return false
  }
  
  // 基本校验
  passwordErrors.value.oldPassword = validatePassword(trimmedOld, '旧密码')
  if (passwordErrors.value.oldPassword) return false
  
  passwordErrors.value.newPassword = validatePassword(trimmedNew, '新密码')
  if (passwordErrors.value.newPassword) return false
  
  passwordErrors.value.confirmPassword = validatePassword(trimmedConfirm, '确认密码')
  if (passwordErrors.value.confirmPassword) return false
  
  // 新密码不能与旧密码相同
  if (trimmedNew === trimmedOld) {
    passwordErrors.value.newPassword = '新密码不能与旧密码相同'
    return false
  }
  
  // 确认密码必须与新密码一致
  if (trimmedNew !== trimmedConfirm) {
    passwordErrors.value.confirmPassword = '两次输入的密码不一致'
    return false
  }
  
  return true
}

// 清除密码错误提示
const clearPasswordError = (field) => {
  passwordErrors.value[field] = ''
}

// 保存密码修改
const handleSavePassword = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!validatePasswordForm()) {
    return
  }
  
  savingPassword.value = true
  try {
    const response = await request(USER_API.CHANGE_PASSWORD, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId: props.userInfo.userId,
        oldPassword: passwordForm.value.oldPassword.trim(),
        newPassword: passwordForm.value.newPassword.trim(),
        confirmPassword: passwordForm.value.confirmPassword.trim()
      })
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      showPasswordDialog.value = false
      ElMessage.success('密码修改成功')
    } else {
      ElMessage.error(result.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    savingPassword.value = false
  }
}

// 打开手机修改对话框
const openPhoneDialog = () => {
  phoneForm.value = {
    phone: ''
  }
  phoneError.value = ''
  showPhoneDialog.value = true
}

// 手机号校验正则（中国大陆11位手机号）
const phoneRegex = /^1[3-9]\d{9}$/

// 校验手机号格式
const validatePhoneNumber = (phone) => {
  if (!phone) return '请输入手机号'
  if (!/^\d+$/.test(phone)) return '手机号只能包含数字'
  if (phone.length !== 11) return '手机号必须为11位'
  if (!phoneRegex.test(phone)) return '请输入正确的手机号格式'
  return ''
}

// 清除手机号错误提示
const clearPhoneError = () => {
  phoneError.value = ''
}

// 手机号输入处理（只允许输入数字）
const handlePhoneInput = () => {
  // 过滤非数字字符
  phoneForm.value.phone = phoneForm.value.phone.replace(/[^\d]/g, '')
  // 清除错误提示
  if (phoneError.value) {
    phoneError.value = ''
  }
}

// 手机号失焦校验
const handlePhoneBlur = () => {
  const trimmedPhone = phoneForm.value.phone?.trim() || ''
  if (trimmedPhone) {
    const error = validatePhoneNumber(trimmedPhone)
    if (error) {
      phoneError.value = error
    }
  }
}

// 格式化手机号显示（隐藏中间4位）
const formatPhoneDisplay = (phone) => {
  if (!phone || phone.length !== 11) return phone || '未绑定'
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

// 保存手机修改
const handleSavePhone = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 去除空白字符
  const trimmedPhone = phoneForm.value.phone?.trim() || ''
  
  // 空白字符校验
  if (phoneForm.value.phone && !trimmedPhone) {
    phoneError.value = '手机号不能只包含空白字符'
    return
  }
  
  // 手机号格式校验
  const validationError = validatePhoneNumber(trimmedPhone)
  if (validationError) {
    phoneError.value = validationError
    return
  }
  
  // 检查是否与当前手机号相同
  if (trimmedPhone === currentPhone.value) {
    phoneError.value = '新手机号不能与当前手机号相同'
    return
  }
  
  savingPhone.value = true
  try {
    // 使用 FormData 传递参数
    const formData = new FormData()
    formData.append('userId', props.userInfo.userId)
    formData.append('phone', trimmedPhone)
    
    const response = await request(USER_API.CHANGE_PHONE, {
      method: 'POST',
      headers: {},
      body: formData
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      currentPhone.value = result.data
      showPhoneDialog.value = false
      ElMessage.success('手机号修改成功')
    } else {
      ElMessage.error(result.message || '手机号修改失败')
    }
  } catch (error) {
    console.error('修改手机号异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    savingPhone.value = false
  }
}

// 获取用户手机号
const fetchUserPhone = async () => {
  if (!props.userInfo?.userId) return
  
  try {
    const response = await request(`${USER_API.GET_PHONE}?userId=${props.userInfo.userId}`, {
      method: 'GET'
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      currentPhone.value = result.data
    }
  } catch (error) {
    console.error('获取手机号失败:', error)
  }
}

// 打开邮箱修改对话框
const openEmailDialog = () => {
  emailForm.value = {
    email: ''
  }
  emailError.value = ''
  showEmailDialog.value = true
}

// 邮箱校验正则
const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

// 校验邮箱格式
const validateEmail = (email) => {
  if (!email) return '请输入邮箱'
  if (!emailRegex.test(email)) return '请输入正确的邮箱格式'
  if (email.length > 100) return '邮箱长度不能超过100个字符'
  return ''
}

// 清除邮箱错误提示
const clearEmailError = () => {
  emailError.value = ''
}

// 邮箱输入处理
const handleEmailInput = () => {
  // 清除错误提示
  if (emailError.value) {
    emailError.value = ''
  }
}

// 邮箱失焦校验
const handleEmailBlur = () => {
  const trimmedEmail = emailForm.value.email?.trim() || ''
  if (trimmedEmail) {
    const error = validateEmail(trimmedEmail)
    if (error) {
      emailError.value = error
    }
  }
}

// 格式化邮箱显示（明文显示）
const formatEmailDisplay = (email) => {
  return email || '未绑定'
}

// 保存邮箱修改
const handleSaveEmail = async () => {
  if (!props.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 去除空白字符
  const trimmedEmail = emailForm.value.email?.trim() || ''
  
  // 空白字符校验
  if (emailForm.value.email && !trimmedEmail) {
    emailError.value = '邮箱不能只包含空白字符'
    return
  }
  
  // 邮箱格式校验
  const validationError = validateEmail(trimmedEmail)
  if (validationError) {
    emailError.value = validationError
    return
  }
  
  // 检查是否与当前邮箱相同
  if (trimmedEmail === currentEmail.value) {
    emailError.value = '新邮箱不能与当前邮箱相同'
    return
  }
  
  savingEmail.value = true
  try {
    // 使用 FormData 传递参数
    const formData = new FormData()
    formData.append('userId', props.userInfo.userId)
    formData.append('email', trimmedEmail)
    
    const response = await request(USER_API.CHANGE_EMAIL, {
      method: 'POST',
      headers: {},
      body: formData
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      currentEmail.value = result.data
      showEmailDialog.value = false
      ElMessage.success('邮箱修改成功')
    } else {
      ElMessage.error(result.message || '邮箱修改失败')
    }
  } catch (error) {
    console.error('修改邮箱异常:', error)
    // ElMessage.error('网络错误，请稍后重试')
  } finally {
    savingEmail.value = false
  }
}

// 获取用户邮箱
const fetchUserEmail = async () => {
  if (!props.userInfo?.userId) return
  
  try {
    const response = await request(`${USER_API.GET_EMAIL}?userId=${props.userInfo.userId}`, {
      method: 'GET'
    })
    const result = await response.json()
    
    if (result.code === 0 && result.data) {
      currentEmail.value = result.data
    }
  } catch (error) {
    console.error('获取邮箱失败:', error)
  }
}

onMounted(() => {
  fetchReceiptAddress()
  fetchUserPhone()
  fetchUserEmail()
})
</script>

<template>
  <div class="settings">
    <h2 class="section-title">
      <el-icon><Setting /></el-icon>
      账号管理
    </h2>

    <!-- 地址管理 -->
    <div class="settings-section">
      <h3 class="sub-title">
        <el-icon><Location /></el-icon>
        地址管理
      </h3>
      <div class="settings-list">
        <div class="settings-item address-card">
          <div class="address-info">
            <el-skeleton v-if="addressLoading" :rows="2" animated />
            <template v-else>
              <div class="address-row">
                <span class="address-label">收货人</span>
                <span class="address-value">{{ addressInfo.receiptName || '未设置' }}</span>
              </div>
              <div class="address-row">
                <span class="address-label">收货地址</span>
                <span class="address-value address-text">{{ addressInfo.receiptAddress || '未设置' }}</span>
              </div>
            </template>
          </div>
          <el-button text type="primary" @click="openAddressDialog">修改</el-button>
        </div>
      </div>
    </div>
    
    <!-- 安全设置 -->
    <div class="settings-section">
      <h3 class="sub-title">
        <el-icon><Lock /></el-icon>
        安全设置
      </h3>
      <div class="settings-list">
        <div class="settings-item">
          <div class="item-info">
            <span class="item-label">登录密码</span>
            <span class="item-desc">定期更换密码可以保护账号安全</span>
          </div>
          <el-button text type="primary" @click="openPasswordDialog">修改</el-button>
        </div>
        <div class="settings-item">
          <div class="item-info">
            <span class="item-label">绑定手机</span>
            <span class="item-desc">已绑定：{{ formatPhoneDisplay(currentPhone) }}</span>
          </div>
          <el-button text type="primary" @click="openPhoneDialog">更换</el-button>
        </div>
        <div class="settings-item">
          <div class="item-info">
            <span class="item-label">绑定邮箱</span>
            <span class="item-desc">已绑定：{{ formatEmailDisplay(currentEmail) }}</span>
          </div>
          <el-button text type="primary" @click="openEmailDialog">更换</el-button>
        </div>
      </div>
    </div>
    
    <!-- 危险操作 -->
    <div class="settings-section danger-zone">
      <h3 class="sub-title">
        <el-icon><Warning /></el-icon>
        危险操作
      </h3>
      <div class="settings-list">
        <div class="settings-item">
          <div class="item-info">
            <span class="item-label">注销账号</span>
            <span class="item-desc danger">注销后账号数据将无法恢复，请谨慎操作</span>
          </div>
          <el-button type="danger" @click="handleDeleteAccount">
            <el-icon><Delete /></el-icon>
            注销账号
          </el-button>
        </div>
      </div>
    </div>

    <!-- 地址修改对话框 -->
    <Teleport to="body">
      <Transition name="address-modal">
        <div v-if="showAddressDialog" class="address-modal-overlay">
          <div class="address-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Location /></el-icon>
                修改收货信息
              </h3>
              <button class="modal-close" :disabled="savingAddress" @click="showAddressDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <div class="modal-body">
              <!-- 收货人 -->
              <div class="form-item">
                <label class="form-label">收货人</label>
                <input 
                  type="text"
                  v-model="editAddressForm.receiptName" 
                  placeholder="请输入收货人姓名" 
                  maxlength="20"
                  class="form-input"
                />
              </div>
              
              <!-- 收货地址 -->
              <div class="form-item">
                <label class="form-label">收货地址</label>
                <div class="address-input-wrapper">
                  <div class="address-input-row">
                    <input
                      type="text"
                      v-model="editAddressForm.receiptAddress"
                      placeholder="请输入地址名称进行搜索，或在地图上点击选择"
                      class="form-input address-input"
                      @keyup.enter="handleSearchAddress"
                      @blur="closeSuggestions"
                    />
                    <button class="search-btn" :disabled="mapSearching" @click="handleSearchAddress">
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
                  <button class="get-location-btn" :disabled="gettingLocation" @click="getCurrentLocation">
                    <el-icon v-if="!gettingLocation"><Location /></el-icon>
                    <span v-else class="loading-spinner-small"></span>
                    {{ gettingLocation ? '定位中...' : '获取当前位置' }}
                  </button>
                </div>
                <div class="map-wrapper">
                  <div ref="mapContainer" class="map-container"></div>
                  <div v-if="mapLoading || gettingLocation" class="map-loading-overlay">
                    <div class="loading-content">
                      <el-icon class="is-loading"><Loading /></el-icon>
                      <span>{{ gettingLocation ? '正在获取位置...' : '地址解析中...' }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" :disabled="savingAddress" @click="showAddressDialog = false">取消</button>
              <button class="btn-confirm" :disabled="savingAddress" @click="handleSaveAddress">
                <span v-if="savingAddress" class="loading-spinner"></span>
                {{ savingAddress ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 密码修改对话框 -->
    <Teleport to="body">
      <Transition name="password-modal">
        <div v-if="showPasswordDialog" class="password-modal-overlay">
          <div class="password-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Lock /></el-icon>
                修改登录密码
              </h3>
              <button class="modal-close" :disabled="savingPassword" @click="showPasswordDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <div class="modal-body">
              <!-- 旧密码 -->
              <div class="form-item">
                <label class="form-label">旧密码</label>
                <div class="password-input-wrapper">
                  <input 
                    :type="showOldPassword ? 'text' : 'password'"
                    v-model="passwordForm.oldPassword" 
                    placeholder="请输入当前密码" 
                    maxlength="30"
                    class="form-input"
                    :class="{ 'is-error': passwordErrors.oldPassword }"
                    @input="clearPasswordError('oldPassword')"
                  />
                  <button class="toggle-password" type="button" @click="showOldPassword = !showOldPassword">
                    <el-icon v-if="showOldPassword"><View /></el-icon>
                    <el-icon v-else><Hide /></el-icon>
                  </button>
                </div>
                <span v-if="passwordErrors.oldPassword" class="error-text">{{ passwordErrors.oldPassword }}</span>
              </div>
              
              <!-- 新密码 -->
              <div class="form-item">
                <label class="form-label">新密码</label>
                <div class="password-input-wrapper">
                  <input 
                    :type="showNewPassword ? 'text' : 'password'"
                    v-model="passwordForm.newPassword" 
                    placeholder="请输入新密码（6-30位）" 
                    maxlength="30"
                    class="form-input"
                    :class="{ 'is-error': passwordErrors.newPassword }"
                    @input="clearPasswordError('newPassword')"
                  />
                  <button class="toggle-password" type="button" @click="showNewPassword = !showNewPassword">
                    <el-icon v-if="showNewPassword"><View /></el-icon>
                    <el-icon v-else><Hide /></el-icon>
                  </button>
                </div>
                <span v-if="passwordErrors.newPassword" class="error-text">{{ passwordErrors.newPassword }}</span>
              </div>
              
              <!-- 确认密码 -->
              <div class="form-item">
                <label class="form-label">确认新密码</label>
                <div class="password-input-wrapper">
                  <input 
                    :type="showConfirmPassword ? 'text' : 'password'"
                    v-model="passwordForm.confirmPassword" 
                    placeholder="请再次输入新密码" 
                    maxlength="30"
                    class="form-input"
                    :class="{ 'is-error': passwordErrors.confirmPassword }"
                    @input="clearPasswordError('confirmPassword')"
                  />
                  <button class="toggle-password" type="button" @click="showConfirmPassword = !showConfirmPassword">
                    <el-icon v-if="showConfirmPassword"><View /></el-icon>
                    <el-icon v-else><Hide /></el-icon>
                  </button>
                </div>
                <span v-if="passwordErrors.confirmPassword" class="error-text">{{ passwordErrors.confirmPassword }}</span>
              </div>
              
              <!-- 密码提示 -->
              <div class="password-tips">
                <p class="tip-title">密码要求：</p>
                <ul class="tip-list">
                  <li>密码长度为 6-30 位</li>
                  <li>新密码不能与旧密码相同</li>
                </ul>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" :disabled="savingPassword" @click="showPasswordDialog = false">取消</button>
              <button class="btn-confirm" :disabled="savingPassword" @click="handleSavePassword">
                <span v-if="savingPassword" class="loading-spinner"></span>
                {{ savingPassword ? '保存中...' : '确认修改' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 手机修改对话框 -->
    <Teleport to="body">
      <Transition name="phone-modal">
        <div v-if="showPhoneDialog" class="phone-modal-overlay">
          <div class="phone-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Phone /></el-icon>
                更换绑定手机
              </h3>
              <button class="modal-close" :disabled="savingPhone" @click="showPhoneDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <div class="modal-body">
              <!-- 当前手机号 -->
              <div class="current-phone-info">
                <span class="info-label">当前绑定手机：</span>
                <span class="info-value">{{ formatPhoneDisplay(currentPhone) }}</span>
              </div>
              
              <!-- 新手机号 -->
              <div class="form-item">
                <label class="form-label">新手机号</label>
                <input 
                  type="tel"
                  v-model="phoneForm.phone" 
                  placeholder="请输入新手机号" 
                  maxlength="11"
                  class="form-input"
                  :class="{ 'is-error': phoneError }"
                  @input="handlePhoneInput"
                  @blur="handlePhoneBlur"
                />
                <span v-if="phoneError" class="error-text">{{ phoneError }}</span>
              </div>
              
              <!-- 手机号提示 -->
              <div class="phone-tips">
                <p class="tip-title">温馨提示：</p>
                <ul class="tip-list">
                  <li>请输入11位中国大陆手机号</li>
                  <li>更换后原手机号将无法用于登录</li>
                </ul>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" :disabled="savingPhone" @click="showPhoneDialog = false">取消</button>
              <button class="btn-confirm" :disabled="savingPhone" @click="handleSavePhone">
                <span v-if="savingPhone" class="loading-spinner"></span>
                {{ savingPhone ? '保存中...' : '确认更换' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 邮箱修改对话框 -->
    <Teleport to="body">
      <Transition name="email-modal">
        <div v-if="showEmailDialog" class="email-modal-overlay">
          <div class="email-modal">
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Message /></el-icon>
                更换绑定邮箱
              </h3>
              <button class="modal-close" :disabled="savingEmail" @click="showEmailDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <div class="modal-body">
              <!-- 当前邮箱 -->
              <div class="current-email-info">
                <span class="info-label">当前绑定邮箱：</span>
                <span class="info-value">{{ formatEmailDisplay(currentEmail) }}</span>
              </div>
              
              <!-- 新邮箱 -->
              <div class="form-item">
                <label class="form-label">新邮箱</label>
                <input 
                  type="email"
                  v-model="emailForm.email" 
                  placeholder="请输入新邮箱地址" 
                  maxlength="100"
                  class="form-input"
                  :class="{ 'is-error': emailError }"
                  @input="handleEmailInput"
                  @blur="handleEmailBlur"
                />
                <span v-if="emailError" class="error-text">{{ emailError }}</span>
              </div>
              
              <!-- 邮箱提示 -->
              <div class="email-tips">
                <p class="tip-title">温馨提示：</p>
                <ul class="tip-list">
                  <li>请输入有效的邮箱地址</li>
                  <li>更换后原邮箱将无法用于登录</li>
                </ul>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-cancel" :disabled="savingEmail" @click="showEmailDialog = false">取消</button>
              <button class="btn-confirm" :disabled="savingEmail" @click="handleSaveEmail">
                <span v-if="savingEmail" class="loading-spinner"></span>
                {{ savingEmail ? '保存中...' : '确认更换' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.settings {
  .section-title {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: var(--font-size-lg);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-xl);
    padding-bottom: var(--spacing-base);
    border-bottom: 1px solid var(--color-border-light);
  }
}

.settings-section {
  margin-bottom: var(--spacing-2xl);
  
  &.danger-zone {
    padding: var(--spacing-lg);
    background: rgba(239, 83, 80, 0.05);
    border-radius: var(--radius-lg);
    border: 1px solid rgba(239, 83, 80, 0.2);
  }
}

.sub-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-base);
}

.settings-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-base) var(--spacing-lg);
  border-radius: var(--radius-base);
  
  // 覆盖 el-button text 类型的颜色为主题色
  :deep(.el-button--primary) {
    color: var(--color-primary);
    
    &:hover,
    &:focus {
      color: var(--color-primary-dark);
    }
    
    &:active {
      color: var(--color-primary-darker);
    }
  }
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
  flex: 1;
  min-width: 0;
}

.item-label {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.item-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  
  &.danger {
    color: var(--color-danger);
  }
}

// 地址卡片样式
.address-card {
  align-items: flex-start;
  padding: var(--spacing-lg);
  
  // 覆盖 el-button text 类型的颜色为主题色
  :deep(.el-button--primary) {
    color: var(--color-primary);
    
    &:hover,
    &:focus {
      color: var(--color-primary-dark);
    }
    
    &:active {
      color: var(--color-primary-darker);
    }
  }
  
  .address-info {
    flex: 1;
    min-width: 0;
  }
  
  .address-row {
    display: flex;
    align-items: baseline;
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-sm);
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .address-label {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
    flex-shrink: 0;
    width: 60px;
  }
  
  .address-value {
    font-size: var(--font-size-base);
    color: var(--color-text-primary);
    flex: 1;
    min-width: 0;
    
    &.address-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 地址修改对话框样式
.address-modal-overlay {
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

.address-modal {
  width: 680px;
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
      
      .el-icon { color: #FF8A5B; }
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
      &:hover:not(:disabled) { background: #f5f7fa; }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45); }
      &:disabled { opacity: 0.7; cursor: not-allowed; }
    }
  }
  
  .form-item {
    margin-bottom: 20px;
    &:last-child { margin-bottom: 0; }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
    .el-icon { color: #FF8A5B; }
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
    
    &::placeholder { color: #c0c4cc; }
    &:hover { border-color: #c0c4cc; }
    &:focus { border-color: #FF8A5B; box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1); }
  }
  
  .address-input-wrapper {
    position: relative;
  }
  
  .address-input-row {
    display: flex;
    align-items: stretch;
    width: 100%;
    
    .address-input { 
      flex: 1;
      min-width: 0;
      border-radius: 8px 0 0 8px;
      border-right: none;
      margin: 0;
    }
    
    .search-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      padding: 0 16px;
      height: 44px;
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      border-radius: 0 8px 8px 0;
      color: #fff;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.2s ease;
      white-space: nowrap;
      flex-shrink: 0;
      margin: 0;
      margin-left: -1px;
      
      &:hover:not(:disabled) { box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35); }
      &:disabled { opacity: 0.7; cursor: not-allowed; }
    }
  }
  
  .address-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    margin-top: 4px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    z-index: 100;
    max-height: 240px;
    overflow-y: auto;
    
    .suggestions-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 14px;
      border-bottom: 1px solid #f0f0f0;
      font-size: 12px;
      color: #999;
      
      .suggestions-close {
        width: 20px;
        height: 20px;
        border: none;
        background: transparent;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
        &:hover { color: #333; }
      }
    }

    .suggestion-item {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 12px 14px;
      cursor: pointer;
      transition: background 0.2s ease;
      
      &:hover { background: #f5f7fa; }
      
      .suggestion-icon { color: #FF8A5B; margin-top: 2px; flex-shrink: 0; }
      
      .suggestion-content {
        flex: 1;
        min-width: 0;
        
        .suggestion-title {
          font-size: 14px;
          color: #333;
          margin-bottom: 2px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .suggestion-address {
          font-size: 12px;
          color: #999;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
  
  .get-location-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    font-size: 13px;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:hover:not(:disabled) { border-color: #FF8A5B; color: #FF8A5B; }
    &:disabled { opacity: 0.6; cursor: not-allowed; }
    
    .el-icon { font-size: 14px; }
  }
  
  .map-wrapper {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #eee;
  }
  
  .map-container {
    width: 100%;
    height: 280px;
  }
  
  .map-loading-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    align-items: center;
    justify-content: center;
    
    .loading-content {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #FF8A5B;
      font-size: 14px;
    }
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-spinner-small {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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

// 对话框动画
.address-modal-enter-active,
.address-modal-leave-active {
  transition: all 0.3s ease;
}

.address-modal-enter-from,
.address-modal-leave-to {
  opacity: 0;
  
  .address-modal {
    transform: scale(0.9) translateY(-20px);
  }
}

// 密码修改对话框样式
.password-modal-overlay {
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

.password-modal {
  width: 420px;
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
      
      .el-icon { color: #FF8A5B; }
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
      &:hover:not(:disabled) { background: #f5f7fa; }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45); }
      &:disabled { opacity: 0.7; cursor: not-allowed; }
    }
  }
  
  .form-item {
    margin-bottom: 20px;
    &:last-child { margin-bottom: 0; }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
  }

  .form-input {
    width: 100%;
    height: 44px;
    padding: 0 44px 0 14px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    font-size: 14px;
    color: #333;
    outline: none;
    transition: all 0.2s ease;
    box-sizing: border-box;
    
    &::placeholder { color: #c0c4cc; }
    &:hover { border-color: #c0c4cc; }
    &:focus { border-color: #FF8A5B; box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1); }
    &.is-error { border-color: #f56c6c; }
    &.is-error:focus { box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1); }
  }
  
  .password-input-wrapper {
    position: relative;
    
    .toggle-password {
      position: absolute;
      right: 12px;
      top: 50%;
      transform: translateY(-50%);
      background: none;
      border: none;
      cursor: pointer;
      color: #909399;
      padding: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: color 0.2s ease;
      
      &:hover { color: #FF8A5B; }
    }
  }
  
  .error-text {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #f56c6c;
  }
  
  .password-tips {
    margin-top: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .tip-title {
      font-size: 13px;
      font-weight: 500;
      color: #606266;
      margin: 0 0 8px 0;
    }
    
    .tip-list {
      margin: 0;
      padding-left: 18px;
      
      li {
        font-size: 12px;
        color: #909399;
        line-height: 1.8;
      }
    }
  }
}

// 密码对话框动画
.password-modal-enter-active,
.password-modal-leave-active {
  transition: all 0.3s ease;
}

.password-modal-enter-from,
.password-modal-leave-to {
  opacity: 0;
  
  .password-modal {
    transform: scale(0.9) translateY(-20px);
  }
}

// 手机修改对话框样式
.phone-modal-overlay {
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

.phone-modal {
  width: 420px;
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
      
      .el-icon { color: #FF8A5B; }
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
      &:hover:not(:disabled) { background: #f5f7fa; }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45); }
      &:disabled { opacity: 0.7; cursor: not-allowed; }
    }
  }
  
  .current-phone-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;
    
    .info-label {
      font-size: 14px;
      color: #606266;
    }
    
    .info-value {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
  }
  
  .form-item {
    margin-bottom: 20px;
    &:last-child { margin-bottom: 0; }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
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
    
    &::placeholder { color: #c0c4cc; }
    &:hover { border-color: #c0c4cc; }
    &:focus { border-color: #FF8A5B; box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1); }
    &.is-error { border-color: #f56c6c; }
    &.is-error:focus { box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1); }
  }
  
  .error-text {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #f56c6c;
  }
  
  .phone-tips {
    margin-top: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .tip-title {
      font-size: 13px;
      font-weight: 500;
      color: #606266;
      margin: 0 0 8px 0;
    }
    
    .tip-list {
      margin: 0;
      padding-left: 18px;
      
      li {
        font-size: 12px;
        color: #909399;
        line-height: 1.8;
      }
    }
  }
}

// 手机对话框动画
.phone-modal-enter-active,
.phone-modal-leave-active {
  transition: all 0.3s ease;
}

.phone-modal-enter-from,
.phone-modal-leave-to {
  opacity: 0;
  
  .phone-modal {
    transform: scale(0.9) translateY(-20px);
  }
}

// 邮箱修改对话框样式
.email-modal-overlay {
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

.email-modal {
  width: 420px;
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
      
      .el-icon { color: #FF8A5B; }
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
      &:hover:not(:disabled) { background: #f5f7fa; }
      &:disabled { opacity: 0.6; cursor: not-allowed; }
    }
    
    .btn-confirm {
      background: linear-gradient(135deg, #FF8A5B 0%, #ff6b3d 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);
      &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45); }
      &:disabled { opacity: 0.7; cursor: not-allowed; }
    }
  }
  
  .current-email-info {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;
    
    .info-label {
      font-size: 14px;
      color: #606266;
    }
    
    .info-value {
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }
  }
  
  .form-item {
    margin-bottom: 20px;
    &:last-child { margin-bottom: 0; }
  }
  
  .form-label {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #333;
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
    
    &::placeholder { color: #c0c4cc; }
    &:hover { border-color: #c0c4cc; }
    &:focus { border-color: #FF8A5B; box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.1); }
    &.is-error { border-color: #f56c6c; }
    &.is-error:focus { box-shadow: 0 0 0 3px rgba(245, 108, 108, 0.1); }
  }
  
  .error-text {
    display: block;
    margin-top: 6px;
    font-size: 12px;
    color: #f56c6c;
  }
  
  .email-tips {
    margin-top: 16px;
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    
    .tip-title {
      font-size: 13px;
      font-weight: 500;
      color: #606266;
      margin: 0 0 8px 0;
    }
    
    .tip-list {
      margin: 0;
      padding-left: 18px;
      
      li {
        font-size: 12px;
        color: #909399;
        line-height: 1.8;
      }
    }
  }
}

// 邮箱对话框动画
.email-modal-enter-active,
.email-modal-leave-active {
  transition: all 0.3s ease;
}

.email-modal-enter-from,
.email-modal-leave-to {
  opacity: 0;
  
  .email-modal {
    transform: scale(0.9) translateY(-20px);
  }
}
</style>

<!-- 全局样式 - 注销账号确认对话框 -->
<style lang="scss">
.delete-account-confirm {
  .el-message-box__message {
    padding: 0;
  }
  
  .el-message-box__btns {
    padding-top: 20px;
    
    .el-button--default {
      &:hover {
        color: var(--color-primary);
        border-color: var(--color-primary);
      }
    }
    
    .el-button--primary {
      background: #f56c6c;
      border-color: #f56c6c;
      
      &:hover {
        background: #f78989;
        border-color: #f78989;
      }
    }
  }
}
</style>
