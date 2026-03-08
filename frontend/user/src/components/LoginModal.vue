<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { User, Lock, Message, View, Hide, Close, Shop, ShoppingCart, ChatDotRound, Star, ChatLineRound, Refresh, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { API_BASE_URL, AUTH_API, CAPTCHA_API } from '@/api/config.js'

const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['update:visible', 'login-success'])

// ── 全局状态 ──────────────────────────────────────────
const mode = ref('login')           // 'login' | 'register'
const registerStep = ref(1)         // 注册步骤 1=基础信息 2=验证码
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)

// ── 图片验证码（登录 & 图片验证弹窗共用同一 session） ──
const captchaUrl = ref('')
const refreshCaptcha = () => {
  captchaUrl.value = `${CAPTCHA_API.GET_CAPTCHA}?t=${Date.now()}`
}

// ── 图片验证码弹窗（发送邮箱验证码前弹出） ────────────
const imgCaptchaDialogVisible = ref(false)
const imgCaptchaDialogUrl = ref('')
const imgCaptchaInput = ref('')
const checkingImgCaptcha = ref(false)

const openImgCaptchaDialog = () => {
  imgCaptchaInput.value = ''
  imgCaptchaDialogUrl.value = `${CAPTCHA_API.GET_CAPTCHA}?t=${Date.now()}`
  imgCaptchaDialogVisible.value = true
}
const refreshDialogCaptcha = () => {
  imgCaptchaDialogUrl.value = `${CAPTCHA_API.GET_CAPTCHA}?t=${Date.now()}`
  imgCaptchaInput.value = ''
}

// ── 邮箱验证码发送 ────────────────────────────────────
const sendingEmail = ref(false)
const sendCooldown = ref(0)
let cooldownTimer = null

// ── 表单数据 ──────────────────────────────────────────
const loginForm = ref({ email: '', password: '', captcha: '' })
const registerForm = ref({
  username: '', email: '', password: '', confirmPassword: '', emailCaptcha: ''
})

// ── 计算属性 ──────────────────────────────────────────
const title = computed(() => {
  if (mode.value === 'login') return '欢迎回来'
  return registerStep.value === 1 ? '创建账号' : '验证邮箱'
})
const subtitle = computed(() => {
  if (mode.value === 'login') return '登录您的小橘岛账号'
  return registerStep.value === 1 ? '填写基础信息' : `验证码将发送至 ${registerForm.value.email}`
})

// ── 重置 ──────────────────────────────────────────────
const resetRegister = () => {
  registerStep.value = 1
  registerForm.value = { username: '', email: '', password: '', confirmPassword: '', emailCaptcha: '' }
  showPassword.value = false
  showConfirmPassword.value = false
  sendCooldown.value = 0
  if (cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = null }
}

const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  resetRegister()
  refreshCaptcha()
}

watch(() => props.visible, (val) => { if (val) refreshCaptcha() })

const handleClose = () => {
  emit('update:visible', false)
  setTimeout(() => {
    mode.value = 'login'
    loginForm.value = { email: '', password: '', captcha: '' }
    resetRegister()
    googleLoading.value = false
    googleError.value = ''
  }, 300)
}

// ── 登录 ──────────────────────────────────────────────
const handleLogin = async () => {
  const email = loginForm.value.email?.trim() || ''
  const password = loginForm.value.password || ''
  const captcha = loginForm.value.captcha?.trim() || ''
  if (!email) { ElMessage.warning('请输入邮箱'); return }
  if (!password) { ElMessage.warning('请输入密码'); return }
  if (!captcha) { ElMessage.warning('请输入验证码'); return }

  try {
    loginLoading.value = true
    const fd = new FormData()
    fd.append('inputCaptcha', captcha)
    const captchaRes = await fetch(CAPTCHA_API.CHECK_CAPTCHA, { method: 'POST', body: fd, credentials: 'include' })
    if (!await captchaRes.json()) {
      ElMessage.error('验证码错误或已过期')
      refreshCaptcha(); loginForm.value.captcha = ''; return
    }
    const res = await fetch(AUTH_API.LOGIN, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
      credentials: 'include'
    })
    const result = await res.json()
    if (result.code === 0 && result.data) {
      const u = result.data
      localStorage.setItem('userInfo', JSON.stringify({
        userId: u.userId, username: u.username, avatar: u.avatarUrl || '', roleId: u.roleId, token: u.token
      }))
      ElMessage.success('登录成功')
      emit('login-success', { userId: u.userId, avatar: u.avatarUrl || '', nickname: u.username, roleId: u.roleId, token: u.token })
      handleClose()
    } else {
      ElMessage.error(result.message || '登录失败')
      refreshCaptcha(); loginForm.value.captcha = ''
    }
  } catch (e) {
    console.error(e)
    refreshCaptcha(); loginForm.value.captcha = ''
  } finally {
    loginLoading.value = false
  }
}

