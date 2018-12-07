package com.buaa.cloud_evaluation.item;


class WindowItem extends Item {

  private double max;
  private int windowSize;

  public WindowItem(int itemId, int type, String itemName, String tableName, double max, int windowSize) {
    this.itemId = itemId;
    this.type = type;
    this.max = max;
    this.itemName = itemName;
    this.tableName = tableName;
    this.windowSize = windowSize;
  }

  @Override
  public double getScore(int timestamp){
    int count = DbQuery.getWindowCount(tableName, timestamp, windowSize);

    double score;
    if (count >= max) {
      score = 0.0;
    } else {
      score = (max - count) / (float) max;
    }

    return score;
  }
}
