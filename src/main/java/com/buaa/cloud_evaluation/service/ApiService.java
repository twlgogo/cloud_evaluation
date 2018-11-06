package com.buaa.cloud_evaluation.service;

import com.buaa.cloud_evaluation.model.NodeModel;
import java.util.List;

public interface ApiService {

  /**
   * Returns the node with the id
   */
  NodeModel getNode(int id);

  /**
   * Returns all nodes order by id.
   */
  List<NodeModel> getAllNodes();

  /**
   * Creates a node with the name, the type and the parent.
   */
  NodeModel addNode(String name, int type, int parent);

  /**
   * Deletes all nodes and all node values.
   */
  void deleteAll();
}
