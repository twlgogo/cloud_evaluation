package com.buaa.cloud_evaluation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Application {

    @RequestMapping("/")
    public static String index(){
        return "Hello world, by Spring boot";
    }
}
