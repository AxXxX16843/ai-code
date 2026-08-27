package com.szb.aicode.config;


import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("langchain4j.open-ai.chat-model")
@Configuration
@Data
public class ReasoningChatModelConfig {


    private String baseUrl;

    private String apiKey;

    @Bean
    public StreamingChatModel ReasoningStreamingChatModel() {
//
//        final String modelName="deepseek-reasoner";
//
//        final int maxToken=32768;

        final String modelName="deepseek-chat";

        final int maxToken=8192;

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxToken)
                .logRequests(true)
                .logResponses(true)
                .build();

    }

}
