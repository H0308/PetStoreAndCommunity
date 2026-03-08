<script setup>
import { Iphone, ChatLineRound, Message } from '@element-plus/icons-vue'
</script>

<template>
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
</template>

<style scoped lang="scss">
.footer {
  background: var(--color-text-primary);
  color: rgba(255, 255, 255, 0.8);
  padding: var(--spacing-4xl) 0 var(--spacing-2xl);
  margin-top: var(--spacing-4xl);

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

@media (max-width: 992px) {
  .footer .footer-content {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .footer .footer-content {
    grid-template-columns: 1fr;
  }
}
</style>
