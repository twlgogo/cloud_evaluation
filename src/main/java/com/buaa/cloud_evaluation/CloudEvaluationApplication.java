package com.buaa.cloud_evaluation;

import com.buaa.cloud_evaluation.tool.DatabaseGenerator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.buaa.cloud_evaluation.mapper")
public class CloudEvaluationApplication {

  public static void main(String[] args) {
    String dbPath = "src/main/resources/db/cloud_evaluate.db";
    String itemDbPath = "src/main/resources/db/cloud_evaluate_item.db";
    DatabaseGenerator.generate(dbPath);
    DatabaseGenerator.generateItemDataDB(itemDbPath);

    SpringApplication.run(CloudEvaluationApplication.class, args);
  }
}
