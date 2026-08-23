package com.szb.aicode.model.enums;


import lombok.Getter;
import org.springframework.web.util.pattern.PathPattern;

@Getter
public enum UserRoleEnum {

    USER("user","普通用户"),
    ADMIN("admin","管理员");


    private final String text;
    private final String value;


    UserRoleEnum(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static UserRoleEnum getByValue(String value) {
        if(value == null || value.isEmpty()){
            return null;
        }

        for (UserRoleEnum e : UserRoleEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
