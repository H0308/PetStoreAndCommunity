<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <!-- 左侧用户卡片 -->
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-9 pb-6 overflow-hidden text-center">
          <img
            v-if="formData.avatar"
            class="w-20 h-20 mx-auto object-cover border-2 border-white rounded-full shadow"
            :src="formData.avatar"
          />
          <div
            v-else
            class="w-20 h-20 mx-auto border-2 border-white rounded-full bg-gray-200 flex items-center justify-center shadow"
          >
            <span class="text-2xl text-gray-500">{{ formData.username?.charAt(0) || 'U' }}</span>
          </div>
          <h2 class="mt-5 text-xl font-normal">{{ formData.username || '管理员' }}</h2>
          <p class="mt-5 text-sm text-g-500">{{ formData.profile || '这个人很懒，什么都没写~' }}</p>

          <div class="w-75 mx-auto mt-7.5 text-left">
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:shield-user-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ getRoleName(formData.roleId) }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:user-3-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ getGenderName(formData.gender) }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:verified-badge-line" class="text-g-700" />
              <span class="ml-2 text-sm">{{ formData.realNameAuthFlag === 1 ? '已实名认证' : '未实名认证' }}</span>
            </div>
            <div class="mt-2.5">
              <ArtSvgIcon icon="ri:checkbox-circle-line" class="text-g-700" />
              <span class="ml-2 text-sm">账号状态：{{ formData.banFlag === 0 ? '正常' : '已禁言' }}</span>
            </div>
          </div>
        </div>

        <!-- 账号安全 -->
        <div class="art-card-sm mt-5 p-5">
          <h3 class="text-base font-medium mb-4">账号安全</h3>
          
          <!-- 实名认证 -->
          <div class="security-item" @click="openAuthDialog">
            <div class="security-left">
              <ArtSvgIcon icon="ri:shield-check-line" class="security-icon" />
              <div class="security-info">
                <span class="security-title">实名认证</span>
                <span class="security-desc" :class="{ 'text-green-500': formData.realNameAuthFlag === 1 }">
                  {{ formData.realNameAuthFlag === 1 ? '已认证' : '未认证' }}
                </span>
              </div>
            </div>
            <ArtSvgIcon v-if="formData.realNameAuthFlag !== 1" icon="ri:arrow-right-s-line" class="text-g-400" />
          </div>

          <!-- 绑定手机 -->
          <div class="security-item" @click="openPhoneDialog">
            <div class="security-left">
              <ArtSvgIcon icon="ri:smartphone-line" class="security-icon" />
              <div class="security-info">
                <span class="security-title">绑定手机</span>
                <span class="security-desc" :class="{ 'text-green-500': phoneNumber }">
                  {{ phoneNumber ? maskPhone(phoneNumber) : '未绑定' }}
                </span>
              </div>
            </div>
            <ArtSvgIcon icon="ri:arrow-right-s-line" class="text-g-400" />
          </div>

          <!-- 绑定邮箱 -->
          <div class="security-item" @click="openEmailDialog">
            <div class="security-left">
              <ArtSvgIcon icon="ri:mail-line" class="security-icon" />
              <div class="security-info">
                <span class="security-title">绑定邮箱</span>
                <span class="security-desc" :class="{ 'text-green-500': emailAddress }">
                  {{ emailAddress || '未绑定' }}
                </span>
              </div>
            </div>
            <ArtSvgIcon icon="ri:arrow-right-s-line" class="text-g-400" />
          </div>
        </div>
      </div>

      <!-- 右侧表单区域 -->
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <!-- 基本设置 -->
        <div class="art-card-sm">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">基本设置</h1>

          <ElForm
            ref="formRef"
            :model="formData"
            :rules="formRules"
            class="box-border p-5"
            label-width="86px"
            label-position="top"
          >
            <!-- 头像上传 -->
            <ElFormItem label="头像">
              <ElUpload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="handleAvatarUpload"
                accept="image/*"
              >
                <img v-if="formData.avatar" :src="formData.avatar" class="avatar-img" />
                <div v-else class="avatar-placeholder">
                  <ArtSvgIcon icon="ri:user-line" :size="32" />
                </div>
                <div class="avatar-overlay">
                  <ArtSvgIcon v-if="!uploading" icon="ri:camera-line" :size="20" />
                  <ArtSvgIcon v-else icon="ri:loader-4-line" :size="20" class="animate-spin" />
                </div>
              </ElUpload>
              <span class="ml-4 text-sm text-g-500">点击更换头像，支持 jpg、png 格式，大小不超过 2MB</span>
            </ElFormItem>

            <ElRow :gutter="20">
              <ElCol :span="12">
                <ElFormItem label="用户名" prop="username">
                  <ElInput v-model.trim="formData.username" :disabled="!isEdit" maxlength="20" show-word-limit />
                </ElFormItem>
              </ElCol>
              <ElCol :span="12">
                <ElFormItem label="性别" prop="gender">
                  <ElSelect v-model="formData.gender" placeholder="请选择性别" :disabled="!isEdit" class="w-full">
                    <ElOption
                      v-for="item in genderOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                </ElFormItem>
              </ElCol>
            </ElRow>

            <ElFormItem label="个人简介" prop="profile">
              <ElInput
                type="textarea"
                :rows="4"
                v-model.trim="formData.profile"
                :disabled="!isEdit"
                maxlength="200"
                show-word-limit
                placeholder="介绍一下自己吧~"
              />
            </ElFormItem>

            <div class="flex-c justify-end">
              <ElButton v-if="isEdit" @click="handleCancel" v-ripple>取消</ElButton>
              <ElButton type="primary" @click="handleEditOrSave" :loading="saving" v-ripple>
                {{ isEdit ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>

        <!-- 更改密码 -->
        <div class="art-card-sm my-5">
          <h1 class="p-4 text-xl font-normal border-b border-g-300">更改密码</h1>

          <ElForm
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdFormRules"
            class="box-border p-5"
            label-width="86px"
            label-position="top"
          >
            <ElFormItem label="当前密码" prop="oldPassword">
              <ElInput
                v-model="pwdForm.oldPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入当前密码"
              />
            </ElFormItem>

            <ElFormItem label="新密码" prop="newPassword">
              <ElInput
                v-model="pwdForm.newPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请输入新密码"
              />
            </ElFormItem>

            <ElFormItem label="确认新密码" prop="confirmPassword">
              <ElInput
                v-model="pwdForm.confirmPassword"
                type="password"
                :disabled="!isEditPwd"
                show-password
                placeholder="请再次输入新密码"
              />
            </ElFormItem>

            <div class="flex-c justify-end">
              <ElButton v-if="isEditPwd" @click="handleCancelPwd" v-ripple>取消</ElButton>
              <ElButton type="primary" @click="handleEditOrSavePwd" :loading="savingPwd" v-ripple>
                {{ isEditPwd ? '保存' : '编辑' }}
              </ElButton>
            </div>
          </ElForm>
        </div>
      </div>
    </div>

    <!-- 实名认证弹窗 -->
    <ElDialog
      v-model="authDialogVisible"
      title="实名认证"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="mb-4 p-3 bg-yellow-50 rounded text-sm text-yellow-600">
        <ArtSvgIcon icon="ri:error-warning-line" class="mr-1" />
        认证后不可修改，请确保信息准确
      </div>
      <ElForm ref="authFormRef" :model="authForm" :rules="authFormRules" label-width="80px">
        <ElFormItem label="真实姓名" prop="realName">
          <ElInput v-model.trim="authForm.realName" placeholder="请输入真实姓名" maxlength="20" />
        </ElFormItem>
        <ElFormItem label="身份证号" prop="idCard">
          <ElInput v-model.trim="authForm.idCard" placeholder="请输入18位身份证号" maxlength="18" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="authDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitAuth" :loading="authSubmitting">提交认证</ElButton>
      </template>
    </ElDialog>

    <!-- 绑定手机弹窗 -->
    <ElDialog
      v-model="phoneDialogVisible"
      title="绑定手机"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ElForm ref="phoneFormRef" :model="phoneForm" :rules="phoneFormRules" label-width="80px">
        <ElFormItem label="手机号" prop="phone">
          <ElInput v-model.trim="phoneForm.phone" placeholder="请输入手机号" maxlength="11" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="phoneDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitPhone" :loading="phoneSubmitting">确定</ElButton>
      </template>
    </ElDialog>

    <!-- 绑定邮箱弹窗 -->
    <ElDialog
      v-model="emailDialogVisible"
      title="绑定邮箱"
      width="450px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <ElForm ref="emailFormRef" :model="emailForm" :rules="emailFormRules" label-width="80px">
        <ElFormItem label="邮箱" prop="email">
          <ElInput v-model.trim="emailForm.email" placeholder="请输入邮箱地址" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="emailDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitEmail" :loading="emailSubmitting">确定</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { 
  getUserInfo, changeUserInfo, uploadAvatar, changePassword,
  realNameAuth, getPhone, changePhone, getEmail, changeEmail
} from '@/api/profile'
import { ElMessage, type FormInstance, type FormRules, type UploadRawFile } from 'element-plus'

defineOptions({ name: 'UserCenter' })

const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const saving = ref(false)
const savingPwd = ref(false)
const uploading = ref(false)
const authSubmitting = ref(false)
const phoneSubmitting = ref(false)
const emailSubmitting = ref(false)

// 编辑状态
const isEdit = ref(false)
const isEditPwd = ref(false)

// 弹窗状态
const authDialogVisible = ref(false)
const phoneDialogVisible = ref(false)
const emailDialogVisible = ref(false)

// 表单引用
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const authFormRef = ref<FormInstance>()
const phoneFormRef = ref<FormInstance>()
const emailFormRef = ref<FormInstance>()

// 手机号和邮箱
const phoneNumber = ref('')
const emailAddress = ref('')

// 原始数据（用于取消编辑时恢复）
const originalData = ref({
  username: '',
  gender: 3,
  profile: ''
})

// 用户信息表单
const formData = reactive({
  avatar: '',
  username: '',
  gender: 3,
  profile: '',
  roleId: 0,
  banFlag: 0,
  realNameAuthFlag: 0
})

// 密码表单
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 实名认证表单
const authForm = reactive({
  realName: '',
  idCard: ''
})

// 手机表单
const phoneForm = reactive({
  phone: ''
})

// 邮箱表单
const emailForm = reactive({
  email: ''
})

// 性别选项
const genderOptions = [
  { value: 1, label: '男' },
  { value: 2, label: '女' },
  { value: 3, label: '保密' }
]

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value || value.trim() === '') {
          callback(new Error('用户名不能为空或纯空格'))
        } else if (value !== value.trim()) {
          callback(new Error('用户名首尾不能包含空格'))
        } else if (value.trim().length < 2 || value.trim().length > 20) {
          callback(new Error('用户名长度为2-20个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  profile: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value && value !== value.trim()) {
          callback(new Error('个人简介首尾不能包含空格'))
        } else if (value && value.trim().length > 200) {
          callback(new Error('个人简介不能超过200个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 密码表单验证规则
const pwdFormRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 实名认证表单验证规则
const authFormRules: FormRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value || value.trim() === '') {
          callback(new Error('真实姓名不能为空'))
        } else if (value !== value.trim()) {
          callback(new Error('真实姓名首尾不能包含空格'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        const reg = /(^\d{15}$)|(^\d{17}(\d|X|x)$)/
        if (!value || !reg.test(value)) {
          callback(new Error('请输入正确的身份证号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 手机表单验证规则
const phoneFormRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        const reg = /^1[3-9]\d{9}$/
        if (!value || !reg.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 邮箱表单验证规则
const emailFormRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

// 获取角色名称
const getRoleName = (roleId: number) => roleId === 0 ? '管理员' : '普通用户'

// 获取性别名称
const getGenderName = (gender: number) => {
  const option = genderOptions.find(item => item.value === gender)
  return option?.label || '保密'
}

// 获取用户信息
const fetchUserInfo = async () => {
  const userId = userStore.info?.userId
  if (!userId) {
    console.warn('用户ID不存在')
    return
  }

  loading.value = true
  try {
    const res = await getUserInfo(Number(userId))
    if (res) {
      formData.avatar = res.avatarUrl || ''
      formData.username = res.username || ''
      formData.gender = res.gender ?? 3
      formData.profile = res.profile || ''
      formData.roleId = res.roleId ?? 0
      formData.banFlag = res.banFlag ?? 0
      formData.realNameAuthFlag = res.realNameAuthFlag ?? 0

      // 保存原始数据
      originalData.value = {
        username: res.username || '',
        gender: res.gender ?? 3,
        profile: res.profile || ''
      }

      // 同步更新 userStore 中的头像和用户名
      userStore.setUserInfo({
        ...userStore.info,
        avatar: res.avatarUrl || '',
        userName: res.username || userStore.info.userName || ''
      } as Api.Auth.UserInfo)
    }

    // 获取手机号和邮箱
    await fetchPhoneAndEmail()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取手机号和邮箱
const fetchPhoneAndEmail = async () => {
  const userId = userStore.info?.userId
  if (!userId) return

  try {
    const [phone, email] = await Promise.all([
      getPhone(Number(userId)).catch(() => ''),
      getEmail(Number(userId)).catch(() => '')
    ])
    phoneNumber.value = phone || ''
    emailAddress.value = email || ''
  } catch (error) {
    console.error('获取手机号/邮箱失败:', error)
  }
}

// 手机号脱敏
const maskPhone = (phone: string) => {
  if (!phone || phone.length < 7) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 邮箱脱敏
const maskEmail = (email: string) => {
  if (!email) return email
  const [name, domain] = email.split('@')
  if (!domain) return email
  const maskedName = name.length > 2 ? name.slice(0, 2) + '***' : name + '***'
  return `${maskedName}@${domain}`
}

// 上传头像
const handleAvatarUpload = async (file: UploadRawFile) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return false
  }

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

  uploading.value = true
  try {
    await uploadAvatar(Number(userId), file)
    ElMessage.success('头像上传成功')
    await fetchUserInfo()
  } catch (error: any) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    uploading.value = false
  }
  return false
}

// 编辑或保存用户信息
const handleEditOrSave = async () => {
  if (!isEdit.value) {
    isEdit.value = true
    return
  }

  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  // 检测是否有修改
  const changedData: any = { userId: Number(userId) }
  let hasChanges = false

  const trimmedUsername = formData.username.trim()
  const trimmedProfile = formData.profile.trim()

  if (trimmedUsername !== originalData.value.username.trim()) {
    changedData.username = trimmedUsername
    hasChanges = true
  }
  if (formData.gender !== originalData.value.gender) {
    changedData.gender = formData.gender
    hasChanges = true
  }
  if (trimmedProfile !== originalData.value.profile.trim()) {
    changedData.profile = trimmedProfile
    hasChanges = true
  }

  if (!hasChanges) {
    ElMessage.info('没有修改任何内容')
    isEdit.value = false
    return
  }

  saving.value = true
  try {
    await changeUserInfo(changedData)
    ElMessage.success('保存成功')
    originalData.value = {
      username: trimmedUsername,
      gender: formData.gender,
      profile: trimmedProfile
    }
    isEdit.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  formData.username = originalData.value.username
  formData.gender = originalData.value.gender
  formData.profile = originalData.value.profile
  isEdit.value = false
  formRef.value?.clearValidate()
}

// 编辑或保存密码
const handleEditOrSavePwd = async () => {
  if (!isEditPwd.value) {
    isEditPwd.value = true
    return
  }

  if (!pwdFormRef.value) return

  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  savingPwd.value = true
  try {
    await changePassword({
      userId: Number(userId),
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
      confirmPassword: pwdForm.confirmPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    isEditPwd.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    savingPwd.value = false
  }
}

// 取消密码编辑
const handleCancelPwd = () => {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  isEditPwd.value = false
  pwdFormRef.value?.clearValidate()
}

// 打开实名认证弹窗
const openAuthDialog = () => {
  if (formData.realNameAuthFlag === 1) {
    ElMessage.info('您已完成实名认证')
    return
  }
  authForm.realName = ''
  authForm.idCard = ''
  authDialogVisible.value = true
}

// 提交实名认证
const submitAuth = async () => {
  if (!authFormRef.value) return

  try {
    await authFormRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  authSubmitting.value = true
  try {
    await realNameAuth({
      userId: Number(userId),
      realName: authForm.realName.trim(),
      idCard: authForm.idCard.trim()
    })
    ElMessage.success('实名认证成功')
    formData.realNameAuthFlag = 1
    authDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '实名认证失败')
  } finally {
    authSubmitting.value = false
  }
}

// 打开手机绑定弹窗
const openPhoneDialog = () => {
  phoneForm.phone = phoneNumber.value || ''
  phoneDialogVisible.value = true
}

// 提交手机绑定
const submitPhone = async () => {
  if (!phoneFormRef.value) return

  try {
    await phoneFormRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  phoneSubmitting.value = true
  try {
    await changePhone({
      userId: Number(userId),
      phone: phoneForm.phone.trim()
    })
    ElMessage.success('手机绑定成功')
    phoneNumber.value = phoneForm.phone.trim()
    phoneDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '手机绑定失败')
  } finally {
    phoneSubmitting.value = false
  }
}

// 打开邮箱绑定弹窗
const openEmailDialog = () => {
  emailForm.email = emailAddress.value || ''
  emailDialogVisible.value = true
}

// 提交邮箱绑定
const submitEmail = async () => {
  if (!emailFormRef.value) return

  try {
    await emailFormRef.value.validate()
  } catch {
    return
  }

  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  emailSubmitting.value = true
  try {
    await changeEmail({
      userId: Number(userId),
      email: emailForm.email.trim()
    })
    ElMessage.success('邮箱绑定成功')
    emailAddress.value = emailForm.email.trim()
    emailDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '邮箱绑定失败')
  } finally {
    emailSubmitting.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.avatar-uploader {
  :deep(.el-upload) {
    position: relative;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid var(--el-border-color);
    transition: all 0.3s;

    &:hover {
      border-color: var(--el-color-primary);

      .avatar-overlay {
        opacity: 1;
      }
    }
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 账号安全项样式
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--el-fill-color-light);
    margin: 0 -20px;
    padding: 12px 20px;
  }

  .security-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .security-icon {
    font-size: 20px;
    color: var(--el-color-primary);
  }

  .security-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .security-title {
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  .security-desc {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}
</style>
