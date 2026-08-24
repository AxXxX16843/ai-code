package com.szb.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("生成多文件代码")
public class GeneratorMultiFileResp {
        @Description("CSS代码")
        private String CSSCode;

        @Description("Html代码")
        private String HtmlCode;

        @Description("javascript代码")
        private String javascriptCode;


        @Description("代码描述")
        private String description;


}
