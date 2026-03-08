<template>
  <div class="flex flex-col gap-4 pb-5">
    <!-- 搜索区域 -->
    <ArtSearchBar
      ref="searchBarRef"
      v-model="searchFormState"
      :items="searchItems"
      :is-expand="false"
      :show-expand="true"
      :show-reset-button="true"
      :show-search-button="true"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 表格区域 -->
    <ElCard class="flex-1 art-table-card" shadow="never" style="margin-top: 0">
      <template #header>
        <div class="flex-cb">
          <h4 class="m-0">用户列表</h4>
          <div class="flex gap-2">
            <ElTag v-if="loading" type="warning">加载中...</ElTag>
            <ElTag v-else type="success">{{ tableData.length }} 条数据</ElTag>
          </div>
        </div>
      </template>

      <!-- 表格工具栏 -->
      <ArtTableHeader
        v-model:columns="columns"
        :loading="loading"
        @refresh="handleRefresh"
        layout="refresh,size,fullscreen,columns"
        fullClass="art-table-card"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton type="primary" @click="handleAdd" v-ripple>
              <ArtSvgIcon icon="ri:add-line" class="mr-1" />
              新增用户
            </ElButton>
            <ElButton @click="handleBatchBan" :disabled="selectedRows.length === 0" v-ripple>
              <ArtSvgIcon icon="ri:forbid-line" class="mr-1" />
              批量禁言 ({{ selectedRows.length }})
            </ElButton>
            <ElButton @click="handleBatchUnban" :disabled="selectedRows.length === 0" v-ripple>
              <ArtSvgIcon icon="ri:chat-check-line" class="mr-1" />
              批量解禁 ({{ selectedRows.length }})
            </ElButton>
            <ElButton @click="handleBatchDelete" :disabled="selectedRows.length === 0" v-ripple>
              <ArtSvgIcon icon="ri:delete-bin-line" class="mr-1" />
              批量删除 ({{ selectedRows.length }})
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        :loading="loading"
        :pagination="pagination"
        :data="tableData"
        :columns="visibleColumns"
        table-layout="fixed"
        height="680px"
        empty-height="360px"
        style="min-height: 680px;"
        @selection-change="handleSelectionChange"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      >
        <!-- 用户信息列 -->
        <template #userInfo="{ row }">
          <div class="flex items-center gap-3 user-info">
            <ElAvatar 
              :src="row.avatarUrl" 
              :size="36"
              class="user-avatar"
            >
              <template #default>
                <ArtSvgIcon icon="ri:user-line" class="text-base" />
              </template>
            </ElAvatar>
            <span class="font-medium">{{ row.username }}</span>
          </div>
        </template>

        <!-- 性别列 -->
        <template #gender="{ row }">
          <span>{{ getGenderText(row.gender) }}</span>
        </template>

        <!-- 角色列 -->
        <template #role="{ row }">
          <ElTag :class="row.roleId === 0 ? 'tag-admin' : 'tag-user'">
            {{ row.roleId === 0 ? '管理员' : '普通用户' }}
          </ElTag>
        </template>

        <!-- 账户状态列 -->
        <template #status="{ row }">
          <ElTag :class="row.status === 0 ? 'tag-active' : 'tag-closed'">
            {{ row.status === 0 ? '正常' : '已销户' }}
          </ElTag>
        </template>

        <!-- 禁言状态列 -->
        <template #banFlag="{ row }">
          <ElTag :class="row.banFlag === 0 ? 'tag-normal' : 'tag-banned'">
            {{ row.banFlag === 0 ? '正常' : '已禁言' }}
          </ElTag>
        </template>

        <!-- 认证状态列 -->
        <template #realNameAuthFlag="{ row }">
          <ElTag :class="row.realNameAuthFlag === 1 ? 'tag-verified' : 'tag-unverified'">
            {{ row.realNameAuthFlag === 1 ? '已认证' : '未认证' }}
          </ElTag>
        </template>

        <!-- 收货信息列 -->
        <template #receipt="{ row }">
          <div class="receipt-info" v-if="row.receiptName?.trim() || row.receiptAddress?.trim()">
            <p class="m-0 overflow-hidden text-ellipsis whitespace-nowrap">
              {{ row.receiptName }}
            </p>
            <p class="m-0 mt-1 overflow-hidden text-xs text-g-700 text-ellipsis whitespace-nowrap" :title="row.receiptAddress">
              {{ row.receiptAddress }}
            </p>
          </div>
          <span v-else>无</span>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <ArtButtonTable type="view" :row="row" tooltip="查看详情" @click="handleView(row)" />
            <ArtButtonTable type="edit" :row="row" tooltip="编辑用户" @click="handleEdit(row)" />
            <ArtButtonTable 
              icon="ri:lock-password-line" 
              iconClass="bg-primary/12 text-primary"
              tooltip="重置密码"
              @click="handleResetPassword(row)" 
            />
            <ArtButtonTable
              icon="ri:mail-send-line"
              iconClass="bg-[rgba(255,138,91,0.12)] text-[#FF8A5B]"
              tooltip="发送邮件"
              @click="handleSendEmail(row)"
            />
            <ArtButtonTable 
              v-if="row.banFlag === 0" 
              icon="ri:forbid-line" 
              iconClass="bg-warning/12 text-warning"
              tooltip="禁言用户"
              @click="handleBan(row)" 
            />
            <ArtButtonTable 
              v-else 
              icon="ri:checkbox-circle-line" 
              iconClass="bg-success/12 text-success"
              tooltip="解除禁言"
              @click="handleUnban(row)" 
            />
            <ArtButtonTable 
              v-if="row.status === 0"
              icon="ri:user-unfollow-line" 
              iconClass="bg-danger/12 text-danger"
              tooltip="销户"
              @click="handleCloseAccount(row)" 
            />
            <ArtButtonTable type="delete" :row="row" tooltip="删除用户" @click="handleDelete(row)" />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 用户详情弹窗 -->
    <UserDetailDialog
      v-model="detailDialogVisible"
      :user="currentUser"
    />

    <!-- 用户编辑弹窗 -->
    <UserEditDialog
      v-model="editDialogVisible"
      :user="editUser"
      @success="handleEditSuccess"
    />

    <!-- 用户新增弹窗 -->
    <UserAddDialog
      v-model="addDialogVisible"
      @success="handleAddSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import {
  fetchUserList,
  updateUser,
  resetUserPassword,
  closeUserAccount,
  deleteUser,
  batchDeleteUsers,
  banUser,
  unBanUser,
  batchBanUsers,
  batchUnBanUsers,
  registerUser,
  type UserDetailListVO,
  type UserDetailListWithFilterDTO
} from '@/api/user'
import { ref, reactive, computed, onMounted, onActivated, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import UserDetailDialog from './modules/UserDetailDialog.vue'
import UserEditDialog from './modules/UserEditDialog.vue'
import UserAddDialog from './modules/UserAddDialog.vue'
import { useRoute, useRouter } from 'vue-router'

defineOptions({ name: 'UserList' })

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<UserDetailListVO[]>([])
const selectedRows = ref<UserDetailListVO[]>([])
const searchBarRef = ref()
const tableRef = ref()

// 用户详情弹窗
const detailDialogVisible = ref(false)
const currentUser = ref<UserDetailListVO | null>(null)

// 用户编辑弹窗
const editDialogVisible = ref(false)
const editUser = ref<UserDetailListVO | null>(null)

// 用户新增弹窗
const addDialogVisible = ref(false)

// 搜索表单状态
const searchFormState = ref({
  username: '',
  phone: '',
  email: '',
  roleId: '',
  status: '',
  banFlag: '',
  realNameAuthFlag: ''
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'username',
    label: '用户名',
    type: 'input',
    props: {
      placeholder: '请输入用户名'
    }
  },
  {
    key: 'phone',
    label: '手机号',
    type: 'input',
    props: {
      placeholder: '请输入手机号'
    }
  },
  {
    key: 'email',
    label: '邮箱',
    type: 'input',
    props: {
      placeholder: '请输入邮箱'
    }
  },
  {
    key: 'roleId',
    label: '角色',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '管理员', value: '0' },
        { label: '普通用户', value: '1' }
      ]
    }
  },
  {
    key: 'realNameAuthFlag',
    label: '认证状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '未认证', value: '0' },
        { label: '已认证', value: '1' }
      ]
    }
  },
  {
    key: 'status',
    label: '账户状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '正常', value: '0' },
        { label: '已销户', value: '1' }
      ]
    }
  },
  {
    key: 'banFlag',
    label: '禁言状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '正常', value: '0' },
        { label: '已禁言', value: '1' }
      ]
    }
  }
])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格列配置
const columns = ref<ColumnOption<UserDetailListVO>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 60, label: '序号', fixed: 'left', visible: true },
  {
    prop: 'userInfo',
    label: '用户信息',
    minWidth: 180,
    useSlot: true,
    visible: true
  },
  {
    prop: 'gender',
    label: '性别',
    width: 80,
    useSlot: true,
    visible: true
  },
  {
    prop: 'phone',
    label: '手机号',
    width: 130,
    showOverflowTooltip: true,
    visible: true
  },
  {
    prop: 'email',
    label: '邮箱',
    minWidth: 180,
    showOverflowTooltip: true,
    visible: true
  },
  {
    prop: 'role',
    label: '角色',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'receipt',
    label: '收货信息',
    minWidth: 200,
    useSlot: true,
    visible: true
  },
  {
    prop: 'realNameAuthFlag',
    label: '认证状态',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'status',
    label: '账户状态',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'banFlag',
    label: '禁言状态',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'operation',
    label: '操作',
    width: 320,
    useSlot: true,
    fixed: 'right',
    visible: true
  }
])

