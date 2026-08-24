package com.szb.aicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.io.File;

public class MultiFileSaveTemplate extends CodeFileSaveTemplate<GeneratorMultiFileResp>{


    @Override
    protected void saveFile(GeneratorMultiFileResp result, String path) {
        writeToFiles(path,"index.html",result.getHtmlCode());
        writeToFiles(path,"style.css",result.getCSSCode());
        writeToFiles(path,"script.js",result.getJavascriptCode());

    }

    @Override
    protected GeneratorTypeEnum getCodeType() {
        return GeneratorTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validateInput(GeneratorMultiFileResp result) {
        super.validateInput(result);
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"html代码不能为空");
        }

    }
}
