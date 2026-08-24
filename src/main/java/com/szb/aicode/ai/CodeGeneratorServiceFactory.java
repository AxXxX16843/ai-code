package com.szb.aicode.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeGeneratorServiceFactory {
    
    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;
    
    @Bean
    public AiGeneratorCodeService getAiGeneratorCodeService() {
        return AiServices.builder(AiGeneratorCodeService.class).
                streamingChatModel(streamingChatModel)
                .chatModel(chatModel)
                .build();
    }
    
    
}
