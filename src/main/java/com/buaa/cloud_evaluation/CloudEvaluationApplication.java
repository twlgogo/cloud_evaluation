package com.buaa.cloud_evaluation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.buaa.cloud_evaluation.mapper")
public class CloudEvaluationApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudEvaluationApplication.class, args);
    }
}
