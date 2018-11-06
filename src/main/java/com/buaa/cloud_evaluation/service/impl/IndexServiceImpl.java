package com.buaa.cloud_evaluation.service.impl;

import com.buaa.cloud_evaluation.mapper.IndexMapper;
import com.buaa.cloud_evaluation.model.IndexModel;
import com.buaa.cloud_evaluation.service.IndexService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IndexServiceImpl implements IndexService {

  @Resource
  private IndexMapper indexMapper;

  @Override
  public IndexModel findById(int id){
    return indexMapper.findIndexById(id);
  }

  @Override
  public int insertByNameAndParent(String indexName, int parent){
    return indexMapper.insertByNameAndParent(indexName,parent);
  }

  @Override
  public List<IndexModel> findAll(){
    return null;
  }

  @Override
  public void deleteById(int id){

  }
}
