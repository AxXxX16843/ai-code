<template>
  <div class="app-chat-page">
    <div class="chat-header">
      <div class="chat-header-left">
        <a-button type="text" @click="goBack">返回工作台</a-button>
        <span class="app-name">{{ app.appName || '应用对话' }}</span>
      </div>
      <div class="chat-header-right">
        <a-button @click="showDetail = true">应用详情</a-button>
        <a-button :disabled="!app.id" @click="handleDownload">
          <template #icon><DownloadOutlined /></template>
          下载源码
        </a-button>
        <a-button type="primary" :loading="deploying" @click="handleDeploy">部署</a-button>
      </div>
    </div>

    <div class="chat-body">
      <div class="chat-left">
        <div class="message-list" ref="messageListRef">
          <div v-if="hasMore" class="load-more-wrap">
            <a-button size="small" :loading="loadingHistory" @click="loadHistory">
              加载更多
            </a-button>
          </div>
          <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
            <a-avatar
              :class="'message-avatar-' + msg.role"
              :src="msg.role === 'user' ? loginUserStore.loginUser.userAvatar : undefined"
              :size="32"
              style="flex-shrink: 0; background: transparent;"
            >
              <template v-if="msg.role === 'ai'">
                <AppLogo :size="30" variant="avatar" />
              </template>
            </a-avatar>
            <div class="message-bubble">
              <div v-if="msg.role === 'ai' && msg.content && msg.loading" class="streaming-content">
                {{ msg.content }}
              </div>
              <div
                v-else-if="msg.role === 'ai' && msg.content"
                class="markdown-body"
                v-html="renderMarkdown(msg.content)"
              ></div>
              <div v-else-if="msg.role === 'ai'" class="loading-text">AI 思考中...</div>
              <div v-else-if="msg.role === 'tool'" class="tool-message">
                <a-spin v-if="msg.status === 'loading'" size="small" />
                <CheckCircleOutlined v-else class="tool-success-icon" />
                <span>{{ msg.content }}</span>
              </div>
              <div v-else class="user-text">{{ msg.content }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <a-alert
            v-if="selectedElement"
            class="selected-element-alert"
            type="info"
            show-icon
            closable
            @close="clearSelectedElement"
          >
            <template #message>
              已选中 <strong>&lt;{{ selectedElement.tagName }}&gt;</strong> 元素
            </template>
            <template #description>
              <div class="selected-element-detail">
                <code>{{ selectedElement.selector }}</code>
                <span v-if="selectedElement.textContent">{{ selectedElement.textContent }}</span>
              </div>
            </template>
          </a-alert>
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
            <a-tooltip :title="visualEditTooltip">
              <a-button
                :type="editMode ? 'primary' : 'default'"
                :danger="editMode"
                :disabled="!canUseVisualEditor"
                @click="toggleEditMode"
              >
                <template #icon><SelectOutlined /></template>
                {{ editMode ? '退出编辑' : '可视化编辑' }}
              </a-button>
            </a-tooltip>
            <a-button type="primary" :loading="isGenerating" :disabled="!canChat" @click="handleSend">
              发送
            </a-button>
          </div>
        </div>
      </div>

      <div class="chat-right">
        <div v-if="editMode" class="preview-edit-status">
          <SelectOutlined />
          <span>选择要修改的页面元素</span>
        </div>
        <!-- 生成中：普通加载状态 -->
        <div v-if="previewState === 'generating'" class="generating-animation">
          <a-spin size="large" />
          <p class="generating-text">正在生成项目，请稍候…</p>
        </div>
        <!-- 构建中（Vue 项目） -->
        <div v-else-if="previewState === 'building'" class="building-hint">
          <a-spin size="large" />
          <p>正在构建项目 <small>安装依赖 · 打包</small></p>
        </div>
        <!-- 渲染中 -->
        <div v-else-if="previewState === 'rendering'" class="rendering-hint">
          <a-spin size="large" />
          <p>正在渲染网页效果 <small>即将完成</small></p>
        </div>
        <!-- 完成：展示网站 -->
        <iframe
          v-else-if="previewState === 'ready' && previewUrl"
          ref="iframeRef"
          :src="previewUrl"
          :class="['preview-frame', { 'preview-frame-editing': editMode }]"
          @load="handleIframeLoad"
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

    <!-- 部署成功弹窗 -->
    <a-modal
      v-model:open="deployModalVisible"
      title="部署成功"
      :footer="null"
    >
      <div class="deploy-info">
        <div class="deploy-item">
          <span class="deploy-label">创建者：</span>
          <span>{{ deployOwner }}</span>
        </div>
        <div class="deploy-item">
          <span class="deploy-label">部署地址：</span>
          <a :href="deployUrl" target="_blank" rel="noopener noreferrer">{{ deployUrl }}</a>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import AppLogo from '@/components/AppLogo.vue'
import { CheckCircleOutlined, DownloadOutlined, SelectOutlined } from '@ant-design/icons-vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import { getAppVoById, deploy } from '@/api/appController'
import { get as getChatHistoryPage } from '@/api/chatHistoryController'
import { API_BASE_URL, getStaticPreviewUrl, getDeployUrl } from '@/config/env'
import { useLoginUserStore } from '@/stores/loginUser'
import { buildVisualEditPrompt, useVisualEditor } from '@/composables/useVisualEditor'

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
const deployModalVisible = ref(false)
const deployUrl = ref('')
const deployOwner = ref('')
const previewUrl = ref('')
const previewState = ref('idle') // idle | generating | rendering | ready
const hasMore = ref(false)
const loadingHistory = ref(false)
const cursorTime = ref(null)
const messageListRef = ref(null)
let previewPollTimer = null
let scrollTimer = null

const {
  iframeRef,
  editMode,
  selectedElement,
  toggleEditMode,
  exitEditMode,
  clearSelectedElement,
  handleIframeLoad,
} = useVisualEditor({
  onError: () => message.error('无法开启可视化编辑，请确认预览页面与当前页面同源'),
})

// 是否允许对话：仅作品本人可对话
const canChat = computed(() => {
  const user = loginUserStore.loginUser
  return user && user.id && String(user.id) === String(app.value.userId)
})

const downloadUrl = computed(() => `${API_BASE_URL}/app/download/${appId.value}`)
const canUseVisualEditor = computed(
  () => canChat.value && !isGenerating.value && previewState.value === 'ready' && Boolean(previewUrl.value),
)
const visualEditTooltip = computed(() => {
  if (editMode.value) return '退出可视化编辑'
  if (!canChat.value) return '只能编辑自己的应用'
  if (previewState.value !== 'ready') return '网站预览就绪后可使用'
  return '在右侧预览中选择要修改的元素'
})

// 加载应用信息
const fetchAppInfo = async () => {
  const res = await getAppVoById({ id: appId.value })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
  }
}

