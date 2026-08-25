package com.szb.aicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.model.entity.ChatHistory;
import com.szb.aicode.mapper.ChatHistoryMapper;
import com.szb.aicode.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author 86186
 * @since 2026-08-25
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
