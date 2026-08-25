<template>
  <div class="app-chat-page">
    <div class="chat-header">
      <div class="chat-header-left">
        <a-button @click="goBack">返回</a-button>
        <span class="app-name">{{ app.appName || '应用对话' }}</span>
      </div>
      <div class="chat-header-right">
        <a-button @click="showDetail = true">应用详情</a-button>
        <a-button type="primary" :loading="deploying" @click="handleDeploy">部署</a-button>
      </div>
    </div>

    <div class="chat-body">
      <div class="chat-left">
        <div class="message-list" ref="messageListRef">
          <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
            <a-avatar
              :class="'message-avatar-' + msg.role"
              :src="msg.role === 'user' ? loginUserStore.loginUser.userAvatar : undefined"
              :size="32"
              style="flex-shrink: 0; background: transparent;"
            >
              <template v-if="msg.role === 'ai'">
                <AppLogo :size="30" />
              </template>
            </a-avatar>
            <div class="message-bubble">
              <div
                v-if="msg.role === 'ai' && msg.content"
                class="markdown-body"
                v-html="renderMarkdown(msg.content)"
              ></div>
              <div v-else-if="msg.role === 'ai'" class="loading-text">AI 思考中...</div>
              <div v-else class="user-text">{{ msg.content }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <a-tooltip
            :title="!canChat ? '无法在别人的作品下对话哦~' : ''"
            placement="top"
            :mouse-enter-delay="0.1"
          >
            <a-textarea
              v-model:value="inputValue"
              :disabled="!canChat || isGenerating"
              :placeholder="canChat ? '请描述你想生成的网站，越详细效果越好哦' : '无法在别人的作品下对话哦~'"
              :auto-size="{ minRows: 2, maxRows: 4 }"
              @pressEnter="handleSend"
            />
          </a-tooltip>
          <div class="chat-input-tools">
            <a-button type="primary" :loading="isGenerating" :disabled="!canChat" @click="handleSend">
              发送
            </a-button>
          </div>
        </div>
      </div>

      <div class="chat-right">
        <!-- 生成中：风趣加载动画 -->
        <div v-if="previewState === 'generating'" class="generating-animation">
          <AppLogo :size="90" />
          <div class="thinking-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <p class="generating-text">AI 正在努力生成中...</p>
        </div>
        <!-- 渲染中 -->
        <div v-else-if="previewState === 'rendering'" class="rendering-hint">
          <a-spin />
          <p>正在渲染网页效果...</p>
        </div>
        <!-- 完成：展示网站 -->
        <iframe
          v-else-if="previewState === 'ready' && previewUrl"
          :src="previewUrl"
          class="preview-frame"
        />
        <a-empty v-else description="生成完成后将在这里展示网站效果" class="preview-empty" />
      </div>
    </div>

    <AppDetailModal
      :open="showDetail"
      :app="app"
      :current-user="loginUserStore.loginUser"
      @close="showDetail = false"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import AppLogo from '@/components/AppLogo.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import { getAppVoById, deploy } from '@/api/appController'
import { API_BASE_URL, getStaticPreviewUrl, getDeployUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'

marked.use(
  markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    },
  }),
)

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = ref(route.params.appId)
const app = ref({})
const messages = ref([])
const inputValue = ref('')
const isGenerating = ref(false)
const deploying = ref(false)
const showDetail = ref(false)
const previewUrl = ref('')
const previewState = ref('idle') // idle | generating | rendering | ready
const messageListRef = ref(null)

const aiAvatar = '' // 可替换为 assets/aiAvatar.png

// 是否允许对话：仅作品本人可对话
const canChat = computed(() => {
  const user = loginUserStore.loginUser
  return user && user.id && String(user.id) === String(app.value.userId)
})

// 加载应用信息
const fetchAppInfo = async () => {
  const res = await getAppVoById({ id: appId.value })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
  }
}

