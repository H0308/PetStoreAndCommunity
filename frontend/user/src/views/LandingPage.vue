<script>
export default {
  name: 'LandingPage'
}
</script>

<script setup>
import { ref, markRaw } from 'vue'
import { ArrowRight, ShoppingBag, MessageBox, Service, Iphone, ChatLineRound, Message } from '@element-plus/icons-vue'
import CatScene from '@/components/CatScene.vue'

// 轮播文字数据
const carouselTexts = ref([
  {
    title: '给宠物一个温暖的家',
    subtitle: '优质商品 · 贴心服务 · 温馨社区'
  },
  {
    title: '精选优质宠物用品',
    subtitle: '健康安全 · 品质保障 · 价格实惠'
  },
  {
    title: '加入宠物爱好者社区',
    subtitle: '分享经验 · 结交好友 · 共同成长'
  }
])


// 核心功能展示数据
const features = ref([
  {
    icon: markRaw(ShoppingBag),
    title: '商城',
    description: '精选全球优质宠物用品，从食品到玩具，应有尽有。严格把控品质，让您的宠物享受最好的。',
    image: 'https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600&h=400&fit=crop',
    align: 'left'
  },
  {
    icon: markRaw(MessageBox),
    title: '宠物社区',
    description: '与全国宠物爱好者交流分享，晒出您的萌宠，记录成长点滴。在这里找到志同道合的朋友。',
    image: 'https://images.unsplash.com/photo-1450778869180-41d0601e046e?w=600&h=400&fit=crop',
    align: 'right'
  },
  {
    icon: markRaw(Service),
    title: '专业服务',
    description: '高响应式客服，随时解答你的疑问；AI智能聊天助手，随时帮助你了解宠物知识',
    image: 'https://images.unsplash.com/photo-1628009368231-7bb7cfcb0def?w=600&h=400&fit=crop',
    align: 'left'
  }
])

const emit = defineEmits(['start-shopping'])
</script>

<template>
  <div class="landing-page">
    <!-- 英雄区 / Mesh Gradient 背景 -->
    <section class="hero-section">
      <!-- Mesh Gradient 色块层 -->
      <div class="mesh-bg" aria-hidden="true">
        <div class="mesh-blob mesh-blob-1"></div>
        <div class="mesh-blob mesh-blob-2"></div>
        <div class="mesh-blob mesh-blob-3"></div>
        <div class="mesh-blob mesh-blob-4"></div>
      </div>


      <!-- 3D 猫咪 -->
      <div class="cat-container">
        <CatScene />
      </div>

      <div class="hero-content">
        <el-carousel 
          :interval="5000" 
          arrow="never" 
          height="auto"
          indicator-position="none"
          class="text-carousel"
        >
          <el-carousel-item v-for="(item, index) in carouselTexts" :key="index">
            <h1 class="hero-title">{{ item.title }}</h1>
            <p class="hero-subtitle">{{ item.subtitle }}</p>
          </el-carousel-item>
        </el-carousel>
        <div class="hero-actions">
          <el-button 
            type="primary" 
            size="large" 
            class="cta-button"
            @click="emit('start-shopping')"
          >
            立即选购
            <el-icon class="ml-sm" :size="16"><ArrowRight /></el-icon>
          </el-button>
          <el-button 
            size="large" 
            class="secondary-button"
            @click="$emit('navigate', 'community')"
          >
            畅游贴吧
          </el-button>
        </div>
      </div>

      <!-- 滚动提示 -->
      <div class="scroll-indicator">
        <span class="scroll-text">向下滚动探索更多</span>
        <div class="scroll-arrow">↓</div>
      </div>
    </section>



    <!-- 核心功能展示区 -->
    <section class="features-section">
      <div class="container-base">
        <div class="section-header">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">为宠物和铲屎官提供一站式服务</p>
        </div>

        <div class="features-list">
          <div 
            v-for="(feature, index) in features" 
            :key="index" 
            class="feature-block"
            :class="{ 'feature-reverse': feature.align === 'right' }"
          >
            <div class="feature-image">
              <img :src="feature.image" :alt="feature.title" />
            </div>
            <div class="feature-content">
              <div class="feature-icon">
                <el-icon :size="40"><component :is="feature.icon" /></el-icon>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-description">{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA 区域 -->
    <section class="cta-section">
      <div class="container-base">
        <div class="cta-content">
          <h2 class="cta-title">准备好开始了吗？</h2>
          <p class="cta-subtitle">加入我们，给您的宠物最好的关爱</p>
          <el-button 
            type="primary" 
            size="large" 
            class="cta-button"
            @click="emit('start-shopping')"
          >
            立即开始
            <el-icon class="ml-sm" :size="16"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container-base">
        <div class="footer-content">
          <div class="footer-section">
            <h4 class="footer-title">关于我们</h4>
            <ul class="footer-links">
              <li><a href="#">公司简介</a></li>
              <li><a href="#">联系我们</a></li>
              <li><a href="#">加入我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4 class="footer-title">客户服务</h4>
            <ul class="footer-links">
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">售后服务</a></li>
              <li><a href="#">配送说明</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4 class="footer-title">法律条款</h4>
            <ul class="footer-links">
              <li><a href="#">隐私政策</a></li>
              <li><a href="#">服务条款</a></li>
              <li><a href="#">退换货政策</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4 class="footer-title">关注我们</h4>
            <div class="social-links">
              <a href="#" class="social-icon">
                <el-icon :size="20"><Iphone /></el-icon>
              </a>
              <a href="#" class="social-icon">
                <el-icon :size="20"><ChatLineRound /></el-icon>
              </a>
              <a href="#" class="social-icon">
                <el-icon :size="20"><Message /></el-icon>
              </a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2025 小橘岛 - 给宠物一个温暖的家</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped lang="scss">
