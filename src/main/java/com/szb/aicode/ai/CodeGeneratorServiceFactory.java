package com.szb.aicode.ai;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.szb.aicode.ai.guardrail.PromptSafetyInputGuardrail;
import com.szb.aicode.ai.guardrail.RetryOutputGuardrail;
import com.szb.aicode.ai.tools.ToolManager;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import com.szb.aicode.service.ChatHistoryService;
import com.szb.aicode.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class CodeGeneratorServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ChatHistoryService chatHistoryService;


    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ToolManager toolManager;


    private final Cache<String,AiGeneratorCodeService> serviceCache= Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
            log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);})
            .build();

    public AiGeneratorCodeService getAiGeneratorCodeService(Long appId) {

        String cacheKey = getCacheKey(appId, GeneratorTypeEnum.HTML);
        return serviceCache.get(cacheKey,key->getAiService(appId,GeneratorTypeEnum.HTML));
    }


    public AiGeneratorCodeService getAiGeneratorCodeService(Long appId,GeneratorTypeEnum generatorTypeEnum) {

        String cacheKey = getCacheKey(appId, generatorTypeEnum);

        return serviceCache.get(cacheKey,key->getAiService(appId,generatorTypeEnum));

    }

    private AiGeneratorCodeService getAiService(Long appId, GeneratorTypeEnum generatorTypeEnum) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder().id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        chatHistoryService.loadMemory(appId,chatMemory,20);
        return switch (generatorTypeEnum) {
            case MULTI_FILE,HTML-> {
                StreamingChatModel streamingChatModel = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield  AiServices.builder(AiGeneratorCodeService.class).
                        streamingChatModel(streamingChatModel)
                        .chatModel(chatModel)
                        .chatMemory(chatMemory)
                        .maxSequentialToolsInvocations(20)
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .build();
            }
            case VUE_PROJECT -> {
                StreamingChatModel reasoningStreamingChatModel = SpringContextUtil.getBean("reasoningStreamChatModelPrototype", StreamingChatModel.class);
                yield  AiServices.builder(AiGeneratorCodeService.class)
                        .streamingChatModel(reasoningStreamingChatModel)
                        .chatModel(chatModel)
                        .maxSequentialToolsInvocations(20)
                        .tools(toolManager.getAllTool())
                        .inputGuardrails(new PromptSafetyInputGuardrail())
                        .outputGuardrails(new RetryOutputGuardrail())
                        .chatMemoryProvider(memoryId -> chatMemory)
                        .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                                toolExecutionRequest, "Error: there is no tool called " + toolExecutionRequest.name()
                        )).build();
            }
        };

    }

    @Bean
    public AiGeneratorCodeService aiGeneratorCodeService() {
        return getAiService(0L,GeneratorTypeEnum.MULTI_FILE);
    }

    private String getCacheKey(Long appId, GeneratorTypeEnum typeEnum) {
        return  typeEnum.getValue()+"_"+appId;
    }

}
