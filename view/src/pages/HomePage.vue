<template>
  <div class="home-page">
    <div class="ambient-scene" aria-hidden="true">
      <span class="scene-grid"></span>
      <span class="scene-orbit scene-orbit--one"></span>
      <span class="scene-orbit scene-orbit--two"></span>
      <span class="scene-beam"></span>
      <span class="scene-line scene-line--one"></span>
      <span class="scene-line scene-line--two"></span>
      <span class="scene-line scene-line--three"></span>
      <span class="scene-spark scene-spark--one"></span>
      <span class="scene-spark scene-spark--two"></span>
    </div>
    <!-- 顶部 Hero：ChatGPT 式对话框 -->
    <div class="hero">
      <div class="hero-eyebrow"><span class="eyebrow-dot"></span> AI APPLICATION STUDIO</div>
      <h1 class="hero-title">把想法，变成<br /><em>可以运行的产品。</em></h1>
      <p class="hero-subtitle">用自然语言描述你的构想，小狐狸会替你完成从设计到部署。</p>

      <div class="input-box">
        <a-textarea
          v-model:value="prompt"
          :placeholder="currentPlaceholder"
          :auto-size="{ minRows: 3, maxRows: 6 }"
          class="prompt-input"
          @pressEnter="handleCreate"
        />
        <div class="input-actions">
          <span class="input-hint">ENTER 发送 · SHIFT + ENTER 换行</span>
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
      <div class="quick-prompts" aria-label="预设提示词">
        <span class="quick-label">试试这样开始</span>
        <button v-for="item in quickPrompts" :key="item.label" type="button" class="quick-prompt" @click="useQuickPrompt(item.prompt)">
          <span class="quick-icon">{{ item.icon }}</span>{{ item.label }}
        </button>
      </div>
    </div>

    <!-- 应用列表 -->
    <div class="app-sections">
      <template v-if="isLogin">
        <div class="app-section">
          <div class="section-heading"><h2 class="section-title">我的应用</h2><span class="section-rule"></span></div>
          <a-row v-if="myApps.length" :gutter="[16, 16]">
            <a-col v-for="app in myApps" :key="app.id" :xs="24" :sm="12" :lg="8">
              <AppCard :app="app" />
            </a-col>
          </a-row>
          <a-empty v-else description="还没有应用，试试在上方输入提示词创建" />
        </div>
      </template>

      <div class="app-section">
        <div class="section-heading"><h2 class="section-title">精选应用</h2><span class="section-rule"></span></div>
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
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
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

const quickPrompts = [
  { icon: '✦', label: '极简作品集', prompt: '创建一个极简风格的个人作品集，包含项目筛选、项目详情和联系表单，适配移动端' },
  { icon: '◈', label: '数据看板', prompt: '创建一个 SaaS 数据看板，展示核心指标、趋势图表和最近活动，支持深色模式' },
  { icon: '⌁', label: '旅行规划器', prompt: '创建一个旅行规划器，可以添加目的地、安排每日行程，并用时间线展示路线' },
  { icon: '＋', label: '待办清单', prompt: '创建一个待办事项应用，支持标签、优先级、截止日期和本地存储，界面清晰高效' },
]

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

