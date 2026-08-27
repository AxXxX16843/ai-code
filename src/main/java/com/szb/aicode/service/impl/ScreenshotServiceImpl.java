package com.szb.aicode.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import com.szb.aicode.exception.ThrowUtils;
import com.szb.aicode.manager.CosManager;
import com.szb.aicode.service.ScreenshotService;
import com.szb.aicode.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;


    @Override
    public String generateAndUploadScreenshot(String webUrl) {

        if (webUrl == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"网址不能为空");
        }
        log.info("开始生成截图");
        String resourcePath = WebScreenshotUtils.saveWebScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(resourcePath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");

        try {
            String cosUrl = uploadScreenshotToCos(resourcePath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.OPERATION_ERROR, "截图上传对象存储失败");
            log.info("网页截图生成并上传成功: {} -> {}", webUrl, cosUrl);
            return cosUrl;
        }  finally {
            cleanupLocalFile(resourcePath);
        }

    }


    private String uploadScreenshotToCos(String localScreenshotPath){

        if(StrUtil.isBlank(localScreenshotPath)){
            return null;
        }
        File file = new File(localScreenshotPath);
        if(!file.exists()){
            log.error("该截图不存在：{}", localScreenshotPath);
            return null;
        }
        String fileName = RandomUtil.randomNumbers(5)+"_compress.jpg";
        String key=generatorUploadKey(fileName);

        return cosManager.uploadFile(key,file);

    }

    private String generatorUploadKey(String fileName) {

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return String.format("/screenshots/%s/%s", datePath, fileName);

    }
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }


}
