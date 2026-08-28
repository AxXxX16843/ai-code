package com.szb.aicode.ai.tools;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
            // LangChain4j 默认使用 @Tool 方法名发起调用，同时保留现有业务名以兼容已有代码。
            for (Method method : tool.getClass().getMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    tools.put(method.getName(), tool);
                }
            }
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
        log.info("初始化完成，共注册 {} 个工具名称及别名", tools.size());
    }


    public BaseTool getTool(String toolName) {
        return tools.get(toolName);
    }


    public BaseTool[] getAllTool() {
        return baseTools;
    }

}
