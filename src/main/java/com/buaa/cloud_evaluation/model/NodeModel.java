package com.buaa.cloud_evaluation.model;

import java.util.ArrayList;
import lombok.Data;

@Data
public class NodeModel {
  private int id;
  private String name;
  private int type;
  private int parent;
  private String historyValues;
  private Integer currentValue;
  private Integer source;

  public RelationNodeModel wrap() {
    RelationNodeModel rNode = new RelationNodeModel();
    rNode.setNode(this);
    rNode.setChildren(new ArrayList<>());
    return rNode;
  }

  public static final int TYPE_ELEMENT = 0;
  public static final int TYPE_CRITERIA = 1;
}
