package com.szb.aicode.langgraph4j.ai;

import com.szb.aicode.langgraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;

public interface QualityCheckService {

    @SystemMessage("prompt/code-quality-check-system-prompt.txt")
    QualityResult checkCodeQuality(String message);

}
