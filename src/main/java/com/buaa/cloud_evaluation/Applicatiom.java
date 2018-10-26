package com.buaa.cloud_evaluation;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Applicatiom {

    @RequestMapping("/")
    public static String index(){
        return "Hello world, by Spring boot";
    }
}
