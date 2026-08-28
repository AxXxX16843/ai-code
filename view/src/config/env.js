/**
 * 环境变量配置
 */
// 应用部署域名
export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

// 静态资源地址
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

// 获取部署应用的完整 URL
export const getDeployUrl = (deployKey) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

// 获取静态资源预览 URL
export const getStaticPreviewUrl = (codeGenType, appId) => {
  return `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
}
