package com.szb.aicode.core.parser;

import com.szb.aicode.ai.model.GeneratorMultiFileResp;
import com.szb.aicode.model.enums.GeneratorTypeEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiFileCodeParser implements CodeParser<GeneratorMultiFileResp>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


    @Override
    public GeneratorMultiFileResp parserCode(String re) {
        GeneratorMultiFileResp result = new GeneratorMultiFileResp();
        // 提取各类代码
        String htmlCode = extractCodeByPattern(re, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(re, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(re, JS_CODE_PATTERN);

        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCSSCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJavascriptCode(jsCode.trim());
        }
        return result;
    }
    private static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


}
