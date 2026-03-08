import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { setupMessageDedup } from './utils/message.js'

// 导入全局样式（包含 Element Plus 主题覆盖）
import './styles/index.scss'

// 全局消息去重（相同内容 3 秒内只弹一次）
setupMessageDedup()

const app = createApp(App)

// 注册 Vue Router
app.use(router)

// 注册 Element Plus（使用中文语言包）
app.use(ElementPlus, { locale: zhCn })

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
