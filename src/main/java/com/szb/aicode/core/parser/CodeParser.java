package com.szb.aicode.core.parser;

import com.szb.aicode.model.enums.GeneratorTypeEnum;

public interface CodeParser<T> {

     T parserCode(String result);

}
