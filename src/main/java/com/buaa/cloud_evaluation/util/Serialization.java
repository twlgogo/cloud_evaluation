package com.buaa.cloud_evaluation.util;

import java.util.ArrayList;
import java.util.List;

public class Serialization {

  public static String intListToString(List<Integer> list) {
    StringBuilder sb = new StringBuilder();
    sb.append(list.size());
    for (Integer i : list) {
      sb.append(",");
      sb.append(i);
    }
    return sb.toString();
  }

  public static String doubleListToString(List<Double> list) {
    StringBuilder sb = new StringBuilder();
    sb.append(list.size());
    for (Double i : list) {
      sb.append(",");
      sb.append(i);
    }
    return sb.toString();
  }

  public static List<Integer> stringToIntList(String string) {
    String[] parts = string.split(",");

    int length = Integer.parseInt(parts[0]);
    if (length + 1 != parts.length) {
      throw new IllegalStateException("Bad int list string");
    }

    List<Integer> list = new ArrayList<>(length);
    for (int i = 1; i < parts.length; i++) {
      int element = Integer.parseInt(parts[i]);
      list.add(element);
    }

    return list;
  }

  public static List<Double> stringToDoubleList(String string) {
    String[] parts = string.split(",");

    int length = Integer.parseInt(parts[0]);
    if (length + 1 != parts.length) {
      throw new IllegalStateException("Bad double list string");
    }

    List<Double> list = new ArrayList<>(length);
    for (int i = 1; i < parts.length; i++) {
      double element = Double.parseDouble(parts[i]);
      list.add(element);
    }

    return list;
  }
}
