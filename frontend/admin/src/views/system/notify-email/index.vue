<template>
  <div class="notify-email-page art-full-height">
    <ElCard shadow="never" class="art-table-card">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="text-base font-medium">邮件通知</span>
          <ElSpace>
            <ElButton @click="handleSaveDraft" :disabled="!isDirty">
              <ArtSvgIcon icon="ri:save-line" class="mr-1" />
              保存草稿
            </ElButton>
            <ElButton type="primary" @click="handleSend" :loading="sending" v-ripple>
              <ArtSvgIcon icon="ri:send-plane-line" class="mr-1" />
              发送邮件
            </ElButton>
          </ElSpace>
        </div>
      </template>

      <ElForm ref="formRef" :model="form" :rules="rules" label-width="80px" class="max-w-3xl">
        <ElFormItem label="收件邮箱" prop="email">
          <ElInput v-model.trim="form.email" placeholder="请输入收件人邮箱" clearable />
        </ElFormItem>

        <ElFormItem label="邮件主题" prop="subject">
          <ElInput v-model.trim="form.subject" placeholder="请输入邮件主题" clearable maxlength="100" show-word-limit />
        </ElFormItem>

        <ElFormItem label="邮件内容" prop="content">
          <ElInput
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="请输入邮件正文内容"
            maxlength="2000"
            show-word-limit
            resize="none"
          />
        </ElFormItem>
      </ElForm>

      <!-- 草稿提示 -->
      <div v-if="hasDraft" class="mt-2 flex items-center gap-2 text-sm text-g-500">
        <ArtSvgIcon icon="ri:draft-line" />
        <span>已有草稿，上次保存于 {{ draftTime }}</span>
        <ElButton link type="primary" size="small" @click="loadDraft">恢复草稿</ElButton>
        <ElButton link type="danger" size="small" @click="clearDraft">清除</ElButton>
      </div>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, onActivated } from 'vue'
  import { onBeforeRouteLeave } from 'vue-router'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { fetchSendNotifyEmail } from '@/api/notify'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'NotifyEmail' })

  const DRAFT_KEY = 'notify-email-draft'

  const userStore = useUserStore()
  const route = useRoute()
  const formRef = ref<FormInstance>()
  const sending = ref(false)

  const form = reactive({
    email: '',
    subject: '',
    content: ''
  })

  const rules: FormRules = {
    email: [
      { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ],
    subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
    content: [{ required: true, message: '请输入邮件内容', trigger: 'blur' }]
  }

  // 草稿相关
  interface Draft {
    email: string
    subject: string
    content: string
    savedAt: string
  }

  const draftTime = ref('')
  const hasDraft = ref(false)

  // 是否有未保存的修改
  const isDirty = computed(() => !!(form.email || form.subject || form.content))

  function saveDraftToStorage() {
    const draft: Draft = {
      email: form.email,
      subject: form.subject,
      content: form.content,
      savedAt: new Date().toLocaleString()
    }
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
    draftTime.value = draft.savedAt
    hasDraft.value = true
  }

  function handleSaveDraft() {
    saveDraftToStorage()
    ElMessage.success('草稿已保存')
  }

  function loadDraft() {
    const raw = localStorage.getItem(DRAFT_KEY)
    if (!raw) return
    const draft: Draft = JSON.parse(raw)
    form.email = draft.email
    form.subject = draft.subject
    form.content = draft.content
    draftTime.value = draft.savedAt
    ElMessage.success('草稿已恢复')
  }

  function clearDraft() {
    localStorage.removeItem(DRAFT_KEY)
    draftTime.value = ''
    hasDraft.value = false
    ElMessage.info('草稿已清除')
  }

  // 初始化时读取草稿时间
  const raw = localStorage.getItem(DRAFT_KEY)
  if (raw) {
    try {
      draftTime.value = (JSON.parse(raw) as Draft).savedAt
      hasDraft.value = true
    } catch {}
  }

  // 从路由 query 接收邮箱（从用户管理页跳转过来时）
  function applyEmailFromQuery() {
    const emailFromQuery = route.query.email as string | undefined
    if (emailFromQuery) {
      form.email = ''
      form.subject = ''
      form.content = ''
      formRef.value?.clearValidate()
      form.email = emailFromQuery
    }
  }

  onMounted(applyEmailFromQuery)
  // keepAlive 激活时也要处理，确保切换不同用户时正确填入
  onActivated(applyEmailFromQuery)

  // 发送邮件
  async function handleSend() {
    if (!formRef.value) return
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    const userId = userStore.info.userId
    if (!userId) {
      ElMessage.error('获取管理员信息失败，请重新登录')
      return
    }

    sending.value = true
    try {
      await fetchSendNotifyEmail({
        userId: userId as number,
        email: form.email,
        subject: form.subject,
        content: form.content
      })
      ElMessage.success('邮件已发送')
      // 发送成功后清除草稿并重置表单
      clearDraft()
      formRef.value.resetFields()
    } catch {
      // 错误由 http 拦截器统一处理
    } finally {
      sending.value = false
    }
  }

  // 离开页面时自动保存草稿
  onBeforeRouteLeave((_to, _from, next) => {
    if (isDirty.value) {
      saveDraftToStorage()
    }
    next()
  })
</script>
