package com.buaa.cloud_evaluation.service;

import com.buaa.cloud_evaluation.model.NodeModel;

public interface ApiService {

  NodeModel getNode(int id);

  NodeModel addNode(String name, int type, int parent);
}