// 游标分页加载对话历史
const loadHistory = async () => {
  loadingHistory.value = true
  const res = await getChatHistoryPage({ appId: appId.value, lastTime: cursorTime.value, pageSize: 10 })
  loadingHistory.value = false
  if (res.data.code === 0 && res.data.data) {
    const records = res.data.data.records || []
    const newMessages = records.map((r) => ({
      role: r.messageType === 'user' ? 'user' : 'ai',
      content: r.message,
      createTime: r.createTime,
    }))
    // 后端按时间降序返回，反转成升序（最早在前）后拼接到列表前面
    messages.value = [...newMessages.reverse(), ...messages.value]
    if (records.length > 0) {
      cursorTime.value = records[records.length - 1].createTime
    }
    hasMore.value = records.length >= 10
  } else {
    hasMore.value = false
  }
}

// 更新预览地址
const updatePreview = () => {
  if (app.value.codeGenType && appId.value) {
    previewUrl.value = getPreviewEntryUrl()
  }
}

const getPreviewEntryUrl = () => {
  const baseUrl = getStaticPreviewUrl(app.value.codeGenType, appId.value).replace(/\/?$/, '/')
  return `${baseUrl}index.html`
}

const scrollToBottom = () => {
  if (scrollTimer !== null) return
  scrollTimer = setTimeout(() => {
    nextTick(() => {
      if (messageListRef.value) {
        messageListRef.value.scrollTop = messageListRef.value.scrollHeight
      }
    })
    scrollTimer = null
  }, 80)
}

const handleError = (error, aiMessageIndex) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  previewState.value = 'idle'
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 生成完成后的处理：Vue 项目是异步构建，轮询预览直到 dist 就绪
const handleGenerationDone = async () => {
  await fetchAppInfo()
  if (app.value.codeGenType === 'vue_project') {
    previewState.value = 'building'
    startPollingPreview()
  } else {
    updatePreview()
    previewState.value = 'ready'
  }
}

// 轮询静态预览，直到构建产物可访问
const startPollingPreview = () => {
  if (previewPollTimer) clearTimeout(previewPollTimer)
  const url = getPreviewEntryUrl()
  console.log('[预览] 开始轮询 URL:', url)
  let retries = 0
  const maxRetries = 90
  const poll = async () => {
    if (retries >= maxRetries) {
      previewState.value = 'idle'
      message.error('项目构建超时，请查看后端日志')
      return
    }
    try {
      const res = await fetch(url)
      console.log('[预览] 轮询状态:', res.status)
      if (res.ok) {
        previewUrl.value = url + '?t=' + Date.now()
        previewState.value = 'rendering'
        setTimeout(() => {
          if (previewState.value === 'rendering') previewState.value = 'ready'
        }, 180)
        return
      }
    } catch (e) {
      console.error('[预览] 轮询失败:', e)
    }
    retries++
    previewPollTimer = setTimeout(poll, 3000)
  }
  poll()
}

