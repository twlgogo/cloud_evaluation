package com.buaa.cloud_evaluation.service.impl;

import com.buaa.cloud_evaluation.mapper.ApiMapper;
import com.buaa.cloud_evaluation.model.NodeModel;
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
    return mapper.selectNode(id);
  }

  @Override
  public List<NodeModel> getAllNodes() {
    return mapper.selectNodes();
  }

  @Override
  public NodeModel addNode(String name, int type, int parent, int source) {
    NodeModel node = new NodeModel();
    node.setName(name);
    node.setType(type);
    node.setParent(parent);
    node.setSource(source);
    mapper.insertNode(node);
    return mapper.selectNode(node.getId());
  }

  @Override
  public NodeModel updateNode(int id, String name, int type, int source) {
    mapper.updateNode(id, name, type, source);
    return mapper.selectNode(id);
  }

  @Override
  public void removeNode(int id) {
    mapper.deleteNode(id);
  }

  @Override
  public void deleteAll() {
    mapper.deleteNodes();
    // TODO delete node values
  }
}
