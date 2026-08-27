<template>
  <div id="appManagePage">
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <a-table
      :columns="columns"
      :data-source="data"
      :loading="loading"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userVo'">
          <a-space>
            <a-avatar :src="record.userVo?.userAvatar" :size="24" />
            {{ record.userVo?.userName || '无名' }}
          </a-space>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ CODE_GEN_TYPE_CONFIG[record.codeGenType]?.label || record.codeGenType || '-' }}
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button type="link" @click="doEdit(record)">编辑</a-button>
          <a-button type="link" @click="doFeature(record)">精选</a-button>
          <a-button type="link" danger @click="doDelete(record)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { deleteAppByAdmin, listAppVoByPageByAdmin, updateAppByAdmin } from '@/api/appController'
import { CODE_GEN_TYPE_CONFIG } from '@/constants/codeGenType'

const router = useRouter()

const columns = [
  { title: 'id', dataIndex: 'id', width: 120 },
  { title: '应用名称', dataIndex: 'appName' },
  { title: '创建者', dataIndex: 'userVo' },
  { title: '生成类型', dataIndex: 'codeGenType' },
  { title: '优先级', dataIndex: 'priority', width: 80 },
  { title: '创建时间', dataIndex: 'createTime' },
  { title: '操作', key: 'action', width: 180 },
]

const data = ref([])
const total = ref(0)
const loading = ref(false)

const searchParams = reactive({
  pageNum: 1,
  pageSize: 10,
  appName: undefined,
})

const fetchData = async () => {
  loading.value = true
  const res = await listAppVoByPageByAdmin({ ...searchParams })
  loading.value = false
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchData()
})

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
}))

const doTableChange = (page) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

// 编辑：跳转修改页
const doEdit = (record) => {
  router.push({
    path: `/app/update/${record.id}`,
  })
}

// 精选：设置优先级为 99
const doFeature = async (record) => {
  const res = await updateAppByAdmin({
    id: record.id,
    priority: 99,
  })
  if (res.data.code === 0) {
    message.success('已设为精选')
    fetchData()
  } else {
    message.error('操作失败，' + res.data.message)
  }
}

// 删除
const doDelete = async (record) => {
  const res = await deleteAppByAdmin({ id: record.id })
  if (res.data.code === 0) {
    message.success('删除成功')
    fetchData()
  } else {
    message.error('删除失败，' + res.data.message)
  }
}
</script>

<style scoped>
#appManagePage {
  max-width: 1220px;
  margin: 26px auto;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 12px 30px rgba(16, 35, 45, .06);
}
@media (max-width: 768px) { #appManagePage { margin: 12px 0; padding: 16px; overflow-x: auto; } }
</style>
