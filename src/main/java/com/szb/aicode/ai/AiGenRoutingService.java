package com.szb.aicode.ai;

import com.szb.aicode.model.enums.GeneratorTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiGenRoutingService {

    @SystemMessage(fromResource = "prompt/codegen-route-system-prompt.txt")

    GeneratorTypeEnum routingType(String userMessage);



}