// ── 注册第一步：校验基础信息，进入第二步 ─────────────
const handleNextStep = () => {
  const { username, email, password, confirmPassword } = registerForm.value
  const u = username?.trim() || ''
  const e = email?.trim() || ''
  const p = password || ''
  const cp = confirmPassword || ''
  if (!u) { ElMessage.warning('请输入用户名'); return }
  if (u.length > 30) { ElMessage.warning('用户名最长30个字符'); return }
  if (!e) { ElMessage.warning('请输入邮箱'); return }
  if (/\s/.test(e)) { ElMessage.warning('邮箱不能包含空格'); return }
  if (e.length > 30) { ElMessage.warning('邮箱最长30个字符'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e)) { ElMessage.warning('请输入正确的邮箱格式'); return }
  if (!p) { ElMessage.warning('请输入密码'); return }
  if (p.length > 30) { ElMessage.warning('密码最长30个字符'); return }
  if (p !== cp) { ElMessage.warning('两次密码不一致'); return }
  registerStep.value = 2
}

// ── 点击"发送验证码"：先弹图片验证码弹窗 ─────────────
const handleSendClick = () => {
  openImgCaptchaDialog()
}

// 图片验证码弹窗确认：校验通过后发送邮箱验证码
const handleImgCaptchaConfirm = async () => {
  const input = imgCaptchaInput.value?.trim() || ''
  if (!input) { ElMessage.warning('请输入图片验证码'); return }
  try {
    checkingImgCaptcha.value = true
    const fd = new FormData()
    fd.append('inputCaptcha', input)
    const res = await fetch(CAPTCHA_API.CHECK_CAPTCHA, { method: 'POST', body: fd, credentials: 'include' })
    const ok = await res.json()
    if (!ok) {
      ElMessage.error('验证码错误或已过期')
      refreshDialogCaptcha(); return
    }
    // 图片验证通过，关闭弹窗，发送邮箱验证码
    imgCaptchaDialogVisible.value = false
    await doSendEmailCaptcha()
  } catch {
    ElMessage.error('校验失败，请重试')
    refreshDialogCaptcha()
  } finally {
    checkingImgCaptcha.value = false
  }
}

// 实际发送邮箱验证码
const doSendEmailCaptcha = async () => {
  const email = registerForm.value.email?.trim() || ''
  try {
    sendingEmail.value = true
    const res = await fetch(`${CAPTCHA_API.GET_EMAIL_CAPTCHA}?email=${encodeURIComponent(email)}`, { credentials: 'include' })
    const result = await res.json()
    if (result.code === 0 && result.data) {
      ElMessage.success('验证码已发送，请查收邮件')
      sendCooldown.value = 60
      cooldownTimer = setInterval(() => {
        if (--sendCooldown.value <= 0) { clearInterval(cooldownTimer); cooldownTimer = null }
      }, 1000)
    } else {
      ElMessage.error(result.message || '发送失败，请稍后重试')
    }
  } catch {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    sendingEmail.value = false
  }
}

// ── 注册提交 ──────────────────────────────────────────
const handleRegister = async () => {
  const emailCaptcha = registerForm.value.emailCaptcha?.trim() || ''
  if (!emailCaptcha) { ElMessage.warning('请输入邮箱验证码'); return }
  const { username, email, password, confirmPassword } = registerForm.value
  try {
    registerLoading.value = true
    // 校验邮箱验证码
    const fd = new FormData()
    fd.append('email', email.trim())
    fd.append('captcha', emailCaptcha)
    const captchaRes = await fetch(CAPTCHA_API.CHECK_EMAIL_CAPTCHA, { method: 'POST', body: fd, credentials: 'include' })
    const captchaResult = await captchaRes.json()
    if (!captchaResult?.data) {
      ElMessage.error('邮箱验证码错误或已过期')
      registerForm.value.emailCaptcha = ''; return
    }
    // 注册
    const res = await fetch(AUTH_API.REGISTER, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.trim(), email: email.trim(), password, confirmPassword }),
      credentials: 'include'
    })
    const result = await res.json()
    if (result.code === 0 && result.data) {
      ElMessage.success('注册成功，请登录')
      mode.value = 'login'
      resetRegister()
      refreshCaptcha()
    } else {
      ElMessage.error(result.message || '注册失败')
    }
  } catch (e) {
    console.error(e)
  } finally {
    registerLoading.value = false
  }
}

