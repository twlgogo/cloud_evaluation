package com.buaa.cloud_evaluation.model;

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
}
