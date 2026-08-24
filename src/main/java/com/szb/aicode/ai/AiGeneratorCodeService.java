package com.szb.aicode.ai;


import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiGeneratorCodeService {

    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    GeneratorHtmlResp generatorHtmlCode(String userMessage);


    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    GeneratorMultiFileResp generatorMultiFileCode(String userMessage);

    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generatorHtmlCodeStream(String userMessage);


    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generatorMultiFileCodeStream(String userMessage);


}
