package com.szb.aicode.core.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.szb.aicode.ai.model.message.*;
import com.szb.aicode.ai.tools.BaseTool;
import com.szb.aicode.ai.tools.ToolManager;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.core.builder.VueProjectBuilder;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.ChatHistoryMessageTypeEnum;
import com.szb.aicode.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;


@Slf4j
@Component
public class JsonMessageStreamHandle {


    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;


    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    try {
                        chatHistoryService.addChatHistory(loginUser, aiResponse, ChatHistoryMessageTypeEnum.AI.getValue(), appId);
                    } catch (Exception e) {
                        // 持久化失败不应破坏已完成的 SSE 响应，避免前端误判为生成失败
                        log.error("保存 AI 对话历史失败，appId={}", appId, e);
                    }
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    try {
                        chatHistoryService.addChatHistory(loginUser, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), appId);
                    } catch (Exception e) {
                        log.error("保存 AI 错误消息失败，appId={}", appId, e);
                    }

                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        if (typeEnum == null) {
            log.warn("忽略未知的流消息类型: {}", streamMessage.getType());
            return "";
        }
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                // 检查是否是第一次看到这个工具 ID
                if (toolId == null || seenToolIds.add(toolId)) {
                    BaseTool tool = toolManager.getTool(toolName);
                    if (tool == null) {
                        log.warn("收到未注册的工具请求: name={}, id={}", toolName, toolId);
                        return String.format("\n\n[选择工具] %s\n\n", StrUtil.blankToDefault(toolName, "未知工具"));
                    }
                    // 第一次调用这个工具时完整返回工具信息
                    return tool.generateToolRequestResponse();
                } else {
                    // 不是第一次调用这个工具，直接返回空
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String toolName = toolExecutedMessage.getName();
                BaseTool tool = toolManager.getTool(toolName);
                String result;
                if (tool == null) {
                    log.warn("收到未注册工具的执行结果: name={}, id={}", toolName, toolExecutedMessage.getId());
                    result = StrUtil.blankToDefault(toolExecutedMessage.getResult(), "未知工具执行失败");
                } else {
                    result = tool.generateToolExecutedResult(jsonObject);
                }
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);

//                String relativeFilePath = jsonObject.getStr("relativeFilePath");
//                String suffix = FileUtil.getSuffix(relativeFilePath);
//                String content = jsonObject.getStr("content");
//                String result = String.format("""
//                        🔧工具调用 写入文件 %s
//                        ```%s
//                        %s
//                        ```
//                        """, relativeFilePath, suffix, content);
//                // 输出前端和要持久化的内容
//                String output = String.format("\n\n%s\n\n", result);
//                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }

}
