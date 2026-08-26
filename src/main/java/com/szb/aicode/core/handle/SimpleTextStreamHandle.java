package com.szb.aicode.core.handle;

import cn.hutool.core.util.StrUtil;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.szb.aicode.service.ChatHistoryService;
import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

public class SimpleTextStreamHandle {


    public Flux<String> handle(Flux<String> stream, ChatHistoryService chatHistoryService,User loginUser,Long appId) {

        StringBuilder stringBuilder = new StringBuilder();
        return stream.map(chunk->{
                    stringBuilder.append(chunk);
                    return chunk;
                })
                .doOnComplete(()->{
                    String aiMessage= stringBuilder.toString();
                    if (StrUtil.isNotBlank(aiMessage)) {
                        chatHistoryService.addChatHistory(loginUser,aiMessage, ChatHistoryMessageTypeEnum.AI.getValue(),appId);
                    }
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.addChatHistory(loginUser, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), appId);
                });
    }


}
