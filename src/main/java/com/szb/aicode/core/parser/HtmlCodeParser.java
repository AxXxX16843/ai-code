package com.szb.aicode.core.parser;

import com.szb.aicode.ai.model.GeneratorHtmlResp;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeParser implements CodeParser<GeneratorHtmlResp> {


    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


    @Override
    public  GeneratorHtmlResp parserCode(String re) {
        GeneratorHtmlResp result = new GeneratorHtmlResp();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(re);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(re.trim());
        }
        return result;
    }
    private static String extractHtmlCode(String content) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
