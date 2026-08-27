package com.szb.aicode.utils;


import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import com.szb.aicode.exception.BusinessException;
import com.szb.aicode.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

@Slf4j
public class WebScreenshotUtils {

    private static final WebDriver webDriver;

    static {
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        System.setProperty("wdm.timeout", "500");
        System.setProperty("wdm.retryCount", "3");
// 设置国内镜像
        System.setProperty("wdm.chromeDownloadUrl", "https://npmmirror.com/mirrors/chromedriver/");


    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Edge 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 edge 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 edge 浏览器失败");
        }
    }


    public static String saveWebScreenshot(String webUrl) {

        try {
            if(webDriver == null) {
                return "";
            }

            String imagePath= System.getProperty("user.dir") + "/tmp/Screenshot/"+ UUID.randomUUID().toString().substring(0,8);
            FileUtil.mkdir(imagePath);
            String imageName = UUID.randomUUID().toString().replace("-","");
            final String IMAGE_SUFFIX = ".png";
            String savePath = imagePath + File.separator + imageName + IMAGE_SUFFIX;
            webDriver.get(webUrl);

            waitForPageLoad(webDriver);

            byte[] screenshotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            saveImage(screenshotBytes, savePath);

            log.info("原始截图保存成功: {}", savePath);
            final String COMPRESSION_SUFFIX = "_compress.jpg";
            String compressedImagePath = imagePath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(savePath, compressedImagePath);
            FileUtil.del(savePath);

            log.info("压缩图片保存成功: {}", compressedImagePath);

            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return null;
        }
    }



    private static void saveImage(byte[] imageBytes, String savePath) {

        try {
            FileUtil.writeBytes(imageBytes, savePath);
        }catch (Exception e){
            log.error("截图保存失败：{}",savePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片保存失败");
        }

    }

    private static void compressImage(String savePath,String compressPath) {

        try {
            final float COMPRESS_QUALITY = 0.3f;
            ImgUtil.compress(FileUtil.file(savePath),FileUtil.file(compressPath),COMPRESS_QUALITY);
        } catch (IORuntimeException e) {
            log.error("图片压缩失败：{}-{}",savePath,compressPath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图片压缩失败");
        }

    }

    private static void waitForPageLoad(WebDriver driver) {

        try {
            WebDriverWait webDriverWait = new WebDriverWait(driver,Duration.ofSeconds(20));

            webDriverWait.until(webDriver->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                        .equals("complete")
            );
            Thread.sleep(2000);
            log.info("页面加载成功");
        } catch (InterruptedException e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }


    }

}