.landing-page {
  width: 100%;
}

// 英雄区
.hero-section {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: #d4e8e0; // 底色，防止色块间隙露白
}

// Mesh Gradient
.mesh-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.mesh-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  will-change: transform;
}

.mesh-blob-1 {
  width: 70vw;
  height: 70vw;
  background: radial-gradient(circle, rgba(47, 143, 106, 0.75) 0%, transparent 70%);
  top: -20%;
  left: -15%;
  animation: blobMove1 18s ease-in-out infinite;
}

.mesh-blob-2 {
  width: 65vw;
  height: 65vw;
  background: radial-gradient(circle, rgba(255, 138, 91, 0.70) 0%, transparent 70%);
  top: 10%;
  right: -20%;
  animation: blobMove2 22s ease-in-out infinite;
}

.mesh-blob-3 {
  width: 55vw;
  height: 55vw;
  background: radial-gradient(circle, rgba(255, 210, 184, 0.80) 0%, transparent 70%);
  bottom: -15%;
  left: 20%;
  animation: blobMove3 16s ease-in-out infinite;
}

.mesh-blob-4 {
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.65) 0%, transparent 70%);
  top: 30%;
  left: 35%;
  animation: blobMove4 20s ease-in-out infinite;
}

@keyframes blobMove1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%       { transform: translate(8vw, 6vh) scale(1.08); }
  66%       { transform: translate(-4vw, 10vh) scale(0.95); }
}
@keyframes blobMove2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%       { transform: translate(-10vw, 8vh) scale(1.1); }
  66%       { transform: translate(5vw, -6vh) scale(0.92); }
}
@keyframes blobMove3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  40%       { transform: translate(6vw, -10vh) scale(1.12); }
  70%       { transform: translate(-8vw, -4vh) scale(0.96); }
}
@keyframes blobMove4 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  30%       { transform: translate(-6vw, 8vh) scale(1.15); }
  65%       { transform: translate(8vw, 4vh) scale(0.9); }
}


