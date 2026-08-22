package com.szb.aicode.controller;


import com.szb.aicode.common.BaseResponse;
import com.szb.aicode.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @GetMapping("hello")
    public BaseResponse<String> hello() {
        return ResultUtils.success("hello");
    }


}
