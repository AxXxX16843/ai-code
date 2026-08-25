package com.szb.aicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaveTemplate<T> {

    private static final String SAVE_ROOT_PATH_DIR= AppConstant.CODE_OUTPUT_ROOT_DIR;

    public final File fileSave(T result,Long appId) {

        validateInput(result);

        String path = getUniqueDir(appId);

        saveFile(result,path);

        return new File(path);

    }

    protected abstract void saveFile(T result, String path);

    protected void validateInput(T result) {

        if(null == result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"保存代码为空");
        }

    }

    private String getUniqueDir(Long appId) {

        String dizType=getCodeType().getValue();

        String string = SAVE_ROOT_PATH_DIR + "/" + dizType + "_" + appId;

        FileUtil.mkdir(string);

        return string;

    }
    protected abstract GeneratorTypeEnum getCodeType();

    protected final  void writeToFiles(String dir,String fileName,String content){

//        String path= dir+File.separator+fileName;

        File targetFile = new File(dir, fileName);

        FileUtil.writeString(content, targetFile, StandardCharsets.UTF_8);
    }

}