// 3D 猫咪容器
.cat-container {
  position: absolute;
  right: -2%;
  top: 0;
  width: 55%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.hero-content {
  position: absolute;
  left: 12%;
  top: 0;
  z-index: 2;
  height: 100%;
  width: 44%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

  .text-carousel {
    width: 100%;
    margin-bottom: var(--spacing-xl);
    
    :deep(.el-carousel__container) {
      height: auto !important;
      min-height: 150px;
    }
    
    :deep(.el-carousel__item) {
      display: flex;
      flex-direction: column;
      justify-content: flex-start;
    }
  }

  .hero-title {
    font-size: var(--font-size-4xl);
    font-weight: var(--font-weight-bold);
    margin-bottom: var(--spacing-lg);
    line-height: 1.2;
    max-width: 600px;
    color: white;
  }

  .hero-subtitle {
    font-size: var(--font-size-xl);
    font-weight: var(--font-weight-medium);
    opacity: 0.95;
    color: white;
  }

  .hero-actions {
    display: flex;
    gap: var(--spacing-lg);
    margin-top: 0;

    .cta-button {
      height: 56px;
      padding: 0 var(--spacing-3xl);
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      border-radius: var(--radius-lg);

      &:hover {
        transform: translateY(-3px);
      }
      
      // 修复按钮图标样式
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

    .secondary-button {
      height: 56px;
      padding: 0 var(--spacing-3xl);
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      border-radius: var(--radius-lg);
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      border: 2px solid rgba(255, 255, 255, 0.5);
      color: white;

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: translateY(-3px);
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.scroll-indicator {
  position: absolute;
  bottom: var(--spacing-4xl);
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
  text-align: center;
  color: white;
  animation: bounce 2s infinite;

  .scroll-text {
    font-size: var(--font-size-sm);
    display: block;
    margin-bottom: var(--spacing-sm);
    opacity: 0.9;
  }

  .scroll-arrow {
    font-size: var(--font-size-2xl);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  40% {
    transform: translateX(-50%) translateY(-10px);
  }
  60% {
    transform: translateX(-50%) translateY(-5px);
  }
}



// 功能展示区
.features-section {
  padding: var(--spacing-5xl) 0;
}

.section-header {
  text-align: center;
  margin-bottom: var(--spacing-5xl);

  .section-title {
    font-size: var(--font-size-3xl);
    font-weight: var(--font-weight-bold);
    color: var(--color-text-primary);
    margin-bottom: var(--spacing-md);
  }

  .section-subtitle {
    font-size: var(--font-size-lg);
    color: var(--color-text-tertiary);
  }
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-5xl);
}

.feature-block {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-4xl);
  align-items: center;

  &.feature-reverse {
    .feature-image {
      order: 2;
    }

    .feature-content {
      order: 1;
    }
  }

  .feature-image {
    img {
      width: 100%;
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-lg);
      transition: transform var(--transition-slow) var(--ease-out);

      &:hover {
        transform: scale(1.02);
      }
    }
  }

  .feature-content {
    .feature-icon {
      width: 72px;
      height: 72px;
      border-radius: var(--radius-lg);
      background: linear-gradient(135deg, var(--color-secondary-light) 0%, var(--color-secondary) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--color-primary);
      margin-bottom: var(--spacing-xl);
    }

    .feature-title {
      font-size: var(--font-size-2xl);
      font-weight: var(--font-weight-bold);
      color: var(--color-text-primary);
      margin-bottom: var(--spacing-lg);
    }

    .feature-description {
      font-size: var(--font-size-md);
      color: var(--color-text-secondary);
      line-height: var(--line-height-relaxed);
    }
  }
}

// CTA区域
.cta-section {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  padding: var(--spacing-5xl) 0;
  text-align: center;

  .cta-content {
    .cta-title {
      font-size: var(--font-size-3xl);
      font-weight: var(--font-weight-bold);
      color: white;
      margin-bottom: var(--spacing-md);
    }

    .cta-subtitle {
      font-size: var(--font-size-lg);
      color: rgba(255, 255, 255, 0.9);
      margin-bottom: var(--spacing-3xl);
    }

    .cta-button {
      height: 56px;
      padding: 0 var(--spacing-4xl);
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
      background: white;
      color: var(--color-primary);
      border: none;
      border-radius: var(--radius-lg);

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
      }
      
      // 修复按钮图标样式
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
  }
}

// 页脚
.footer {
  background: var(--color-text-primary);
  color: rgba(255, 255, 255, 0.8);
  padding: var(--spacing-4xl) 0 var(--spacing-2xl);

  .footer-content {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-3xl);
    margin-bottom: var(--spacing-3xl);

    .footer-section {
      .footer-title {
        color: white;
        font-size: var(--font-size-md);
        font-weight: var(--font-weight-semibold);
        margin-bottom: var(--spacing-lg);
      }

      .footer-links {
        list-style: none;
        padding: 0;
        margin: 0;

        li {
          margin-bottom: var(--spacing-sm);

          a {
            color: rgba(255, 255, 255, 0.7);
            text-decoration: none;
            font-size: var(--font-size-sm);
            transition: color var(--transition-base);

            &:hover {
              color: var(--color-primary);
            }
          }
        }
      }

      .social-links {
        display: flex;
        gap: var(--spacing-base);

        .social-icon {
          width: 40px;
          height: 40px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(255, 255, 255, 0.1);
          border-radius: 50%;
          text-decoration: none;
          transition: all var(--transition-base);
          color: rgba(255, 255, 255, 0.8);

          &:hover {
            background: var(--color-primary);
            color: white;
            transform: translateY(-3px);
          }
          
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
      }
    }
  }

  .footer-bottom {
    text-align: center;
    padding-top: var(--spacing-2xl);
    border-top: 1px solid rgba(255, 255, 255, 0.1);

    p {
      font-size: var(--font-size-sm);
      color: rgba(255, 255, 255, 0.6);
      margin: 0;
    }
  }
}

// 响应式
@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .feature-block {
    grid-template-columns: 1fr !important;

    .feature-image,
    .feature-content {
      order: initial !important;
    }
  }

  .footer-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-content .hero-title {
    font-size: var(--font-size-2xl);
  }

  .hero-content .hero-subtitle {
    font-size: var(--font-size-md);
  }

  .hero-content {
    left: 4%;
    width: 92%;
  }

  .cat-container {
    display: none;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .footer-content {
    grid-template-columns: 1fr;
  }
}
</style>
