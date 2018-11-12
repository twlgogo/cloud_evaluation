package com.buaa.cloud_evaluation.item;

public abstract class Item {
  int itemId;
  int direction;
  double max;
  double min;

  public abstract double getScore(int timestamp);
}
