package com.szb.aicode.langgraph4j.ai;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ImageCollectionServiceTest {



        @Resource
        private ImageCollectionService imageCollectionService;


        @Test
        void testTechWebsiteImageCollection() {
            String result = imageCollectionService.collectImages("创建一个技术博客网站，需要展示编程教程和系统架构");
            Assertions.assertNotNull(result);
            System.out.println("技术网站收集到的图片: " + result);
        }


}