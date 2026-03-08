<!-- 管理员工作台页面 -->
<template>
  <div class="console-page">
    <!-- 统计卡片 -->
    <CardList />

    <!-- 图表区域：热销商品 + 用户增长趋势 -->
    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="10">
        <ActiveUser />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="14">
        <SalesOverview />
      </ElCol>
    </ElRow>

    <!-- 图表区域：热门话题排行 + 缺货商品 -->
    <ElRow :gutter="20">
      <ElCol :sm="24" :md="12" :lg="12">
        <HotTopic />
      </ElCol>
      <ElCol :sm="24" :md="12" :lg="12">
        <OutOfStock />
      </ElCol>
    </ElRow>
  </div>
</template>

<script setup lang="ts">
  import CardList from './modules/card-list.vue'
  import ActiveUser from './modules/active-user.vue'
  import SalesOverview from './modules/sales-overview.vue'
  import HotTopic from './modules/hot-topic.vue'
  import OutOfStock from './modules/out-of-stock.vue'

  defineOptions({ name: 'Console' })
</script>

<style lang="scss" scoped>
  .console-page {
    padding: 0;
  }
</style>