// 更新预览地址
const updatePreview = () => {
  if (app.value.codeGenType && app.value.id) {
    previewUrl.value = getStaticPreviewUrl(app.value.codeGenType, app.value.id)
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleError = (error, aiMessageIndex) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  previewState.value = 'idle'
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// SSE 流式生成代码
const generateCode = async (userMessage, aiMessageIndex) => {
  let eventSource = null
  let streamCompleted = false

  try {
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })
    const url = `${API_BASE_URL}/app/gene?${params}`

    eventSource = new EventSource(url, { withCredentials: true })
    previewState.value = 'generating'

    let fullContent = ''

    eventSource.onmessage = function (event) {
      if (streamCompleted) return
      try {
        const parsed = JSON.parse(event.data)
        const content = parsed.d
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    eventSource.addEventListener('done', function () {
      if (streamCompleted) return
      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()
      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        previewState.value = 'rendering'
        await fetchAppInfo()
        updatePreview()
        previewState.value = 'ready'
      }, 800)
    })

    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
        setTimeout(async () => {
          previewState.value = 'rendering'
          await fetchAppInfo()
          updatePreview()
          previewState.value = 'ready'
        }, 800)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    handleError(error, aiMessageIndex)
  }
}

// 发送消息
const handleSend = async () => {
  const userMessage = inputValue.value.trim()
  if (!userMessage) return
  if (!canChat.value) {
    message.warning('无法在别人的作品下对话哦~')
    return
  }
  if (isGenerating.value) return

  messages.value.push({ role: 'user', content: userMessage })
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '', loading: true })
  inputValue.value = ''
  isGenerating.value = true
  scrollToBottom()
  await generateCode(userMessage, aiMessageIndex)
}

// 部署应用
const handleDeploy = async () => {
  deploying.value = true
  const res = await deploy({ appId: appId.value })
  deploying.value = false
  if (res.data.code === 0) {
    message.success('部署成功')
    await fetchAppInfo()
    // 部署成功后打开部署地址（新页面）
    if (app.value.deployKey) {
      window.open(getDeployUrl(app.value.deployKey), '_blank')
    }
  } else {
    message.error('部署失败，' + res.data.message)
  }
}

const goBack = () => {
  router.push('/')
}

// Markdown 渲染
const renderMarkdown = (content) => {
  try {
    return marked.parse(content || '')
  } catch (e) {
    return content
  }
}

onMounted(async () => {
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  await fetchAppInfo()
  updatePreview()

  // 无 view=1 参数时，自动发送应用的初始 prompt（仅本人）
  if (!route.query.view && canChat.value && app.value.initPrompt) {
    messages.value.push({ role: 'user', content: app.value.initPrompt })
    const aiMessageIndex = messages.value.length
    messages.value.push({ role: 'ai', content: '', loading: true })
    isGenerating.value = true
    scrollToBottom()
    await generateCode(app.value.initPrompt, aiMessageIndex)
  }
})
</script>

<style scoped>
.app-chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
}

.chat-header-right {
  display: flex;
  gap: 8px;
}

.chat-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.chat-left {
  width: 40%;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f0f0f0;
}

.chat-right {
  width: 60%;
  display: flex;
  align-items: stretch;
  background: #f7f9fc;
  padding: 16px;
}

.preview-frame {
  flex: 1;
  border: none;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.preview-empty {
  margin: auto;
}

/* 生成中：风趣加载动画 */
.generating-animation {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.thinking-dots {
  display: flex;
  gap: 8px;
}

.thinking-dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #1677ff;
  animation: dotBounce 1.2s ease-in-out infinite;
}

.thinking-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes dotBounce {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.generating-text {
  color: rgba(0, 0, 0, 0.45);
  font-size: 14px;
}

/* 渲染中 */
.rendering-hint {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  gap: 8px;
  animation: msgIn 0.3s ease;
}

@keyframes msgIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(22, 119, 255, 0.2);
}

.message-item.ai .message-bubble {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  width: 100%;
}

.user-text {
  white-space: pre-wrap;
}

.loading-text {
  color: rgba(0, 0, 0, 0.45);
}

.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
}

.chat-input-tools {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
