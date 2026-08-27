package com.szb.aicode.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

class WebScreenshotUtilsTest {

    @Test
    void saveWebScreenshot() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenshotUtils.saveWebScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);

    }
}