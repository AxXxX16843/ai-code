package com.szb.aicode.config;


import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamChatModelConfig {

    private String modelName;
    private String baseUrl;
    private String apiKey;
    private int maxTokens;
    private Double temperature;
    private boolean logRequests;
    private boolean logResponses;

    @Bean
    @Scope("prototype")
    public StreamingChatModel reasoningStreamChatModelPrototype() {

        return OpenAiStreamingChatModel.builder().apiKey(apiKey)
                .logResponses(logResponses)
                .logRequests(logRequests)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
    }


}
