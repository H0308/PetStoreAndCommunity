<script setup>
import { ref } from 'vue'
import ProductLayout from '../components/ProductLayout.vue'

// 接收props
const props = defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false
  },
  userInfo: {
    type: Object,
    default: () => null
  }
})

const emit = defineEmits(['openLogin'])
</script>

<template>
  <div class="supplies-page">
    <ProductLayout 
      page-type="supply"
      :is-logged-in="isLoggedIn"
      :user-info="userInfo"
      @open-login="emit('openLogin')"
    />
  </div>
</template>

<style scoped lang="scss">
.supplies-page {
  width: 100%;
}
</style>
