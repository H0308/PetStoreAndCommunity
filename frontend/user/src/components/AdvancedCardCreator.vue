<template>
  <div class="advanced-card-creator">
    <!-- 左侧控制区 -->
    <div class="control-section">
      <!-- 标题输入 -->
      <div class="input-group">
        <label class="input-label">标题</label>
        <el-input
          v-model="title"
          placeholder="输入帖子标题..."
          maxlength="20"
          show-word-limit
          class="title-input"
        />
      </div>

      <!-- 正文输入 -->
      <div class="input-group content-editor-group">
        <label class="input-label">正文</label>
        <TextCodeEditor
          v-model="content"
          placeholder="分享你和毛孩子的故事，支持 Markdown 语法"
          :max-length="2000"
        />
      </div>

      <!-- 模版选择器 -->
      <div class="input-group">
        <label class="input-label">选择模版</label>
        <div class="template-carousel">
          <div
            v-for="tpl in templates"
            :key="tpl.id"
            class="template-thumb"
            :class="{ active: selectedTemplate === tpl.id }"
            @click="selectedTemplate = tpl.id"
          >
            <div class="thumb-preview" :class="`thumb-${tpl.id}`">
              <span class="thumb-title">Aa</span>
            </div>
            <span class="thumb-name">{{ tpl.name }}</span>
          </div>
        </div>
      </div>

      <!-- 配色选择（仅部分模版支持） -->
      <div class="input-group" v-if="currentTemplate.colors">
        <label class="input-label">选择配色</label>
        <div class="color-selector">
          <div
            v-for="(color, index) in currentTemplate.colors"
            :key="index"
            class="color-item"
            :class="{ active: selectedColorIndex === index }"
            :style="{ background: color.bg }"
            @click="selectedColorIndex = index"
          >
            <el-icon v-if="selectedColorIndex === index" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>

      <!-- 随机按钮 -->
      <el-button class="random-btn" @click="randomize">
        <el-icon><Refresh /></el-icon>
        随机一下
      </el-button>
    </div>

    <!-- 右侧预览区 -->
    <div class="preview-section">
      <label class="input-label">预览效果</label>
      <div class="preview-wrapper">
        <!-- 卡片预览 DOM - 用于截图 -->
        <div 
          ref="cardPreviewRef"
          class="card-preview"
          :class="[`template-${selectedTemplate}`, currentColorClass]"
        >
          <!-- 模版 A: 杂志封面风 -->
          <template v-if="selectedTemplate === 'magazine'">
            <div class="magazine-circle"></div>
            <div class="magazine-content">
              <h1 class="magazine-title">{{ displayTitle }}</h1>
              <span class="magazine-author">@{{ username }}</span>
            </div>
          </template>

          <!-- 模版 B: 萌宠手账风 -->
          <template v-else-if="selectedTemplate === 'cute'">
            <div class="cute-tape cute-tape-tl"></div>
            <div class="cute-tape cute-tape-tr"></div>
            <div class="cute-tape cute-tape-bl"></div>
            <div class="cute-tape cute-tape-br"></div>
            <div class="cute-content">
              <h1 class="cute-title">{{ displayTitle }}</h1>
              <span class="cute-author">@{{ username }}</span>
            </div>
            <div class="cute-paw"></div>
          </template>

          <!-- 模版 C: 文艺语录风 -->
          <template v-else-if="selectedTemplate === 'quote'">
            <div class="quote-mark quote-mark-open">"</div>
            <div class="quote-mark quote-mark-close">"</div>
            <div class="quote-content">
              <h1 class="quote-title">{{ displayTitle }}</h1>
              <span class="quote-author">—— @{{ username }}</span>
            </div>
          </template>

          <!-- 模版 D: 波普艺术风 -->
          <template v-else-if="selectedTemplate === 'pop'">
            <div class="pop-shapes">
              <div class="pop-triangle pop-triangle-1"></div>
              <div class="pop-triangle pop-triangle-2"></div>
              <div class="pop-wave"></div>
            </div>
            <div class="pop-content">
              <h1 class="pop-title">{{ displayTitle }}</h1>
              <span class="pop-author">@{{ username }}</span>
            </div>
          </template>
        </div>
      </div>
      <p class="preview-hint">发布时将自动生成封面图</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Check, Refresh } from '@element-plus/icons-vue'
import html2canvas from 'html2canvas'
import TextCodeEditor from './TextCodeEditor.vue'

