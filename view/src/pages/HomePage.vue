<template>
  <div class="home-page">
    <!-- 顶部 Hero：ChatGPT 式对话框 -->
    <div class="hero">
      <h1 class="hero-title">AI 应用生成平台</h1>
      <p class="hero-subtitle">一句话，呈所想</p>

      <div class="input-box">
        <a-textarea
          v-model:value="prompt"
          :placeholder="currentPlaceholder"
          :auto-size="{ minRows: 3, maxRows: 6 }"
          class="prompt-input"
          @pressEnter="handleCreate"
        />
        <div class="input-actions">
          <span class="input-hint">按 Enter 发送</span>
          <a-button
            type="primary"
            shape="circle"
            :size="'large'"
            :loading="creating"
            :disabled="!prompt.trim()"
            @click="handleCreate"
          >
            <template #icon><SendOutlined /></template>
          </a-button>
        </div>
      </div>
    </div>

    <!-- 应用列表 -->
    <div class="app-sections">
      <template v-if="isLogin">
        <div class="app-section">
          <h2 class="section-title">我的应用</h2>
          <a-row v-if="myApps.length" :gutter="[16, 16]">
            <a-col v-for="app in myApps" :key="app.id" :xs="24" :sm="12" :lg="8">
              <AppCard :app="app" />
            </a-col>
          </a-row>
          <a-empty v-else description="还没有应用，试试在上方输入提示词创建" />
        </div>
      </template>

      <div class="app-section">
        <h2 class="section-title">精选应用</h2>
        <a-row v-if="goodApps.length" :gutter="[16, 16]">
          <a-col v-for="app in goodApps" :key="app.id" :xs="24" :sm="12" :lg="8">
            <AppCard :app="app" />
          </a-col>
        </a-row>
        <a-empty v-else description="暂无精选应用" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SendOutlined } from '@ant-design/icons-vue'
import AppCard from '@/components/AppCard.vue'
import { addApp, listGoodAppVoByPage, listMyAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const prompt = ref('')
const creating = ref(false)
const myApps = ref([])
const goodApps = ref([])
const isLogin = ref(false)

// 动态轮换的示例提示词
const placeholderPrompts = [
  '帮我创建一个个人博客网站，包含文章列表和文章详情页，支持 Markdown 展示，界面简洁美观',
  '帮我创建一个待办事项管理网站，支持添加、勾选、删除待办事项，数据本地存储',
  '帮我创建一个美食菜谱网站，展示各种菜品的做法步骤，支持按分类筛选',
  '帮我创建一个个人作品集网站，展示我的项目和联系方式，适配移动端',
]
const currentPlaceholder = ref(placeholderPrompts[0])
let placeholderIndex = 0
let placeholderTimer = null

const startPlaceholderRotation = () => {
  placeholderTimer = setInterval(() => {
    placeholderIndex = (placeholderIndex + 1) % placeholderPrompts.length
    currentPlaceholder.value = placeholderPrompts[placeholderIndex]
  }, 4000)
}

// 创建应用并跳转对话页
const handleCreate = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入提示词')
    return
  }
  creating.value = true
  const res = await addApp({ initPrompt: prompt.value })
  creating.value = false
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // appId 保持字符串，避免雪花 ID 精度丢失
    router.push({
      path: `/app/chat/${res.data.data}`,
    })
  } else {
    message.error('创建应用失败，' + res.data.message)
  }
}

// 加载我的应用
const loadMyApps = async () => {
  const res = await listMyAppVoByPage({
    pageNum: 1,
    pageSize: 12,
    sortField: 'createTime',
    sortOrder: 'desc',
  })
  if (res.data.code === 0 && res.data.data) {
    myApps.value = res.data.data.records || []
  }
}

// 加载精选应用
const loadGoodApps = async () => {
  const res = await listGoodAppVoByPage({
    pageNum: 1,
    pageSize: 12,
    sortField: 'createTime',
    sortOrder: 'desc',
  })
  if (res.data.code === 0 && res.data.data) {
    goodApps.value = res.data.data.records || []
  }
}

onMounted(async () => {
  startPlaceholderRotation()
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  isLogin.value = !!loginUserStore.loginUser.id
  if (isLogin.value) {
    loadMyApps()
  }
  loadGoodApps()
})

onBeforeUnmount(() => {
  clearInterval(placeholderTimer)
})
</script>

<style scoped>
.home-page {
  position: relative;
  min-height: calc(100vh - 64px);
  padding: 24px 0 48px;
  overflow: hidden;
}

/* 背景光斑 */
.home-page::before,
.home-page::after {
  content: '';
  position: fixed;
  border-radius: 50%;
  filter: blur(90px);
  z-index: 0;
  pointer-events: none;
}

.home-page::before {
  width: 420px;
  height: 420px;
  background: rgba(22, 119, 255, 0.16);
  top: -120px;
  left: -120px;
  animation: float 9s ease-in-out infinite;
}

.home-page::after {
  width: 340px;
  height: 340px;
  background: rgba(114, 46, 209, 0.14);
  bottom: -80px;
  right: -80px;
  animation: float 12s ease-in-out infinite reverse;
}

@keyframes float {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(40px, 30px);
  }
}

/* Hero 区域 */
.hero {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  text-align: center;
  animation: fadeUp 0.6s ease;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-title {
  font-size: 44px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #1677ff 0%, #722ed1 50%, #eb2f96 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradientMove 6s ease infinite;
}

@keyframes gradientMove {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.45);
  margin-bottom: 32px;
}

/* 输入框 */
.input-box {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s, border-color 0.3s;
}

.input-box:focus-within {
  border-color: #1677ff;
  box-shadow: 0 4px 24px rgba(22, 119, 255, 0.15);
}

.prompt-input {
  border: none;
  font-size: 15px;
  resize: none;
  box-shadow: none;
  background: transparent;
}

.prompt-input:hover,
.prompt-input:focus {
  border: none;
  box-shadow: none;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.input-hint {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.25);
}

/* 应用列表 */
.app-sections {
  position: relative;
  z-index: 1;
  max-width: 1080px;
  margin: 0 auto;
  padding: 24px;
}

.app-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
}
</style>
