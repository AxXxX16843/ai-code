package com.szb.aicode.ai;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiGenRoutingServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Bean
    public AiGenRoutingService getAiGenRoutingService() {
        return AiServices.builder(AiGenRoutingService.class).
            chatModel(chatModel).
                build();
    }


}
