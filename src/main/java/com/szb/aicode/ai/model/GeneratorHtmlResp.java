package com.szb.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;


@Data
@Description("返回HTML的生成结果")
public class GeneratorHtmlResp {

    @Description("Html代码")
    private String HtmlCode;


    @Description("代码描述")
    private String description;
}
