<template>
  <ElRow :gutter="20" class="flex">
    <ElCol v-for="(item, index) in dataList" :key="index" :sm="12" :md="6" :lg="6">
      <div class="art-card relative flex flex-col justify-center h-35 px-5 mb-5 max-sm:mb-4">
        <span class="text-g-700 text-sm">{{ item.des }}</span>
        <ArtCountTo 
          class="text-[26px] font-medium mt-2" 
          :target="item.num" 
          :duration="1300"
          :decimals="item.decimals || 0"
          :prefix="item.prefix || ''"
        />
        <div class="flex-c mt-1">
          <span class="text-xs text-g-600">{{ item.unit }}</span>
        </div>
        <div
          class="absolute top-0 bottom-0 right-5 m-auto size-12.5 rounded-xl flex-cc bg-theme/10"
        >
          <ArtSvgIcon :icon="item.icon" class="text-xl text-theme" />
        </div>
      </div>
    </ElCol>
  </ElRow>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { 
    fetchUserCount, 
    fetchProductCount, 
    fetchOrderCount, 
    fetchOrderPrice 
  } from '@/api/dashboard'

  interface CardDataItem {
    des: string
    icon: string
    num: number
    unit: string
    decimals?: number
    prefix?: string
  }

  const userStore = useUserStore()

  /**
   * 卡片统计数据列表
   * 展示用户数量、商品数量、订单数量和交易金额等核心数据指标
   */
  const dataList = reactive<CardDataItem[]>([
    {
      des: '用户总数',
      icon: 'ri:group-line',
      num: 0,
      unit: '位用户'
    },
    {
      des: '商品总数',
      icon: 'ri:shopping-bag-line',
      num: 0,
      unit: '件商品'
    },
    {
      des: '成交订单',
      icon: 'ri:file-list-3-line',
      num: 0,
      unit: '笔订单'
    },
    {
      des: '交易总额',
      icon: 'ri:money-cny-circle-line',
      num: 0,
      unit: '元',
      decimals: 2,
      prefix: '¥'
    }
  ])

  // 获取统计数据
  const loadStatistics = async () => {
    const userId = userStore.info?.userId
    if (!userId) return

    try {
      const [userCount, productCount, orderCount, orderPrice] = await Promise.all([
        fetchUserCount(userId),
        fetchProductCount(userId),
        fetchOrderCount(userId),
        fetchOrderPrice(userId)
      ])

      dataList[0].num = userCount || 0
      dataList[1].num = productCount || 0
      dataList[2].num = orderCount || 0
      dataList[3].num = orderPrice || 0
    } catch (error) {
      console.error('获取统计数据失败:', error)
    }
  }

  onMounted(() => {
    loadStatistics()
  })
</script>
