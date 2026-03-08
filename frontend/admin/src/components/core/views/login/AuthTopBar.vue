<!-- 授权页右上角组件 -->
<template>
  <div
    class="absolute w-full flex-cb top-4.5 z-10 flex-c !justify-end max-[1180px]:!justify-between"
  >
    <div class="flex-cc !hidden max-[1180px]:!flex ml-2 max-sm:ml-6">
      <ArtLogo class="icon" size="46" />
      <h1 class="text-xl ont-mediumf ml-2">{{ AppConfig.systemInfo.name }}</h1>
    </div>

    <div class="flex-cc gap-1.5 mr-2 max-sm:mr-5">
      <ElDropdown
        v-if="shouldShowLanguage"
        @command="changeLanguage"
        popper-class="langDropDownStyle"
      >
        <div class="btn language-btn h-8 w-8 c-p flex-cc tad-300">
          <ArtSvgIcon
            icon="ri:translate-2"
            class="text-[19px] text-g-800 transition-colors duration-300"
          />
        </div>
        <template #dropdown>
          <ElDropdownMenu>
            <div v-for="lang in languageOptions" :key="lang.value" class="lang-btn-item">
              <ElDropdownItem
                :command="lang.value"
                :class="{ 'is-selected': locale === lang.value }"
              >
                <span class="menu-txt">{{ lang.label }}</span>
                <ArtSvgIcon icon="ri:check-fill" class="text-base" v-if="locale === lang.value" />
              </ElDropdownItem>
            </div>
          </ElDropdownMenu>
        </template>
      </ElDropdown>
      <div
        v-if="shouldShowThemeToggle"
        class="btn theme-btn h-8 w-8 c-p flex-cc tad-300"
        @click="themeAnimation"
      >
        <ArtSvgIcon
          :icon="isDark ? 'ri:sun-fill' : 'ri:moon-line'"
          class="text-xl text-g-800 transition-colors duration-300"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n'
  import { useSettingStore } from '@/store/modules/setting'
  import { useUserStore } from '@/store/modules/user'
  import { useHeaderBar } from '@/hooks/core/useHeaderBar'
  import { themeAnimation } from '@/utils/ui/animation'
  import { languageOptions } from '@/locales'
  import { LanguageEnum } from '@/enums/appEnum'
  import AppConfig from '@/config'

  defineOptions({ name: 'AuthTopBar' })

  const settingStore = useSettingStore()
  const userStore = useUserStore()
  const { isDark } = storeToRefs(settingStore)
  const { shouldShowThemeToggle, shouldShowLanguage } = useHeaderBar()
  const { locale } = useI18n()

  const changeLanguage = (lang: LanguageEnum) => {
    if (locale.value === lang) return
    locale.value = lang
    userStore.setLanguage(lang)
  }
</script>

<style scoped>
</style>
