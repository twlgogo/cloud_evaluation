package com.buaa.cloud_evaluation.item;


class WindowItem extends Item {


  public WindowItem(int itemId, int direction, int type, double max, double min, String itemName, String tableName) {
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
    double value = 22;
    double score = 0.0;

    return score;
  }
}
