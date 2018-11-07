package com.buaa.cloud_evaluation.controller;

import com.buaa.cloud_evaluation.ahp.AHPCacluator;
import com.buaa.cloud_evaluation.ahp.AHPRequest;
import com.buaa.cloud_evaluation.ahp.AHPResult;
import com.buaa.cloud_evaluation.model.ApiResultModule;
import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.NodeValueModel;
import com.buaa.cloud_evaluation.model.RelationNodeModel;
import com.buaa.cloud_evaluation.service.ApiService;
import com.buaa.cloud_evaluation.util.Serialization;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class ApiController {

  private final ApiService service;
  private RelationNodeModel root;

  @Autowired
  public ApiController(ApiService service) {
    this.service = service;

    RelationNodeModel root;
    try {
      root = generateTree();
    } catch (Throwable e) {
      e.printStackTrace();
      root = null;
    }
    if (root == null) {
      service.deleteAll();
    }

    this.root = root;
  }

  private RelationNodeModel generateTree() {
    List<NodeModel> nodes = service.getAllNodes();

    // Find root node
    List<NodeModel> roots = pickNodesByParent(nodes, NodeModel.INVALID_PARENT);
    // Only one root node is supported
    if (roots.size() != 1) {
      throw new IllegalStateException("Find more than one root");
    }
    checkNode(roots.get(0));

    RelationNodeModel root = roots.get(0).wrap();
    Queue<RelationNodeModel> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
      RelationNodeModel rNode = queue.poll();

      List<NodeModel> childNodes = pickNodesByParent(nodes, rNode.getNode().getId());
      childNodes.forEach(this::checkNode);
      if (rNode.getNode().getType() == NodeModel.TYPE_CRITERIA && childNodes.size() != 0) {
        throw new IllegalStateException("Criteria can own any child");
      }

      childNodes.forEach(n -> {
        RelationNodeModel r = n.wrap();
        rNode.getChildren().add(r);
        queue.offer(r);
      });
    }

    if (nodes.size() != 0) {
      throw new IllegalStateException("Contains redundancy");
    }

    return root;
  }

  private List<NodeModel> pickNodesByParent(List<NodeModel> nodes, int parent) {
    List<NodeModel> result = nodes.stream().filter(n -> n.getParent() == parent).collect(Collectors.toList());
    for (NodeModel node : result) {
      nodes.remove(node);
    }
    return result;
  }

  private void checkNode(NodeModel node) {
    // TODO throw error if the node is invalid
  }

  @RequestMapping("/get_tree")
  public ApiResultModule<RelationNodeModel> getTree() {
    return ApiResultModule.success(root);
  }

  @RequestMapping("/add_node")
  public ApiResultModule<NodeModel> addNode(
      String name,
      int type,
      int parent,
      int source
  ) {
    // Checks the validity of the parent
    RelationNodeModel parentRNode = null;
    if (parent == NodeModel.INVALID_PARENT) {
      // Add a root
      RelationNodeModel root = findNodeByParent(parent);
      if (root != null) {
        return ApiResultModule.error("There already be a root");
      }
    } else {
      parentRNode = findNodeById(parent);
      if (parentRNode == null) {
        return ApiResultModule.error("Can't find node with id: " + parent);
      }
      if (parentRNode.getNode().getType() == NodeModel.TYPE_CRITERIA) {
        return ApiResultModule.error("Criteria node can't be a parent");
      }
    }
    // Checks the validity of the source
    if (type == NodeModel.TYPE_ELEMENT && source != NodeModel.INVALID_SOURCE) {
      return ApiResultModule.error("Element doesn't support source");
    }

    NodeModel node = service.addNode(name, type, parent, source);

    // Maintains tree
    RelationNodeModel rNode = node.wrap();
    if (parentRNode != null) {
      parentRNode.getChildren().add(rNode);
    } else {
      root = rNode;
    }

    return ApiResultModule.success(node);
  }

  @RequestMapping("/update_node")
  public ApiResultModule<NodeModel> updateNode(
      int id,
      String name,
      int type,
      int source
  ) {
    RelationNodeModel rNode = findNodeById(id);
    if (rNode == null) {
      return ApiResultModule.error("Can't find node with the id");
    }
    if (type == NodeModel.TYPE_CRITERIA && !rNode.getChildren().isEmpty()) {
      return ApiResultModule.error("Can't find node with the id");
    }
    if (type == NodeModel.TYPE_ELEMENT && source != NodeModel.INVALID_SOURCE) {
      return ApiResultModule.error("Element doesn't support source");
    }

    NodeModel node = service.updateNode(id, name, type, source);
    rNode.setNode(node);

    return ApiResultModule.success(node);
  }

  @RequestMapping("/remove_node")
  public ApiResultModule<Void> removeNode(
      int id
  ) {
    RelationNodeModel rNode = findNodeById(id);
    if (rNode == null) {
      return ApiResultModule.error("Can't find node with the id");
    }
    if (!rNode.getChildren().isEmpty()) {
      return ApiResultModule.error("Can't remove node with any child");
    }
    if (rNode.getNode().getParent() == NodeModel.INVALID_PARENT) {
      return ApiResultModule.error("Can't remove root node");
    }
    RelationNodeModel parentRNode = findNodeById(rNode.getNode().getParent());
    if (parentRNode == null) {
      return ApiResultModule.error("Can't find parent node, It's an internal error");
    }

    service.removeNode(id);

    // Remove it's history values and current value
    Serialization.stringToIntList(parentRNode.getNode().getHistoryValueIds()).forEach(service::removeNodeValue);
    if (parentRNode.getNode().getCurrentValueId() != NodeValueModel.INVALID_VALUE_ID) {
      service.removeNodeValue(parentRNode.getNode().getCurrentValueId());
    }
    service.updateValueOfNode(parentRNode.getNode().getId(), "0", NodeValueModel.INVALID_VALUE_ID);
    parentRNode.getChildren().removeIf(n -> n.getNode().getId() == id);
    parentRNode.getNode().setHistoryValueIds(Serialization.intListToString(Collections.emptyList()));
    parentRNode.getNode().setCurrentValueId(NodeValueModel.INVALID_VALUE_ID);
    parentRNode.getNode().fillNodeValues(service);

    return ApiResultModule.success(null);
  }

  @RequestMapping("/add_node_value")
  public ApiResultModule<Void> addNodeValue(
      int nodeId,
      String matrixStr
  ) {
    RelationNodeModel rNode = findNodeById(nodeId);
    if (rNode == null) {
      return ApiResultModule.error("Can't find node with the id");
    }
    List<Double> matrix = Serialization.stringToDoubleList(matrixStr);
    int n = rNode.getChildren().size();
    if (matrix.size() == 0 || matrix.size() != n * (n - 1) / 2) {
      return ApiResultModule.error("Matrix size doesn't match");
    }

    // AHP on the matrix
    AHPRequest request = new AHPRequest();
    request.setN(n);
    request.setList(matrix);
    AHPResult result = AHPCacluator.getAHPResult(request);
    if (!result.isFitCI()) {
      return ApiResultModule.error("No fit CI");
    }

    NodeValueModel historyValue = new NodeValueModel();
    historyValue.setN(n);
    historyValue.setMatrix(matrix);
    historyValue.setVector(result.getResList());

    // AHP on all matrix
    List<NodeValueModel> list = new ArrayList<>(rNode.getNode().getHistoryValues());
    list.add(historyValue);
    List<AHPRequest> requests = list.stream().map(NodeValueModel::toAHPRequest).collect(Collectors.toList());
    AHPResult fixResult = AHPCacluator.fixAHPWeight(requests);
    if (!fixResult.isFitCI()) {
      return ApiResultModule.error("No fit CI");
    }

    NodeValueModel currentValue = new NodeValueModel();
    currentValue.setN(n);
    currentValue.setMatrix(null);
    currentValue.setVector(fixResult.getResList());

    // Add new history value
    historyValue = service.addNodeValue(historyValue);
    // Add new current value
    currentValue = service.addNodeValue(currentValue);
    // Update value of node
    List<Integer> newHistoryValueIds = Serialization.stringToIntList(rNode.getNode().getHistoryValueIds());
    newHistoryValueIds.add(historyValue.getId());
    NodeModel node = service.updateValueOfNode(nodeId, Serialization.intListToString(newHistoryValueIds), currentValue.getId());
    rNode.setNode(node);

    return ApiResultModule.success(null);
  }

  @RequestMapping("/remove_node_value")
  public ApiResultModule<Void> removeNodeValue(
      int nodeId,
      int nodeValueId
  ) {
    RelationNodeModel rNode = findNodeById(nodeId);
    if (rNode == null) {
      return ApiResultModule.error("Can't find node with the id");
    }
    NodeValueModel nodeValue = rNode.getNode().getHistoryValues().stream().filter(nv -> nv.getId() == nodeValueId).findFirst().orElse(null);
    if (nodeValue == null) {
      return ApiResultModule.error("Can't find node value with the id");
    }

    // AHP on all matrix
    NodeValueModel currentValue = null;
    if (rNode.getNode().getHistoryValues().size() > 1) {
      List<NodeValueModel> list = new ArrayList<>(rNode.getNode().getHistoryValues());
      list.remove(nodeValue);
      List<AHPRequest> requests = list.stream().map(NodeValueModel::toAHPRequest).collect(Collectors.toList());
      AHPResult fixResult = AHPCacluator.fixAHPWeight(requests);
      if (!fixResult.isFitCI()) {
        return ApiResultModule.error("No fit CI");
      }

      currentValue = new NodeValueModel();
      currentValue.setN(fixResult.getN());
      currentValue.setMatrix(null);
      currentValue.setVector(fixResult.getResList());
    }

    // Remove the node value
    service.removeNodeValue(nodeValueId);
    service.removeNodeValue(rNode.getNode().getCurrentValueId());
    // Add new current value
    if (currentValue != null) {
      currentValue = service.addNodeValue(currentValue);
    }
    // Update value of node
    List<Integer> newHistoryValueIds = Serialization.stringToIntList(rNode.getNode().getHistoryValueIds());
    newHistoryValueIds.remove((Integer) nodeValueId);
    NodeModel node = service.updateValueOfNode(nodeId, Serialization.intListToString(newHistoryValueIds), currentValue != null ? currentValue.getId() : NodeValueModel.INVALID_VALUE_ID);
    rNode.setNode(node);

    return ApiResultModule.success(null);
  }

  @Nullable
  private RelationNodeModel findNodeById(int id) {
    return findNode(rn -> rn.getNode().getId() == id);
  }

  @Nullable
  private RelationNodeModel findNodeByParent(int parent) {
    return findNode(rn -> rn.getNode().getParent() == parent);
  }

  @Nullable
  private RelationNodeModel findNode(Predicate<? super RelationNodeModel> predicate) {
    if (root == null) return null;

    Queue<RelationNodeModel> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
      RelationNodeModel rNode = queue.poll();

      if (predicate.test(rNode)) {
        return rNode;
      }

      for (RelationNodeModel child : rNode.getChildren()) {
        queue.offer(child);
      }
    }

    return null;
  }
}
