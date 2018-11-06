package com.buaa.cloud_evaluation.controller;

import com.buaa.cloud_evaluation.model.ApiResultModule;
import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.RelationNodeModel;
import com.buaa.cloud_evaluation.service.ApiService;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.ibatis.annotations.Param;
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
    List<NodeModel> roots = pickNodesByParent(nodes, 0);
    if (roots.size() != 1) return null;
    checkNode(roots.get(0));

    RelationNodeModel root = roots.get(0).wrap();
    Queue<RelationNodeModel> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
      RelationNodeModel rNode = queue.poll();

      List<NodeModel> childNodes = pickNodesByParent(nodes, rNode.getNode().getId());
      childNodes.forEach(this::checkNode);
      if (rNode.getNode().getType() == 1 && childNodes.size() != 0) {
        throw new IllegalStateException("Sub-criteria can own any child");
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
      @Param("name") String name,
      @Param("type") int type,
      @Param("parent") int parent
  ) {
    // Checks the validity of the parent
    RelationNodeModel parentRNode = null;
    if (parent == 0) {
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

    NodeModel node = service.addNode(name, type, parent);

    // Maintains tree
    RelationNodeModel rNode = node.wrap();
    if (parentRNode != null) {
      parentRNode.getChildren().add(rNode);
    } else {
      root = rNode;
    }

    return ApiResultModule.success(node);
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
