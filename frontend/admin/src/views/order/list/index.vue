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
          <h4 class="m-0">订单列表</h4>
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
            <ElButton @click="handleExport" :loading="exporting" v-ripple>
              <ArtSvgIcon icon="ri:file-excel-2-line" class="mr-1" />
              导出所有订单到Excel
            </ElButton>
            <ElButton @click="handleExportSelected" :disabled="selectedRows.length === 0" v-ripple>
              <ArtSvgIcon icon="ri:file-excel-2-line" class="mr-1" />
              导出选中订单 ({{ selectedRows.length }})
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
        <!-- 商品信息列 -->
        <template #productInfo="{ row }">
          <div class="flex gap-3 product-info">
            <ElImage 
              :src="row.imgUrl" 
              fit="cover"
              class="product-image"
            >
              <template #error>
                <div class="image-placeholder">
                  <ArtSvgIcon icon="ri:image-line" class="text-xl text-g-500" />
                </div>
              </template>
            </ElImage>
            <div class="flex-1 min-w-0">
              <p class="m-0 overflow-hidden font-medium text-ellipsis whitespace-nowrap">
                {{ row.productName }}
              </p>
              <p class="m-0 mt-1 overflow-hidden text-xs text-g-700 text-ellipsis whitespace-nowrap">
                <ElTag size="small" :class="row.productType === 1 ? 'tag-pet' : 'tag-supply'">
                  {{ row.productType === 1 ? '宠物' : '宠物用品' }}
                </ElTag>
              </p>
            </div>
          </div>
        </template>

        <!-- 买家信息列 -->
        <template #buyerInfo="{ row }">
          <div class="buyer-info">
            <span class="font-medium">{{ row.username }}</span>
          </div>
        </template>

        <!-- 收货信息列 -->
        <template #receiptInfo="{ row }">
          <div class="receipt-info">
            <p class="m-0">
              <span class="font-medium">{{ row.receiptName || '-' }}</span>
              <span class="text-g-700 ml-2">{{ row.phone || '-' }}</span>
            </p>
            <p class="m-0 mt-1 text-xs text-g-700 overflow-hidden text-ellipsis whitespace-nowrap" :title="row.receiptAddress">
              {{ row.receiptAddress || '-' }}
            </p>
          </div>
        </template>

        <!-- 数量列 -->
        <template #totalCount="{ row }">
          <span>x{{ row.totalCount }}</span>
        </template>

        <!-- 金额列 -->
        <template #totalPrice="{ row }">
          <span class="font-medium price-text">¥{{ row.totalPrice?.toFixed(2) }}</span>
        </template>

        <!-- 下单时间列 -->
        <template #createTime="{ row }">
          <span class="text-sm">{{ row.createTime }}</span>
        </template>

        <!-- 状态列 -->
        <template #status="{ row }">
          <!-- 退款中时只显示退款标记，否则显示订单状态 -->
          <ElTag v-if="row.refundFlag === 1" type="danger">
            退款中
          </ElTag>
          <ElTag v-else :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </ElTag>
        </template>

        <!-- 操作列 -->
        <template #operation="{ row }">
          <div class="flex">
            <!-- 退款中的订单：显示查看退款详情按钮 -->
            <ArtButtonTable 
              v-if="row.refundFlag === 1" 
              icon="ri:file-list-3-line" 
              iconClass="bg-warning/12 text-warning"
              tooltip="查看退款详情"
              @click="handleViewRefund(row)" 
            />
            <ArtButtonTable type="edit" :row="row" tooltip="编辑订单" @click="handleEdit(row)" />
            <!-- 已发货或待签收的订单：显示查看物流按钮 -->
            <ArtButtonTable 
              v-if="row.status === 3 || row.status === 4" 
              icon="ri:map-pin-line" 
              iconClass="bg-primary/12 text-primary"
              tooltip="查看物流"
              @click="handleViewLogistics(row)" 
            />
            <!-- 待发货的订单：显示发货按钮 -->
            <ArtButtonTable 
              v-if="row.status === 2" 
              icon="ri:truck-line" 
              iconClass="bg-success/12 text-success"
              tooltip="发货"
              @click="handleShip(row)" 
            />
            <!-- 退款中的订单：显示处理退款按钮 -->
            <ArtButtonTable 
              v-if="row.refundFlag === 1" 
              icon="ri:refund-2-line" 
              iconClass="bg-danger/12 text-danger"
              tooltip="处理退款"
              @click="handleRefund(row)" 
            />
          </div>
        </template>
      </ArtTable>
    </ElCard>

    <!-- 编辑订单弹窗 -->
    <EditOrderDialog
      v-model="editDialogVisible"
      :order-data="currentEditOrder"
      @success="handleEditSuccess"
    />
    
    <!-- 物流追踪弹窗 -->
    <ElDialog 
      v-model="logisticsDialogVisible" 
      title="物流追踪" 
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <LogisticsMap
        v-if="currentLogisticsOrder && logisticsDialogVisible"
        :order-id="currentLogisticsOrder.orderId"
        :user-id="currentLogisticsOrder.userId"
        :product-type="currentLogisticsOrder.productType"
        :order-status="currentLogisticsOrder.status"
        :visible="logisticsDialogVisible"
        @arrived="handleLogisticsArrived"
      />
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/modules/user'
import { fetchOrderList, fetchRefundInfo, handleRefund as handleRefundApi, deliverOrder, exportOrdersToExcel, ORDER_STATUS_TEXT, ORDER_STATUS_TYPE, type OrderListItem, type OrderListWithFilterDTO, type RefundInfoVO } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ColumnOption } from '@/types/component'
import EditOrderDialog from './EditOrderDialog.vue'
import LogisticsMap from '@/components/LogisticsMap.vue'
import { watch } from 'vue'
import { useRoute } from 'vue-router'

