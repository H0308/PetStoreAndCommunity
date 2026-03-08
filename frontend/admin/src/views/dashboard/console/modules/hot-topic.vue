<template>
  <div class="art-card h-105 p-4 box-border mb-5 max-sm:mb-4">
    <div class="art-card-header mb-2">
      <div class="title">
        <h4>热门话题排行</h4>
        <p>贴吧话题热度TOP5</p>
      </div>
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { fetchHotTopic, type HotTopic } from '@/api/dashboard'
  import * as echarts from 'echarts'

  const userStore = useUserStore()
  const loading = ref(true)
  const chartRef = ref<HTMLElement | null>(null)
  let chartInstance: echarts.ECharts | null = null

  // 初始化图表
  const initChart = (data: HotTopic[]) => {
    if (!chartRef.value) return

    if (chartInstance) {
      chartInstance.dispose()
    }

    chartInstance = echarts.init(chartRef.value)

    // 数据倒序，让最高的在最上面
    const sortedData = [...data].reverse()
    const names = sortedData.map(item => item.name)
    const counts = sortedData.map(item => item.count)

    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: '{b}<br/>帖子数: {c}'
      },
      grid: {
        left: '3%',
        right: '8%',
        bottom: '3%',
        top: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: {
          lineStyle: {
            type: 'dashed',
            color: '#e8e8e8'
          }
        }
      },
      yAxis: {
        type: 'category',
        data: names,
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          fontSize: 12,
          color: '#666',
          width: 80,
          overflow: 'truncate',
          ellipsis: '...'
        }
      },
      series: [
        {
          type: 'bar',
          data: counts,
          barWidth: 16,
          itemStyle: {
            borderRadius: [0, 8, 8, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#5470c6' },
              { offset: 1, color: '#91cc75' }
            ])
          },
          label: {
            show: true,
            position: 'right',
            fontSize: 12,
            color: '#666'
          }
        }
      ]
    }

    chartInstance.setOption(option)
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
      const data = await fetchHotTopic(userId)
      initChart(data || [])
    } catch (error) {
      console.error('获取热门话题数据失败:', error)
      initChart([])
    } finally {
      loading.value = false
    }
  }

  // 监听窗口大小变化
  const handleResize = () => {
    chartInstance?.resize()
  }

  onMounted(() => {
    loadData()
    window.addEventListener('resize', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
  })
</script>

<style lang="scss" scoped>
  .chart-container {
    width: 100%;
    height: calc(100% - 50px);
    min-height: 280px;
  }
</style>
