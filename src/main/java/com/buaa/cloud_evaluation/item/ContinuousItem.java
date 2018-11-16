package com.buaa.cloud_evaluation.item;

public class ContinuousItem extends Item {

  public ContinuousItem(int itemId, int direction, int type, double max, double min, String itemName, String tableName) {
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
    //direction 为负,指标值越小越好
    //direction 为正,指标值越大越好
    if (value >= max){
      score = direction == -1 ? 0 : 1;
    }else if(value <= min) {
      score = direction == -1 ? 1: 0;
    }else {
      score  = (value - min) / (max - min);
      score = direction == 1 ? score : 1 - score;
    }
    return score;
  }

}
