package com.szb.aicode.exception;

import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
@Hidden
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error(e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }
    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e) {

        log.error(e.getMessage(), e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统异常");
    }


}
