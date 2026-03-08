<template>
  <ElDialog
    v-model="dialogVisible"
    title="新增用户"
    width="500px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="left"
      v-loading="loading"
    >
      <ElFormItem label="用户名" prop="username">
        <ElInput 
          v-model="formData.username" 
          placeholder="请输入用户名（最长30字符）" 
          maxlength="30"
          show-word-limit
        />
      </ElFormItem>

      <ElFormItem label="邮箱" prop="email">
        <ElInput 
          v-model="formData.email" 
          placeholder="请输入邮箱（最长30字符）" 
          maxlength="30"
          show-word-limit
        />
      </ElFormItem>

      <ElFormItem label="密码" prop="password">
        <ElInput 
          v-model="formData.password" 
          type="password" 
          placeholder="请输入密码（最长30字符）" 
          maxlength="30"
          show-password
        />
      </ElFormItem>

      <ElFormItem label="确认密码" prop="confirmPassword">
        <ElInput 
          v-model="formData.confirmPassword" 
          type="password" 
          placeholder="请再次输入密码" 
          maxlength="30"
          show-password
        />
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit" :loading="loading">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { registerUser, type UserRegisterDTO } from '@/api/user'

defineOptions({ name: 'UserAddDialog' })

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单数据
const formData = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 用户名验证器（不能只包含空格）
const validateUsername = (_rule: any, value: string, callback: any) => {
  if (!value || !value.trim()) {
    callback(new Error('用户名不能为空或只包含空格'))
  } else {
    callback()
  }
}

// 邮箱验证器（格式校验 + 不能包含空格）
const validateEmail = (_rule: any, value: string, callback: any) => {
  if (!value || !value.trim()) {
    callback(new Error('邮箱不能为空或只包含空格'))
    return
  }
  if (/\s/.test(value)) {
    callback(new Error('邮箱不能包含空格'))
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
    return
  }
  callback()
}

// 密码验证器（不能只包含空格）
const validatePassword = (_rule: any, value: string, callback: any) => {
  if (!value || !value.trim()) {
    callback(new Error('密码不能为空或只包含空格'))
  } else {
    callback()
  }
}

// 确认密码验证器
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== formData.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' },
    { max: 30, message: '用户名最长不超过30个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' },
    { max: 30, message: '邮箱最长不超过30个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
    { max: 30, message: '密码最长不超过30个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 重置表单
const resetForm = () => {
  formData.value = {
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  }
  formRef.value?.resetFields()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const params: UserRegisterDTO = {
      username: formData.value.username.trim(),
      email: formData.value.email.trim(),
      password: formData.value.password,
      confirmPassword: formData.value.confirmPassword
    }
    
    await registerUser(params)
    
    ElMessage.success('用户创建成功')
    resetForm()
    dialogVisible.value = false
    emit('success')
  } catch (error: any) {
    if (error !== 'cancel' && error !== false) {
      ElMessage.error(error.message || '创建用户失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
