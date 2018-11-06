package com.buaa.cloud_evaluation.model;

import java.util.List;
import lombok.Data;

@Data
public class RelationNodeModel {
  private NodeModel node;
  private List<RelationNodeModel> children;
}
