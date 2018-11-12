package com.buaa.cloud_evaluation.ahp;

import java.util.List;
import lombok.Data;

@Data
public class AHPResult {
  private int n;
  private boolean fitCI;
  private List<Double> resList;
  private double CI;
}
