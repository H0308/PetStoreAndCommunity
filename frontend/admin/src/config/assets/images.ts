/**
 * 配置图片资源
 *
 * 统一管理设置中心使用的预览图片资源。
 * 包含主题样式、菜单布局、菜单风格的预览图。
 *
 * @module config/assets/images
 */

/**
 * 配置中心图片资源对象
 */
export const configImages = {
  /** 系统主题预览图 */
  themeStyles: {
    /** 亮色主题 */
    light: '',
    /** 暗色主题 */
    dark: '',
    /** 自动主题（跟随系统） */
    system: ''
  },
  /** 菜单布局预览图 */
  menuLayouts: {
    /** 左侧菜单 */
    vertical: '',
    /** 顶部菜单 */
    horizontal: '',
    /** 混合菜单 */
    mixed: '',
    /** 双栏菜单 */
    dualColumn: ''
  },
  /** 菜单风格预览图 */
  menuStyles: {
    /** 设计风格 */
    design: '',
    /** 暗色风格 */
    dark: '',
    /** 亮色风格 */
    light: ''
  }
}
