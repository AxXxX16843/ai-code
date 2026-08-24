package com.szb.aicode.core;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.model.enums.GeneratorTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AICodeGeneratorFacedTest {

    @Resource
    private AICodeGeneratorFaced aiCodeGeneratorFaced;

    @Test
    void generatorAndSave() {

        aiCodeGeneratorFaced.generatorAndSave("帮我生成一个留言板界面，二十行代码以内", GeneratorTypeEnum.MULTI_FILE);

    }

    @Test
    void generatorAndSaveStream() {

        List<String> list = aiCodeGeneratorFaced.generatorAndSaveFluxStream("帮我生成一个个人简介页面，三十行代码以内", GeneratorTypeEnum.HTML).collectList().block();
        String join = String.join("", list);
    }
}