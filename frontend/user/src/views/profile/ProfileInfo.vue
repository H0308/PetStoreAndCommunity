<script setup>
import { ref, onMounted, watch, inject } from 'vue'
import { Camera, Check, Warning, User, Loading, Male, Female, Lock, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { USER_API, request } from '@/api/config.js'

const props = defineProps({
  userInfo: { type: Object, default: () => null }
})

// 注入全局用户信息更新方法
const updateUserInfo = inject('updateUserInfo', () => {})

// 表单数据
const formData = ref({
  avatar: '',
  username: '',
  gender: 3,
  profile: '',
  roleId: 1,
  banFlag: 0,
  realNameAuthFlag: 0
})

// 原始数据（用于对比是否修改）
const originalData = ref({
  avatar: '',
  username: '',
  gender: 3,
  profile: ''
})

// 性别选项
const genderOptions = [
  { value: 1, label: '男', icon: 'male' },
  { value: 2, label: '女', icon: 'female' },
  { value: 3, label: '保密', icon: 'secret' }
]

// 加载状态
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)

// 实名认证弹窗
const showAuthDialog = ref(false)
const authForm = ref({ realName: '', idCard: '' })
const authSubmitting = ref(false)
const idCardError = ref('')

// 身份证号校验
const validateIdCard = (idCard) => {
  if (!idCard) {
    return { valid: false, message: '' }
  }
  
  // 基本格式校验：15位或18位
  const reg = /(^\d{15}$)|(^\d{17}(\d|X|x)$)/
  if (!reg.test(idCard)) {
    return { valid: false, message: '身份证号格式不正确' }
  }
  
  // 18位身份证校验
  if (idCard.length === 18) {
    // 校验出生日期
    const year = parseInt(idCard.substring(6, 10))
    const month = parseInt(idCard.substring(10, 12))
    const day = parseInt(idCard.substring(12, 14))
    const birthDate = new Date(year, month - 1, day)
    
    if (birthDate.getFullYear() !== year || 
        birthDate.getMonth() !== month - 1 || 
        birthDate.getDate() !== day) {
      return { valid: false, message: '出生日期不合法' }
    }
    
    // 校验码验证
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    let sum = 0
    for (let i = 0; i < 17; i++) {
      sum += parseInt(idCard[i]) * weights[i]
    }
    const checkCode = checkCodes[sum % 11]
    if (idCard[17].toUpperCase() !== checkCode) {
      return { valid: false, message: '身份证号校验码错误' }
    }
  }
  
  return { valid: true, message: '' }
}

// 身份证输入处理
const handleIdCardInput = () => {
  const result = validateIdCard(authForm.value.idCard)
  idCardError.value = result.message
}

// 身份证失焦校验
const handleIdCardBlur = () => {
  if (authForm.value.idCard) {
    const result = validateIdCard(authForm.value.idCard)
    idCardError.value = result.message
  }
}

// 获取当前用户ID
const getUserId = () => {
  let userId = props.userInfo?.userId
  if (!userId) {
    try {
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        const parsed = JSON.parse(storedUser)
        userId = parsed.userId
      }
    } catch (e) {
      console.error('解析本地用户信息失败:', e)
    }
  }
  return userId
}

