import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLogin } from '@/api/userController'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref({
    userName: '未登录',
  })

  // 获取登录用户信息
  async function fetchLoginUser() {
    try {
      const res = await getLogin()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch {
      // 未登录或获取失败时保持默认值
    }
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