// 过滤后的可见列
const visibleColumns = computed(() => 
  columns.value.filter(col => col.visible !== false)
)

// 获取性别文本
const getGenderText = (gender: number) => {
  const map: Record<number, string> = {
    1: '男',
    2: '女',
    3: '保密'
  }
  return map[gender] || '未知'
}

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) {
    console.warn('用户ID不存在，无法加载用户列表')
    return
  }

  try {
    loading.value = true
    
    // 构建请求参数
    const params: UserDetailListWithFilterDTO = {
      userDetailListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      userDetailFilterDTO: {
        userId: Number(userId),
        username: searchFormState.value.username || null,
        phone: searchFormState.value.phone || null,
        email: searchFormState.value.email || null,
        roleId: searchFormState.value.roleId !== '' ? Number(searchFormState.value.roleId) : null,
        status: searchFormState.value.status !== '' ? Number(searchFormState.value.status) : null,
        banFlag: searchFormState.value.banFlag !== '' ? Number(searchFormState.value.banFlag) : null,
        realNameAuthFlag: searchFormState.value.realNameAuthFlag !== '' ? Number(searchFormState.value.realNameAuthFlag) : null
      }
    }
    
    const res = await fetchUserList(params)
    
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  pagination.current = 1
  loadData()
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 选择变化
const handleSelectionChange = (selection: UserDetailListVO[]) => {
  selectedRows.value = selection
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.current = page
  loadData()
}

// 新增用户
const handleAdd = () => {
  addDialogVisible.value = true
}

// 新增用户成功回调
const handleAddSuccess = () => {
  loadData()
}

// 查看用户详情
const handleView = (row: UserDetailListVO) => {
  currentUser.value = row
  detailDialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserDetailListVO) => {
  editUser.value = row
  editDialogVisible.value = true
}

// 编辑成功回调
const handleEditSuccess = () => {
  loadData()
}

// 修改用户密码
const handleResetPassword = async (row: UserDetailListVO) => {
  try {
    await ElMessageBox.confirm(`确定要重置用户"${row.username}"的密码吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    await resetUserPassword(row.userId, Number(userId))
    
    ElMessage.success('密码重置成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '密码重置失败')
    }
  }
}

// 禁言用户
const handleBan = async (row: UserDetailListVO) => {
  try {
    await ElMessageBox.confirm(`确定要禁言用户"${row.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    await banUser(row.userId, Number(userId))
    
    ElMessage.success('禁言成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '禁言失败')
    }
  }
}

// 解除禁言
const handleUnban = async (row: UserDetailListVO) => {
  try {
    await ElMessageBox.confirm(`确定要解除用户"${row.username}"的禁言吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    await unBanUser(row.userId, Number(userId))
    
    ElMessage.success('解除禁言成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '解除禁言失败')
    }
  }
}

// 销户
const handleCloseAccount = async (row: UserDetailListVO) => {
  try {
    await ElMessageBox.confirm(`确定要销户用户"${row.username}"吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    await closeUserAccount(row.userId, Number(userId))
    
    ElMessage.success('销户成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '销户失败')
    }
  }
}

// 批量禁言
const handleBatchBan = () => {
  if (selectedRows.value.length === 0) return
  
  ElMessageBox.confirm(`确定要禁言选中的 ${selectedRows.value.length} 个用户吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    try {
      await batchBanUsers(selectedRows.value.map(row => row.userId), Number(userId))
      
      ElMessage.success('批量禁言成功')
      selectedRows.value = []
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '批量禁言失败')
    }
  }).catch(() => {})
}

// 批量取消禁言
const handleBatchUnban = () => {
  if (selectedRows.value.length === 0) return
  
  ElMessageBox.confirm(`确定要解除选中的 ${selectedRows.value.length} 个用户的禁言吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    try {
      await batchUnBanUsers(selectedRows.value.map(row => row.userId), Number(userId))
      
      ElMessage.success('批量解除禁言成功')
      selectedRows.value = []
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '批量解除禁言失败')
    }
  }).catch(() => {})
}

// 发送邮件
const handleSendEmail = (row: UserDetailListVO) => {
  router.push({ name: 'NotifyEmail', query: { email: row.email } })
}

// 删除用户
const handleDelete = async (row: UserDetailListVO) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    await deleteUser(row.userId, Number(userId))
    
    ElMessage.success('删除成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 批量删除用户
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) return
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    const userId = userStore.info?.userId
    if (!userId) {
      ElMessage.error('管理员信息不存在')
      return
    }
    
    try {
      await batchDeleteUsers(selectedRows.value.map(row => row.userId), Number(userId))
      
      ElMessage.success('批量删除成功')
      selectedRows.value = []
      loadData()
    } catch (error: any) {
      ElMessage.error(error.message || '批量删除失败')
    }
  }).catch(() => {})
}

