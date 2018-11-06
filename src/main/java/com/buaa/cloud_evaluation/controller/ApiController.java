package com.buaa.cloud_evaluation.controller;

import com.buaa.cloud_evaluation.model.NodeModel;
import com.buaa.cloud_evaluation.model.RelationNodeModel;
import com.buaa.cloud_evaluation.service.ApiService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  private final ApiService service;
  private final RelationNodeModel root;

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

    RelationNodeModel root = new RelationNodeModel();
    root.setNode(roots.get(0));
    root.setChildren(new ArrayList<>());

    Queue<RelationNodeModel> queue = new LinkedList<>();
    queue.offer(root);

    for (;;) {
      RelationNodeModel rNode = queue.poll();
      if (rNode == null) break;

      List<NodeModel> childNodes = pickNodesByParent(nodes, rNode.getNode().getId());
      childNodes.forEach(this::checkNode);
      if (rNode.getNode().getType() == 1 && childNodes.size() != 0) {
        throw new IllegalStateException("Sub-criteria can own any child");
      }

      childNodes.forEach(n -> {
        RelationNodeModel r = new RelationNodeModel();
        r.setNode(n);
        r.setChildren(new ArrayList<>());
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
  public RelationNodeModel getTree() {
    return root;
  }

  @RequestMapping("/add_node")
  public NodeModel addNode(
      @Param("name") String name,
      @Param("type") int type,
      @Param("parent") int parent
  ) {
    return service.addNode(name, type, parent);
  }
}
