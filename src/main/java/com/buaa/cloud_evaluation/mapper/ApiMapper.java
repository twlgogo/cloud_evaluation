package com.buaa.cloud_evaluation.mapper;

import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.NodeValueModel;
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

  void updateNode(
      @Param("id") int id,
      @Param("name") String name,
      @Param("type") int type,
      @Param("source") int source
  );

  void deleteNode(
      @Param("id") int id
  );

  void deleteNodes();

  NodeValueModel selectNodeValue(
      @Param("id") int id
  );
}
