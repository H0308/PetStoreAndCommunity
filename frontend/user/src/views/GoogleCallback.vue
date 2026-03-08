<script setup>
/**
 * Google OAuth 回调中转页
 * 从 Google 授权重定向回来后，将 code 通过 postMessage 传给主窗口（LoginModal），然后自动关闭
 */
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

onMounted(() => {
  const code = route.query.code
  const error = route.query.error

  if (window.opener) {
    // 由 window.open 打开的弹窗，通过 postMessage 传回主窗口
    window.opener.postMessage(
      { type: 'google-oauth-callback', code: code || null, error: error || null },
      window.location.origin
    )
    window.close()
  } else {
    // 如果不是弹窗打开的（用户直接访问），跳转回首页
    window.location.href = '/'
  }
})
</script>

<template>
  <div style="display:flex;align-items:center;justify-content:center;min-height:100vh;background:#f5f5f5;">
    <p style="color:#666;font-size:16px;">正在处理 Google 登录，请稍候...</p>
  </div>
</template>