// 获取用户信息
const fetchUserInfo = async () => {
  // 优先使用 props.userInfo.userId，如果没有则尝试从 localStorage 获取
  let userId = props.userInfo?.userId
  if (!userId) {
    try {
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        const parsed = JSON.parse(storedUser)
        userId = parsed.userId
      }
    } catch (e) {
      console.error('解析本地用户信息失败:', e)
    }
  }
  
  if (!userId) {
    console.warn('无法获取用户ID')
    return
  }
  
  loading.value = true
  try {
    const response = await request(`${USER_API.GET_USER_INFO}?userId=${userId}`)
    const result = await response.json()
    
    console.log('获取用户信息响应:', result) // 调试日志
    
    if (result.code === 0 && result.data) {
      const data = result.data
      formData.value = {
        avatar: data.avatarUrl || '',
        username: data.username || '',
        gender: data.gender ?? 3,
        profile: data.profile || '',
        roleId: data.roleId ?? 1,
        banFlag: data.banFlag ?? 0,
        realNameAuthFlag: data.realNameAuthFlag ?? 0
      }
      originalData.value = {
        avatar: data.avatarUrl || '',
        username: data.username || '',
        gender: data.gender ?? 3,
        profile: data.profile || ''
      }
      // 同步最新头像和昵称到导航栏/侧边栏
      updateUserInfo({
        avatar: data.avatarUrl || '',
        nickname: data.username || ''
      })
    } else {
      console.error('获取用户信息失败:', result.message)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 保存个人资料（仅保存用户名、性别、简介）
const handleSave = async () => {
  const userId = getUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  
  const trimmedUsername = formData.value.username?.trim() || ''
  const trimmedProfile = formData.value.profile?.trim() || ''
  
  if (!trimmedUsername) {
    ElMessage.warning('用户名不能为空')
    return
  }
  if (formData.value.profile && !trimmedProfile) {
    ElMessage.warning('个人简介不能只包含空白字符')
    return
  }
  
  const changedData = { userId }
  let hasChanges = false
  
  if (trimmedUsername !== (originalData.value.username?.trim() || '')) {
    changedData.username = trimmedUsername
    hasChanges = true
  }
  if (formData.value.gender !== originalData.value.gender) {
    changedData.gender = formData.value.gender
    hasChanges = true
  }
  if (trimmedProfile !== (originalData.value.profile?.trim() || '')) {
    changedData.profile = trimmedProfile
    hasChanges = true
  }

  if (!hasChanges) {
    ElMessage.info('没有修改任何内容')
    return
  }

  saving.value = true
  try {
    const response = await request(USER_API.CHANGE_USER_INFO, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(changedData)
    })
    const result = await response.json()
    
    if (result.code !== 0) {
      ElMessage.error(result.message || '保存失败')
      return
    }

    ElMessage.success('保存成功')
    await fetchUserInfo()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 选择头像后立即上传并同步
const handleAvatarUpload = async (uploadFile) => {
  const file = uploadFile.raw || uploadFile
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }

  const userId = getUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return false
  }

  // 本地预览
  formData.value.avatar = URL.createObjectURL(file)
  uploading.value = true

  try {
    const formDataObj = new FormData()
    formDataObj.append('file', file)

    const response = await request(`${USER_API.UPLOAD_AVATAR}?userId=${userId}`, {
      method: 'POST',
      headers: {},
      body: formDataObj
    })
    const result = await response.json()

    if (result.code !== 0) {
      ElMessage.error(result.message || '头像上传失败')
      // 恢复原头像
      formData.value.avatar = originalData.value.avatar
      return false
    }

    // 用后端返回的真实 URL 替换本地预览
    const newAvatarUrl = result.data || formData.value.avatar
    formData.value.avatar = newAvatarUrl
    originalData.value.avatar = newAvatarUrl
    ElMessage.success('头像更新成功')
    // 立即同步到导航栏和侧边栏
    updateUserInfo({ avatar: newAvatarUrl })
  } catch (error) {
    ElMessage.error('头像上传失败')
    formData.value.avatar = originalData.value.avatar
  } finally {
    uploading.value = false
  }

  return false
}

// 提交实名认证
const submitRealNameAuth = async () => {
  // 去除空白字符
  const trimmedRealName = authForm.value.realName?.trim() || ''
  const trimmedIdCard = authForm.value.idCard?.trim() || ''
  
  // 校验是否只包含空白字符
  if (authForm.value.realName && !trimmedRealName) {
    ElMessage.warning('真实姓名不能只包含空白字符')
    return
  }
  if (authForm.value.idCard && !trimmedIdCard) {
    ElMessage.warning('身份证号不能只包含空白字符')
    return
  }
  
  if (!trimmedRealName) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!trimmedIdCard) {
    ElMessage.warning('请输入身份证号')
    return
  }
  
  // 使用完整校验
  const idCardResult = validateIdCard(trimmedIdCard)
  if (!idCardResult.valid) {
    idCardError.value = idCardResult.message
    ElMessage.warning(idCardResult.message || '请输入正确的身份证号')
    return
  }

  const userId = getUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  authSubmitting.value = true
  try {
    const response = await request(USER_API.REAL_NAME_AUTH, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        userId,
        realName: trimmedRealName,
        idCard: trimmedIdCard
      })
    })
    const result = await response.json()
    
    if (result.code === 0) {
      ElMessage.success('实名认证成功')
      formData.value.realNameAuthFlag = 1
      showAuthDialog.value = false
    } else {
      ElMessage.error(result.message || '实名认证失败')
    }
  } catch (error) {
    ElMessage.error('实名认证失败')
  } finally {
    authSubmitting.value = false
  }
}

