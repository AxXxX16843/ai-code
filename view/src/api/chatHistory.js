import request from '@/request'

/**
 * 游标分页查询对话历史（本人/管理员）
 * @param appId 应用 id
 * @param lastTime 游标：查询 createTime 早于该时间的记录
 * @param pageSize 每页数量（默认 10）
 */
export async function getChatHistoryPage(appId, lastTime, pageSize = 10) {
  return request(`/chatHistory/getPage/${appId}`, {
    method: 'GET',
    params: {
      lastTime: lastTime || undefined,
      pageSize,
    },
  })
}

/**
 * 管理员分页查询全部对话历史
 */
export async function listChatHistoryByPage(body) {
  return request('/chatHistory/admin/getPage', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/**
 * 删除对话历史
 */
export async function removeChatHistory(id) {
  return request(`/chatHistory/remove/${id}`, {
    method: 'DELETE',
  })
}

/**
 * 保存对话历史
 */
export async function saveChatHistory(body) {
  return request('/chatHistory/save', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}
