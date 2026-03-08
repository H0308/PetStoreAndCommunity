/**
 * 主题动画工具模块
 *
 * 提供主题相关的过渡效果（当前只支持浅色主题）
 *
 * @module utils/theme/animation
 * @author Art Design Pro Team
 */

/**
 * 主题切换动画（已禁用，只保留浅色主题）
 * @param _e 鼠标点击事件
 */
export const themeAnimation = (_e: any) => {
  // 主题切换已禁用，只保留浅色主题
}

/**
 * 切换主题过渡效果
 * @param enable 是否启用过渡效果
 */
export const toggleTransition = (enable: boolean) => {
  const body = document.body

  if (enable) {
    body.classList.add('theme-change')
  } else {
    setTimeout(() => {
      body.classList.remove('theme-change')
    }, 300)
  }
}