const props = defineProps({
  username: {
    type: String,
    default: '匿名用户'
  },
  initialTitle: {
    type: String,
    default: ''
  },
  initialContent: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:title', 'update:content'])

// 表单数据
const title = ref(props.initialTitle)
const content = ref(props.initialContent)
const selectedTemplate = ref('magazine')
const selectedColorIndex = ref(0)

// 预览 DOM 引用
const cardPreviewRef = ref(null)

// 模版配置
const templates = [
  {
    id: 'magazine',
    name: '杂志封面',
    colors: [
      { bg: '#FFFFFF', accent: '#FF8A5B', text: '#2A2A2A' },
      { bg: '#F5F5F5', accent: '#2F8F6A', text: '#2A2A2A' },
      { bg: '#FFF8F4', accent: '#FF6B6B', text: '#2A2A2A' }
    ]
  },
  {
    id: 'cute',
    name: '萌宠手账',
    colors: [
      { bg: '#FFFEF0', accent: '#FFD93D', text: '#5A4A3A' },
      { bg: '#FFF0F5', accent: '#FF9A9E', text: '#5A4A4A' },
      { bg: '#F0FFF4', accent: '#96C93D', text: '#3A5A4A' }
    ]
  },
  {
    id: 'quote',
    name: '文艺语录',
    colors: [
      { bg: 'linear-gradient(135deg, #2F8F6A 0%, #1a5276 100%)', text: '#FFFFFF' },
      { bg: 'linear-gradient(135deg, #2C3E50 0%, #4CA1AF 100%)', text: '#FFFFFF' },
      { bg: 'linear-gradient(135deg, #434343 0%, #000000 100%)', text: '#FFFFFF' }
    ]
  },
  {
    id: 'pop',
    name: '波普艺术',
    colors: [
      { bg: '#FF8A5B', accent: '#FFFFFF', text: '#FFFFFF' },
      { bg: '#FFD93D', accent: '#FF6B6B', text: '#2A2A2A' },
      { bg: '#6C5CE7', accent: '#FFEAA7', text: '#FFFFFF' }
    ]
  }
]

// 当前模版
const currentTemplate = computed(() => {
  return templates.find(t => t.id === selectedTemplate.value) || templates[0]
})

// 当前配色 class
const currentColorClass = computed(() => {
  return `color-${selectedColorIndex.value}`
})

// 显示文本
const displayTitle = computed(() => title.value || '在这里输入标题')
const displayContent = computed(() => content.value || '在这里输入正文内容...')

// 随机切换
const randomize = () => {
  if (currentTemplate.value.colors) {
    const newIndex = Math.floor(Math.random() * currentTemplate.value.colors.length)
    selectedColorIndex.value = newIndex
  }
}

// 同步数据到父组件
watch(title, (val) => emit('update:title', val))
watch(content, (val) => emit('update:content', val))

// 切换模版时重置配色
watch(selectedTemplate, () => {
  selectedColorIndex.value = 0
})

// 生成图片
const generateImageBlob = async () => {
  const element = cardPreviewRef.value
  if (!element) throw new Error('Preview element not found')

  const canvas = await html2canvas(element, {
    scale: 3,
    useCORS: true,
    backgroundColor: null,
    logging: false
  })

  return new Promise((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        const timestamp = Date.now()
        const file = new File([blob], `card_${timestamp}.png`, { type: 'image/png' })
        resolve(file)
      } else {
        reject(new Error('Failed to generate image'))
      }
    }, 'image/png', 1.0)
  })
}

// 验证表单
const validate = () => {
  if (!title.value.trim()) {
    return { valid: false, message: '请输入标题' }
  }
  if (!content.value.trim()) {
    return { valid: false, message: '请输入正文内容' }
  }
  return { valid: true }
}

// 获取表单数据
const getFormData = () => ({
  title: title.value.trim(),
  content: content.value.trim(),
  template: selectedTemplate.value,
  colorIndex: selectedColorIndex.value
})

defineExpose({
  generateImageBlob,
  validate,
  getFormData
})
</script>

<style lang="scss" scoped>
.advanced-card-creator {
  display: flex;
  gap: var(--spacing-3xl);
  width: 100%;
  
  @media (max-width: 900px) {
    flex-direction: column;
  }
}

// 左侧控制区
.control-section {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.input-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.title-input {
  :deep(.el-input__wrapper) {
    border-radius: var(--radius-lg);
    box-shadow: none;
    border: 1px solid var(--color-border-base);
    padding: 10px 14px;
    
    .el-input__inner {
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-semibold);
    }
    
    &:hover { border-color: var(--color-primary-light); }
    &.is-focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(255, 138, 91, 0.15);
    }
  }
}

