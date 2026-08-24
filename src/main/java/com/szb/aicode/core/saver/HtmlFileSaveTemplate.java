package com.szb.aicode.core.saver;

import cn.hutool.core.util.StrUtil;
import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

public class HtmlFileSaveTemplate extends CodeFileSaveTemplate<GeneratorHtmlResp>{

    @Override
    protected void saveFile(GeneratorHtmlResp result, String path) {
        writeToFiles(path,"index.html", result.getHtmlCode());
    }

    @Override
    protected GeneratorTypeEnum getCodeType() {
        return GeneratorTypeEnum.HTML;
    }

    @Override
    protected void validateInput(GeneratorHtmlResp result) {
        super.validateInput(result);
        if(StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"html代码不能为空");
        }
    }
}
