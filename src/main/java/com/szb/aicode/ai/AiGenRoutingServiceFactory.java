package com.szb.aicode.ai;


import com.szb.aicode.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiGenRoutingServiceFactory {


    public AiGenRoutingService createAiGenRoutingService() {

        ChatModel routingChatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiGenRoutingService.class)
                .chatModel(routingChatModel)
                .build();

    }


    @Bean
    public AiGenRoutingService getAiGenRoutingService() {
        return createAiGenRoutingService();
    }


}
