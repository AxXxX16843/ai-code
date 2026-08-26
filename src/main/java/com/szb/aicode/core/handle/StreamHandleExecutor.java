package com.szb.aicode.core.handle;

import com.szb.aicode.ai.model.message.StreamMessageTypeEnum;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.entity.User;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import com.szb.aicode.service.ChatHistoryService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
public class StreamHandleExecutor {

    private static final SimpleTextStreamHandle STREAM_HANDLE = new SimpleTextStreamHandle();
    private static final JsonMessageStreamHandle JSON_HANDLE = new JsonMessageStreamHandle();


    public Flux<String> handle(User loginUser, Flux<String> stream,Long appId,
                               ChatHistoryService chatHistoryService,
                               GeneratorTypeEnum generatorTypeEnum) {
        return switch (generatorTypeEnum) {

            case HTML,MULTI_FILE ->STREAM_HANDLE.handle(stream,chatHistoryService,loginUser,appId);

            case VUE_PROJECT -> JSON_HANDLE.handle(stream,chatHistoryService,appId,loginUser);

            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR,"未知的生成类型");
        };
    }

}
