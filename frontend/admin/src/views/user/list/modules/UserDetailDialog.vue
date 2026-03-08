<template>
  <ElDialog
    v-model="dialogVisible"
    title="用户详情"
    width="600px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div v-if="user" class="user-detail">
      <!-- 用户基本信息 -->
      <div class="user-header">
        <ElAvatar :src="user.avatarUrl" :size="80" class="user-avatar">
          <template #default>
            <ArtSvgIcon icon="ri:user-line" class="text-3xl" />
          </template>
        </ElAvatar>
        <div class="user-basic">
          <h3 class="username">{{ user.username }}</h3>
          <div class="user-tags">
            <ElTag :class="user.roleId === 0 ? 'tag-admin' : 'tag-user'" size="small">
              {{ user.roleId === 0 ? '管理员' : '普通用户' }}
            </ElTag>
            <ElTag :class="user.realNameAuthFlag === 1 ? 'tag-verified' : 'tag-unverified'" size="small">
              {{ user.realNameAuthFlag === 1 ? '已认证' : '未认证' }}
            </ElTag>
            <ElTag :class="user.status === 0 ? 'tag-active' : 'tag-closed'" size="small">
              {{ user.status === 0 ? '正常' : '已销户' }}
            </ElTag>
            <ElTag :class="user.banFlag === 0 ? 'tag-normal' : 'tag-banned'" size="small">
              {{ user.banFlag === 0 ? '正常' : '已禁言' }}
            </ElTag>
          </div>
        </div>
      </div>

      <ElDivider />

      <!-- 详细信息 -->
      <ElDescriptions :column="2" border>
        <ElDescriptionsItem label="性别">
          {{ getGenderText(user.gender) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="手机号">
          {{ user.phone || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="邮箱" :span="2">
          {{ user.email || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="收货人">
          {{ user.receiptName?.trim() ? user.receiptName : '无' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="收货地址" :span="2">
          {{ user.receiptAddress?.trim() ? user.receiptAddress : '无' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type { UserDetailListVO } from '@/api/user'

defineOptions({ name: 'UserDetailDialog' })

const props = defineProps<{
  modelValue: boolean
  user: UserDetailListVO | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 获取性别文本
const getGenderText = (gender: number) => {
  const map: Record<number, string> = {
    1: '男',
    2: '女',
    3: '保密'
  }
  return map[gender] || '未知'
}
</script>

<style scoped lang="scss">
.user-detail {
  .user-header {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .user-avatar {
      flex-shrink: 0;
    }
    
    .user-basic {
      .username {
        margin: 0 0 12px 0;
        font-size: 20px;
        font-weight: 600;
      }
      
      .user-tags {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
}

// 自定义标签样式
.tag-admin {
  background-color: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.tag-user {
  background-color: #f6ffed;
  color: #52c41a;
  border-color: #b7eb8f;
}

.tag-active {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-closed {
  background-color: #e2e3e5;
  color: #383d41;
  border-color: #d6d8db;
}

.tag-normal {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-banned {
  background-color: #f8d7da;
  color: #721c24;
  border-color: #f5c6cb;
}

.tag-verified {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-unverified {
  background-color: #fff3cd;
  color: #856404;
  border-color: #ffeeba;
}
</style>
