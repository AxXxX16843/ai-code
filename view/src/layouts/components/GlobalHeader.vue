<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MenuOutlined, UserOutlined } from '@ant-design/icons-vue'
import logo from '@/assets/logo.png'
import { menuItems } from '@/config/menu'

// 网站标题（可在此配置）
const siteTitle = 'AI Code'

const router = useRouter()
const route = useRoute()

const selectedKeys = computed(() => [route.path])
const drawerOpen = ref(false)

function handleMenuClick({ key }) {
  router.push(key)
  drawerOpen.value = false
}

// 登录占位：后续接入真实登录逻辑
function handleLogin() {}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-inner">
      <!-- 左侧：logo + 标题 -->
      <div class="header-left">
        <img class="header-logo" :src="logo" alt="logo" />
        <span class="header-title">{{ siteTitle }}</span>
      </div>

      <!-- 中间：菜单（桌面端） -->
      <div class="header-menu">
        <a-menu
          mode="horizontal"
          :items="menuItems"
          :selected-keys="selectedKeys"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧：登录按钮（移动端为汉堡菜单） -->
      <div class="header-right">
        <a-button class="login-btn" type="text" @click="handleLogin">
          <template #icon><UserOutlined /></template>
          登录
        </a-button>
        <a-button class="menu-trigger" type="text" @click="drawerOpen = true">
          <template #icon><MenuOutlined /></template>
        </a-button>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <a-drawer
      v-model:open="drawerOpen"
      placement="right"
      :closable="false"
      :width="220"
    >
      <a-menu
        mode="inline"
        :items="menuItems"
        :selected-keys="selectedKeys"
        @click="handleMenuClick"
      />
    </a-drawer>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  line-height: 64px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.header-logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  white-space: nowrap;
}

.header-menu {
  flex: 1;
  margin: 0 24px;
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 桌面端隐藏汉堡按钮 */
.menu-trigger {
  display: none;
}

/* 移动端：隐藏内联菜单，显示汉堡按钮 */
@media (max-width: 768px) {
  .header-menu {
    display: none;
  }

  .menu-trigger {
    display: inline-flex;
  }
}
</style>
