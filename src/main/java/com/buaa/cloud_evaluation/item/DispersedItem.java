package com.buaa.cloud_evaluation.item;

public class DispersedItem extends Item {

  public DispersedItem(int itemId, int type, String itemName, String tableName) {
    this.itemId = itemId;
    this.type = type;
    this.itemName = itemName;
    this.tableName = tableName;
  }

  @Override
  public double getScore(int timestamp){
    return DbQuery.getDispersedState(tableName, timestamp) ? 1.0 : 0.0;
  }
}
