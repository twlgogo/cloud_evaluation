package com.buaa.cloud_evaluation.mapper;

import com.buaa.cloud_evaluation.model.NodeModel;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApiMapper {

  NodeModel selectNode(
      @Param("id") int id
  );

  List<NodeModel> selectNodes();

  void insertNode(
      @Param("node") NodeModel node
  );

  void deleteNodes();
}
