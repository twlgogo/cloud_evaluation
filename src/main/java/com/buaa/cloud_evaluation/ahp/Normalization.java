package com.buaa.cloud_evaluation.ahp;


import java.util.Arrays;

public class Normalization {

  public static void main(String[] args) {
    double[] nums = {0.1,0.2,0.3,0.001};
    normal(nums);
    System.out.println(Arrays.toString(nums));
  }

  public static void normal(double[] rawData) {
    int n = rawData.length;
    double sum = getSum(rawData);
    for (int i = 0; i < n; i++) {
      rawData[i] = rawData[i]/sum;
    }
  }

  public static double getSum (double []data) {
    double sum = 0.0;
    for (double num: data) {
      sum += num;
    }
    return sum;
  }

}
