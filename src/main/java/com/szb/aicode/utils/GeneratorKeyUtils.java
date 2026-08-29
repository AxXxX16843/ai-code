package com.szb.aicode.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;


public class GeneratorKeyUtils {

    public static String generatorKey(Object obj) {

        if(obj==null){
            return DigestUtil.md5Hex("null");
        }
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);

    }

}
