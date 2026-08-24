package com.szb.aicode.ai;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AiGeneratorCodeServiceTest {

    @Autowired
    private AiGeneratorCodeService aiGeneratorCodeService;

    @Test
    void generatorHtmlCode() {

        GeneratorHtmlResp generatorHtmlResp = aiGeneratorCodeService.generatorHtmlCode("生成ax的博客网页，代码不超过20行");


    }

    @Test
    void generatorMultiFileCode() {
    }
}