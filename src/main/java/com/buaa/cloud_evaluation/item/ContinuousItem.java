package com.buaa.cloud_evaluation.item;

public class ContinuousItem extends Item {

  private double max;
  private double min;
  private int direction;

  public ContinuousItem(int itemId, int type, String itemName, String tableName, double max, double min, int direction) {
    this.itemId = itemId;
    this.itemName = itemName;
    this.tableName = tableName;
    this.type = type;
    this.max = max;
    this.min = min;
    this.direction = direction;
  }

  @Override
  public double getScore(int timestamp){
    double value = DbQuery.getContinuousValue(tableName, timestamp);
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
