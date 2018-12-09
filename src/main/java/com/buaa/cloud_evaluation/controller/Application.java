package com.buaa.cloud_evaluation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
public class Application {

    @RequestMapping("/")
    public static String index(HttpServletResponse response){
        response.setStatus(302);
        response.addHeader("Location", "/login.html");
        return "Hello world, by Spring boot";
    }
}