// ── Google OAuth 登录 ─────────────────────────────────
const googleLoading = ref(false)
const googleError = ref('')
let googlePopup = null
let popupCheckTimer = null
let googleCallbackReceived = false

const startPopupCheck = () => {
  stopPopupCheck()
  popupCheckTimer = setInterval(() => {
    if (googlePopup && googlePopup.closed) {
      // 弹窗关了，但如果已经收到回调 code 说明是授权成功后自动关的，不取消
      if (!googleCallbackReceived && googleLoading.value && !googleError.value) {
        cancelGoogleLogin()
      }
      stopPopupCheck()
    }
  }, 500)
}

const stopPopupCheck = () => {
  if (popupCheckTimer) {
    clearInterval(popupCheckTimer)
    popupCheckTimer = null
  }
}

const handleGoogleLogin = async () => {
  try {
    const res = await fetch(AUTH_API.GOOGLE_AUTH_URL)
    const result = await res.json()
    if (result.code === 0 && result.data) {
      const w = 500, h = 600
      const left = (screen.width - w) / 2
      const top = (screen.height - h) / 2
      googlePopup = window.open(
        result.data,
        'google-oauth',
        `width=${w},height=${h},left=${left},top=${top},toolbar=no,menubar=no`
      )
      googleLoading.value = true
      googleError.value = ''
      googleCallbackReceived = false
      startPopupCheck()
    } else {
      ElMessage.error('获取 Google 授权地址失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 监听 Google 回调中转页通过 postMessage 传回的 code
const handleGoogleMessage = async (event) => {
  if (event.origin !== window.location.origin) return
  if (event.data?.type !== 'google-oauth-callback') return

  googleCallbackReceived = true
  stopPopupCheck()

  const { code, error } = event.data
  if (error || !code) {
    googleLoading.value = false
    googleError.value = error || '授权失败：未获取到授权码'
    return
  }

  try {
    const res = await fetch(`${AUTH_API.GOOGLE_CALLBACK}?code=${encodeURIComponent(code)}`)
    const result = await res.json()
    if (result.code === 0 && result.data) {
      const u = result.data
      localStorage.setItem('userInfo', JSON.stringify({
        userId: u.userId, username: u.username, avatar: u.avatarUrl || '', roleId: u.roleId, token: u.token
      }))
      ElMessage.success('Google 登录成功')
      googleLoading.value = false
      emit('login-success', { userId: u.userId, avatar: u.avatarUrl || '', nickname: u.username, roleId: u.roleId, token: u.token })
      handleClose()
    } else {
      googleLoading.value = false
      googleError.value = result.message || 'Google 登录失败'
    }
  } catch (e) {
    console.error('Google 登录回调失败:', e)
    googleLoading.value = false
    googleError.value = '网络错误，请稍后重试'
  }
}

// 取消 Google 登录
const cancelGoogleLogin = () => {
  googleLoading.value = false
  googleError.value = ''
  stopPopupCheck()
  if (googlePopup && !googlePopup.closed) {
    googlePopup.close()
  }
  googlePopup = null
}

// 注册/注销 message 监听
onMounted(() => {
  window.addEventListener('message', handleGoogleMessage)
})
onUnmounted(() => {
  window.removeEventListener('message', handleGoogleMessage)
  stopPopupCheck()
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="emit('update:visible', $event)"
    :width="900"
    :show-close="false"
    align-center
    class="login-modal"
    @close="handleClose"
  >
    <div class="modal-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-icon">
            <el-icon :size="64"><Shop /></el-icon>
          </div>
          <h2 class="brand-title">小橘岛</h2>
          <p class="brand-slogan">给宠物一个温暖的家</p>
          <div class="features">
            <div class="feature-item">
              <span class="feature-icon"><el-icon :size="24"><ShoppingCart /></el-icon></span>
              <span class="feature-text">优质商品</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon"><el-icon :size="24"><Star /></el-icon></span>
              <span class="feature-text">贴心服务</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon"><el-icon :size="24"><ChatDotRound /></el-icon></span>
              <span class="feature-text">社区交流</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="form-header">
          <h3 class="form-title">{{ title }}</h3>
          <p class="form-subtitle">{{ subtitle }}</p>
          <button class="close-btn" @click="handleClose">
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>

        <!-- ── 登录表单 ── -->
        <div v-if="mode === 'login'" class="form-content">
          <el-form :model="loginForm" label-position="top">
            <el-form-item label="邮箱">
              <el-input v-model="loginForm.email" placeholder="请输入邮箱地址" :prefix-icon="Message" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码" :prefix-icon="Lock" size="large">
                <template #suffix>
                  <el-icon class="password-toggle" @click="showPassword = !showPassword">
                    <View v-if="!showPassword" /><Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="验证码">
              <div class="captcha-row">
                <el-input v-model="loginForm.captcha" placeholder="请输入验证码" size="large"
                  class="captcha-input" maxlength="6" />
                <div class="captcha-img-wrapper" @click="refreshCaptcha">
                  <img :src="captchaUrl" alt="验证码" class="captcha-img" />
                  <div class="captcha-refresh">
                    <el-icon :size="14"><Refresh /></el-icon><span>换一张</span>
                  </div>
                </div>
              </div>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loginLoading" @click="handleLogin">
              {{ loginLoading ? '登录中...' : '立即登录' }}
            </el-button>
            <div class="divider"><span>或</span></div>
            <el-button class="google-btn" size="large" @click="handleGoogleLogin">
              <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              Google 账号登录
            </el-button>
          </el-form>
          <div class="switch-mode">
            还没有账号？<a href="#" @click.prevent="toggleMode">立即注册</a>
          </div>
        </div>

        <!-- ── 注册：第一步 基础信息 ── -->
        <div v-else-if="registerStep === 1" class="form-content">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="请输入用户名（最长30字符）"
                :prefix-icon="User" size="large" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱地址"
                :prefix-icon="Message" size="large" maxlength="30" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码（最长30字符）" :prefix-icon="Lock" size="large" maxlength="30">
                <template #suffix>
                  <el-icon class="password-toggle" @click="showPassword = !showPassword">
                    <View v-if="!showPassword" /><Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="registerForm.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="请再次输入密码" :prefix-icon="Lock" size="large" maxlength="30">
                <template #suffix>
                  <el-icon class="password-toggle" @click="showConfirmPassword = !showConfirmPassword">
                    <View v-if="!showConfirmPassword" /><Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-button type="primary" size="large" class="submit-btn" @click="handleNextStep">
              下一步
            </el-button>
          </el-form>
          <div class="switch-mode">
            已有账号？<a href="#" @click.prevent="toggleMode">立即登录</a>
          </div>
        </div>

        <!-- ── 注册：第二步 邮箱验证码 ── -->
        <div v-else class="form-content">
          <!-- 邮箱展示 -->
          <div class="email-display">
            <el-icon :size="16"><Message /></el-icon>
            <span>{{ registerForm.email }}</span>
          </div>

          <el-form :model="registerForm" label-position="top">
            <el-form-item label="邮箱验证码">
              <div class="captcha-row">
                <el-input v-model="registerForm.emailCaptcha" placeholder="请输入邮箱验证码"
                  size="large" class="captcha-input" maxlength="10" />
                <el-button type="primary" size="large" class="send-captcha-btn"
                  :disabled="sendCooldown > 0" :loading="sendingEmail" @click="handleSendClick">
                  {{ sendCooldown > 0 ? `${sendCooldown}s 后重发` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-button type="primary" size="large" class="submit-btn"
              :loading="registerLoading" @click="handleRegister">
              {{ registerLoading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form>

          <div class="back-row">
            <el-button link :icon="ArrowLeft" @click="registerStep = 1">返回修改信息</el-button>
          </div>
          <div class="switch-mode">
            已有账号？<a href="#" @click.prevent="toggleMode">立即登录</a>
          </div>
        </div>
      </div>

      <!-- Google 登录中遮罩层（覆盖在登录弹窗之上） -->
      <transition name="google-overlay">
        <div v-if="googleLoading || googleError" class="google-overlay">
          <div v-if="googleLoading && !googleError" class="google-overlay__content">
            <div class="google-spinner"></div>
            <p class="google-overlay__text">正在使用 Google 登录，请稍候...</p>
            <button class="google-overlay__cancel" @click="cancelGoogleLogin">取消</button>
          </div>
          <div v-if="googleError" class="google-overlay__content">
            <div class="google-error-card">
              <div class="google-error-card__icon">
                <svg viewBox="0 0 48 48" width="48" height="48" fill="none">
                  <circle cx="24" cy="24" r="22" fill="#FFF1F0" stroke="#FFCCC7" stroke-width="2"/>
                  <circle cx="24" cy="33" r="2" fill="#FF4D4F"/>
                  <rect x="22.5" y="13" width="3" height="14" rx="1.5" fill="#FF4D4F"/>
                </svg>
              </div>
              <p class="google-error-card__title">登录失败</p>
              <p class="google-error-card__msg">{{ googleError }}</p>
              <div class="google-error-card__actions">
                <button class="google-overlay__cancel" @click="cancelGoogleLogin">返回</button>
                <button class="google-overlay__retry" @click="googleError = ''; handleGoogleLogin()">重试</button>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </el-dialog>

  <!-- 图片验证码弹窗（自定义，仅关闭按钮可关闭） -->
  <teleport to="body">
    <transition name="captcha-dialog">
      <div v-if="imgCaptchaDialogVisible" class="captcha-overlay">
        <div class="captcha-card">
          <div class="captcha-card__header">
            <span class="captcha-card__title">请完成图片验证</span>
            <button class="captcha-card__close" @click="imgCaptchaDialogVisible = false">
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                <path d="M12 4L4 12M4 4l8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
          <div class="captcha-card__body">
            <div class="captcha-row">
              <el-input
                v-model="imgCaptchaInput"
                placeholder="请输入图片验证码"
                size="large"
                class="captcha-input"
                maxlength="6"
                @keyup.enter="handleImgCaptchaConfirm"
              />
              <div class="captcha-img-wrapper" @click="refreshDialogCaptcha">
                <img :src="imgCaptchaDialogUrl" alt="验证码" class="captcha-img" />
                <div class="captcha-refresh">
                  <el-icon :size="14"><Refresh /></el-icon><span>换一张</span>
                </div>
              </div>
            </div>
          </div>
          <div class="captcha-card__footer">
            <button class="captcha-btn captcha-btn--cancel" @click="imgCaptchaDialogVisible = false">取消</button>
            <button class="captcha-btn captcha-btn--confirm" :disabled="checkingImgCaptcha" @click="handleImgCaptchaConfirm">
              <span v-if="checkingImgCaptcha" class="captcha-btn__spinner"></span>
              {{ checkingImgCaptcha ? '验证中...' : '确认' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<!-- 全局样式 - 用于覆盖 el-dialog teleport 到 body 的样式 -->
<style lang="scss">
.login-modal.el-dialog {
  background: transparent !important;
  box-shadow: none !important;
  border-radius: var(--radius-xl);
  
  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 0;
    background: transparent !important;
  }
}

/* 自定义图片验证码弹窗 */
.captcha-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(2px);
}

.captcha-card {
  width: 360px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
  overflow: hidden;

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px 0;
  }

  &__title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a1a;
  }

  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border: none;
    background: #f5f5f5;
    border-radius: 50%;
    cursor: pointer;
    color: #888;
    transition: all 0.2s;

    &:hover {
      background: #ffe8dc;
      color: #ff8a5b;
    }
  }

  &__body {
    padding: 20px 24px;
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 0 24px 20px;
  }
}

.captcha-btn {
  height: 38px;
  padding: 0 22px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;

  &--cancel {
    background: #f5f5f5;
    color: #555;

    &:hover {
      background: #ebebeb;
    }
  }

  &--confirm {
    background: linear-gradient(135deg, #ff8a5b 0%, #e06030 100%);
    color: #fff;
    box-shadow: 0 4px 12px rgba(255, 138, 91, 0.35);

    &:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(255, 138, 91, 0.45);
    }

    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
      transform: none;
    }
  }

  &__spinner {
    width: 14px;
    height: 14px;
    border: 2px solid rgba(255,255,255,0.4);
    border-top-color: #fff;
    border-radius: 50%;
    animation: spin 0.7s linear infinite;
    flex-shrink: 0;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 弹窗过渡动画 */
.captcha-dialog-enter-active,
.captcha-dialog-leave-active {
  transition: opacity 0.2s ease;

  .captcha-card {
    transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.2s ease;
  }
}

.captcha-dialog-enter-from,
.captcha-dialog-leave-to {
  opacity: 0;

  .captcha-card {
    transform: scale(0.92);
    opacity: 0;
  }
}
</style>

<style scoped lang="scss">

.modal-container {
  display: flex;
  min-height: 600px;
  background: var(--color-bg-surface);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
}

// 左侧品牌区
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-secondary-light) 100%);
  padding: var(--spacing-4xl);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 300px;
    height: 300px;
    background: var(--color-primary);
    opacity: 0.1;
    border-radius: 50%;
    top: -100px;
    right: -100px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 200px;
    height: 200px;
    background: var(--color-accent);
    opacity: 0.08;
    border-radius: 50%;
    bottom: -50px;
    left: -50px;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;

  .brand-icon {
    margin-bottom: var(--spacing-lg);
    animation: float 3s ease-in-out infinite;
    color: var(--color-primary);
    
    .el-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      svg {
        width: 1em;
        height: 1em;
        fill: currentColor;
      }
    }
  }

  .brand-title {
    font-size: var(--font-size-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-primary);
    margin-bottom: var(--spacing-sm);
  }

  .brand-slogan {
    font-size: var(--font-size-md);
    color: var(--color-text-secondary);
    margin-bottom: var(--spacing-3xl);
  }

  .features {
    display: flex;
    gap: var(--spacing-xl);
    justify-content: center;

    .feature-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--spacing-xs);

      .feature-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-primary);
        
        .el-icon {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          
          svg {
            width: 1em;
            height: 1em;
            fill: currentColor;
          }
        }
      }

      .feature-text {
        font-size: var(--font-size-sm);
        color: var(--color-text-secondary);
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

// 右侧表单区
.form-section {
  flex: 1;
  padding: var(--spacing-4xl);
  display: flex;
  flex-direction: column;
}

.form-header {
  position: relative;
  margin-bottom: var(--spacing-3xl);

  .form-title {
    font-size: var(--font-size-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-xs);
  }

  .form-subtitle {
    font-size: var(--font-size-sm);
    color: var(--color-text-tertiary);
  }

  .close-btn {
    position: absolute;
    top: 0;
    right: 0;
    background: none;
    border: none;
    cursor: pointer;
    color: var(--color-text-tertiary);
    padding: var(--spacing-sm);
    border-radius: var(--radius-base);
    transition: all var(--transition-base);

    &:hover {
      background: var(--color-border-light);
      color: var(--color-text-primary);
    }
  }
}

.form-content {
  flex: 1;

  :deep(.el-form-item) {
    margin-bottom: var(--spacing-xl);
  }

  :deep(.el-form-item__label) {
    font-weight: var(--font-weight-medium);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-sm);
  }

  :deep(.el-input__wrapper) {
    border-radius: var(--radius-lg);
    box-shadow: 0 0 0 1px var(--color-border-base);
    transition: all var(--transition-base);

    &:hover {
      box-shadow: 0 0 0 1px var(--color-border-dark);
    }

    &.is-focus {
      box-shadow: 0 0 0 2px var(--color-primary) !important;
    }
  }

  .password-toggle {
    cursor: pointer;
    color: var(--color-text-tertiary);
    transition: color var(--transition-base);

    &:hover {
      color: var(--color-primary);
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);

  .forgot-link {
    color: var(--color-primary);
    font-size: var(--font-size-sm);
    text-decoration: none;
    transition: opacity var(--transition-base);

    &:hover {
      opacity: 0.8;
    }
  }
}

// 验证码样式
.captcha-row {
  display: flex;
  gap: var(--spacing-base);
  align-items: center;
  width: 100%;

  .captcha-input {
    flex: 1;
  }

  .captcha-img-wrapper {
    flex-shrink: 0;
    cursor: pointer;
    position: relative;
    border-radius: var(--radius-base);
    overflow: hidden;
    border: 1px solid var(--color-border-base);
    transition: all var(--transition-base);

    &:hover {
      border-color: var(--color-primary);

      .captcha-refresh {
        opacity: 1;
      }
    }

    .captcha-img {
      display: block;
      height: 40px;
      width: auto;
      min-width: 100px;
    }

    .captcha-refresh {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: var(--spacing-xs);
      color: #fff;
      font-size: var(--font-size-xs);
      opacity: 0;
      transition: opacity var(--transition-base);
    }

    .captcha-passed-mask {
      position: absolute;
      inset: 0;
      background: rgba(103, 194, 58, 0.85);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: var(--font-size-xs);
      font-weight: var(--font-weight-semibold);
    }
  }
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 138, 91, 0.3);
  transition: all var(--transition-base);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 138, 91, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.divider {
  position: relative;
  text-align: center;
  margin: var(--spacing-2xl) 0;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    height: 1px;
    background: var(--color-border-base);
  }

  span {
    position: relative;
    background: var(--color-bg-surface);
    padding: 0 var(--spacing-base);
    color: var(--color-text-tertiary);
    font-size: var(--font-size-sm);
  }
}

.google-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  border: 2px solid var(--color-border-base);
  background: var(--color-bg-surface);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  transition: all var(--transition-base);

  .google-icon {
    flex-shrink: 0;
  }

  &:hover {
    border-color: #4285F4;
    color: #4285F4;
    background: rgba(66, 133, 244, 0.05);
  }
}

.switch-mode {
  text-align: center;
  margin-top: var(--spacing-2xl);
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);

  a {
    color: var(--color-primary);
    text-decoration: none;
    font-weight: var(--font-weight-medium);
    margin-left: var(--spacing-xs);
    transition: opacity var(--transition-base);

    &:hover {
      opacity: 0.8;
    }
  }
}

// 响应式
// Google 登录遮罩层
.google-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(4px);
  border-radius: var(--radius-xl);

  &__content {
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  &__text {
    color: #555;
    font-size: 15px;
  }

  &__cancel {
    padding: 8px 24px;
    background: #f0f0f0;
    color: #666;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #e5e5e5;
    }
  }

  &__retry {
    padding: 8px 24px;
    background: linear-gradient(135deg, #ff8a5b, #e06030);
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      opacity: 0.9;
    }
  }
}