const openAuthDialog = () => {
  authForm.value = { realName: '', idCard: '' }
  idCardError.value = ''
  showAuthDialog.value = true
}

const getRoleName = (roleId) => roleId === 0 ? '管理员' : '普通用户'

watch(() => props.userInfo, (newVal) => {
  if (newVal?.userId) fetchUserInfo()
}, { immediate: true })

onMounted(() => {
  // 组件挂载时立即尝试获取用户信息
  fetchUserInfo()
})
</script>

<template>
  <div class="profile-info" v-loading="loading">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">编辑个人资料</h1>
      <p class="page-subtitle">完善信息让大家更了解你</p>
    </div>
    
    <!-- 状态概览栏 -->
    <div class="stats-bar">
      <div class="stat-item">
        <div class="stat-icon" :class="formData.realNameAuthFlag === 1 ? 'verified' : 'unverified'">
          <el-icon v-if="formData.realNameAuthFlag === 1"><Check /></el-icon>
          <el-icon v-else><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">实名认证</span>
          <span class="stat-value" :class="formData.realNameAuthFlag === 1 ? 'verified' : 'unverified'">
            {{ formData.realNameAuthFlag === 1 ? '已认证' : '未认证' }}
          </span>
        </div>
        <button 
          v-if="formData.realNameAuthFlag !== 1" 
          class="stat-action"
          @click="openAuthDialog"
        >
          去认证
        </button>
      </div>
      
      <div class="stat-divider"></div>
      
      <div class="stat-item">
        <div class="stat-icon role">
          <svg class="custom-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">账号角色</span>
          <span class="stat-value role">{{ getRoleName(formData.roleId) }}</span>
        </div>
      </div>
      
      <div class="stat-divider"></div>
      
      <div class="stat-item">
        <div class="stat-icon" :class="formData.banFlag === 0 ? 'normal' : 'banned'">
          <el-icon v-if="formData.banFlag === 0"><Check /></el-icon>
          <el-icon v-else><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">账号状态</span>
          <span class="stat-value" :class="formData.banFlag === 0 ? 'normal' : 'banned'">
            {{ formData.banFlag === 0 ? '正常' : '已禁言' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 核心表单区 -->
    <div class="form-container">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="handleAvatarUpload"
          accept="image/*"
          :disabled="uploading"
        >
          <div class="avatar-wrapper" :class="{ uploading }">
            <img v-if="formData.avatar" :src="formData.avatar" class="avatar-img" />
            <div v-else class="avatar-placeholder">
              <el-icon><User /></el-icon>
            </div>
            <div class="avatar-overlay">
              <el-icon v-if="!uploading"><Camera /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
            </div>
          </div>
        </el-upload>
        <p class="avatar-tip">
          <span v-if="uploading" class="avatar-uploading-tip">上传中...</span>
          <span v-else>点击更换头像</span>
        </p>
      </div>
      
      <!-- 表单输入区 -->
      <div class="form-fields">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="input-wrapper">
            <input 
              v-model="formData.username" 
              type="text"
              class="form-input"
              placeholder="给自己取个好听的名字吧"
              maxlength="20"
            />
            <span class="input-count">{{ formData.username?.length || 0 }}/20</span>
          </div>
        </div>
        
        <div class="form-group">
          <label class="form-label">性别</label>
          <div class="gender-selector">
            <button 
              v-for="option in genderOptions" 
              :key="option.value"
              class="gender-option"
              :class="{ active: formData.gender === option.value }"
              @click="formData.gender = option.value"
            >
              <span class="gender-icon">
                <el-icon v-if="option.icon === 'male'"><Male /></el-icon>
                <el-icon v-else-if="option.icon === 'female'"><Female /></el-icon>
                <svg v-else class="custom-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v4h-2zm0 6h2v2h-2z"/>
                </svg>
              </span>
              <span class="gender-text">{{ option.label }}</span>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">个人简介</label>
          <div class="textarea-wrapper">
            <textarea 
              v-model="formData.profile"
              class="form-textarea"
              placeholder="介绍一下自己吧，让大家更了解你~"
              maxlength="200"
              rows="4"
            ></textarea>
            <span class="textarea-count">{{ formData.profile?.length || 0 }}/200</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 底部操作区 -->
    <div class="action-bar">
      <button class="btn-primary" @click="handleSave" :disabled="saving">
        <span v-if="saving" class="btn-loading"></span>
        {{ saving ? '保存中...' : '保存修改' }}
      </button>
    </div>
    
    <!-- 实名认证弹窗 -->
    <Teleport to="body">
      <Transition name="auth-modal">
        <div v-if="showAuthDialog" class="auth-modal-overlay">
          <div class="auth-modal">
            <!-- 模态框头部 -->
            <div class="modal-header">
              <h3 class="modal-title">
                <el-icon><Lock /></el-icon>
                实名认证
              </h3>
              <button class="modal-close" :disabled="authSubmitting" @click="showAuthDialog = false">
                <el-icon><Close /></el-icon>
              </button>
            </div>
            
            <!-- 模态框内容 -->
            <div class="modal-body">
              <div class="auth-notice">
                <el-icon><Warning /></el-icon>
                <span>认证后不可修改，请确保信息准确</span>
              </div>
              
              <div class="form-item">
                <label class="form-label">
                  <span class="required">*</span>真实姓名
                </label>
                <input 
                  v-model="authForm.realName" 
                  type="text"
                  class="form-input"
                  placeholder="请输入真实姓名"
                  maxlength="20"
                />
              </div>
              
              <div class="form-item">
                <label class="form-label">
                  <span class="required">*</span>身份证号
                </label>
                <input 
                  v-model="authForm.idCard" 
                  type="text"
                  class="form-input"
                  :class="{ 'is-error': idCardError }"
                  placeholder="请输入18位身份证号"
                  maxlength="18"
                  @input="handleIdCardInput"
                  @blur="handleIdCardBlur"
                />
                <div v-if="idCardError" class="form-error">
                  <el-icon><Warning /></el-icon>
                  {{ idCardError }}
                </div>
              </div>
            </div>
            
            <!-- 模态框底部 -->
            <div class="modal-footer">
              <button class="btn-cancel" :disabled="authSubmitting" @click="showAuthDialog = false">取消</button>
              <button class="btn-confirm" :disabled="authSubmitting" @click="submitRealNameAuth">
                <span v-if="authSubmitting" class="loading-spinner"></span>
                {{ authSubmitting ? '提交中...' : '提交认证' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped lang="scss">
.profile-info {
  padding: var(--spacing-lg);
}

/* 页面标题 */
.page-header {
  margin-bottom: var(--spacing-xl);
  
  .page-title {
    font-size: var(--font-size-2xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin: 0 0 var(--spacing-xs) 0;
  }
  
  .page-subtitle {
    font-size: var(--font-size-base);
    color: var(--color-text-tertiary);
    margin: 0;
  }
}

/* 状态概览栏 */
.stats-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--color-bg-base);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-2xl);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex: 1;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  
  &.verified, &.normal {
    background: rgba(47, 143, 106, 0.1);
    color: var(--color-success);
  }
  
  &.unverified, &.banned {
    background: rgba(239, 83, 80, 0.1);
    color: var(--color-danger);
  }
  
  &.role {
    background: var(--color-secondary-light);
    color: var(--color-primary);
  }
  
  .custom-icon {
    width: 18px;
    height: 18px;
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  
  .stat-label {
    font-size: var(--font-size-xs);
    color: var(--color-text-tertiary);
  }
  
  .stat-value {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    
    &.verified, &.normal { color: var(--color-success); }
    &.unverified, &.banned { color: var(--color-danger); }
    &.role { color: var(--color-primary); }
  }
}

.stat-action {
  margin-left: auto;
  padding: 6px 14px;
  font-size: var(--font-size-xs);
  color: var(--color-primary);
  background: transparent;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-circle);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    background: var(--color-primary);
    color: white;
  }
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--color-border-light);
}