.content-editor-group {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  
  .text-code-editor {
    flex: 1;
    display: flex;
    flex-direction: column;
    
    :deep(.editor-content) {
      flex: 1;
      
      .ProseMirror {
        min-height: 200px;
      }
    }
  }
}

// 模版选择器
.template-carousel {
  display: flex;
  gap: var(--spacing-md);
  overflow-x: auto;
  padding: var(--spacing-sm);
  margin: calc(-1 * var(--spacing-sm));
  
  &::-webkit-scrollbar { height: 4px; }
  &::-webkit-scrollbar-thumb {
    background: var(--color-border-base);
    border-radius: 2px;
  }
}

.template-thumb {
  flex-shrink: 0;
  cursor: pointer;
  text-align: center;
  transition: all var(--transition-base);
  
  &:hover .thumb-preview {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  }
  
  &.active .thumb-preview {
    box-shadow: 0 0 0 3px var(--color-primary);
  }
}

.thumb-preview {
  width: 70px;
  height: 93px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  
  .thumb-title {
    font-weight: bold;
    font-size: 18px;
  }
}

// 缩略图样式
.thumb-magazine {
  background: #fff;
  .thumb-title { color: #2A2A2A; }
  &::before {
    content: '';
    position: absolute;
    bottom: 8px;
    left: 8px;
    width: 24px;
    height: 24px;
    background: #FF8A5B;
    border-radius: 50%;
  }
}

.thumb-cute {
  background: linear-gradient(#FFFEF0, #FFFEF0) padding-box,
              repeating-linear-gradient(0deg, #E8E0C8 0, #E8E0C8 1px, transparent 1px, transparent 20px) border-box;
  .thumb-title { color: #5A4A3A; font-family: 'Comic Sans MS', cursive; }
}

.thumb-quote {
  background: linear-gradient(135deg, #2F8F6A 0%, #1a5276 100%);
  .thumb-title { color: #fff; font-family: 'Georgia', serif; }
}

.thumb-pop {
  background: #FF8A5B;
  background-image: radial-gradient(#fff 15%, transparent 16%);
  background-size: 12px 12px;
  .thumb-title { color: #fff; text-shadow: 2px 2px 0 #2A2A2A; }
}

.thumb-name {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: var(--spacing-xs);
}

// 配色选择器
.color-selector {
  display: flex;
  gap: var(--spacing-md);
}

.color-item {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all var(--transition-base);
  
  &:hover { transform: scale(1.1); }
  &.active { box-shadow: 0 0 0 3px rgba(255, 138, 91, 0.4); }
  
  .check-icon {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #fff;
    font-size: 18px;
    filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.5));
  }
}

.random-btn {
  margin-top: var(--spacing-sm);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--color-border-base);
  background: transparent;
  color: var(--color-text-secondary);
  height: 42px;
  
  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
    background: rgba(255, 138, 91, 0.05);
  }
}

// 右侧预览区
.preview-section {
  flex-shrink: 0;
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  
  @media (max-width: 900px) {
    width: 100%;
    max-width: 320px;
    align-items: center;
  }
}

.preview-wrapper {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.preview-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  text-align: center;
  margin: 0;
}

// ==================== 卡片预览基础样式 ====================
.card-preview {
  width: 100%;
  aspect-ratio: 3 / 4;
  position: relative;
  overflow: hidden;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

// ==================== 模版 A: 杂志封面风 ====================
.template-magazine {
  background: #FFFFFF;
  
  &.color-1 { background: #F5F5F5; .magazine-circle { background: #2F8F6A; } }
  &.color-2 { background: #FFF8F4; .magazine-circle { background: #FF6B6B; } }
  
  .magazine-circle {
    position: absolute;
    bottom: -15%;
    left: -15%;
    width: 55%;
    aspect-ratio: 1;
    background: #FF8A5B;
    border-radius: 50%;
  }
  
  .magazine-content {
    position: relative;
    z-index: 1;
    padding: 12% 8%;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  .magazine-title {
    font-size: clamp(28px, 8vw, 42px);
    font-weight: 900;
    color: #2A2A2A;
    line-height: 1.15;
    margin: 0 0 6% 0;
    word-break: break-all;
    overflow-wrap: break-word;
    letter-spacing: -1px;
    max-width: 100%;
  }
  
  .magazine-text {
    font-size: clamp(14px, 3.5vw, 16px);
    color: #5A5A5A;
    line-height: 1.8;
    margin: 0;
    flex: 1;
  }
  
  .magazine-author {
    font-size: clamp(12px, 3vw, 14px);
    color: #8F8F8F;
    margin-top: auto;
  }
}

// ==================== 模版 B: 萌宠手账风 ====================
.template-cute {
  background: #FFFEF0;
  background-image: 
    linear-gradient(rgba(200, 180, 140, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(200, 180, 140, 0.15) 1px, transparent 1px);
  background-size: 24px 24px;
  
  &.color-1 {
    background-color: #FFF0F5;
    .cute-tape { background: #FF9A9E; }
    .cute-title, .cute-text { color: #5A4A4A; }
  }
  &.color-2 {
    background-color: #F0FFF4;
    .cute-tape { background: #96C93D; }
    .cute-title, .cute-text { color: #3A5A4A; }
  }
  
  .cute-tape {
    position: absolute;
    width: 16%;
    height: 6%;
    background: #FFD93D;
    opacity: 0.85;
    z-index: 2;
    
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: repeating-linear-gradient(
        45deg,
        transparent,
        transparent 4px,
        rgba(255,255,255,0.3) 4px,
        rgba(255,255,255,0.3) 8px
      );
    }
  }
  
  .cute-tape-tl { top: 4%; left: 4%; transform: rotate(-15deg); }
  .cute-tape-tr { top: 4%; right: 4%; transform: rotate(15deg); }
  .cute-tape-bl { bottom: 4%; left: 4%; transform: rotate(15deg); }
  .cute-tape-br { bottom: 4%; right: 4%; transform: rotate(-15deg); }
  
  .cute-content {
    position: relative;
    z-index: 1;
    padding: 14% 10%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .cute-title {
    font-size: clamp(22px, 6vw, 28px);
    font-weight: 700;
    color: #5A4A3A;
    margin: 0 0 5% 0;
    font-family: 'Comic Sans MS', 'PingFang SC', cursive;
    word-break: break-all;
    overflow-wrap: break-word;
    max-width: 100%;
  }
  
  .cute-text {
    font-size: clamp(14px, 3.5vw, 16px);
    color: #6A5A4A;
    line-height: 2;
    margin: 0;
    flex: 1;
    font-family: 'Comic Sans MS', 'PingFang SC', cursive;
  }
  
  .cute-author {
    font-size: clamp(12px, 3vw, 14px);
    color: #9A8A7A;
    margin-top: auto;
  }
  
  .cute-paw {
    position: absolute;
    right: 10%;
    bottom: 15%;
    width: 13%;
    height: 13%;
    opacity: 0.15;
    background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'%3E%3Cpath fill='%235A4A3A' d='M226.5 92.9c14.3 42.9-.3 86.2-32.6 96.8s-70.1-15.6-84.4-58.5s.3-86.2 32.6-96.8s70.1 15.6 84.4 58.5zM100.4 198.6c18.9 32.4 14.3 70.1-10.2 84.1s-59.7-.9-78.5-33.3S-2.7 179.3 21.8 165.3s59.7 .9 78.5 33.3zM69.2 401.2C121.6 259.9 214.7 224 256 224s134.4 35.9 186.8 177.2c3.6 9.7 5.2 20.1 5.2 30.5v1.6c0 25.8-20.9 46.7-46.7 46.7c-11.5 0-22.9-1.4-34-4.2l-88-22c-15.3-3.8-31.3-3.8-46.6 0l-88 22c-11.1 2.8-22.5 4.2-34 4.2C84.9 480 64 459.1 64 433.3v-1.6c0-10.4 1.6-20.8 5.2-30.5zM421.8 282.7c-24.5-14-29.1-51.7-10.2-84.1s54-47.3 78.5-33.3s29.1 51.7 10.2 84.1s-54 47.3-78.5 33.3zM310.1 189.7c-32.3-10.6-46.9-53.9-32.6-96.8s52.1-69.1 84.4-58.5s46.9 53.9 32.6 96.8s-52.1 69.1-84.4 58.5z'/%3E%3C/svg%3E") no-repeat center;
  }
}
</style>

<style lang="scss" scoped>
// ==================== 模版 C: 文艺语录风 ====================
.template-quote {
  background: linear-gradient(135deg, #2F8F6A 0%, #1a5276 100%);
  
  &.color-1 { background: linear-gradient(135deg, #2C3E50 0%, #4CA1AF 100%); }
  &.color-2 { background: linear-gradient(135deg, #434343 0%, #000000 100%); }
  
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(0px);
  }
  
  .quote-mark {
    position: absolute;
    font-size: clamp(100px, 35vw, 180px);
    font-family: Georgia, 'Times New Roman', serif;
    color: rgba(255, 255, 255, 0.08);
    line-height: 1;
    z-index: 1;
    
    &.quote-mark-open {
      top: 5%;
      left: 5%;
    }
    
    &.quote-mark-close {
      bottom: 5%;
      right: 5%;
      transform: rotate(180deg);
    }
  }
  
  .quote-content {
    position: relative;
    z-index: 2;
    padding: 16% 10%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
  }
  
  .quote-title {
    font-size: clamp(22px, 6vw, 28px);
    font-weight: 600;
    color: #FFFFFF;
    margin: 0 0 6% 0;
    font-family: 'STSong', 'SimSun', Georgia, serif;
    letter-spacing: 4px;
    line-height: 1.6;
    word-break: break-all;
    overflow-wrap: break-word;
    max-width: 100%;
  }
  
  .quote-text {
    font-size: clamp(14px, 3.5vw, 16px);
    color: rgba(255, 255, 255, 0.85);
    line-height: 2;
    margin: 0;
    font-family: 'STSong', 'SimSun', Georgia, serif;
    letter-spacing: 2px;
  }
  
  .quote-author {
    font-size: clamp(12px, 3vw, 14px);
    color: rgba(255, 255, 255, 0.6);
    margin-top: 8%;
    font-style: italic;
  }
}

// ==================== 模版 D: 波普艺术风 ====================
.template-pop {
  background: #FF8A5B;
  background-image: radial-gradient(#FFFFFF 20%, transparent 20%);
  background-size: 20px 20px;
  
  &.color-1 {
    background-color: #FFD93D;
    background-image: radial-gradient(#FF6B6B 20%, transparent 20%);
    .pop-title, .pop-text { color: #2A2A2A; text-shadow: 3px 3px 0 #FF6B6B; }
    .pop-triangle { border-bottom-color: #FF6B6B; }
  }
  &.color-2 {
    background-color: #6C5CE7;
    background-image: radial-gradient(#FFEAA7 20%, transparent 20%);
    .pop-triangle { border-bottom-color: #FFEAA7; }
  }
  
  .pop-shapes {
    position: absolute;
    inset: 0;
    overflow: hidden;
    pointer-events: none;
  }
  
  .pop-triangle {
    position: absolute;
    width: 0;
    height: 0;
    border-left: 30px solid transparent;
    border-right: 30px solid transparent;
    border-bottom: 52px solid #FFFFFF;
    opacity: 0.3;
    
    &.pop-triangle-1 {
      top: 10%;
      right: 10%;
      transform: rotate(15deg);
    }
    
    &.pop-triangle-2 {
      bottom: 20%;
      left: 8%;
      transform: rotate(-30deg) scale(0.8);
    }
  }
  
  .pop-wave {
    position: absolute;
    bottom: 8%;
    right: 8%;
    width: 20%;
    height: 5%;
    background: repeating-linear-gradient(
      90deg,
      transparent,
      transparent 8px,
      rgba(255, 255, 255, 0.4) 8px,
      rgba(255, 255, 255, 0.4) 16px
    );
    border-radius: 10px;
  }
  
  .pop-content {
    position: relative;
    z-index: 1;
    padding: 12% 10%;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  .pop-title {
    font-size: clamp(28px, 8vw, 36px);
    font-weight: 900;
    color: #FFFFFF;
    margin: 0 0 5% 0;
    text-shadow: 4px 4px 0 #2A2A2A;
    line-height: 1.2;
    letter-spacing: 1px;
    word-break: break-all;
    overflow-wrap: break-word;
    max-width: 100%;
  }
  
  .pop-text {
    font-size: clamp(14px, 4vw, 18px);
    color: #FFFFFF;
    line-height: 1.8;
    margin: 0;
    text-shadow: 2px 2px 0 #2A2A2A;
    flex: 1;
  }
  
  .pop-author {
    font-size: clamp(12px, 3vw, 14px);
    color: rgba(255, 255, 255, 0.9);
    margin-top: auto;
    text-shadow: 1px 1px 0 #2A2A2A;
  }
}
</style>
