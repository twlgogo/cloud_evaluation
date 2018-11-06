package com.buaa.cloud_evaluation.controller;

import com.buaa.cloud_evaluation.mapper.IndexMapper;
import com.buaa.cloud_evaluation.model.IndexModel;
import com.buaa.cloud_evaluation.service.IndexService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActionController {

  @Autowired
  private IndexService indexService;
//  @Autowired
//  private IndexMapper indexMapper;

  @RequestMapping("/update")
  public String updateIndex(@Param("indexName")String indexName,
                                @Param("indexParent")int indexParent){
    IndexModel indexModel;
    int indexId =  indexService.insertByNameAndParent(indexName,indexParent);
    indexModel = indexService.findById(indexId);

    return indexModel.toString();
  }
}
