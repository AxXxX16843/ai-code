package com.szb.aicode.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
import com.szb.aicode.model.dto.chathistory.ChatHistoryQueryRequest;
import com.szb.aicode.model.entity.App;
import com.szb.aicode.model.entity.ChatHistory;
import com.szb.aicode.mapper.ChatHistoryMapper;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.szb.aicode.model.enums.UserRoleEnum;
import com.szb.aicode.service.AppService;
import com.szb.aicode.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author 86186
 * @since 2026-08-25
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatHistory(User loginUser, String message, String messageType, Long appId) {

        ThrowUtils.throwIf(loginUser==null, ErrorCode.PARAMS_ERROR,"用户未登录");
        ThrowUtils.throwIf(message==null, ErrorCode.PARAMS_ERROR,"消息为空");
        ThrowUtils.throwIf(appId==null||appId<0, ErrorCode.PARAMS_ERROR,"应用不存在");

        ChatHistoryMessageTypeEnum enumByValue = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        if(enumByValue==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"错误的消息类型");
        }
        ChatHistory build = ChatHistory.builder().appId(appId)
                .userId(loginUser.getId())
                .message(message)
                .messageType(messageType)
                .build();
        return  save(build);
     }

    @Override
    public boolean deleteChatHistory( Long appId) {
        ThrowUtils.throwIf(appId==null||appId<0,ErrorCode.PARAMS_ERROR,"应用不存在");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("appId", appId);

        return remove(queryWrapper);

    }

    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> getChatHistory(User loginUser, Long appId, LocalDateTime lastTime, int pageSize) {


        ThrowUtils.throwIf(loginUser==null, ErrorCode.PARAMS_ERROR,"用户未登录");

        ThrowUtils.throwIf(appId==null||appId<0, ErrorCode.PARAMS_ERROR,"应用不存在");

        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        App app = appService.getById(appId);

        if(app==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用不存在");
        }
        boolean isSelf = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue());
        if(!isAdmin&&!isSelf){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
        chatHistoryQueryRequest.setAppId(appId);
        chatHistoryQueryRequest.setUserId(loginUser.getId());
        chatHistoryQueryRequest.setLastCreateTime(lastTime);
        QueryWrapper queryWrapper = getQueryWrapper(chatHistoryQueryRequest);

        return page(Page.of(1,pageSize), queryWrapper);
    }
    @Override
    public int loadMemory(Long appId, ChatMemory chatMemory, int maxSize){

        try {
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq("appId", appId)
                    .orderBy("createTime", false)
                    .limit(1, maxSize);
            List<ChatHistory> historyList= list(queryWrapper);
            historyList = historyList.reversed();
            int loadSize=0;
            for (ChatHistory history : historyList) {
                if(history.getMessageType().equals(ChatHistoryMessageTypeEnum.AI.getValue())){
                    AiMessage aiMessage = AiMessage.from(history.getMessage());
                    chatMemory.add(aiMessage);
                    loadSize++;
                }else if(history.getMessageType().equals(ChatHistoryMessageTypeEnum.USER.getValue())){
                    UserMessage userMessage = UserMessage.from(history.getMessage());
                    chatMemory.add(userMessage);
                    loadSize++;
                }
            }
            return loadSize;
        } catch (Exception e) {
            log.error("对话记忆加载失败");
            return 0;
        }
    }

}





















