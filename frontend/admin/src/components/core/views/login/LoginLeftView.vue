<!-- 登录、注册、忘记密码左侧背景 -->
<template>
  <div class="login-left-view">
    <!-- 品牌内容区 -->
    <div class="brand-content">
      <div class="brand-icon">
        <svg viewBox="0 0 100 100" width="64" height="64">
          <!-- 猫爪主体 -->
          <ellipse cx="50" cy="60" rx="28" ry="24" fill="currentColor"/>
          <!-- 猫爪肉垫 - 上方三个小圆 -->
          <circle cx="30" cy="35" r="12" fill="currentColor"/>
          <circle cx="50" cy="28" r="12" fill="currentColor"/>
          <circle cx="70" cy="35" r="12" fill="currentColor"/>
        </svg>
      </div>
      <h2 class="brand-title">小橘岛</h2>
      <p class="brand-slogan">给宠物一个温暖的家</p>

      <div class="features">
        <div class="feature-item">
          <span class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
            </svg>
          </span>
          <span class="feature-text">优质商品</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
            </svg>
          </span>
          <span class="feature-text">贴心服务</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
            </svg>
          </span>
          <span class="feature-text">社区交流</span>
        </div>
      </div>
    </div>

    <!-- 装饰圆圈 -->
    <div class="decoration-circle circle-1"></div>
    <div class="decoration-circle circle-2"></div>
  </div>
</template>

<script setup lang="ts">
  // 定义 props
  defineProps<{
    hideContent?: boolean // 是否隐藏内容，只显示 logo
  }>()
</script>

<style lang="scss" scoped>
  .login-left-view {
    position: relative;
    box-sizing: border-box;
    width: 70%;
    height: 100%;
    overflow: hidden;
    background: linear-gradient(135deg, #fff5f0 0%, #ffe8dc 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    // 装饰圆圈
    .decoration-circle {
      position: absolute;
      border-radius: 50%;

      &.circle-1 {
        width: 300px;
        height: 300px;
        background: var(--el-color-primary);
        opacity: 0.1;
        top: -100px;
        right: -100px;
      }

      &.circle-2 {
        width: 200px;
        height: 200px;
        background: #ffd4c4;
        opacity: 0.3;
        bottom: -50px;
        left: -50px;
      }
    }

    // 品牌内容
    .brand-content {
      position: relative;
      z-index: 1;
      text-align: center;
      padding: 40px;

      .brand-icon {
        margin-bottom: 20px;
        color: var(--el-color-primary);
        animation: float 3s ease-in-out infinite;

        svg {
          display: inline-block;
        }
      }

      .brand-title {
        font-size: 32px;
        font-weight: 700;
        color: var(--el-color-primary);
        margin: 0 0 8px 0;
      }

      .brand-slogan {
        font-size: 16px;
        color: #666;
        margin: 0 0 48px 0;
      }

      .features {
        display: flex;
        gap: 32px;
        justify-content: center;

        .feature-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;

          .feature-icon {
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--el-color-primary);
          }

          .feature-text {
            font-size: 14px;
            color: #666;
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

    // 响应式 - 小屏幕隐藏左侧
    @media only screen and (width <= 1180px) {
      display: none;
    }
  }

  // 暗色主题
  .dark .login-left-view {
    background: linear-gradient(135deg, #2a1f1a 0%, #1a1412 100%);

    .decoration-circle {
      &.circle-1 {
        opacity: 0.08;
      }

      &.circle-2 {
        background: var(--el-color-primary);
        opacity: 0.05;
      }
    }

    .brand-content {
      .brand-slogan,
      .features .feature-item .feature-text {
        color: #999;
      }
    }
  }
</style>
