package com.szb.aicode.core;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.constant.AppConstant;
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

        aiCodeGeneratorFaced.generatorAndSave("帮我生成一个留言板界面，二十行代码以内", GeneratorTypeEnum.MULTI_FILE,1L);

    }

    @Test
    void generatorAndSaveStream() {

        List<String> list = aiCodeGeneratorFaced.generatorAndSaveFluxStream("帮我生成一个个人简介页面，三十行代码以内", GeneratorTypeEnum.HTML,1L).collectList().block();
        String join = String.join("", list);
    }
    @Test
    void generatorAndSaveHtml() {
        System.out.println(AppConstant.CODE_OUTPUT_ROOT_DIR);
        System.out.println(AppConstant.CODE_DEPLOY_ROOT_DIR);
    }

}