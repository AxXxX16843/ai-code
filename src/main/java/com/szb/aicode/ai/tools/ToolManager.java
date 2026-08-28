package com.szb.aicode.ai.tools;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolManager {

    private final Map<String,BaseTool> tools = new HashMap<String,BaseTool>();


    @Resource
    private BaseTool[] baseTools;

    @PostConstruct
    public void initTool() {
        for (BaseTool tool : baseTools) {
            tools.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
        log.info("初始化完成功注册：{}个工具",tools.size());
    }


    public BaseTool getTool(String toolName) {
        return tools.get(toolName);
    }


    public BaseTool[] getAllTool() {
        return baseTools;
    }

}
