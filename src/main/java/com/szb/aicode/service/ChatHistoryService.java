package com.szb.aicode.service;

import cn.hutool.log.Log;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.szb.aicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.szb.aicode.model.entity.ChatHistory;
import com.szb.aicode.model.entity.User;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author 86186
 * @since 2026-08-25
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    boolean addChatHistory(User loginUser, String message, String messageType, Long appId);

    boolean deleteChatHistory(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> getChatHistory(User loginUser, Long appId, LocalDateTime lastTime,int pageSize);

    int loadMemory(Long appId, ChatMemory chatMemory, int maxSize);
}