/* 表单容器 */
.form-container {
  display: flex;
  gap: var(--spacing-3xl);
  margin-bottom: var(--spacing-2xl);
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: none;
    background: transparent;
  }
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 3px solid var(--color-primary);
  padding: 3px;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.02);
    box-shadow: 0 8px 24px rgba(255, 138, 91, 0.25);
    
    .avatar-overlay {
      opacity: 1;
    }
  }
  
  &.uploading {
    opacity: 0.7;
    pointer-events: none;
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-secondary-light), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  
  .el-icon {
    font-size: 48px;
  }
}

.avatar-overlay {
  position: absolute;
  top: 3px;
  left: 3px;
  right: 3px;
  bottom: 3px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  
  .el-icon {
    font-size: 28px;
    color: white;
  }
}

.avatar-tip {
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  
  .avatar-uploading-tip {
    color: var(--color-primary);
    font-weight: var(--font-weight-medium);
  }
}

/* 表单输入区 */
.form-fields {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.form-group {
  .form-label {
    display: block;
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-semibold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-sm);
  }
}

.input-wrapper, .textarea-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-base);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  background: #F7F7F7;
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  
  &::placeholder {
    color: var(--color-text-placeholder);
  }
  
  &:focus {
    background: white;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(255, 138, 91, 0.1);
  }
}

