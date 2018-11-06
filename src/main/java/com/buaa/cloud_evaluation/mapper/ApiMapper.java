package com.buaa.cloud_evaluation.mapper;

import com.buaa.cloud_evaluation.model.NodeModel;
import org.apache.ibatis.annotations.Param;

public interface ApiMapper {

  NodeModel selectNode(
      @Param("id") int id
  );

  void insertNode(
      @Param("node") NodeModel node
  );
}
