<template>
  <a-modal
    :open="open"
    :title="app.appName || '应用详情'"
    :footer="null"
    @cancel="emit('close')"
  >
    <div class="app-detail">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="创建者">
          <a-space>
            <a-avatar :src="app.userVo?.userAvatar" :size="24" />
            {{ app.userVo?.userName || '无名' }}
          </a-space>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ dayjs(app.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </a-descriptions-item>
        <a-descriptions-item label="生成类型">
          {{ codeGenTypeLabel }}
        </a-descriptions-item>
      </a-descriptions>
      <div v-if="canOperate" class="app-detail-actions">
        <a-button @click="emit('edit', app)">修改</a-button>
        <a-button danger @click="emit('delete', app)">删除</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import { CODE_GEN_TYPE_CONFIG } from '@/constants/codeGenType'

const props = defineProps({
  app: {
    type: Object,
    required: true,
  },
  open: {
    type: Boolean,
    default: false,
  },
  currentUser: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['close', 'edit', 'delete'])

const codeGenTypeLabel = computed(
  () => CODE_GEN_TYPE_CONFIG[props.app.codeGenType]?.label || props.app.codeGenType || '-',
)

// 仅本人或管理员可操作
const canOperate = computed(() => {
  const user = props.currentUser
  if (!user || !user.id) {
    return false
  }
  return String(user.id) === String(props.app.userId) || user.userRole === 'admin'
})
</script>

<style scoped>
.app-detail-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}
</style>