.google-error-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 340px;
  padding: 32px 28px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

  &__icon {
    margin-bottom: 12px;
  }

  &__title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 8px;
  }

  &__msg {
    font-size: 13px;
    color: #888;
    line-height: 1.6;
    margin-bottom: 20px;
    word-break: break-word;
  }

  &__actions {
    display: flex;
    gap: 12px;
    width: 100%;
    justify-content: center;
  }
}

.google-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e0e0e0;
  border-top-color: #ff8a5b;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.google-overlay-enter-active,
.google-overlay-leave-active {
  transition: opacity 0.25s ease;
}

.google-overlay-enter-from,
.google-overlay-leave-to {
  opacity: 0;
}

// 响应式
@media (max-width: 768px) {
  .modal-container {
    flex-direction: column;
  }

  .brand-section {
    padding: var(--spacing-2xl);
  }

  .form-section {
    padding: var(--spacing-2xl);
  }
}

// 邮箱展示行
.email-display {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-base) var(--spacing-lg);
  background: var(--color-bg-page);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-2xl);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border-base);

  span { font-weight: var(--font-weight-medium); color: var(--color-text-primary); }
}

// 返回行
.back-row {
  margin-top: var(--spacing-lg);
  text-align: center;
}

.send-captcha-btn {
  flex-shrink: 0;
  min-width: 110px;
  height: 40px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  border: none;

  &.is-disabled,
  &.is-disabled:hover,
  &.is-disabled:focus {
    background: var(--color-border-base) !important;
    border-color: transparent !important;
    box-shadow: none !important;
    color: var(--color-text-tertiary) !important;
  }
}

:deep(.send-captcha-btn.is-disabled),
:deep(.send-captcha-btn.is-disabled:hover) {
  background: var(--color-border-base) !important;
  border-color: transparent !important;
  box-shadow: none !important;
  color: var(--color-text-tertiary) !important;
}
</style>