const useQuickPrompt = async (value) => {
  prompt.value = value
  await nextTick()
  document.querySelector('.prompt-input')?.focus()
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
.home-page { position:relative; isolation:isolate; overflow:hidden; min-height:calc(100vh - 64px); padding:24px 0 56px; background:#f0f6f4; }
.ambient-scene { position:absolute; inset:0; z-index:-1; pointer-events:none; overflow:hidden; }
.scene-grid { position:absolute; inset:0; opacity:.42; background-image:linear-gradient(rgba(18,155,138,.11) 1px, transparent 1px),linear-gradient(90deg, rgba(18,155,138,.11) 1px, transparent 1px); background-size:48px 48px; mask-image:linear-gradient(to bottom, transparent, #000 14%, #000 84%, transparent); }
.scene-orbit { position:absolute; display:block; border:1px solid rgba(18,155,138,.22); border-radius:50%; transform:rotate(-18deg); animation:orbitFloat 16s ease-in-out infinite; box-shadow:0 0 20px rgba(18,155,138,.06); }
.scene-orbit--one { width:480px; height:180px; top:75px; right:-180px; }
.scene-orbit--two { width:300px; height:120px; bottom:105px; left:-110px; border-color:rgba(217,109,87,.22); transform:rotate(24deg); animation-delay:-7s; box-shadow:0 0 20px rgba(217,109,87,.06); }
.scene-beam { position:absolute; top:42%; left:-15%; width:38%; height:2px; background:#2dd4bf; opacity:.3; box-shadow:0 0 18px 3px rgba(45,212,191,.34); animation:beamSweep 12s ease-in-out infinite; }
.scene-line { position:absolute; display:block; width:320px; height:120px; border:1px solid rgba(18,155,138,.14); transform:rotate(-28deg); animation: drift 14s ease-in-out infinite; }
.scene-line--one { top:92px; left:-120px; }
.scene-line--two { top:280px; right:-150px; width:420px; height:180px; border-color:rgba(200,146,62,.14); transform:rotate(24deg); animation-delay:-4s; }
.scene-line--three { bottom:80px; left:12%; width:260px; height:90px; border-color:rgba(217,109,87,.11); transform:rotate(12deg); animation-delay:-8s; }
.scene-spark { position:absolute; width:10px; height:10px; border:2px solid var(--gold); transform:rotate(45deg); animation:spark 3.8s ease-in-out infinite; box-shadow:0 0 12px rgba(200,146,62,.26); }
.scene-spark--one { top:150px; right:14%; }
.scene-spark--two { bottom:180px; left:18%; width:6px; height:6px; border-color:var(--teal); animation-delay:-1.8s; }
.hero { max-width: 760px; margin: 0 auto; padding: 62px 24px 46px; text-align: center; animation: fadeUp .6s ease; }
.hero-eyebrow { display:inline-flex; align-items:center; gap:8px; font-size:11px; letter-spacing:.2em; color:var(--teal-deep); font-weight:700; }
.eyebrow-dot { width:7px; height:7px; border-radius:50%; background:var(--coral); box-shadow:0 0 0 5px rgba(217,109,87,.12); }
.hero-title { margin:16px 0 14px; font-size:clamp(36px, 5vw, 58px); line-height:1.08; letter-spacing:-.02em; color:var(--ink); }
.hero-title em { color:var(--teal-deep); font-style:normal; }
.hero-subtitle { max-width:540px; margin:0 auto 30px; color:var(--ink-soft); font-size:15px; line-height:1.7; }
.input-box { padding:14px 16px 12px; text-align:left; background:var(--surface); border:1px solid var(--line); border-radius:14px; box-shadow:0 14px 36px rgba(16,35,45,.09); transition:border-color .25s, box-shadow .25s; }
.input-box:focus-within { border-color:var(--teal); box-shadow:0 16px 42px rgba(18,155,138,.14); }
.prompt-input { min-height:88px; padding:4px 0; border:0; font-size:15px; resize:none; box-shadow:none; background:transparent; }
.prompt-input:hover,.prompt-input:focus { border:0; box-shadow:none; }
.input-actions { display:flex; align-items:center; justify-content:space-between; margin-top:4px; }
.input-hint { font-size:10px; letter-spacing:.08em; color:var(--muted); }
.quick-prompts { display:flex; flex-wrap:wrap; align-items:center; justify-content:center; gap:8px; margin-top:18px; }
.quick-label { width:100%; margin-bottom:2px; font-size:11px; color:var(--muted); letter-spacing:.08em; }
.quick-prompt { display:inline-flex; align-items:center; gap:7px; min-height:32px; padding:0 11px; border:1px solid #d8e5e0; border-radius:7px; background:rgba(255,255,255,.7); color:var(--ink-soft); font:500 12px inherit; cursor:pointer; transition:transform .2s, border-color .2s, color .2s, background .2s; }
.quick-prompt:hover { transform:translateY(-2px); border-color:var(--teal); background:#fff; color:var(--teal-deep); }
.quick-icon { color:var(--gold); font-size:13px; }
.app-sections { max-width:1120px; margin:0 auto; padding:20px 24px; }
.app-section { margin-bottom:42px; }
.section-heading { display:flex; align-items:center; gap:14px; margin-bottom:16px; }
.section-title { margin:0; font-size:17px; font-weight:700; color:var(--ink); }
.section-rule { height:1px; flex:1; background:var(--line); }
@keyframes fadeUp { from { opacity:0; transform:translateY(16px); } to { opacity:1; transform:translateY(0); } }
@keyframes drift { 0%,100% { transform:rotate(-28deg) translate(0,0); } 50% { transform:rotate(-24deg) translate(18px,10px); } }
@keyframes spark { 0%,100% { opacity:.35; transform:rotate(45deg) scale(.85); } 50% { opacity:1; transform:rotate(135deg) scale(1.2); } }
@keyframes orbitFloat { 0%,100% { transform:rotate(-18deg) translate(0,0); opacity:.65; } 50% { transform:rotate(-12deg) translate(-12px,14px); opacity:1; } }
@keyframes beamSweep { 0%,100% { transform:translateX(0); opacity:0; } 18% { opacity:.18; } 62% { opacity:.18; } 82% { transform:translateX(420%); opacity:0; } }
@media (max-width:600px) { .hero { padding-top:40px; } .app-sections { padding-inline:16px; } .hero-title { font-size:38px; } .quick-prompts { justify-content:flex-start; } .quick-label { text-align:left; } .quick-prompt { flex:1 1 calc(50% - 8px); justify-content:center; } }
</style>
