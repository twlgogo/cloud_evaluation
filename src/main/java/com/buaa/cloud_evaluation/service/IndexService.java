package com.buaa.cloud_evaluation.service;

import com.buaa.cloud_evaluation.model.IndexModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface IndexService {
  IndexModel findById(int id);
  int insertByNameAndParent(String indexName, int parent);
  List<IndexModel> findAll();
  void deleteById(int id);
}
