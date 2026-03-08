<template>
  <div class="art-card h-105 p-5 mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>用户增长趋势</h4>
        <p>最近7天新增用户统计</p>
      </div>
    </div>
    <ArtLineChart
      height="calc(100% - 56px)"
      :data="chartData"
      :xAxisData="xAxisData"
      :showAreaColor="true"
      :showAxisLine="false"
      :loading="loading"
      :isEmpty="chartData.length === 0 || chartData.every(v => v === 0)"
    />
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { fetchNewUserCount, type NewUserCount } from '@/api/dashboard'

  const userStore = useUserStore()
  const loading = ref(true)
  const chartData = ref<number[]>([])
  const xAxisData = ref<string[]>([])

  // 格式化日期
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    return `${date.getMonth() + 1}/${date.getDate()}`
  }

  // 加载数据
  const loadData = async () => {
    const userId = userStore.info?.userId
    if (!userId) {
      loading.value = false
      return
    }

    try {
      loading.value = true
      const data = await fetchNewUserCount(userId)
      
      if (data && data.length > 0) {
        xAxisData.value = data.map((item: NewUserCount) => formatDate(item.createTime))
        chartData.value = data.map((item: NewUserCount) => item.count)
      } else {
        xAxisData.value = []
        chartData.value = []
      }
    } catch (error) {
      console.error('获取用户增长数据失败:', error)
      xAxisData.value = []
      chartData.value = []
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    loadData()
  })
</script>
