package com.szb.aicode.core;


import cn.hutool.json.JSONUtil;
import com.szb.aicode.ai.AiGeneratorCodeService;
import com.szb.aicode.ai.CodeGeneratorServiceFactory;
import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import com.szb.aicode.ai.model.message.AiResponseMessage;
import com.szb.aicode.ai.model.message.ToolExecutedMessage;
import com.szb.aicode.ai.model.message.ToolRequestMessage;
import com.szb.aicode.core.parser.CodeParserExecutor;
import com.szb.aicode.core.saver.CodeFileSaveExecutor;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

@Service
@Slf4j
public class AICodeGeneratorFaced {


    @Resource
    private CodeGeneratorServiceFactory codeGeneratorServiceFactory;

    public File generatorAndSave(String userMessage,GeneratorTypeEnum type,Long appId){

        if(type==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }
        return switch (type){
            case HTML -> {
                AiGeneratorCodeService aiGeneratorCodeService = codeGeneratorServiceFactory.getAiGeneratorCodeService(appId,type);
                GeneratorHtmlResp generatorHtmlResp = aiGeneratorCodeService.generatorHtmlCode(userMessage);
                yield CodeFileSaveExecutor.codeSave(generatorHtmlResp,GeneratorTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                AiGeneratorCodeService aiGeneratorCodeService = codeGeneratorServiceFactory.getAiGeneratorCodeService(appId,type);
                GeneratorMultiFileResp generatorMultiFileResp = aiGeneratorCodeService.generatorMultiFileCode(userMessage);
                yield CodeFileSaveExecutor.codeSave(generatorMultiFileResp,GeneratorTypeEnum.MULTI_FILE,appId);
            }

            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"类型错误"+type.getValue());
        };
    }

    public Flux<String> generatorAndSaveFluxStream(String userMessage,GeneratorTypeEnum type,Long appId){

        if(type==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成类型不能为空");
        }
        return switch (type){
            case HTML -> {
                AiGeneratorCodeService aiGeneratorCodeService = codeGeneratorServiceFactory.getAiGeneratorCodeService(appId,type);
                Flux<String> htmlCodeStream = aiGeneratorCodeService.generatorHtmlCodeStream(userMessage);
                yield getStringFlux(htmlCodeStream,GeneratorTypeEnum.HTML,appId);
            }
            case MULTI_FILE -> {
                AiGeneratorCodeService aiGeneratorCodeService = codeGeneratorServiceFactory.getAiGeneratorCodeService(appId,type);
                Flux<String> multiFileCodeStream = aiGeneratorCodeService.generatorMultiFileCodeStream(userMessage);
                yield getStringFlux(multiFileCodeStream,GeneratorTypeEnum.MULTI_FILE,appId);
            }
            case VUE_PROJECT ->{
                AiGeneratorCodeService aiGeneratorCodeService = codeGeneratorServiceFactory.getAiGeneratorCodeService(appId,type);
                TokenStream tokenStream = aiGeneratorCodeService.generatorVueProjectCodeStream(appId, userMessage);
                yield  processTokenStream(tokenStream);
            }
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"类型错误"+type.getValue());
        };
    }

    private Flux<String> processTokenStream(TokenStream tokenStream) {
        return Flux.create(sink->{
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecution.request());
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });

    }


    private static Flux<String> getStringFlux(Flux<String> htmlCodeStream,GeneratorTypeEnum type,Long appId) {
        StringBuilder htmlString = new StringBuilder();
        return htmlCodeStream.doOnNext(htmlString::append).
                doOnComplete(() -> {
                    try {
                        String result = htmlString.toString();
                        Object o = CodeParserExecutor.codeParser(result, type);
                        CodeFileSaveExecutor.codeSave(o,type,appId);
                        log.info("文件保存成功");
                    } catch (Exception e) {
                        log.error("文件保存失败：{}", e.getMessage());
                    }
                });
    }


}
