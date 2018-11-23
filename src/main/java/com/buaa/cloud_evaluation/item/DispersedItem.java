package com.buaa.cloud_evaluation.item;

public class DispersedItem extends Item {

  public DispersedItem(int itemId, int direction, int type, double max, double min, String itemName, String tableName) {
    this.itemId = itemId;
    this.direction = direction;
    this.type = type;
    this.max = max;
    this.min = min;
    this.itemName = itemName;
    this.tableName = tableName;
  }

  @Override
  public double getScore(int timestamp){
    //todo get value from database
    double score = 0.0;

    return score;
  }
}
