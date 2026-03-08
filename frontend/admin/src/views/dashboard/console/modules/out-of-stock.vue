<template>
  <div class="art-card h-105 p-5 box-border mb-5 max-sm:mb-4">
    <div class="art-card-header mb-4">
      <div class="title">
        <h4>缺货商品</h4>
        <p>当前库存不足的商品列表</p>
      </div>
      <ElButton type="primary" plain size="small" @click="goToProductManage">
        <ArtSvgIcon icon="ri:external-link-line" class="mr-1" />
        查看全部
      </ElButton>
    </div>
    <ElTable 
      :data="tableData" 
      v-loading="loading"
      height="calc(100% - 70px)"
      empty-text="暂无缺货商品"
    >
      <ElTableColumn prop="productName" label="商品名称" min-width="180" show-overflow-tooltip />
      <ElTableColumn prop="mainCategoryName" label="主分类" min-width="120" />
      <ElTableColumn prop="subCategoryName" label="子分类" min-width="120" />
    </ElTable>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'
  import { useUserStore } from '@/store/modules/user'
  import { fetchOutOfStock, type OutOfStockProduct } from '@/api/dashboard'

  const router = useRouter()
  const userStore = useUserStore()
  const loading = ref(true)
  const tableData = ref<OutOfStockProduct[]>([])

  // 加载数据
  const loadData = async () => {
    const userId = userStore.info?.userId
    if (!userId) {
      loading.value = false
      return
    }

    try {
      loading.value = true
      const data = await fetchOutOfStock(userId)
      tableData.value = data || []
    } catch (error) {
      console.error('获取缺货商品数据失败:', error)
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  // 跳转到商品管理页面（携带缺货状态筛选）
  const goToProductManage = () => {
    router.push({
      path: '/product/list',
      query: { status: 'out_of_stock' }
    })
  }

  onMounted(() => {
    loadData()
  })
</script>

<style lang="scss" scoped>
  .art-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
</style>
