package com.buaa.cloud_evaluation.mapper;

import com.buaa.cloud_evaluation.model.IndexModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;


public interface IndexMapper {

  IndexModel findIndexById(@Param("indexId") int indexId);

  int insertByNameAndParent(@Param("indexName") String indexName, @Param("indexParent") int indexParent);

  //int deleteById(@Param("indexId") int indexId);
}
