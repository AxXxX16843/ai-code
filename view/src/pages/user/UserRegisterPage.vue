<template>
  <div id="userRegisterPage">
    <div class="auth-card">
      <div class="auth-mark"><AppLogo :size="56" variant="brand" /></div>
      <h2 class="title">创建你的工作台</h2>
      <div class="desc">从一句话开始构建产品</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item
          name="username"
          :rules="[
            { required: true, message: '请输入账号' },
            { min: 4, message: '账号不能小于 4 位' },
          ]"
        >
          <a-input v-model:value="formState.username" placeholder="请输入账号" size="large" />
        </a-form-item>
        <a-form-item
          name="password"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 6, message: '密码不能小于 6 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.password"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>
        <a-form-item
          name="check"
          :rules="[
            { required: true, message: '请确认密码' },
            { min: 6, message: '密码不能小于 6 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password
            v-model:value="formState.check"
            placeholder="请确认密码"
            size="large"
          />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" size="large" block>注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController'
import AppLogo from '@/components/AppLogo.vue'

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
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #eef3f1;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border: 1px solid #e2e9e7;
  border-radius: 14px;
  padding: 36px;
  box-shadow: 0 18px 42px rgba(16, 35, 45, .1);
}
.auth-mark { display:flex; justify-content:center; margin-bottom:16px; }

.title {
  text-align: center;
  font-size: 25px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #10232d;
}

.desc {
  text-align: center;
  color: #82919a;
  margin-bottom: 24px;
}

.tips {
  margin-bottom: 16px;
  color: #82919a;
  font-size: 13px;
  text-align: right;
}
</style>
