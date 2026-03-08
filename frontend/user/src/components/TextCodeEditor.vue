<template>
  <div class="text-code-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <!-- 排版组 -->
      <div class="toolbar-group">
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('heading', { level: 1 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          title="标题1"
        >
          H1
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('heading', { level: 2 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          title="标题2"
        >
          H2
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('heading', { level: 3 }) }"
          @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          title="标题3"
        >
          H3
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('paragraph') && !editor?.isActive('heading') }"
          @click="editor?.chain().focus().setParagraph().run()"
          title="正文"
        >
          T
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 样式组 -->
      <div class="toolbar-group">
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('bold') }"
          @click="editor?.chain().focus().toggleBold().run()"
          title="粗体"
        >
          <strong>B</strong>
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('italic') }"
          @click="editor?.chain().focus().toggleItalic().run()"
          title="斜体"
        >
          <em>I</em>
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('strike') }"
          @click="editor?.chain().focus().toggleStrike().run()"
          title="删除线"
        >
          <s>S</s>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 引用组 -->
      <div class="toolbar-group">
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('blockquote') }"
          @click="editor?.chain().focus().toggleBlockquote().run()"
          title="引用块"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-divider"></div>

      <!-- 列表组 -->
      <div class="toolbar-group">
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('bulletList') }"
          @click="editor?.chain().focus().toggleBulletList().run()"
          title="无序列表"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M4 10.5c-.83 0-1.5.67-1.5 1.5s.67 1.5 1.5 1.5 1.5-.67 1.5-1.5-.67-1.5-1.5-1.5zm0-6c-.83 0-1.5.67-1.5 1.5S3.17 7.5 4 7.5 5.5 6.83 5.5 6 4.83 4.5 4 4.5zm0 12c-.83 0-1.5.68-1.5 1.5s.68 1.5 1.5 1.5 1.5-.68 1.5-1.5-.67-1.5-1.5-1.5zM7 19h14v-2H7v2zm0-6h14v-2H7v2zm0-8v2h14V5H7z"/>
          </svg>
        </button>
        <button
          class="toolbar-btn"
          :class="{ active: editor?.isActive('orderedList') }"
          @click="editor?.chain().focus().toggleOrderedList().run()"
          title="有序列表"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M2 17h2v.5H3v1h1v.5H2v1h3v-4H2v1zm1-9h1V4H2v1h1v3zm-1 3h1.8L2 13.1v.9h3v-1H3.2L5 10.9V10H2v1zm5-6v2h14V5H7zm0 14h14v-2H7v2zm0-6h14v-2H7v2z"/>
          </svg>
        </button>
      </div>

      <div class="toolbar-spacer"></div>

      <!-- 操作组 -->
      <div class="toolbar-group">
        <button
          class="toolbar-btn"
          :disabled="!editor?.can().undo()"
          @click="editor?.chain().focus().undo().run()"
          title="撤销"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z"/>
          </svg>
        </button>
        <button
          class="toolbar-btn"
          :disabled="!editor?.can().redo()"
          @click="editor?.chain().focus().redo().run()"
          title="重做"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M18.4 10.6C16.55 8.99 14.15 8 11.5 8c-4.65 0-8.58 3.03-9.96 7.22L3.9 16c1.05-3.19 4.05-5.5 7.6-5.5 1.95 0 3.73.72 5.12 1.88L13 16h9V7l-3.6 3.6z"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 编辑器内容区 -->
    <editor-content :editor="editor" class="editor-content" />
    
    <!-- 字数统计 -->
    <div class="editor-footer">
      <span class="char-count" :class="{ 'over-limit': isOverLimit }">
        {{ charCount }} / {{ maxLength }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  maxLength: {
    type: Number,
    default: 10000
  }
})

const emit = defineEmits(['update:modelValue'])

// 字数统计
const charCount = ref(0)
const isOverLimit = computed(() => charCount.value > props.maxLength)

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit,
    Placeholder.configure({
      placeholder: props.placeholder
    })
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
    // 更新字数统计
    charCount.value = editor.getText().length
  }
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (editor.value && newValue !== editor.value.getHTML()) {
    editor.value.commands.setContent(newValue, false)
  }
})

// 暴露编辑器实例
defineExpose({
  editor,
  getHTML: () => editor.value?.getHTML(),
  getText: () => editor.value?.getText(),
  isEmpty: () => editor.value?.isEmpty,
  focus: () => editor.value?.commands.focus()
})
</script>

<style lang="scss" scoped>
.text-code-editor {
  border: 1px solid #EDE1D9;
  border-radius: 12px;
  overflow: hidden;
  background: #FFFFFF;
}

// 工具栏
.editor-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #FFFFFF;
  border-bottom: 1px solid #EDE1D9;
  flex-wrap: wrap;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.15s ease;

  &:hover:not(:disabled) {
    background: #F5F5F5;
    color: #333;
  }

  &.active {
    background: rgba(255, 138, 91, 0.12);
    color: #FF8A5B;
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  svg {
    flex-shrink: 0;
  }
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: #EDE1D9;
  margin: 0 8px;
}

.toolbar-spacer {
  flex: 1;
}

// 编辑器内容区
.editor-content {
  :deep(.ProseMirror) {
    min-height: 400px;
    padding: 24px;
    outline: none;
    color: #2A2A2A;
    line-height: 1.8;
    font-size: 15px;

    > * + * {
      margin-top: 0.75em;
    }

    // 占位符
    p.is-editor-empty:first-child::before {
      content: attr(data-placeholder);
      float: left;
      color: #adb5bd;
      pointer-events: none;
      height: 0;
    }

    // 标题样式
    h1, h2, h3 {
      line-height: 1.4;
      color: #1a1a1a;
    }

    h1 {
      font-size: 1.75em;
      font-weight: 600;
      margin-bottom: 0.5em;
    }

    h2 {
      font-size: 1.5em;
      font-weight: 600;
      margin-bottom: 0.5em;
    }

    h3 {
      font-size: 1.25em;
      font-weight: 600;
    }

    // 引用块 - 主色边框
    blockquote {
      background: #F9F9F9;
      border-left: 4px solid #FF8A5B;
      padding: 12px 16px;
      margin: 1em 0;
      border-radius: 0 8px 8px 0;
      
      p {
        font-style: italic;
        color: #666;
        margin: 0;
      }
    }

    // 列表样式
    ul, ol {
      padding-left: 1.5em;
      
      li {
        margin: 0.25em 0;
        
        p {
          margin: 0;
        }
      }
    }

    ul {
      list-style-type: disc;
    }

    ol {
      list-style-type: decimal;
    }

    // 粗体、斜体、删除线
    strong {
      font-weight: 600;
    }

    em {
      font-style: italic;
    }

    s {
      text-decoration: line-through;
      color: #999;
    }

    // 分割线
    hr {
      border: none;
      border-top: 2px solid #EDE1D9;
      margin: 2em 0;
    }
  }
}

// 底部字数统计
.editor-footer {
  display: flex;
  justify-content: flex-end;
  padding: 8px 16px;
  border-top: 1px solid #F0F0F0;
  background: #FAFAFA;
}

.char-count {
  font-size: 12px;
  color: #999;
  font-variant-numeric: tabular-nums;
  
  &.over-limit {
    color: #E45649;
    font-weight: 500;
  }
}
</style>
