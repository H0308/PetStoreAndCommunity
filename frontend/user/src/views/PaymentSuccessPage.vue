<script setup>
import { useRouter, useRoute } from 'vue-router'
import { CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 查看所有订单
const goToOrders = () => {
  router.push('/profile/orders')
}

// 返回商城
const goToProducts = () => {
  router.push('/products')
}
</script>

<template>
  <div class="payment-success-page">
    <div class="success-card">
      <div class="success-icon">
        <el-icon :size="100"><CircleCheckFilled /></el-icon>
      </div>
      
      <h1 class="success-title">支付成功！</h1>
      <p class="success-desc">感谢您的购买，我们将尽快为您发货</p>
      
      <div class="action-buttons">
        <el-button size="large" @click="goToOrders">
          查看所有订单信息
        </el-button>
        <el-button type="primary" size="large" @click="goToProducts">
          返回商城继续购物
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.payment-success-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg-base);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2xl);
}

.success-card {
  background: var(--color-bg-surface);
  border-radius: var(--radius-xl);
  padding: var(--spacing-5xl) var(--spacing-4xl);
  text-align: center;
  box-shadow: var(--shadow-lg);
  max-width: 500px;
  width: 100%;
}

.success-icon {
  color: var(--color-accent);
  margin-bottom: var(--spacing-xl);
  
  .el-icon {
    filter: drop-shadow(0 4px 12px rgba(47, 143, 106, 0.3));
  }
}

.success-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-base);
}

.success-desc {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-3xl);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-base);
  
  :deep(.el-button) {
    width: 100%;
    height: 48px;
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-semibold);
    border-radius: 24px;
    transition: all var(--transition-base) var(--ease-out);
    
    // 查看订单按钮（描边样式）
    &:not(.el-button--primary) {
      background: #fff;
      border: 2px solid var(--color-primary);
      color: var(--color-primary);
      
      &:hover {
        background: var(--color-secondary-light);
        border-color: var(--color-primary-dark);
        color: var(--color-primary-dark);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(255, 138, 91, 0.2);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
    
    // 返回商城按钮（实心样式）
    &.el-button--primary {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      border: none;
      color: #fff;
      box-shadow: 0 4px 16px rgba(255, 138, 91, 0.35);
      
      &:hover {
        background: linear-gradient(135deg, var(--color-primary-light) 0%, var(--color-primary) 100%);
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(255, 138, 91, 0.45);
      }
      
      &:active {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(255, 138, 91, 0.3);
      }
    }
  }
}

@media (min-width: 480px) {
  .action-buttons {
    flex-direction: row;
    justify-content: center;
    
    :deep(.el-button) {
      width: auto;
      min-width: 180px;
    }
  }
}
</style>
