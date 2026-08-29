package com.szb.aicode.exception;

import cn.hutool.json.JSONUtil;
import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
@Hidden
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error(e.getMessage(), e);
        if (handleSseError(e.getCode(), e.getMessage())) {
            return null;
        }
        return ResultUtils.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e) {

        log.error(e.getMessage(), e);
        if (handleSseError(ErrorCode.SYSTEM_ERROR.getCode(), "系统错误")) {
            return null;
        }
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
    }
    private boolean handleSseError(int errorCode, String errorMessage) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return false;
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        // 判断是否是SSE请求（通过Accept头或URL路径）
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();
        if ((accept != null && accept.contains("text/event-stream")) ||
                uri.contains("/chat/gen/code")) {
            try {
                // 设置SSE响应头
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Connection", "keep-alive");
                // 构造错误消息的SSE格式
                Map<String, Object> errorData = Map.of(
                        "error", true,
                        "code", errorCode,
                        "message", errorMessage
                );
                String errorJson = JSONUtil.toJsonStr(errorData);
                // 发送业务错误事件（避免与标准error事件冲突）
                String sseData = "event: business-error\ndata: " + errorJson + "\n\n";
                response.getWriter().write(sseData);
                response.getWriter().flush();
                // 发送结束事件
                response.getWriter().write("event: done\ndata: {}\n\n");
                response.getWriter().flush();
                // 表示已处理SSE请求
                return true;
            } catch (IOException ioException) {
                log.error("Failed to write SSE error response", ioException);
                // 即使写入失败，也表示这是SSE请求
                return true;
            }
        }
        return false;
    }


}
