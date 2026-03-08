<template>
  <div class="art-card h-105 p-4 box-border mb-5 max-sm:mb-4">
    <div class="art-card-header mb-2">
      <div class="title">
        <h4>热销商品TOP10</h4>
        <p>按销售金额排名</p>
      </div>
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
  </div>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/store/modules/user'
  import { fetchTopTotalPrice, type TopTotalPriceProduct } from '@/api/dashboard'
  import * as echarts from 'echarts'

  const userStore = useUserStore()
  const loading = ref(true)
  const chartRef = ref<HTMLElement | null>(null)
  let chartInstance: echarts.ECharts | null = null

  const colors = [
    '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
    '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b8d0'
  ]

  // 初始化图表
  const initChart = (data: { name: string; value: number }[]) => {
    if (!chartRef.value) return

    if (chartInstance) {
      chartInstance.dispose()
    }

    chartInstance = echarts.init(chartRef.value)

    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: '{b}<br/>销售额: ¥{c}<br/>占比: {d}%'
      },
      legend: {
        type: 'scroll',
        orient: 'vertical',
        right: 10,
        top: 'center',
        itemWidth: 14,
        itemHeight: 14,
        textStyle: {
          fontSize: 12
        }
      },
      color: colors,
      series: [
        {
          name: '销售金额',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['35%', '50%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n¥{c}',
            fontSize: 11
          },
          labelLine: {
            show: true,
            length: 10,
            length2: 15
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            },
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.3)'
            }
          },
          data: data
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
      const data = await fetchTopTotalPrice(userId)
      const chartData = (data || []).map((item: TopTotalPriceProduct) => ({
        name: item.name,
        value: Number(item.totalPrice) || 0
      }))
      initChart(chartData)
    } catch (error) {
      console.error('获取热销商品数据失败:', error)
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
