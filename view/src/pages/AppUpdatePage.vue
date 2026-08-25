<template>
  <div class="app-update-page">
    <a-card title="修改应用信息">
      <a-form :model="formState" layout="vertical" @finish="handleSubmit">
        <a-form-item
          label="应用名称"
          name="appName"
          :rules="[{ required: true, message: '请输入应用名称' }]"
        >
          <a-input v-model:value="formState.appName" placeholder="请输入应用名称" />
        </a-form-item>

        <template v-if="isAdmin">
          <a-form-item label="应用封面" name="cover">
            <a-input v-model:value="formState.cover" placeholder="请输入应用封面 URL" />
          </a-form-item>
          <a-form-item label="优先级" name="priority">
            <a-input-number v-model:value="formState.priority" :min="0" style="width: 100%" />
          </a-form-item>
        </template>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting">保存</a-button>
          <a-button style="margin-left: 8px" @click="goBack">返回</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.appId
const app = ref({})
const submitting = ref(false)

const isAdmin = ref(false)

const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const fetchAppInfo = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
    formState.appName = res.data.data.appName || ''
    formState.cover = res.data.data.cover || ''
    formState.priority = res.data.data.priority || 0
  } else {
    message.error('获取应用信息失败，' + res.data.message)
  }
}

const handleSubmit = async (values) => {
  submitting.value = true
  let res
  if (isAdmin.value) {
    res = await updateAppByAdmin({
      id: appId,
      appName: values.appName,
      cover: values.cover,
      priority: values.priority,
    })
  } else {
    res = await updateApp({
      id: appId,
      appName: values.appName,
    })
  }
  submitting.value = false
  if (res.data.code === 0) {
    message.success('修改成功')
    router.back()
  } else {
    message.error('修改失败，' + res.data.message)
  }
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  isAdmin.value = loginUserStore.loginUser.userRole === 'admin'
  await fetchAppInfo()
})
</script>

<style scoped>
.app-update-page {
  max-width: 480px;
  margin: 24px auto;
}
</style>
