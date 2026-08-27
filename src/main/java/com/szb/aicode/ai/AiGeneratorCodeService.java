package com.szb.aicode.ai;


import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
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

   @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
   TokenStream generatorVueProjectCodeStream(@MemoryId Long appId, @UserMessage String userMessage);

   @SystemMessage(fromResource = "prompt/codegen-name-system-prompt.txt")
    String generatorName(@UserMessage String userMessage);


}
