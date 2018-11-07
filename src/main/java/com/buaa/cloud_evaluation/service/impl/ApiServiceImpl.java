package com.buaa.cloud_evaluation.service.impl;

import com.buaa.cloud_evaluation.mapper.ApiMapper;
import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.NodeValueModel;
import com.buaa.cloud_evaluation.service.ApiService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiServiceImpl implements ApiService {

  @Resource
  private ApiMapper mapper;

  @Override
  public NodeModel getNode(int id) {
    NodeModel node = mapper.selectNode(id);
    node.fillNodeValues(this);
    return node;
  }

  @Override
  public List<NodeModel> getAllNodes() {
    List<NodeModel> nodes = mapper.selectNodes();
    nodes.forEach(n -> n.fillNodeValues(this));
    return nodes;
  }

  @Override
  public NodeModel addNode(String name, int type, int parent, int source) {
    NodeModel node = new NodeModel();
    node.setName(name);
    node.setType(type);
    node.setParent(parent);
    node.setCurrentValueId(NodeValueModel.INVALID_VALUE_ID);
    node.setHistoryValueIds("0");
    node.setSource(source);
    mapper.insertNode(node);
    return getNode(node.getId());
  }

  @Override
  public NodeModel updateNode(int id, String name, int type, int source) {
    mapper.updateNode(id, name, type, source);
    return getNode(id);
  }

  @Override
  public NodeModel updateValueOfNode(int id, String historyValueIds, int currentValueId) {
    mapper.updateValueOfNode(id, historyValueIds, currentValueId);
    return getNode(id);
  }

  @Override
  public void removeNode(int id) {
    mapper.deleteNode(id);
  }

  @Override
  public NodeValueModel getNodeValue(int id) {
    return mapper.selectNodeValue(id);
  }

  @Override
  public NodeValueModel addNodeValue(NodeValueModel nodeValue) {
    mapper.insertNodeValue(nodeValue);
    return getNodeValue(nodeValue.getId());
  }

  @Override
  public void removeNodeValue(int id) {
    mapper.deleteNodeValue(id);
  }

  @Override
  public void deleteAll() {
    mapper.deleteNodes();
    // TODO delete node values
  }
}
