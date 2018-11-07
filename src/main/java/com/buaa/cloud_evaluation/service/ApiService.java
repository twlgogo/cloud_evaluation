package com.buaa.cloud_evaluation.service;

import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.NodeValueModel;
import java.util.List;

public interface ApiService {

  /**
   * Returns the node with the id from database.
   */
  NodeModel getNode(int id);

  /**
   * Returns all nodes order by id from database.
   */
  List<NodeModel> getAllNodes();

  /**
   * Creates a node with the name, the type and the parent into database.
   */
  NodeModel addNode(String name, int type, int parent, int source);

  /**
   * Returns the node with the id from database.
   */
  NodeModel updateNode(int id, String name, int type, int source);

  NodeModel updateValueOfNode(int id, String historyValueIds, int currentValueId);

  /**
   * Removes the node with the id from database.
   */
  void removeNode(int id);

  /**
   * Returns the node with the id from database.
   */
  NodeValueModel getNodeValue(int id);

  /**
   *
   */
  NodeValueModel addNodeValue(NodeValueModel nodeValue);

  /**
   * Returns the node value with the id from database.
   */
  void removeNodeValue(int id);

  /**
   * Deletes all nodes and all node values from database.
   */
  void deleteAll();
}
