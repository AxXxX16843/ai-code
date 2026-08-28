package com.szb.aicode.langgraph4j.node;

import com.szb.aicode.ai.AiGenRoutingService;
import com.szb.aicode.langgraph4j.state.WorkflowContext;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import com.szb.aicode.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 智能路由");

            GeneratorTypeEnum generationType;
            try {
                // 获取AI路由服务
                AiGenRoutingService routingService = SpringContextUtil.getBean(AiGenRoutingService.class);
                // 根据原始提示词进行智能路由
                generationType = routingService.routingType(context.getOriginalPrompt());
                log.info("AI智能路由完成，选择类型: {} ({})", generationType.getValue(), generationType.getText());
            } catch (Exception e) {
                log.error("AI智能路由失败，使用默认HTML类型: {}", e.getMessage());
                generationType = GeneratorTypeEnum.HTML;
            }

            // 更新状态
            context.setCurrentStep("智能路由");
            context.setGenerationType(generationType);
            return WorkflowContext.saveContext(context);
        });
    }
}