defineOptions({ name: 'OrderList' })

const route = useRoute()

const userStore = useUserStore()
const loading = ref(false)
const exporting = ref(false)
const tableData = ref<OrderListItem[]>([])
const tableRef = ref()
const searchBarRef = ref()
const selectedRows = ref<OrderListItem[]>([])

// 编辑弹窗
const editDialogVisible = ref(false)
const currentEditOrder = ref<OrderListItem | null>(null)

// 物流弹窗
const logisticsDialogVisible = ref(false)
const currentLogisticsOrder = ref<OrderListItem | null>(null)

// 根据路由参数初始化订单ID筛选（从聊天页面跳转）
const getInitialOrderId = () => {
  return route.query.orderId ? String(route.query.orderId) : ''
}

// 搜索表单状态
const searchFormState = ref({
  productName: '',
  username: '',
  orderId: getInitialOrderId(),
  productType: '',
  status: '',
  startTime: '',
  endTime: ''
})

// 搜索表单配置
const searchItems = computed(() => [
  {
    key: 'productName',
    label: '商品名称',
    type: 'input',
    props: {
      placeholder: '请输入商品名称'
    }
  },
  {
    key: 'orderId',
    label: '订单编号',
    type: 'input',
    props: {
      placeholder: '请输入订单编号'
    }
  },
  {
    key: 'username',
    label: '收货人',
    type: 'input',
    props: {
      placeholder: '请输入收货人姓名'
    }
  },
  {
    key: 'productType',
    label: '商品类型',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '宠物', value: '1' },
        { label: '宠物用品', value: '2' }
      ]
    }
  },
  {
    key: 'status',
    label: '订单状态',
    type: 'select',
    props: {
      placeholder: '全部',
      clearable: true,
      options: [
        { label: '待支付', value: '1' },
        { label: '待发货', value: '2' },
        { label: '已发货', value: '3' },
        { label: '待签收', value: '4' },
        { label: '已收货', value: '5' },
        { label: '已取消', value: '6' },
        { label: '退款中', value: 'refunding' }
      ]
    }
  },
  {
    key: 'startTime',
    label: '开始时间',
    type: 'datetime',
    props: {
      type: 'datetime',
      placeholder: '选择开始时间',
      valueFormat: 'YYYY-MM-DDTHH:mm:ss',
      clearable: true
    }
  },
  {
    key: 'endTime',
    label: '结束时间',
    type: 'datetime',
    props: {
      type: 'datetime',
      placeholder: '选择结束时间',
      valueFormat: 'YYYY-MM-DDTHH:mm:ss',
      clearable: true
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
const columns = ref<ColumnOption<OrderListItem>[]>([
  { type: 'selection', width: 50, fixed: 'left', visible: true },
  { type: 'globalIndex', width: 60, label: '序号', fixed: 'left', visible: true },
  {
    prop: 'productInfo',
    label: '商品信息',
    minWidth: 240,
    useSlot: true,
    visible: true
  },
  {
    prop: 'buyerInfo',
    label: '买家',
    width: 140,
    useSlot: true,
    align: 'center',
    visible: true
  },
  {
    prop: 'receiptInfo',
    label: '收货信息',
    minWidth: 220,
    useSlot: true,
    visible: true
  },
  {
    prop: 'totalCount',
    label: '数量',
    width: 80,
    useSlot: true,
    visible: true
  },
  {
    prop: 'totalPrice',
    label: '订单金额',
    width: 110,
    useSlot: true,
    visible: true
  },
  {
    prop: 'createTime',
    label: '下单时间',
    width: 170,
    useSlot: true,
    visible: true
  },
  {
    prop: 'status',
    label: '状态',
    width: 100,
    useSlot: true,
    visible: true
  },
  {
    prop: 'operation',
    label: '操作',
    width: 180,
    useSlot: true,
    fixed: 'right',
    visible: true
  }
])

// 过滤后的可见列
const visibleColumns = computed(() => 
  columns.value.filter(col => col.visible !== false)
)

// 获取状态文本
const getStatusText = (status: number) => {
  return ORDER_STATUS_TEXT[status] || '未知'
}

// 获取状态类型
const getStatusType = (status: number) => {
  return ORDER_STATUS_TYPE[status] || 'info'
}

// 加载数据
const loadData = async () => {
  const userId = userStore.info?.userId
  if (!userId) {
    console.warn('用户ID不存在，无法加载订单列表')
    return
  }

  try {
    loading.value = true
    
    // 构建请求参数
    const params: OrderListWithFilterDTO = {
      orderListDTO: {
        userId: Number(userId),
        currentPage: pagination.current,
        pageSize: pagination.size
      },
      orderListFilterDTO: {
        productName: searchFormState.value.productName || null,
        username: searchFormState.value.username || null,
        orderId: searchFormState.value.orderId ? Number(searchFormState.value.orderId) : null,
        productType: searchFormState.value.productType ? Number(searchFormState.value.productType) : null,
        status: searchFormState.value.status && searchFormState.value.status !== 'refunding' ? Number(searchFormState.value.status) : null,
        refundFlag: searchFormState.value.status === 'refunding' ? 1 : null,
        startTime: searchFormState.value.startTime || null,
        endTime: searchFormState.value.endTime || null
      }
    }
    
    const res = await fetchOrderList(params)
    
    tableData.value = res.totalRecords || []
    pagination.total = res.totalCount || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新
const handleRefresh = () => {
  loadData()
}

// 选择变化
const handleSelectionChange = (selection: OrderListItem[]) => {
  selectedRows.value = selection
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

// 查看退款详情
const handleViewRefund = async (row: OrderListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    const refundInfo = await fetchRefundInfo(row.orderId, Number(userId))
    ElMessageBox.alert(
      `<div style="line-height: 1.8;">
        <p><strong>订单编号：</strong>${refundInfo.orderId}</p>
        <p><strong>退款原因：</strong>${refundInfo.message || '未填写'}</p>
        <p><strong>申请时间：</strong>${refundInfo.createTime || '-'}</p>
      </div>`,
      '退款详情',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定'
      }
    )
  } catch (error: any) {
    ElMessage.error(error.message || '获取退款信息失败')
  }
}

// 处理退款
const handleRefund = async (row: OrderListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    const action = await ElMessageBox.confirm(
      `确定要处理订单 ${row.orderId} 的退款申请吗？`,
      '处理退款',
      {
        distinguishCancelAndClose: true,
        confirmButtonText: '同意退款',
        cancelButtonText: '拒绝退款',
        type: 'warning'
      }
    )
    
    // 点击确认按钮 - 同意退款
    await handleRefundApi({
      refundId: row.refundId!,
      orderId: row.orderId,
      userIdNotify: row.userId,
      userId: Number(userId),
      opFlag: 1 // 1-同意退款
    })
    ElMessage.success('已同意退款')
    loadData()
  } catch (action: any) {
    if (action === 'cancel') {
      // 点击取消按钮 - 拒绝退款
      try {
        await handleRefundApi({
          refundId: row.refundId!,
          orderId: row.orderId,
          userIdNotify: row.userId,
          userId: Number(userStore.info?.userId),
          opFlag: 0 // 0-拒绝退款
        })
        ElMessage.success('已拒绝退款')
        loadData()
      } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
      }
    }
    // action === 'close' 时不做任何操作
  }
}

// 编辑订单
const handleEdit = (row: OrderListItem) => {
  currentEditOrder.value = row
  editDialogVisible.value = true
}

// 编辑成功回调
const handleEditSuccess = () => {
  loadData()
}

// 查看物流
const handleViewLogistics = (row: OrderListItem) => {
  currentLogisticsOrder.value = row
  logisticsDialogVisible.value = true
}

// 物流到达处理
const handleLogisticsArrived = () => {
  if (currentLogisticsOrder.value) {
    currentLogisticsOrder.value.status = 4
  }
  loadData()
}

// 发货
const handleShip = async (row: OrderListItem) => {
  const userId = userStore.info?.userId
  if (!userId) {
    ElMessage.error('用户信息获取失败')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要对订单 ${row.orderId} 进行发货吗？`,
      '确认发货',
      {
        confirmButtonText: '确定发货',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deliverOrder(row.orderId, Number(userId))
    ElMessage.success('发货成功')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '发货失败')
    }
  }
}

// 下载Excel文件
const downloadExcel = async (orderIds?: number[]) => {
  const url = exportOrdersToExcel(orderIds)
  const { accessToken } = useUserStore()
  const token = accessToken.startsWith('Bearer ') ? accessToken : `Bearer ${accessToken}`

  try {
    const response = await fetch(`http://localhost:8080${url}`, {
      method: 'GET',
      headers: { Authorization: token }
    })

    if (!response.ok) {
      ElMessage.error('导出失败，请重新登录后重试')
      return
    }

    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = `订单明细表-${Date.now()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(objectUrl)
    ElMessage.success('开始下载订单Excel文件')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

// 导出所有订单
const handleExport = async () => {
  exporting.value = true
  try {
    downloadExcel() // 不传orderIds，导出所有
  } finally {
    exporting.value = false
  }
}

// 导出选中订单
const handleExportSelected = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要导出的订单')
    return
  }
  
  const orderIds = selectedRows.value.map(order => order.orderId)
  downloadExcel(orderIds)
}

// 监听路由参数变化（用于从聊天页面跳转过来的情况）
watch(
  () => route.query,
  async (newQuery, oldQuery) => {
    // 如果是同一个页面内的查询变化，才处理
    if (JSON.stringify(newQuery) === JSON.stringify(oldQuery)) return

    // 处理订单ID筛选（从聊天页面跳转）
    searchFormState.value.orderId = newQuery.orderId ? String(newQuery.orderId) : ''

    pagination.current = 1
    await loadData()
  }
)

onMounted(() => {
  // 从路由参数初始化订单ID（确保路由已准备好）
  if (route.query.orderId) {
    searchFormState.value.orderId = String(route.query.orderId)
  }
  loadData()
})
</script>

<style scoped lang="scss">
.product-info {
  .product-image {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--el-fill-color-light);
    border-radius: 8px;
  }
}

.buyer-info {
  width: 100%;
}

.receipt-info {
  line-height: 1.4;
}

.price-text {
  color: var(--el-color-danger);
}

.font-medium {
  font-weight: 500;
}

// 自定义标签样式
.tag-pet {
  background-color: #d4edda;
  color: #155724;
  border-color: #c3e6cb;
}

.tag-supply {
  background-color: #cce5ff;
  color: #004085;
  border-color: #b8daff;
}

// 固定表格高度
:deep(.art-table) {
  min-height: 680px;
  
  .el-table {
    height: 100% !important;
  }
}
</style>
