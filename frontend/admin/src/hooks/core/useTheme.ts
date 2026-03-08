/**
 * useTheme - 系统主题管理
 *
 * 提供主题管理功能，当前只支持浅色模式。
 *
 * ## 主要功能
 *
 * 1. 主题设置 - 设置浅色主题
 * 2. 颜色适配 - 自动调整主题色的明暗变体（9 个层级）
 * 3. 过渡优化 - 切换时临时禁用过渡效果，避免闪烁
 * 4. 状态持久化 - 主题设置自动保存到 store
 *
 * @module useTheme
 * @author Art Design Pro Team
 */

import { useSettingStore } from '@/store/modules/setting'
import { SystemThemeEnum } from '@/enums/appEnum'
import AppConfig from '@/config'
import { SystemThemeTypes } from '@/types/store'
import { getLightColor, setElementThemeColor } from '@/utils/ui'

export function useTheme() {
  const settingStore = useSettingStore()

  // 禁用过渡效果
  const disableTransitions = () => {
    const style = document.createElement('style')
    style.setAttribute('id', 'disable-transitions')
    style.textContent = '* { transition: none !important; }'
    document.head.appendChild(style)
  }

  // 启用过渡效果
  const enableTransitions = () => {
    const style = document.getElementById('disable-transitions')
    if (style) {
      style.remove()
    }
  }

  // 设置系统主题（固定为浅色）
  const setSystemTheme = (theme: SystemThemeEnum = SystemThemeEnum.LIGHT, themeMode?: SystemThemeEnum) => {
    // 临时禁用过渡效果
    disableTransitions()

    const el = document.getElementsByTagName('html')[0]

    if (!themeMode) {
      themeMode = theme
    }

    const currentTheme = AppConfig.systemThemeStyles[theme as keyof SystemThemeTypes]

    if (currentTheme) {
      el.setAttribute('class', currentTheme.className)
    }

    // 设置按钮颜色变浅（浅色主题）
    const primary = settingStore.systemThemeColor

    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(
        `--el-color-primary-light-${i}`,
        `${getLightColor(primary, i / 10)}`
      )
    }

    // 更新store中的主题设置
    settingStore.setGlopTheme(theme, themeMode)

    // 使用 requestAnimationFrame 确保在下一帧恢复过渡效果
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        enableTransitions()
      })
    })
  }

  // 切换主题（固定为浅色）
  const switchThemeStyles = (_theme?: SystemThemeEnum) => {
    setSystemTheme(SystemThemeEnum.LIGHT)
  }

  return {
    setSystemTheme,
    switchThemeStyles
  }
}

/**
 * 初始化主题系统（固定为浅色主题）
 */
export function initializeTheme() {
  const settingStore = useSettingStore()

  const el = document.getElementsByTagName('html')[0]

  // 固定使用浅色主题
  const currentTheme = AppConfig.systemThemeStyles[SystemThemeEnum.LIGHT as keyof SystemThemeTypes]
  if (currentTheme) {
    el.setAttribute('class', currentTheme.className)
  }

  // 确保 store 中的主题设置为浅色
  settingStore.systemThemeType = SystemThemeEnum.LIGHT
  settingStore.systemThemeMode = SystemThemeEnum.LIGHT

  // 设置主题颜色
  setElementThemeColor(settingStore.systemThemeColor)

  // 设置圆角
  document.documentElement.style.setProperty('--custom-radius', `${settingStore.customRadius}rem`)
}
