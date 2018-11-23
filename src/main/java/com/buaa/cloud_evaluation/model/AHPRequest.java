package com.buaa.cloud_evaluation.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AHPRequest {
  private int n; // matrix n;
  private List<Double> list;
}
