<template>
  <div id="userRegisterPage">
    <h2 class="title">AI Code - 用户注册</h2>
    <div class="desc">不写一行代码，生成完整应用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item
        name="username"
        :rules="[
          { required: true, message: '请输入账号' },
          { min: 4, message: '账号不能小于 4 位' },
        ]"
      >
        <a-input v-model:value="formState.username" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="password"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 6, message: '密码不能小于 6 位' },
        ]"
      >
        <a-input-password v-model:value="formState.password" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="check"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 6, message: '密码不能小于 6 位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.check" placeholder="请确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController'

const router = useRouter()

const formState = reactive({
  username: '',
  password: '',
  check: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 */
const validateCheckPassword = (_rule, value) => {
  if (value && value !== formState.password) {
    return Promise.reject(new Error('两次输入密码不一致'))
  }
  return Promise.resolve()
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values) => {
  const res = await register(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
