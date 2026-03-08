<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ArrowUp } from '@element-plus/icons-vue'

// 是否显示按钮
const showBackTop = ref(false)

// 滚动进度百分比 (0-100)
const scrollProgress = ref(0)

// SVG 圆环参数
const circleRadius = 21
const circleCircumference = 2 * Math.PI * circleRadius

// 计算圆环的 stroke-dashoffset
const progressOffset = computed(() => {
  return circleCircumference * (1 - scrollProgress.value / 100)
})

// 滚动监听
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  
  showBackTop.value = scrollTop > 500
  
  if (docHeight > 0) {
    scrollProgress.value = Math.min(100, (scrollTop / docHeight) * 100)
  }
}

// 回到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div 
    class="back-top-btn"
    :class="{ visible: showBackTop }"
    @click="scrollToTop"
  >
    <svg class="progress-ring" viewBox="0 0 48 48">
      <circle
        class="progress-ring-bg"
        cx="24"
        cy="24"
        :r="circleRadius"
        fill="none"
        stroke="#EEEEEE"
        stroke-width="3"
      />
      <circle
        class="progress-ring-circle"
        cx="24"
        cy="24"
        :r="circleRadius"
        fill="none"
        stroke="#FF8A5B"
        stroke-width="3"
        stroke-linecap="round"
        :stroke-dasharray="circleCircumference"
        :stroke-dashoffset="progressOffset"
      />
    </svg>
    <div class="back-top-icon">
      <el-icon :size="20"><ArrowUp /></el-icon>
    </div>
    <span class="item-tooltip">回到顶部</span>
  </div>
</template>

<style scoped lang="scss">
.back-top-btn {
  position: fixed;
  right: 20px;
  bottom: 100px;
  width: 48px;
  height: 48px;
  background: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  z-index: var(--z-index-fixed);
  opacity: 0;
  pointer-events: none;
  transform: translateY(20px);
  transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);

  &.visible {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }

  &:hover {
    background: var(--color-primary);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(255, 138, 91, 0.3);

    .back-top-icon {
      color: #FFFFFF;
    }

    .progress-ring-circle {
      stroke: rgba(255, 255, 255, 0.6);
    }

    .progress-ring-bg {
      stroke: rgba(255, 255, 255, 0.2);
    }

    .item-tooltip {
      opacity: 1;
      visibility: visible;
      transform: translateY(-50%) translateX(-4px);
    }
  }
}

.progress-ring {
  position: absolute;
  width: 48px;
  height: 48px;
  transform: rotate(-90deg);
}

.progress-ring-bg {
  transition: stroke 0.3s ease;
}

.progress-ring-circle {
  transition: stroke-dashoffset 0.1s ease, stroke 0.3s ease;
}

.back-top-icon {
  position: relative;
  z-index: 1;
  color: var(--color-primary);
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-tooltip {
  position: absolute;
  right: calc(100% + 12px);
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.75);
  color: #FFFFFF;
  font-size: 12px;
  border-radius: 6px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  pointer-events: none;

  &::after {
    content: '';
    position: absolute;
    left: 100%;
    top: 50%;
    transform: translateY(-50%);
    border: 6px solid transparent;
    border-left-color: rgba(0, 0, 0, 0.75);
  }
}

@media (max-width: 768px) {
  .back-top-btn {
    right: 12px;
    bottom: 80px;
    width: 44px;
    height: 44px;
  }

  .progress-ring {
    width: 44px;
    height: 44px;
  }
}
</style>
