<template>
  <div class="app-card">
    <div class="app-card-cover-wrap">
      <img class="app-card-cover" :src="cover" :alt="app.appName" loading="lazy" />
      <!-- 悬停浮现操作按钮 -->
      <div class="app-card-overlay">
        <a-button type="primary" size="middle" block @click="goChat">进入工作台</a-button>
        <a-button size="middle" block ghost @click="goDeploy">查看作品</a-button>
      </div>
    </div>
    <div class="app-card-body">
      <div class="app-card-name">{{ app.appName || '未命名应用' }}</div>
      <div class="app-card-user">
        <UserOutlined />
        {{ app.userVo?.userName || '无名' }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { getDeployUrl } from '@/config/env'

const props = defineProps({
  app: {
    type: Object,
    required: true,
  },
})

const router = useRouter()

// 封面图片：没有则用 picsum 随机图（以 app.id 为种子，同一应用固定不变）
const cover = computed(() => {
  if (props.app.cover) {
    return props.app.cover
  }
  return `https://picsum.photos/640/480?random=${props.app.id}`
})

// 跳转应用对话页
const goChat = () => {
  router.push({
    path: `/app/chat/${props.app.id}`,
  })
}

// 打开部署作品（有 deployKey 才可查看）
const goDeploy = () => {
  if (props.app.deployKey) {
    window.open(getDeployUrl(props.app.deployKey), '_blank')
  } else {
    message.info('该应用尚未部署')
  }
}
</script>

<style scoped>
.app-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 10px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: 0 10px 26px rgba(16, 35, 45, .06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
  height: 100%;
}

.app-card:hover {
  transform: translateY(-4px);
  border-color: rgba(18, 155, 138, .5);
  box-shadow: 0 16px 32px rgba(16, 35, 45, .12);
}

.app-card-cover-wrap {
  position: relative;
  overflow: hidden;
}

.app-card-cover {
  width: 100%;
  aspect-ratio: 1 / 0.68;
  object-fit: cover;
  background: #dfe8e5;
  transition: transform 0.3s ease;
}

.app-card:hover .app-card-cover {
  transform: scale(1.05);
}

/* 悬停浮现操作层 */
.app-card-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 16px;
  background: rgba(16, 35, 45, .78);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.app-card-overlay .ant-btn {
  min-width: 140px;
  border-radius: 10px;
}

.app-card:hover .app-card-overlay {
  opacity: 1;
}

.app-card-body {
  padding: 14px 16px 16px;
}

.app-card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-card-user {
  font-size: 12px;
  color: var(--muted);
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
