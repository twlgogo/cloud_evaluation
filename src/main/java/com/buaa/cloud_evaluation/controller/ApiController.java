package com.buaa.cloud_evaluation.controller;

import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.service.ApiService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  private final ApiService apiService;

  @Autowired
  public ApiController(ApiService apiService) {
    this.apiService = apiService;
  }

  @RequestMapping("/add_node")
  public NodeModel addNode(
      @Param("name") String name,
      @Param("type") int type,
      @Param("parent") int parent
  ) {
    return apiService.addNode(name, type, parent);
  }
}
