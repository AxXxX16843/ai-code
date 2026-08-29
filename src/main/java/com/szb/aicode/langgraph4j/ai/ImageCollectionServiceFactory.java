package com.szb.aicode.langgraph4j.ai;


import com.szb.aicode.langgraph4j.tools.ImageSearchTool;
import com.szb.aicode.langgraph4j.tools.LogoGeneratorTool;
import com.szb.aicode.langgraph4j.tools.MermaidDiagramTool;
import com.szb.aicode.langgraph4j.tools.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageCollectionServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Bean
    public ImageCollectionService imageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .tools(imageSearchTool,
                        logoGeneratorTool,
                        mermaidDiagramTool,
                        undrawIllustrationTool)
                .chatModel(chatModel)
                .build();
    }

}
