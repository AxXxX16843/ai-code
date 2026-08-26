package com.szb.aicode.core;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.constant.AppConstant;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AICodeGeneratorFacedTest {

    @Resource
    private AICodeGeneratorFaced aiCodeGeneratorFaced;

    @Test
    void generatorAndSave() {

        Flux<String> codeStream = aiCodeGeneratorFaced.generatorAndSaveFluxStream(
                "简单的任务记录网站，总代码量不超过 200 行",
                GeneratorTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generatorAndSaveStream() {
        aiCodeGeneratorFaced.generatorAndSave("帮我生成一个个人简介页面，十行代码以内", GeneratorTypeEnum.HTML,1L);
        aiCodeGeneratorFaced.generatorAndSave("我刚才让你干什么", GeneratorTypeEnum.HTML,1L);
        aiCodeGeneratorFaced.generatorAndSave("帮我生成一个留言板，十行代码以内", GeneratorTypeEnum.HTML,2L);
        aiCodeGeneratorFaced.generatorAndSave("我刚才让你干什么", GeneratorTypeEnum.HTML,2L);
    }
    @Test
    void generatorAndSaveHtml() {
        System.out.println(AppConstant.CODE_OUTPUT_ROOT_DIR);
        System.out.println(AppConstant.CODE_DEPLOY_ROOT_DIR);
    }

}