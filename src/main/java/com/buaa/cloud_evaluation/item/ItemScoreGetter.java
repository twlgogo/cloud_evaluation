package com.buaa.cloud_evaluation.item;

public interface ItemScoreGetter {

  double getScore(int itemId, int timestamp);
}
