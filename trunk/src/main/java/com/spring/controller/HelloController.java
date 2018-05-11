package com.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // 声明是一个控制器
public class HelloController{

    // 配置URL和方法之间的映射
    @RequestMapping("/index")
    public String hello(){
        return "index";
    }
}