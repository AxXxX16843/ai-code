package com.szb.aicode.model.enums;


import lombok.Getter;

@Getter
public enum GeneratorTypeEnum {

    HTML("原生Html形式","html"),
    MULTI_FILE("多文件模式","multi_file"),
    VUE_PROJECT("vue项目生成模式","vue_project"),;

    private final String text;

    private final String value;

    GeneratorTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static GeneratorTypeEnum getByValue(String value) {
        if(value == null || value.isEmpty()){
            return null;
        }

        for (GeneratorTypeEnum e : GeneratorTypeEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }

}
