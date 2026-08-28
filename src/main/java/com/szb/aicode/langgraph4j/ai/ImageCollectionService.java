package com.szb.aicode.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;

public interface ImageCollectionService {

    @SystemMessage("prompt/image-collection-system-prompt.txt")
    String collectImages(String userMessage);


}
