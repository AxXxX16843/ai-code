package com.szb.aicode.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaveTemplate<T> {

    private static final String SAVE_ROOT_PATH_DIR=System.getProperty("user.dir")+"/tmp/code_output";

    public final File fileSave(T result) {

        validateInput(result);

        String path = getUniqueDir();

        saveFile(result,path);

        return new File(path);

    }

    protected abstract void saveFile(T result, String path);

    protected void validateInput(T result) {

        if(null == result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"保存代码为空");
        }

    }

    private String getUniqueDir(){

        String dizType=getCodeType().getValue();

        String string = SAVE_ROOT_PATH_DIR + "/" + dizType + "_" + IdUtil.getSnowflakeNextIdStr();

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
