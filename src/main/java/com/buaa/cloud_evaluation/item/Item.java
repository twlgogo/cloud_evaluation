package com.buaa.cloud_evaluation.item;

import lombok.Data;

@Data
public abstract class Item {
  int itemId;
  int direction;
  int type;
  double max;
  double min;
  String itemName;
  String tableName;

  public abstract double getScore(int timestamp);
}
