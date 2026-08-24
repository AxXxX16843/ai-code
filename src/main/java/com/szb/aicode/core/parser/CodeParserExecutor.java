package com.szb.aicode.core.parser;

import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

public class CodeParserExecutor {

    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    private static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();


    public static Object codeParser(String result, GeneratorTypeEnum generatorTypeEnum) {

        return switch (generatorTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parserCode(result);

            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parserCode(result);

            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,"解析类型错误");

        };
    }


}
