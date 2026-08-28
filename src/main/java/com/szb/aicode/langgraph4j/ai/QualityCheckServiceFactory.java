package com.szb.aicode.langgraph4j.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;

public class QualityCheckServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public QualityCheckService qualityCheckService() {
        return AiServices.builder(QualityCheckService.class)
                .chatModel(chatModel)
                .build();
    }


}