// 处理 URL 查询参数
const handleQueryParams = async () => {
  const { username, userId } = route.query

  if (username) {
    // 先设置搜索条件
    searchFormState.value.username = String(username)
    // 等待下一个 tick，确保搜索表单组件已经同步数据
    await nextTick()
    // 执行搜索
    await handleSearch()
    // 清除 query 参数，避免刷新时重复筛选
    router.replace({ path: route.path, query: {} })
  } else if (userId) {
    // 如果通过 userId 查询，可以在这里添加相应的逻辑
    // 目前后端接口没有直接支持 userId 筛选，所以先清空其他条件
    searchFormState.value.username = ''
    await nextTick()
    await handleSearch()
    router.replace({ path: route.path, query: {} })
  }
}

// 组件挂载时加载数据（首次加载）
onMounted(() => {
  loadData()
})

// 组件从 KeepAlive 缓存中激活时处理 URL 参数
onActivated(async () => {
  // 处理 URL 参数（从聊天页面跳转过来时会带有 username 参数）
  await handleQueryParams()
})
</script>

<style scoped lang="scss">
.user-info {
  .user-avatar {
    flex-shrink: 0;
  }
}

.receipt-info {
  max-width: 200px;
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

// 固定表格高度
:deep(.art-table) {
  min-height: 680px;
  
  .el-table {
    height: 100% !important;
  }
}
</style>