.input-count {
  position: absolute;
  right: var(--spacing-base);
  top: 50%;
  transform: translateY(-50%);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* 性别选择器 */
.gender-selector {
  display: flex;
  gap: var(--spacing-sm);
}

.gender-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-lg);
  background: #F7F7F7;
  border: 2px solid transparent;
  border-radius: var(--radius-circle);
  cursor: pointer;
  transition: all 0.3s ease;
  
  .gender-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: var(--color-text-secondary);
    
    .custom-icon {
      width: 16px;
      height: 16px;
    }
  }
  
  .gender-text {
    font-size: var(--font-size-sm);
    color: var(--color-text-secondary);
  }
  
  &:hover {
    background: var(--color-secondary-light);
  }
  
  &.active {
    background: var(--color-primary);
    border-color: var(--color-primary);
    
    .gender-icon {
      color: white;
    }
    
    .gender-text {
      color: white;
    }
  }
}

/* 文本域 */
.form-textarea {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-base);
  font-size: var(--font-size-base);
  font-family: inherit;
  color: var(--color-text-primary);
  background: #F7F7F7;
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  outline: none;
  resize: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
  
  &::placeholder {
    color: var(--color-text-placeholder);
  }
  
  &:focus {
    background: white;
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(255, 138, 91, 0.1);
  }
}

.textarea-count {
  position: absolute;
  right: var(--spacing-base);
  bottom: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* 底部操作区 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-base);
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border-light);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-md) var(--spacing-2xl);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: white;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border: none;
  border-radius: var(--radius-circle);
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(255, 138, 91, 0.3);
  
  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 138, 91, 0.4);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.btn-ghost {
  padding: var(--spacing-md) var(--spacing-xl);
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  background: transparent;
  border: 1px solid var(--color-border-base);
  border-radius: var(--radius-circle);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

.btn-loading {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 实名认证模态框样式 */
.auth-modal-overlay {
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

.auth-modal {
  width: 420px;
  max-width: 100%;
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
      
      .el-icon {
        color: var(--color-primary);
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
  }
  
  .auth-notice {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #fdf6ec;
    border-radius: 8px;
    margin-bottom: 20px;
    color: #e6a23c;
    font-size: 13px;
    
    .el-icon {
      font-size: 16px;
      flex-shrink: 0;
    }
  }
  
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
  }
  
  .form-input {
    width: 100%;
    height: 44px;
    padding: 0 16px;
    font-size: 14px;
    color: #333;
    background: #f7f7f7;
    border: 2px solid transparent;
    border-radius: 10px;
    outline: none;
    transition: all 0.25s ease;
    box-sizing: border-box;
    
    &::placeholder {
      color: #c0c4cc;
    }
    
    &:focus {
      background: #fff;
      border-color: var(--color-primary);
      box-shadow: 0 0 0 4px rgba(255, 138, 91, 0.1);
    }
    
    &.is-error {
      border-color: #F56C6C;
      background: #fff;
      
      &:focus {
        box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.1);
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
  
  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 16px 24px 24px;
    border-top: 1px solid #eee;
    
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
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
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
        transform: none;
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

/* 模态框过渡动画 */
.auth-modal-enter-active,
.auth-modal-leave-active {
  transition: opacity 0.3s ease;
  
  .auth-modal {
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
}

.auth-modal-enter-from,
.auth-modal-leave-to {
  opacity: 0;
  
  .auth-modal {
    transform: scale(0.9) translateY(-20px);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-bar {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-base);
  }
  
  .stat-divider {
    width: 100%;
    height: 1px;
  }
  
  .form-container {
    flex-direction: column;
    align-items: center;
  }
  
  .form-fields {
    width: 100%;
  }
  
  .auth-modal {
    width: 100%;
    margin: 0 16px;
  }
}
</style>
