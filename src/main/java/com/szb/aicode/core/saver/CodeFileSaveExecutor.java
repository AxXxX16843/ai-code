package com.szb.aicode.core.saver;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.io.File;

public class CodeFileSaveExecutor {

    private final static HtmlFileSaveTemplate HTML_FILE_SAVE_TEMPLATE = new HtmlFileSaveTemplate();
    private final static MultiFileSaveTemplate MULTI_FILE_SAVE_TEMPLATE = new MultiFileSaveTemplate();

    public static File codeSave(Object object, GeneratorTypeEnum generatorTypeEnum,Long appId) {

        return switch (generatorTypeEnum) {
            case MULTI_FILE -> MULTI_FILE_SAVE_TEMPLATE.fileSave((GeneratorMultiFileResp) object,appId);

            case HTML -> HTML_FILE_SAVE_TEMPLATE.fileSave((GeneratorHtmlResp) object,appId);

            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"不支持生成的代码类型");
        };
    }


}
