<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MenuOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import AppLogo from '@/components/AppLogo.vue'
import { menuItems as originMenuItems } from '@/config/menu'
import { useLoginUserStore } from '@/stores/loginUser'
import { logout } from '@/api/userController'

// 网站标题（可在此配置）
const siteTitle = 'AI Code'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const selectedKeys = computed(() => [route.path])
const drawerOpen = ref(false)

// 过滤菜单项：/admin 开头的菜单仅管理员可见
const menuItems = computed(() => {
  return originMenuItems.filter((menu) => {
    if (menu.key?.startsWith('/admin')) {
      return loginUserStore.loginUser?.userRole === 'admin'
    }
    return true
  })
})

function handleMenuClick({ key }) {
  router.push(key)
  drawerOpen.value = false
}

function handleLogin() {
  router.push('/user/login')
}

// 用户注销
const doLogout = async () => {
  const res = await logout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<template>
  <a-layout-header class="global-header">
    <div class="header-inner">
      <!-- 左侧：logo + 标题 -->
      <div class="header-left">
        <AppLogo :size="32" />
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

      <!-- 右侧：登录状态 / 登录按钮 -->
      <div class="header-right">
        <div v-if="loginUserStore.loginUser.id" class="user-login-status">
          <a-dropdown>
            <a-space>
              <a-avatar :src="loginUserStore.loginUser.userAvatar" />
              {{ loginUserStore.loginUser.userName ?? '无名' }}
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else>
          <a-button type="primary" @click="handleLogin">登录</a-button>
        </div>
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

.user-login-status {
  cursor: pointer;
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