// SSE 流式生成代码
const generateCode = async (userMessage, aiMessageIndex) => {
  let eventSource = null
  let streamCompleted = false
  let flushTimer = null
  let fullContent = ''
  let pendingContent = ''

  const flushContent = () => {
    if (pendingContent) {
      fullContent += pendingContent
      pendingContent = ''
      messages.value[aiMessageIndex].content = fullContent
      scrollToBottom()
    }
    flushTimer = null
  }

  const scheduleFlush = () => {
    if (flushTimer === null) flushTimer = setTimeout(flushContent, 80)
  }

  const completeStream = async () => {
    if (streamCompleted) return
    streamCompleted = true
    if (flushTimer !== null) clearTimeout(flushTimer)
    flushContent()
    messages.value[aiMessageIndex].loading = false
    isGenerating.value = false
    eventSource?.close()
    inputValue.value = ''
    await handleGenerationDone()
  }

  try {
    const params = new URLSearchParams({
      appId: appId.value || '',
      message: userMessage,
    })
    const url = `${API_BASE_URL}/app/gene?${params}`

    eventSource = new EventSource(url, { withCredentials: true })
    previewState.value = 'generating'

    eventSource.onmessage = function (event) {
      if (streamCompleted) return
      try {
        const parsed = JSON.parse(event.data)
        // 尝试解析内层类型化消息（VUE_PROJECT：{"type":"...","data":"..."}）
        let inner = null
        try {
          const obj = JSON.parse(parsed.d)
          if (obj && typeof obj === 'object' && obj.type) {
            inner = obj
          }
        } catch {
          inner = null
        }

        if (inner) {
          // 类型化消息：tool_request / tool_executed / ai_response
          if (inner.type === 'tool_request') {
            messages.value.push({
              role: 'tool',
              status: 'loading',
              content: `正在执行工具「${inner.name || '工具'}」...`,
            })
          } else if (inner.type === 'tool_executed') {
            // 把最后一条 loading 的 tool 消息更新为成功
            for (let i = messages.value.length - 1; i >= 0; i--) {
              if (messages.value[i].role === 'tool' && messages.value[i].status === 'loading') {
                messages.value[i].status = 'success'
                messages.value[i].content = '工具执行成功'
                break
              }
            }
          } else if (inner.type === 'ai_response') {
            const content = inner.data
            if (content !== undefined && content !== null) {
              pendingContent += content
              scheduleFlush()
            }
          }
        } else {
          // HTML/MULTI_FILE：parsed.d 是原始代码片段
          const content = parsed.d
          if (content !== undefined && content !== null) {
            pendingContent += content
            scheduleFlush()
          }
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        if (flushTimer !== null) clearTimeout(flushTimer)
        handleError(error, aiMessageIndex)
      }
    }

    eventSource.addEventListener('done', function () {
      completeStream()
    })

    eventSource.onerror = function () {
      if (streamCompleted || !isGenerating.value) return
      if (eventSource?.readyState === EventSource.CONNECTING) {
        completeStream()
      } else {
        if (flushTimer !== null) clearTimeout(flushTimer)
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

  const selectedContext = selectedElement.value
  const backendMessage = buildVisualEditPrompt(userMessage, selectedContext)

  messages.value.push({ role: 'user', content: userMessage })
  const aiMessageIndex = messages.value.length
  messages.value.push({ role: 'ai', content: '', loading: true })
  inputValue.value = ''
  isGenerating.value = true
  exitEditMode()
  scrollToBottom()
  await generateCode(backendMessage, aiMessageIndex)
}

// 部署应用
const handleDeploy = async () => {
  exitEditMode()
  deploying.value = true
  const res = await deploy({ appId: appId.value })
  deploying.value = false
  if (res.data.code === 0) {
    message.success('部署成功')
    await fetchAppInfo()
    // 部署后强制刷新预览（Vue 项目 dist 已构建）
    previewUrl.value = getPreviewEntryUrl() + '?t=' + Date.now()
    previewState.value = 'ready'
    // 弹出部署信息框（创建者 + 部署地址超链接）
    deployUrl.value = app.value.deployKey ? getDeployUrl(app.value.deployKey) : ''
    deployOwner.value = app.value.userVo?.userName || '无名'
    deployModalVisible.value = true
  } else {
    message.error('部署失败，' + res.data.message)
  }
}

const handleDownload = () => {
  if (!app.value.id) {
    message.info('应用信息加载中，请稍候')
    return
  }
  window.open(downloadUrl.value, '_blank', 'noopener,noreferrer')
}

const goBack = () => {
  router.push('/')
}

// Markdown 渲染
const renderMarkdown = (content) => {
  try {
    return marked.parse(content || '')
  } catch {
    return content
  }
}

onMounted(async () => {
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  await fetchAppInfo()
  await loadHistory()

  // 如果有至少 2 条对话记录，展示已生成的网站
  if (messages.value.length >= 2) {
    if (app.value.codeGenType === 'vue_project') {
      previewState.value = 'building'
      startPollingPreview()
    } else {
      updatePreview()
      previewState.value = 'ready'
    }
  }

  // 自己的 app 且没有对话历史，才自动发送初始 prompt
  if (canChat.value && messages.value.length === 0 && app.value.initPrompt) {
    inputValue.value = ''
    messages.value.push({ role: 'user', content: app.value.initPrompt })
    const aiMessageIndex = messages.value.length
    messages.value.push({ role: 'ai', content: '', loading: true })
    isGenerating.value = true
    scrollToBottom()
    await generateCode(app.value.initPrompt, aiMessageIndex)
  }
})

onBeforeUnmount(() => {
  if (previewPollTimer) clearTimeout(previewPollTimer)
  if (scrollTimer) clearTimeout(scrollTimer)
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
  padding: 12px clamp(16px, 3vw, 32px);
  border-bottom: 1px solid #e2e9e7;
  background: rgba(255,255,255,.94);
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-name {
  font-size: 16px;
  font-weight: 700;
  color: #10232d;
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
  position: relative;
  width: 60%;
  display: flex;
  align-items: stretch;
  background: #eef3f1;
  padding: 18px;
}

.preview-frame {
  flex: 1;
  border: none;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.preview-frame-editing {
  box-shadow: 0 0 0 2px rgba(19, 168, 138, 0.28), 0 8px 24px rgba(16, 35, 45, 0.1);
}

.preview-edit-status {
  position: absolute;
  top: 28px;
  left: 50%;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 7px 12px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 6px;
  color: #fff;
  background: rgba(8, 125, 104, 0.92);
  box-shadow: 0 7px 20px rgba(8, 55, 47, 0.18);
  font-size: 13px;
  pointer-events: none;
  transform: translateX(-50%);
}

.preview-empty {
  margin: auto;
  color: #82919a;
}

/* 生成中：风趣加载动画 */
.generating-animation {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 36px 48px;
  border: 1px solid #d8e5e0;
  border-radius: 14px;
  background: #f8fbfa;
  box-shadow: 0 12px 26px rgba(16, 35, 45, .07);
}

.generating-text {
  color: #51636c;
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

@media (max-width: 860px) {
  .app-chat-page { height: auto; min-height: calc(100vh - 64px); }
  .chat-body { flex-direction: column; overflow: visible; }
  .chat-left, .chat-right { width: 100%; }
  .chat-left { min-height: 58vh; border-right: 0; border-bottom: 1px solid #e2e9e7; }
  .chat-right { min-height: 42vh; }
  .preview-frame { min-height: 360px; }
}

.building-hint {
  margin: auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.building-hint p, .rendering-hint p { margin: 0; font-size: 14px; color: #51636c; }
.building-hint small, .rendering-hint small { display: block; margin-top: 5px; text-align: center; color: #82919a; font-size: 11px; }
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 22px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  padding-bottom: 4px;
}

.message-item {
  display: flex;
  gap: 10px;
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
  padding: 11px 14px;
  border-radius: 10px;
  word-break: break-word;
}

.message-item.user .message-bubble {
  background: #129b8a;
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 5px 14px rgba(18, 155, 138, .22);
}

.message-item.ai .message-bubble {
  background: #fff;
  border: 1px solid #e2e9e7;
  border-top-left-radius: 4px;
  box-shadow: 0 4px 14px rgba(16, 35, 45, .05);
  width: 100%;
}

.user-text {
  white-space: pre-wrap;
}

.streaming-content {
  max-height: 360px;
  overflow: auto;
  white-space: pre-wrap;
  color: #51636c;
  font: 13px/1.65 ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.loading-text {
  color: rgba(0, 0, 0, 0.45);
}

.tool-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.65);
  font-size: 13px;
}

.tool-success-icon {
  color: #52c41a;
}

.deploy-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.deploy-item {
  font-size: 14px;
  line-height: 1.6;
  word-break: break-all;
}

.deploy-label {
  color: rgba(0, 0, 0, 0.45);
}

.chat-input-area {
  padding: 14px 18px 16px;
  border-top: 1px solid #e2e9e7;
  background: #fbfcfc;
}
.selected-element-alert {
  margin-bottom: 10px;
}
.selected-element-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}
.selected-element-detail code {
  overflow: hidden;
  color: #087d68;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.selected-element-detail span {
  display: -webkit-box;
  overflow: hidden;
  color: #51636c;
  font-size: 12px;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.chat-input-tools {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
